package org.nem.nac.models.api;

import android.support.annotation.Nullable;

import com.annimon.stream.Optional;
import com.fasterxml.jackson.annotation.JsonCreator;

import org.nem.core.utils.HexEncoder;
import org.nem.nac.models.BinaryData;

import java.util.Arrays;

public final class MessagePayloadApiDto {

	private byte[] _raw;

	@JsonCreator
	public MessagePayloadApiDto(@Nullable final String raw) {
		_raw = HexEncoder.tryGetBytes(raw);
	}

	public Optional<BinaryData> getData() {
		if (_raw != null) {
			return Optional.of(new BinaryData(_raw));
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return _raw != null ? HexEncoder.getString(_raw) : "null";
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		MessagePayloadApiDto that = (MessagePayloadApiDto)o;

		return Arrays.equals(_raw, that._raw);
	}

	@Override
	public int hashCode() {
		return _raw != null ? Arrays.hashCode(_raw) : 0;
	}
}
