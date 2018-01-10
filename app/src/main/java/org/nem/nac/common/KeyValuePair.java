package org.nem.nac.common;

import android.support.annotation.NonNull;

import org.nem.nac.common.utils.AssertUtils;

import java.util.Map;

public class KeyValuePair<TKey, TValue> implements Map.Entry<TKey, TValue> {

	private final TKey   _key;
	private       TValue _value;

	public KeyValuePair(@NonNull final TKey key, final TValue value) {
		AssertUtils.notNull(key, "Key was null");
		_key = key;
		_value = value;
	}

	@Override
	public TKey getKey() {
		return _key;
	}

	@Override
	public TValue getValue() {
		return _value;
	}

	@Override
	public TValue setValue(final TValue value) {
		TValue prev = _value;
		_value = value;
		return prev;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		KeyValuePair<?, ?> that = (KeyValuePair<?, ?>)o;

		if (!_key.equals(that._key)) { return false; }
		return !(_value != null ? !_value.equals(that._value) : that._value != null);
	}

	@Override
	public int hashCode() {
		int result = _key.hashCode();
		result = 31 * result + (_value != null ? _value.hashCode() : 0);
		return result;
	}
}
