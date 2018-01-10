package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.exceptions.NacRuntimeException;

public enum ImportanceTransferMode {
	ACTIVATE_REMOTE_HARVESTING(1),
	DEACTIVATE_REMOTE_HARVESTING(2);

	private static final ImportanceTransferMode[] values = ImportanceTransferMode.values();
	private final int _value;

	ImportanceTransferMode(final int value) {
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	@JsonCreator
	public static ImportanceTransferMode fromValue(int value) {
		for (ImportanceTransferMode obj : values) {
			if (obj._value == value) return obj;
		}
		throw new NacRuntimeException("Unknown ImportanceTransferMode found");
	}
}
