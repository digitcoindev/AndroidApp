package org.nem.nac.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.nem.nac.common.exceptions.NacRuntimeException;

public enum NetworkVersion {
	MAIN_NETWORK(((byte)0x68)),
	TEST_NETWORK(((byte)0x98));

	private static final NetworkVersion[] values = NetworkVersion.values();
	private final byte _version;

	NetworkVersion(final byte version) {
		_version = version;
	}

	public byte get() {
		return _version;
	}

	@JsonCreator
	public static NetworkVersion fromValue(final int value) {
		for (NetworkVersion obj : values) {
			byte version = ((byte)(value >> 24 & 0xff));
			if (obj._version == version) return obj;
		}
		throw new NacRuntimeException(String.format("Unknown NetworkVersion found: %X", value));
	}
}
