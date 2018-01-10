package org.nem.nac.common.utils;

public final class ArrayUtils {
	public static void copy(final byte[] src, final int srcPos, final byte[] dst, final int dstPos, final int length) {
		System.arraycopy(src, srcPos, dst, dstPos, length); // Burn in hell, creators of methods that take Object where they shouldn't!
	}
}
