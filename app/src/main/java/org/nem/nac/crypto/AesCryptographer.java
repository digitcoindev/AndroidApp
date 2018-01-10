package org.nem.nac.crypto;

import android.support.annotation.NonNull;

import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedBinaryData;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.lang.ref.SoftReference;
import java.security.SecureRandom;
import java.util.Arrays;

import timber.log.Timber;

public final class AesCryptographer {

	private static final int KEY_LENGTH = 256 / 8;
	private static final int BLOCK_SIZE = new AESEngine().getBlockSize();

	private static SoftReference<AesCryptographer> _instance = new SoftReference<>(null);

	public static synchronized AesCryptographer instance() {
		AesCryptographer instance = _instance.get();
		if (instance == null) {
			instance = new AesCryptographer();
			_instance = new SoftReference<>(instance);
		}
		return instance;
	}

	private final SecureRandom random;

	private AesCryptographer() {
		this.random = new SecureRandom();
	}

	public BinaryData encrypt(@NonNull final BinaryData data, @NonNull final BinaryData key)
		throws NacCryptoException {
		// Edited copy of Ed25519BlockCipher.encrypt
		AssertUtils.notNull(data, key);
		if (key.length() != KEY_LENGTH) {
			Timber.e("Supplied key has different length! Expected %d, found %d", KEY_LENGTH, key.length());
		}

		// Setup IV.
		final byte[] ivData = new byte[BLOCK_SIZE];
		this.random.nextBytes(ivData);

		// Setup block cipher.
		final BufferedBlockCipher cipher = this.setupBlockCipher(key.getRaw(), ivData, true);

		// Encode.
		final byte[] buf = this.transform(cipher, data.getRaw());

		final byte[] result = new byte[ivData.length + buf.length];
		System.arraycopy(ivData, 0, result, 0, ivData.length);
		System.arraycopy(buf, 0, result, ivData.length, buf.length);
		return new BinaryData(result);
	}

	public BinaryData decrypt(@NonNull final EncryptedBinaryData input, @NonNull final BinaryData key)
		throws NacCryptoException {
		AssertUtils.notNull(input, key);
		if (key.length() != KEY_LENGTH) {
			Timber.e("Supplied key has different length! Expected %d, found %d", KEY_LENGTH, key.length());
		}

		if (input.length() < 32) { // 32 because no salt expected
			return null;
		}

		byte[] inputRaw = input.getRaw();

		final byte[] ivData = Arrays.copyOfRange(inputRaw, 0, BLOCK_SIZE);
		final byte[] encData = Arrays.copyOfRange(inputRaw, BLOCK_SIZE, inputRaw.length);

		// Setup block cipher.
		final BufferedBlockCipher cipher = this.setupBlockCipher(key.getRaw(), ivData, false);

		// Decode.
		return new BinaryData(this.transform(cipher, encData));
	}

	// Copied from Ed25519BlockCipher.
	private BufferedBlockCipher setupBlockCipher(final byte[] sharedKey, final byte[] ivData, final boolean forEncryption) {
		// Setup cipher parameters with key and IV.
		final KeyParameter keyParam = new KeyParameter(sharedKey);
		final CipherParameters params = new ParametersWithIV(keyParam, ivData);

		// Setup AES cipher in CBC mode with PKCS7 padding.
		final BlockCipherPadding padding = new PKCS7Padding();
		final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
		cipher.reset();
		cipher.init(forEncryption, params);
		return cipher;
	}

	// Copied from Ed25519BlockCipher, added exceptions
	private byte[] transform(final BufferedBlockCipher cipher, final byte[] data)
		throws NacCryptoException {
		final byte[] buf = new byte[cipher.getOutputSize(data.length)];
		int length = cipher.processBytes(data, 0, data.length, buf, 0);
		try {
			length += cipher.doFinal(buf, length);
		} catch (IllegalArgumentException | DataLengthException | InvalidCipherTextException e) {
			String message;
			if (e instanceof IllegalArgumentException) {
				message = "Illegal argument";
			}
			else if (e instanceof DataLengthException) {
				message = "Buffer was too small";
			}
			else {
				message = "Invalid ciphertext";
			}
			throw new NacCryptoException(message, e);
		}

		return Arrays.copyOf(buf, length);
	}
}
