package org.nem.nac.common;

import android.os.SystemClock;

/**
 * Class for time measurements. Uses {@link SystemClock#elapsedRealtime()} under the hood.
 */
public final class Stopwatch {
	private long _start;
	private long _stop;

	public Stopwatch() { }

	public Stopwatch(final boolean startImmediately) {
		if (startImmediately) {
			start();
		}
	}

	public void start() {
		_start = SystemClock.elapsedRealtime();
	}

	public void stop() {
		_stop = SystemClock.elapsedRealtime();
		if (_start == 0) {
			_start = _stop;
		}
	}

	public void reset() {
		_start = 0;
		_stop = 0;
	}

	/**
	 * @return Elapsed time from start to stop, in milliseconds.
	 */
	public long getMillis() {
		return _stop - _start;
	}

	public TimeSpan getTimeSpan() {
		return TimeSpan.fromMilliSeconds(getMillis());
	}
}
