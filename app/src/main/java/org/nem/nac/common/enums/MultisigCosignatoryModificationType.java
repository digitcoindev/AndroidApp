package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.exceptions.NacRuntimeException;

/**
 * The type of modification.
 */
public enum MultisigCosignatoryModificationType {
	ADD_NEW_COSIGNATORY(1),
	DELETE_EXISTING_COSIGNATORY(2);

	private static final MultisigCosignatoryModificationType[] values = MultisigCosignatoryModificationType.values();
	private final int _value;

	MultisigCosignatoryModificationType(final int value) {
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	@JsonCreator
	public static MultisigCosignatoryModificationType fromValue(int value) {
		for (MultisigCosignatoryModificationType obj : values) {
			if (obj._value == value) return obj;
		}
		throw new NacRuntimeException("Unknown MultisigCosignatoryModificationType found");
	}
}
