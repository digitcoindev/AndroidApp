package org.nem.nac.common.enums;

import org.nem.nac.common.exceptions.NacRuntimeException;

public enum LastTransactionType {
	/**
	 * Last transaction that user seen in the app
	 */
	SEEN(0),
	/**
	 * Last transaction that user was notified of
	 */
	NOTIFIED(1);

	public static LastTransactionType fromValue(int value) {
		for (LastTransactionType obj : values()) {
			if (obj._raw == value) { return obj; }
		}
		throw new NacRuntimeException("Unknown LastTransactionType found");
	}

	private int _raw;

	LastTransactionType(int raw) {
		_raw = raw;
	}

	public int getRaw() {
		return _raw;
	}

}
