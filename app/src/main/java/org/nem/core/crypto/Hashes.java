package org.nem.core.crypto;

import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.logging.Logger;

/**
 * Static class that exposes hash functions.
 */
public class Hashes {
	private static final Logger LOGGER = Logger.getLogger(Hashes.class.getName());

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Performs a SHA3-256 hash of the concatenated inputs.
	 *
	 * @param inputs The byte arrays to concatenate and hash.
	 * @return The hash of the concatenated inputs.
	 * @throws CryptoException if the hash operation failed.
	 */
	public static byte[] sha3_256(final byte[]... inputs) {
		return hash("SHA3-256", inputs);
	}

	/**
	 * Performs a SHA3-512 hash of the concatenated inputs.
	 *
	 * @param inputs The byte arrays to concatenate and hash.
	 * @return The hash of the concatenated inputs.
	 * @throws CryptoException if the hash operation failed.
	 */
	public static byte[] sha3_512(final byte[]... inputs) {
		SHA3Digest digest = new SHA3Digest(512);
		for (final byte[] input : inputs) {
			digest.update(input, 0, input.length);
		}
		byte[] signature = new byte[512 / 8];
		digest.doFinal(signature, 0);
		return signature;
		//return hash("SHA3-512", inputs);
	}

	/**
	 * Performs a RIPEMD160 hash of the concatenated inputs.
	 *
	 * @param inputs The byte arrays to concatenate and hash.
	 * @return The hash of the concatenated inputs.
	 * @throws CryptoException if the hash operation failed.
	 */
	public static byte[] ripemd160(final byte[]... inputs) {
		return hash("RIPEMD160", inputs);
	}

	private static byte[] hash(final String algorithm, final byte[]... inputs) throws CryptoException {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(algorithm, "SC"); // It's SpongyCastle on Android
			for (final byte[] input : inputs) {
				digest.update(input);
			}
			return digest.digest();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new CryptoException("Hashing error: " + e.getMessage(), e);
		}
	}
}
