package org.nem.core.crypto.ed25519;

import org.junit.Assert;
import org.junit.Test;
import org.nem.core.crypto.DsaSigner;
import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PrivateKey;
import org.nem.core.crypto.PublicKey;
import org.nem.core.utils.HexEncoder;

public class Ed25519DsaSignerTest {
	@Test
	public void testSign() throws Exception {
		final String dataHex = "010100000100009803555e0020000000f5496c59ff336ae2d497140f0ad48306092beeda0d36c31c7373103956c9026180841e000000000063a95e002800000054435550565143373754414d4837514b504650354f5433544c5556344a59525056364345474a585700ca9a3b000000001900000001000000110000004669727374205445535421205965616821";
		final byte[] data = HexEncoder.getBytes(dataHex);
		final String privateKeyHex = "c7a10487f0c2be5cf691b42864e13be95d172f67aa1c8e018932ce09f700d962";
		final String publicKeyHex = "f5496c59ff336ae2d497140f0ad48306092beeda0d36c31c7373103956c90261";
		final KeyPair keyPair = new KeyPair(PrivateKey.fromHexString(privateKeyHex), PublicKey.fromHexString(publicKeyHex));
		DsaSigner dsaSigner = new Ed25519DsaSigner(keyPair);
		final byte[] expectedSignature = HexEncoder.getBytes("79eb96112c2c57d064316105b80ebe6c14eb2622b25844b96d96ed51136cb434f1919658f02eaf8c592ac5e55d38bce4a64fcadeece160295dd16fb2955f020a");

		final byte[] actual = dsaSigner.sign(data).getBytes();

		Assert.assertArrayEquals("Wrong signature!", expectedSignature, actual);
	}
}
