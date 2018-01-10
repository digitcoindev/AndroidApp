package org.nem.nac.common.models;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.NumberUtils;

import java.util.Date;

/**
 * Model class for timestamp values.
 * <b>Internal value in seconds!</b>
 */
public final class TimeValue implements Comparable<TimeValue> {
	private final int _seconds;

	public static final TimeValue ZERO = new TimeValue(0);
	public static final TimeValue INVALID = new TimeValue(-1);

	public TimeValue(final int seconds) {
		this._seconds = seconds;
	}

	@JsonValue
	public int getValue() {
		return _seconds;
	}

	@JsonCreator
	public static TimeValue fromValue(final int value) {
		return new TimeValue(value);
	}

	public TimeValue subtract(final TimeValue value) {
		return fromValue(_seconds - value._seconds);
	}

	public TimeValue add(final TimeValue value) {
		return new TimeValue(_seconds + value._seconds);
	}

	public TimeValue addSeconds(final int seconds) {
		return new TimeValue(_seconds + seconds);
	}

	public TimeValue addDefaultDeadline() {
		return add(AppConstants.DEFAULT_DEADLINE);
	}

	public long toMilliSeconds() {
		return _seconds * 1000L;
	}

	public Date toDate() {
		return new Date(toMilliSeconds());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimeValue timeValue = (TimeValue)o;

		return _seconds == timeValue._seconds;
	}

	@Override
	public int hashCode() {
		return _seconds;
	}

	@Override
	public int compareTo(@NonNull final TimeValue another) {
		AssertUtils.notNull(another);
		if (this.equals(another)) { return 0;}
		return this._seconds > another._seconds ? 1 : -1;
	}

	@Override
	public String toString() {
		return NumberUtils.toString(_seconds);
	}
}
