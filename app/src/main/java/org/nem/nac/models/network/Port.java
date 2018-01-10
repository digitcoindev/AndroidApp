package org.nem.nac.models.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.NacRuntimeException;

public final class Port {
	private final int _value;

	@JsonCreator
	public Port(final int value)
			throws NacRuntimeException {
		if (value < 1 || value > 65535) {
			throw new RuntimeException("Invalid port: " + value);
		}
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	public boolean isDefault() {
		return this.equals(AppConstants.DEFAULT_PORT);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Port port = (Port)o;

		return _value == port._value;
	}

	@Override
	public int hashCode() {
		return _value;
	}

	@Override
	public String toString() {
		return Integer.toString(_value);
	}
}
