package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import timber.log.Timber;

public enum TransactionType {
	UNKNOWN(0xFFFFFFFF),
	TRANSFER_TRANSACTION(0x101),                                    // 257
	IMPORTANCE_TRANSFER_TRANSACTION(0x0801),                        // 2049
	MULTISIG_AGGREGATE_MODIFICATION_TRANSACTION(0x1001),    // 4097
	MULTISIG_SIGNATURE_TRANSACTION(0x1002),                            // 4098
	MULTISIG_TRANSACTION(0x1004),                                    // 4100
	PROVISION_NAMESPACE_TRANSACTION(0x2001), // 8193
	MOSAIC_DEFINITION_CREATION_TRANSACTION(0x4001), // 16385
	MOSAIC_SUPPLY_CHANGE_TRANSACTION(0x4002); // 16386

	private static final TransactionType[] values = TransactionType.values();

	private final int _value;

	TransactionType(int value) {
		_value = value;
	}

	@JsonValue
	public int getValue() {
		return _value;
	}

	@JsonCreator
	public static TransactionType fromValue(int value) {
		for (TransactionType obj : values) {
			if (obj._value == value) { return obj; }
		}
		Timber.w("Unknown TransactionType found");
		return TransactionType.UNKNOWN;
	}
}
