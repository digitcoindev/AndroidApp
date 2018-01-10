package org.nem.nac.common;

import android.support.annotation.NonNull;

/**
 * Used to reflect some time period.
 * Uses nanos internally.
 */
public final class TimeSpan {

	public static final TimeSpan ZERO     = new TimeSpan(0);
	public static final TimeSpan INFINITE = new TimeSpan(Long.MAX_VALUE);

	public static TimeSpan fromHours(final double hours) {
		return fromSeconds(hours * 3600);
	}

	public static TimeSpan fromMinutes(final double minutes) {
		return fromSeconds(minutes * 60);
	}

	public static TimeSpan fromSeconds(final double seconds) {
		return new TimeSpan((long)(seconds * 1_000_000_000L));
	}

	public static TimeSpan fromMilliSeconds(final double millis) {
		return new TimeSpan(((long)(millis * 1_000_000)));
	}

	public static TimeSpan fromNanoSeconds(final long nanos) {
		return new TimeSpan(nanos);
	}

	public static TimeSpan now() {
		return new TimeSpan(System.nanoTime());
	}

	private final long _nanos;

	private TimeSpan(final long nanos) {
		_nanos = nanos;
	}

	public long getNanos() {
		return _nanos;
	}

	public boolean isLessThan(@NonNull final TimeSpan o) {
		return this._nanos < o._nanos;
	}

	public boolean isGreaterThan(@NonNull final TimeSpan o) {
		return this._nanos > o._nanos;
	}

	public boolean isLessThanOrEqual(@NonNull final TimeSpan o) {
		return this._nanos <= o._nanos;
	}

	public boolean isGreaterThanOrEqual(@NonNull final TimeSpan o) {
		return this._nanos >= o._nanos;
	}

	public TimeSpan subtract(final TimeSpan timeSpan) {
		return new TimeSpan(this._nanos - timeSpan._nanos);
	}

	public double toMilliSeconds() {
		return ((double)_nanos) / 1000_000.0;
	}

	public double toSeconds() {
		return ((double)_nanos) / 1000_000_000.0;
	}

	public double toMinutes() {
		return ((double)_nanos) / 1000_000_000.0 / 60.0;
	}

	public double toHours() {
		return ((double)_nanos) / 1000_000_000.0 / 3600.0;
	}

	/**
	 * Returns true if difference is less than 1microsec (accounting other equals checks)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		TimeSpan timeSpan = (TimeSpan)o;

		return Math.abs(_nanos - timeSpan._nanos) < 1000L;
	}

	@Override
	public int hashCode() {
		return (int)(_nanos ^ (_nanos >>> 32));
	}

	@Override
	public String toString() {
		return Float.toString(((float)_nanos) / 1000_000.0f) + "ms";
	}
}
