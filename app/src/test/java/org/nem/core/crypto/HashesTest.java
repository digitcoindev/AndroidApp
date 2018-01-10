package org.nem.core.crypto;

import org.junit.Assert;
import org.junit.Test;

public class HashesTest {

	@Test
	public void test_doesntFail() throws Exception {
		final byte[] input = new byte[]{-11, 73, 108, 89, -1, 51, 106, -30, -44, -105, 20, 15, 10, -44, -125, 6, 9, 43, -18, -38, 13, 54, -61, 28, 115, 115, 16, 57, 86, -55, 2, 97};
		final byte[] expectedripemd160 = new byte[]{53, -74, -112, 124, 107, 67, -11, 112, -126, -119, 53, -39, 32, 47, -6, -70, -125, 35, -41, 90};
		final byte[] expectedsha3_256 = new byte[]{66, 92, 87, -122, -36, -110, -103, -42, 42, 99, 10, 120, -40, 73, -92, -101, 116, 20, 63, 13, 73, 32, 16, 103, 25, -58, 112, -117, 7, 63, 73, 75};
		final byte[] expectedsha3_512 = new byte[]{90, 10, -7, -69, 5, -120, 76, 20, -39, 112, -14, 120, -97, 84, 44, -73, 120, 15, 36, 47, -81, 8, 53, 124, -21, -33, -37, -72, -57, -26, -103, -9, -28, 115, -40, -32, -122, 124, 86, -68, 16, 84, 116, -44, -67, -13, -1, 93, 46, -70, 108, 77, -66, 45, -60, 60, -18, -13, -95, -40, -118, -94, 112, 8};

		final byte[] ripemd160 = Hashes.ripemd160(input);
		final byte[] sha3_256 = Hashes.sha3_256(input);
		final byte[] sha3_512 = Hashes.sha3_512(input);

		Assert.assertArrayEquals("RipeMD160 fail", expectedripemd160, ripemd160);
		Assert.assertArrayEquals("SHA3-256 fail", expectedsha3_256, sha3_256);
		Assert.assertArrayEquals("SHA3-512 fail", expectedsha3_512, sha3_512);
	}
}
