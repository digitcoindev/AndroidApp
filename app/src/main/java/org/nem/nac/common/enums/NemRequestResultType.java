package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.exceptions.NacRuntimeException;

public enum NemRequestResultType {
	/**
	 * 1
	 */
	VALIDATION_RESULT(1),
	/**
	 * 2
	 */
	HEARTBEAT_RESULT(2),
	/**
	 * 4
	 */
	STATUS(4);

	private static final NemRequestResultType[] values = NemRequestResultType.values();
	private final int _value;

	NemRequestResultType(final int value) {
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	@JsonCreator
	public static NemRequestResultType fromValue(final int value) {
		for (NemRequestResultType obj : values) {
			if (obj._value == value)
				return obj;
		}
		throw new NacRuntimeException(String.format("Unknown NemRequestResultType found: %X", value));
	}

}
