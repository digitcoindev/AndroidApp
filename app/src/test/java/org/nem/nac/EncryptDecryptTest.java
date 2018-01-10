package org.nem.nac;

import org.junit.Assert;
import org.junit.Test;
import org.nem.nac.crypto.KeyProvider;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPrivateKey;

import java.util.Random;

public final class EncryptDecryptTest {

	@Test
	public void testEncryptionKeysAlwaysTheSame()
		throws Exception {
		BinaryData eKeyFirst;
		for (int i = 100; i >= 0; i--) {
			final int pwdLength = new Random().nextInt(15) + 6;
			final String password = getRandomPassword(pwdLength);
			for (int k = 11; k > 0; k--) {
				final BinaryData salt = KeyProvider.generateSalt();
				eKeyFirst = KeyProvider.deriveKey(password, salt);
				System.out.println(String.format("First key for pwd: %s, salt: %s, key: %s", password, salt, eKeyFirst));
				for (int j = 17; j > 0; j--) {
					final BinaryData newKey = KeyProvider.deriveKey(password, salt);
					Assert.assertArrayEquals("Keys not match!!!", eKeyFirst.getRaw(), newKey.getRaw());
				}
			}
		}
	}

	@Test
	public void testEncryptDecryptIsCorrect()
		throws Exception {
		final NacPrivateKey originalKey =
			new NacPrivateKey("c7a10487f0c2be5cf691b42864e13be95d172f67aa1c8e018932ce09f700d962");
		final String pass1 = "123456";
		final String pass2 = "йцукен";
		final BinaryData salt1 = KeyProvider.generateSalt();

		///
		/// 1
		final BinaryData eKeyEnc1 = KeyProvider.deriveKey(pass1, salt1);
		final EncryptedNacPrivateKey encrypted1 = originalKey.encryptKey(eKeyEnc1);
		System.out.println("Encrypted1: " + encrypted1.toString());

		final BinaryData eKeyDec1 = KeyProvider.deriveKey(pass1, salt1);
		// Encryption keys are generated exactly the same
		Assert.assertArrayEquals("Encryption key 1 is different!", eKeyEnc1.getRaw(), eKeyDec1.getRaw());
		System.out.println("Encryption keys 1 are same");
		final NacPrivateKey decryptedKey1 = encrypted1.decryptKey(eKeyDec1);
		Assert.assertArrayEquals("Decrypted key 1 is different!", originalKey.getRaw(), decryptedKey1.getRaw());
		System.out.println("Decrypted key 1 OK");
		System.gc();
		Thread.sleep(1000, 0);
		///
		/// 2
		final BinaryData salt2 = KeyProvider.generateSalt();
		final BinaryData eKeyEnc2 = KeyProvider.deriveKey(pass2, salt2);
		final EncryptedNacPrivateKey encrypted2 = originalKey.encryptKey(eKeyEnc2);
		System.out.println("Encrypted2: " + encrypted2.toString());

		final BinaryData eKeyDec2 = KeyProvider.deriveKey(pass2, salt2);
		// Encryption keys are generated exactly the same
		Assert.assertArrayEquals("Encryption key 2 is different!", eKeyEnc2.getRaw(), eKeyDec2.getRaw());
		System.out.println("Encryption keys 2 are same");
		final NacPrivateKey decryptedKey2 = encrypted2.decryptKey(eKeyDec2);
		Assert.assertArrayEquals("Decrypted key 2 is different!", originalKey.getRaw(), decryptedKey2.getRaw());
		System.out.println("Decrypted key 2 OK");

		/// 3 load
		for (int i = 0; i > 0; i--) {
			final String pass = getRandomPassword(new Random().nextInt(15) + 6);
			final BinaryData salt = KeyProvider.generateSalt();
			final BinaryData eKeyEnc = KeyProvider.deriveKey(pass, salt);
			final EncryptedNacPrivateKey encrypted = originalKey.encryptKey(eKeyEnc);
			System.out.println("Encrypted: " + encrypted.toString());

			final BinaryData eKeyDec = KeyProvider.deriveKey(pass, salt);
			// Encryption keys are generated exactly the same
			Assert.assertArrayEquals("Encryption key is different!", eKeyEnc.getRaw(), eKeyDec.getRaw());
			System.out.println("Encryption keys are same");
			final NacPrivateKey decryptedKey = encrypted.decryptKey(eKeyDec);
			Assert.assertArrayEquals("Decrypted key is different!", originalKey.getRaw(), decryptedKey.getRaw());
			System.out.println("Decrypted key OK");
		}
	}

	private String getRandomPassword(final int outputLength) {
		char[] chars = "abcdefghijklmnopqrstuvwxyzабвгдеєжзиіїйклмнопрстуфхцчшщьюя!@#$%^&*()_+-=".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < outputLength; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
}
