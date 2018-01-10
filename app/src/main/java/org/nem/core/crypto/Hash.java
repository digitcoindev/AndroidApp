package org.nem.core.crypto;

import org.nem.core.utils.ByteUtils;
import org.nem.core.utils.HexEncoder;

import java.util.Arrays;

/**
 * A hash.
 */
public class Hash {

	/**
	 * An empty hash.
	 */
	public static final Hash ZERO = new Hash(new byte[32]);

	private final byte[] data;

	/**
	 * Creates new Hash object.
	 *
	 * @param data The raw hash.
	 */
	public Hash(final byte[] data) {
		this.data = data;
	}

	/**
	 * Creates a hash from a hex string.
	 *
	 * @param hex The hex string.
	 * @return The new public key.
	 */
	public static Hash fromHexString(final String hex) {
		try {
			return new Hash(HexEncoder.getBytes(hex));
		} catch (final IllegalArgumentException e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * Gets the raw hash.
	 *
	 * @return The raw hash.
	 */
	public byte[] getRaw() {
		return this.data;
	}

	/**
	 * Gets the short id of this hash.
	 *
	 * @return The short id.
	 */
	public long getShortId() {
		return ByteUtils.bytesToLong(this.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.data);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Hash)) {
			return false;
		}

		final Hash rhs = (Hash) obj;
		return Arrays.equals(this.data, rhs.data);
	}

	@Override
	public String toString() {
		return HexEncoder.getString(this.data);
	}
}
