package org.nem.nac.common.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.enums.MessageType;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.helpers.Ed25519Helper;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.NacPublicKey;

/**
 * Outgoing message
 */
public final class MessageDraft {

	public static boolean isLengthValid(final String message, final boolean encrypted) {
		if (message == null) { return true; }
		final int length = encrypted ? Ed25519Helper.getEncryptedMessageLength(message) : message.getBytes().length;
		return length <= AppConstants.MAX_MESSAGE_LENGTH_BYTES;
	}

	private MessageType type = MessageType.NOT_ENCRYPTED;
	/**
	 * The value is the actual (possibly encrypted) message data.
	 */
	@Nullable
	private BinaryData payload;

	/**
	 * Creates a non-encrypted message from data.
	 */
	private MessageDraft(@NonNull final byte[] data) {
		payload = new BinaryData(data);
	}

	@Nullable
	public BinaryData getPayload() {
		return payload;
	}

	public boolean hasPayload() {
		return payload != null && payload.length() > 0;
	}

	public boolean isEncrypted() {
		return type == MessageType.ENCRYPTED;
	}

	/**
	 * @throws NacRuntimeException If already encrypted, or keys are from same account
	 */
	public void encryptPayload(@NonNull final NacPrivateKey privateKeyAcc1, @NonNull final NacPublicKey publicKeyAcc2)
			throws NacRuntimeException {
		if (payload == null || payload.length() == 0) {
			return;
		}
		if (type == MessageType.ENCRYPTED) {
			throw new NacRuntimeException("Already encrypted");
		}
		payload = Ed25519Helper.Ed25519BlockCipherEncrypt(payload, privateKeyAcc1, publicKeyAcc2);
		type = MessageType.ENCRYPTED;
	}

	/**
	 * Creates a non-encrypted message from data, or null if data is null or empty.
	 */
	@Nullable
	public static MessageDraft create(@Nullable final byte[] data) {
		return data != null && data.length > 0 ? new MessageDraft(data) : null;
	}

	/**
	 * @return a (possibly) readable message.
	 */
	@Override
	public String toString() {
		return payload != null ? new String(payload.getRaw()) : "null";
	}
}
