package org.nem.nac.helpers;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.ed25519.Ed25519BlockCipher;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.NacPublicKey;

public final class Ed25519Helper {

	private static final int AES_BLOCK_SIZE = 16;
	private static final int SALT_LENGTH    = 32; // same as public key

	public static BinaryData Ed25519BlockCipherEncrypt(
			@NonNull final BinaryData input,
			@NonNull final NacPrivateKey account1, @NonNull final NacPublicKey account2) {
		final KeyPair senderKeyPair = new KeyPair(account1.toPrivateKey());
		final KeyPair recipientKeyPair = new KeyPair(null, account2.toPublicKey());
		final byte[] inputBytes = input.getRaw();
		final byte[] encryptedBytes = new Ed25519BlockCipher(senderKeyPair, recipientKeyPair).encrypt(inputBytes);
		return new BinaryData(encryptedBytes);
	}

	public static Optional<BinaryData> Ed25519BlockCipherDecrypt(
			@NonNull final BinaryData cipher,
			@NonNull final NacPrivateKey account1, @NonNull final NacPublicKey account2) {
		final KeyPair senderKeyPair = new KeyPair(null, account2.toPublicKey());
		final KeyPair recipientKeyPair = new KeyPair(account1.toPrivateKey());
		final byte[] cipherBytes = cipher.getRaw();
		final byte[] decryptedBytes = new Ed25519BlockCipher(senderKeyPair, recipientKeyPair).decrypt(cipherBytes);
		return decryptedBytes != null ? Optional.of(new BinaryData(decryptedBytes)) : Optional.<BinaryData>empty();
	}

	public static int getEncryptedMessageLength(final String message) {
		String msg = message != null ? message : "";
		return getEncryptedMessageLength(msg.getBytes().length);
	}

	public static int getEncryptedMessageLength(final int inputLengthBytes) {
		if (inputLengthBytes == 0) {
			return 0;
		}
		final int ivLength = AES_BLOCK_SIZE;
		return SALT_LENGTH + ivLength + ((inputLengthBytes / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE); // http://stackoverflow.com/questions/3716691/relation-between-input-and-ciphertext-length-in-aes
	}
}
