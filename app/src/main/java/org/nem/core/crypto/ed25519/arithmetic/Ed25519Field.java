package org.nem.core.crypto.ed25519.arithmetic;

import android.util.Log;

import org.nem.core.utils.ArrayUtils;
import org.nem.core.utils.HexEncoder;

import java.math.BigInteger;

/**
 * Represents the underlying finite field for Ed25519.
 * The field has p = 2^255 - 19 elements.
 */
public final class Ed25519Field {
	public static final byte[] ZERO_SHORT = new byte[32];
	public static final byte[] ZERO_LONG = new byte[64];
	public static final Ed25519FieldElement ZERO;
	public static final Ed25519FieldElement ONE;
	public static final Ed25519FieldElement TWO;
	public static final BigInteger P;
	public static final Ed25519FieldElement D;
	public static final Ed25519FieldElement D_Times_TWO;

	/**
	 * I ^ 2 = -1
	 */
	public static Ed25519FieldElement I;

	static {
		try {
			ZERO = getFieldElement(0);
			ONE = getFieldElement(1);
			TWO = getFieldElement(2);
			final byte[] bytes = HexEncoder.getBytes("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffed");
			P = new BigInteger(bytes);
			D = getD();
			D_Times_TWO = D.multiply(TWO);
			I = new Ed25519EncodedFieldElement(HexEncoder.getBytes(
					"b0a00e4a271beec478e42fad0618432fa7d7fb3d99004d2b0bdfc14f8024832b")).decode();
		} catch (Throwable throwable) {
			Log.e(Ed25519Field.class.getSimpleName(), "Static init EXCEPTION", throwable);
			throw throwable;
		}
	}

	private static Ed25519FieldElement getFieldElement(final int value) { // ok
		final int[] f = new int[10];
		f[0] = value;
		return new Ed25519FieldElement(f);
	}

	private static Ed25519FieldElement getD() { // ok
		final BigInteger d = new BigInteger("-121665")
				.multiply(new BigInteger("121666").modInverse(Ed25519Field.P))
				.mod(Ed25519Field.P);
		return new Ed25519EncodedFieldElement(ArrayUtils.toByteArray(d, 32)).decode();
	}
}
