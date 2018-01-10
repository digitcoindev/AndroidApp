package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.exceptions.NacRuntimeException;

public enum MessageType {
	NOT_ENCRYPTED(1),
	ENCRYPTED(2);

	private static final MessageType[] values = MessageType.values();
	private final int _value;

	MessageType(int value) {
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	@JsonCreator
	public static MessageType fromValue(int value) {
		for (MessageType obj : values) {
			if (obj._value == value)
				return obj;
		}
		throw new NacRuntimeException("Unknown MessageType found");
	}
}
