package org.nem.nac.servers;

import android.os.ConditionVariable;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.common.ThreadPoolExecutorFactory;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.network.Server;
import org.nem.nac.providers.NodeInfoProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

public final class ServerFinder {

	private static final int SERVER_SEARCH_VALIDITY_TIME_MS = 300000;
	private static final int                        FIND_SERVER_TIMEOUT_MS         = 20000;
	private static final int                        WAITERS_TIMEOUT_MS             = 25000;
	private static final int                        HEARTBEATS_TIMEOUT_MS          = 10000;
	private static final Comparator<ServerResponse> SMALLER_TIME_FIRST_COMPARATOR  =
			(lhs, rhs) -> lhs.responseTime.isLessThan(rhs.responseTime) ? 1 : -1;

	private static ServerFinder _instance;

	public static synchronized ServerFinder instance() {
		if (_instance == null) {
			_instance = new ServerFinder();
		}
		return _instance;
	}

	private final AtomicReference<Server> _best              = new AtomicReference<>(null);
	private final AtomicReference<Long>   _lastFoundNanoTime = new AtomicReference<>(0L);
	private final ConditionVariable       _findConditionVar  = new ConditionVariable(true);
	private final ReentrantLock           _lock              = new ReentrantLock(true);

	private ServerFinder() {
	}

	public void clearBest() {
		final Thread thread = Thread.currentThread();
		try {
			Timber.d("Thread %d wants to clear server", thread.getId());
			if (!_lock.tryLock(WAITERS_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
				Timber.e("Clear waiter %d timeout!", thread.getId());
				return;
			}
			_best.set(null);
			_lock.unlock();
		} catch (InterruptedException e) {
			Timber.e("Clear waiter %d interrupted!", thread.getId());
			_lock.unlock();
		}
	}

	public void setBest(final Server server) {
		final Thread thread = Thread.currentThread();
		try {
			Timber.d("Thread %d wants to set server %s", thread.getId(), server);
			if (!_lock.tryLock(WAITERS_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
				Timber.e("Set waiter %d timeout!", thread.getId());
				return;
			}
			_best.set(server);
			NodeInfoProvider.instance().clearData();
			_lock.unlock();
		} catch (InterruptedException e) {
			Timber.e("Set waiter %d interrupted!", thread.getId());
			_lock.unlock();
		}
	}

	/**
	 * Returns immediate best server value, or empty if not exist. No scanning involved.
	 */
	public Optional<Server> peekBest() {
		return Optional.ofNullable(_best.get());
	}

	public void getBestAsync() {
		new Thread(this::getBest).start();
	}

	/**
	 * Returns best server if already exists. Finds new best if not.
	 */
	@WorkerThread
	public Optional<Server> getBest() {
		Timber.d("getBest()");
		final Thread thread = Thread.currentThread();
		if (Looper.getMainLooper().equals(Looper.myLooper())) {
			Timber.e("Called from main thread!");
		}

		try {
			Timber.d("Thread %d comes for server", thread.getId());
			if (!_lock.tryLock(WAITERS_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
				Timber.e("Waiter %d timeout!", thread.getId());
				return Optional.empty();
			}
		} catch (InterruptedException e) {
			Timber.e("Waiter %d interrupted!", thread.getId());
			return Optional.empty();
		}

		// Only one thread here
		try {
			Timber.d("Lock acquired by %d", thread.getId());
			final Server best = _best.get();
			final long lastFoundNanoTime = _lastFoundNanoTime.get();
			if (best != null
					&& TimeSpan.fromNanoSeconds(System.nanoTime())
					.subtract(TimeSpan.fromNanoSeconds(lastFoundNanoTime))
					.isLessThan(TimeSpan.fromMilliSeconds(SERVER_SEARCH_VALIDITY_TIME_MS))) {
				Timber.d("Server exists and still valid, returning: %s", best);
				return Optional.of(best);
			}
			NodeInfoProvider.instance().clearData();

			Timber.d("%d/%s finding server...", thread.getId(), thread.getName());

			_findConditionVar.close();
			Timber.d("Find condition close");
			final Thread findThread = new Thread(() -> {
				try {
					Timber.d("Find server thread started.");
					final Optional<Server> newBest = find();
					_lastFoundNanoTime.set(System.nanoTime());
					_best.set(newBest.isPresent() ? newBest.get() : null);
					Timber.d("New best found and set: %s", newBest);
				} catch (Throwable throwable) {
					Timber.e(throwable, "Server finder thread failed!");
				} finally {
					Timber.d("Find condition open");
					_findConditionVar.open();
				}
			});
			findThread.start();

			if (!_findConditionVar.block(FIND_SERVER_TIMEOUT_MS)) {
				Timber.e("Find server timeout!");
				return Optional.empty();
			}
			if (_best.get() == null) {
				Timber.e("Best server not found!");
				return Optional.empty();
			}
			Timber.d("Best server found: %s", _best.get());
			return Optional.of(_best.get());
		} finally {
			// Release others
			_lock.unlock();
			Timber.d("Lock released by %d", thread.getId());
		}
	}

	/**
	 * Finds best server
	 */
	@NonNull
	private Optional<Server> find() {
		final ServerManager serverManager = ServerManager.instance();
		final Map<Long, Server> servers = serverManager.getAllServers();

		if (servers.size() == 1) {
			Timber.d("Found one server in list, returning");
			return Optional.of(servers.values().iterator().next());
		}
		if (servers.isEmpty()) {
			Timber.d("No servers in list");
			return Optional.empty();
		}

		final ThreadPoolExecutor executor = ThreadPoolExecutorFactory.createDefaultExecutor();

		final NisApi api = new NisApi();
		List<Callable<ServerResponse<Boolean>>> tasks = new ArrayList<>(servers.size());
		SortedSet<ServerResponse> successfulResponses = new TreeSet<>(SMALLER_TIME_FIRST_COMPARATOR);
		Stream.of(servers)
				.forEach(x -> tasks.add(() -> {
					Timber.d("Heartbeat task started: %s", x.getValue());
					final ServerResponse<Boolean> response = api.heartbeat(x.getValue());
					Timber.d("Heartbeat task ended: %s", x.getValue());
					return response;
				}));

		Timber.d("Invoking futures");
		//final List<Future<ServerResponse<Boolean>>> finishedFutures = executor.invokeAll(tasks, HEARTBEATS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		final CompletionService<ServerResponse<Boolean>> completionService =
				new ExecutorCompletionService<ServerResponse<Boolean>>(executor);

		List<Future<ServerResponse<Boolean>>> futures = new ArrayList<>();
		for (Callable<ServerResponse<Boolean>> task : tasks) {
			futures.add(completionService.submit(task));
		}
		executor.shutdown();

		final TimeSpan start = TimeSpan.now();
		final TimeSpan timeout = TimeSpan.fromMilliSeconds(HEARTBEATS_TIMEOUT_MS);
		final TimeSpan veryFast = TimeSpan.fromSeconds(1);

		while (true) {
			try {
				if (TimeSpan.now().subtract(start).isGreaterThan(timeout)) {
					break;
				}
				//
				final Future<ServerResponse<Boolean>> future = completionService.poll(10L, TimeUnit.MILLISECONDS);
				if (future == null) {
					continue;
				}
				//
				final ServerResponse<Boolean> response = future.get();
				final boolean isSuccessful = response.model;
				Timber.d("Response \"%s\" from server %s in %s", (isSuccessful ? "Success" : "Failure"), response.server, response.responseTime);

				if (isSuccessful) {
					if (response.responseTime.isLessThan(veryFast)) {
						for (Future<ServerResponse<Boolean>> f : futures) {
							if (!f.equals(future)) {
								f.cancel(true);
							}
						}
						return Optional.of(response.server);
					}
					successfulResponses.add(response);
				}
			} catch (CancellationException | InterruptedException e) {
				Timber.d(e, "Find server canceled");
			} catch (ExecutionException e) {
				Timber.w(e, "Failed to heartbeat server");
			} catch (Throwable throwable) {
				Timber.e(throwable, "Fatal error");
			}
		}

		Timber.d("Futures finished");

		if (successfulResponses.isEmpty()) {
			Timber.e("No successful responses!");
			return Optional.empty();
		}
		final ServerResponse fastest = successfulResponses.first();
		Timber.i("Best server selected: %s", fastest.server);
		return Optional.of(fastest.server);
	}
}
