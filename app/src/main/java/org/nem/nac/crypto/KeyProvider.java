package org.nem.nac.crypto;

import org.nem.core.crypto.Hashes;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPrivateKey;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class KeyProvider {
	public static BinaryData generateSalt() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[AppConstants.SALT_SIZE_BYTES];
			sr.nextBytes(salt);
			return new BinaryData(salt);
		} catch (NoSuchAlgorithmException e) {
			throw new NacRuntimeException("SHA1PRNG is not supported on this device");
		}
	}

	public static BinaryData getDelegatedKey(NacPrivateKey key) {
		return new BinaryData(Hashes.sha3_256(key.getRaw()));
	}

	public static BinaryData deriveKey(final String password, final BinaryData salt)
			throws NacCryptoException {
		return deriveKey(password, salt, AppConstants.DERIVED_KEY_SIZE_BITS);
	}

	private static BinaryData deriveKey(final String password, final BinaryData salt, final int keySizeBits)
			throws NacCryptoException {
		AssertUtils.notNull(password);
		if(password.isEmpty()) {
			throw new IllegalArgumentException("password cannot be empty");
		}
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getRaw(), AppConstants.DERIVE_KEY_ITERATIONS, keySizeBits);
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
			SecretKey key = new SecretKeySpec(keyBytes, "AES");
			return new BinaryData(key.getEncoded());
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new NacCryptoException("Failed to derive key", e);
		}
	}
}
