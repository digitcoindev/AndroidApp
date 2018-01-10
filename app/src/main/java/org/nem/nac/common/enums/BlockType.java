package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.exceptions.NacRuntimeException;

public enum BlockType {
	NEMESIS_BLOCK(-1),
	REGULAR_BLOCK(1);

	private static final BlockType[] values = BlockType.values();
	private final int _value;

	BlockType(final int value) {
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	@JsonCreator
	public static BlockType fromValue(int value) {
		for (BlockType obj : values) {
			if (obj._value == value)
				return obj;
		}
		throw new NacRuntimeException("Unknown BlockType found");
	}
}
