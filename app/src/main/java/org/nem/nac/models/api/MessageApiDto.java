package org.nem.nac.models.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import org.nem.core.utils.HexEncoder;
import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.enums.MessageType;
import org.nem.nac.models.BinaryData;

import java.util.Arrays;

public final class MessageApiDto {

	/**
	 * If message is encrypted, returns placeholder string.
	 * If Message is a hex message, returns hex string representation.
	 * Otherwise decodes data to normal utf-8 string.
	 */
	public static Optional<String> toReadableString(@Nullable final MessageApiDto msg) {
		if (msg == null) {
			return Optional.empty();
		}
		if (msg.type == MessageType.ENCRYPTED) {
			return Optional.of(NacApplication.getResString(R.string.placeholder_encrypted_message));
		}
		// todo: Status of hex messages is "hidden", remove this when really not needed
		final byte[] raw = msg.getData().getRaw();
		if (raw.length > 0 && raw[0] == ((byte)0xFE)) { // Hex message
			int skip = 1;
			final byte[] rawHex = Arrays.copyOfRange(raw, skip, raw.length - skip);
			return Optional.of("Hex: " + HexEncoder.getString(rawHex));
		}
		//
		return Optional.of(new String(raw));
	}

	public static Optional<String> toReadableString(@Nullable final BinaryData msgData) {
		if (msgData == null) {
			return Optional.empty();
		}
		return Optional.of(toString(msgData));
	}

	private static String toString(@NonNull final BinaryData data) {
		final byte[] raw = data.getRaw();
		if (raw.length > 0 && raw[0] == ((byte)0xFE)) { // Hex message
			int skip = 1;
			final byte[] rawHex = Arrays.copyOfRange(raw, skip, raw.length - skip);
			return "Hex: " + HexEncoder.getString(rawHex);
		}
		return new String(raw);
	}

	/**
	 * The payload is the actual (possibly encrypted) message data.
	 */
	public MessagePayloadApiDto payload;
	/**
	 * The field holds the message type information.
	 */
	public MessageType          type;

	@NonNull
	public BinaryData getData() {
		return payload != null ? payload.getData().get() : BinaryData.EMPTY;
	}

	/**
	 * @return a readable message.
	 */
	@Override
	public String toString() {
		final Optional<String> readable = MessageApiDto.toReadableString(this);
		return readable.orElse(null);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		MessageApiDto that = (MessageApiDto)o;

		if (payload != null ? !payload.equals(that.payload) : that.payload != null) { return false; }
		return type == that.type;
	}

	@Override
	public int hashCode() {
		int result = payload != null ? payload.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}
}
