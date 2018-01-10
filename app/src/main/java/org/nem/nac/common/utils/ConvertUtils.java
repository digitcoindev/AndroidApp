package org.nem.nac.common.utils;

public final class ConvertUtils {

	/**
	 * Converts int value to little-endian byte array
	 */
	public static byte[] toLeBytes(final int value) {
		byte[] ret = new byte[4];
		ret[0] = (byte) (value & 0xFF);
		ret[1] = (byte) ((value >> 8) & 0xFF);
		ret[2] = (byte) ((value >> 16) & 0xFF);
		ret[3] = (byte) ((value >> 24) & 0xFF);
		return ret;
	}

	/**
	 * Converts long value to little-endian byte array
	 */
	public static byte[] toLeBytes(final long value) {
		byte[] ret = new byte[8];
		ret[0] = (byte) (value & 0xFF);
		ret[1] = (byte) ((value >> 8) & 0xFF);
		ret[2] = (byte) ((value >> 16) & 0xFF);
		ret[3] = (byte) ((value >> 24) & 0xFF);
		ret[4] = (byte) ((value >> 32) & 0xFF);
		ret[5] = (byte) ((value >> 40) & 0xFF);
		ret[6] = (byte) ((value >> 48) & 0xFF);
		ret[7] = (byte) ((value >> 56) & 0xFF);

		return ret;
	}
}
