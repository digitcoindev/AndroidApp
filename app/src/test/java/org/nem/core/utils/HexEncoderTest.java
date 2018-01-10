package org.nem.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class HexEncoderTest {

	@Test
	public void testGetBytes() throws Exception {
		final String hexData = "f5496c59ff336ae2d497140f0ad48306092beeda0d36c31c7373103956c90261"; // test acc public key
		final byte[] actual = HexEncoder.getBytes(hexData);
		final byte[] expected = new byte[]
				{(byte) 0xf5, 0x49, 0x6c, 0x59, (byte) 0xff, 0x33, 0x6a, (byte) 0xe2, (byte) 0xd4, (byte) 0x97, 0x14, 0x0f,
						0x0a, (byte) 0xd4, (byte) 0x83, 0x06, 0x09, 0x2b, (byte) 0xee, (byte) 0xda, 0x0d, 0x36,
						(byte) 0xc3, 0x1c, 0x73, 0x73, 0x10, 0x39, 0x56, (byte) 0xc9, 0x02, 0x61};

		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testGetString() throws Exception {
		final byte[] byteData = new byte[]
				{(byte) 0xf5, 0x49, 0x6c, 0x59, (byte) 0xff, 0x33, 0x6a, (byte) 0xe2, (byte) 0xd4, (byte) 0x97, 0x14, 0x0f,
						0x0a, (byte) 0xd4, (byte) 0x83, 0x06, 0x09, 0x2b, (byte) 0xee, (byte) 0xda, 0x0d, 0x36,
						(byte) 0xc3, 0x1c, 0x73, 0x73, 0x10, 0x39, 0x56, (byte) 0xc9, 0x02, 0x61};
		final String actual = HexEncoder.getString(byteData);
		final String expected = "f5496c59ff336ae2d497140f0ad48306092beeda0d36c31c7373103956c90261"; // test acc public key
		Assert.assertEquals(expected, actual);
	}
}
