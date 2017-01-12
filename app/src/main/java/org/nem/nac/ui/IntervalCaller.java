package org.nem.nac.ui;

import android.os.Handler;
import android.os.Looper;

import com.annimon.stream.function.Consumer;

import org.nem.nac.common.TimeSpan;

import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public final class IntervalCaller {

	private final int _maxCalls;
	private final AtomicInteger _callsCount = new AtomicInteger(0);
	private Consumer<IntervalCaller> _onMaxCallsReachedCallback;
	private Handler                  _mainHandler;
	private int      _intervalMs;
	private Runnable _callback;
	private boolean _running = false;

	public IntervalCaller(final TimeSpan interval, final Runnable callback) {
		_mainHandler = new Handler(Looper.getMainLooper());
		_intervalMs = ((int)interval.toMilliSeconds());
		_callback = callback;
		_maxCalls = Integer.MAX_VALUE;
		_onMaxCallsReachedCallback = null;
	}

	public IntervalCaller(final TimeSpan interval, final Runnable callback, final int maxCalls, final Consumer<IntervalCaller> onMaxCallsReachedCallback) {
		_onMaxCallsReachedCallback = onMaxCallsReachedCallback;
		_mainHandler = new Handler(Looper.getMainLooper());
		_intervalMs = ((int)interval.toMilliSeconds());
		_callback = callback;
		_maxCalls = Math.max(1, maxCalls);
		_onMaxCallsReachedCallback = onMaxCallsReachedCallback;
	}

	public synchronized boolean isRunning() {
		return _running;
	}

	public synchronized void start(final boolean callNow) {
		if (_running && callNow) {
			stop();
		}
		if (!_running) {
			if (callNow) {
				_mainHandler.post(_looperRunnable);
			}
			else {
				_mainHandler.postDelayed(_looperRunnable, _intervalMs);
			}
			_running = true;
		}
	}

	public synchronized void stop() {
		if (_running) {
			_mainHandler.removeCallbacks(_looperRunnable);
			_running = false;
		}
	}

	public synchronized void resetCallCount() {
		_callsCount.set(0);
	}

	private final Runnable _looperRunnable = new Runnable() {
		@Override
		public void run() {
			final int calls = _callsCount.incrementAndGet();
			Timber.d("run(), calls %s/%s", calls, _maxCalls);
			if (_callback != null) {
				_callback.run();
			}
			if (calls >= _maxCalls) {
				_running = false;
				Timber.d("Max calls reached");
				if (_onMaxCallsReachedCallback != null) {
					_onMaxCallsReachedCallback.accept(IntervalCaller.this);
				}
			}
			else {
				_mainHandler.postDelayed(_looperRunnable, _intervalMs);
			}
		}
	};
}
