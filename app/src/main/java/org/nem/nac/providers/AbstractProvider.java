package org.nem.nac.providers;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;

import org.nem.nac.common.exceptions.NacException;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

public abstract class AbstractProvider<TData> {
	protected static final int TIMEOUT_S         = 120;
	protected static final int DEFAULT_MAX_TRIES = 3;
	protected final Handler handler;
	protected final AtomicReference<TData> data          = new AtomicReference<>(null);
	protected final AtomicBoolean          isUpdating    = new AtomicBoolean(false);
	protected final ConditionVariable      updatingBlock = new ConditionVariable(true);
	protected IOException lastException;
	/**
	 * Timestamp of last successful data retrieval. Set using {@link System#nanoTime()}
	 */
	protected final AtomicReference<Long> lastDataNanoTime = new AtomicReference<>(null);

	public AbstractProvider() {
		final HandlerThread thread = new HandlerThread(this.getClass().getName() + new Random().nextInt());
		thread.start();
		handler = new Handler(thread.getLooper());
	}

	/**
	 * @return Time offset in milliseconds from last data update time.
	 */
	public int getUpdateTimeOffsetMs() {
		if (lastDataNanoTime.get() == null) {
			return 0;
		}
		long update = lastDataNanoTime.get();
		return (int)((System.nanoTime() - update) / 1000_000);
	}

	/**
	 * Override this to set max tries for each custom provider.
	 * Do not call super method in overridden.
	 */
	protected int getMaxTries() {
		return DEFAULT_MAX_TRIES;
	}

	/**
	 * Gets data. Remember not to call it from main thread because it can block for some time.
	 *
	 * @return data object. If it is not valid, the thread waits until it is refreshed from source.
	 * @throws NacException when getting data failed.
	 */
	public TData getData()
			throws NacException {
		Timber.d("Thread %d is coming for data", Thread.currentThread().getId());
		synchronized (data) {
			lastException = null;
			if (data.get() != null) {
				return data.get();
			}
			updatingBlock.close();
			if (!isUpdating.get()) {
				isUpdating.set(true);
				handler.post(this::retryingRefresh);
			}
			updatingBlock.block(TIMEOUT_S * 1000);
			if (data.get() != null) {
				lastDataNanoTime.set(System.nanoTime());
				return data.get();
			}
			throw new NacException("Failed to get node info.");
		}
	}

	public void clearData() {
		synchronized (data) {
			data.set(null);
			lastDataNanoTime.set(null);
		}
	}

	/**
	 * Synchronously gets the data.
	 *
	 * @throws IOException if data get failed.
	 */
	protected abstract void refresh()
			throws IOException;

	private void retryingRefresh() { // executed in provider thread
		isUpdating.set(true);
		int triesLeft = getMaxTries();
		Timber.d("Data refresh started");
		try {
			while (data.get() == null && triesLeft > 0) {
				Timber.d("Tries left: %d", triesLeft);
				try {
					data.set(null);
					isUpdating.set(true);
					refresh();
				} catch (IOException e) {
					Timber.w(e, "Data refresh failed: %s", e.getMessage());
					lastException = e;
				} finally {
					triesLeft--;
				}
			}
			Timber.d("Data refresh ended!");
		} finally {
			isUpdating.set(false);
			updatingBlock.open();
		}
	}
}
