package org.nem.nac.models;

import android.support.annotation.NonNull;

import org.nem.core.crypto.PrivateKey;
import org.nem.nac.crypto.NacCryptoException;

public final class NacPrivateKey extends BinaryData {

	public NacPrivateKey(@NonNull final byte[] raw) {
		super(raw);
	}

	public NacPrivateKey(@NonNull final String rawDataHex) {
		super(rawDataHex);
	}

	@NonNull
	public EncryptedNacPrivateKey encryptKey(@NonNull final BinaryData encryptionKey)
			throws NacCryptoException {
		final EncryptedBinaryData encryptedData = super.encrypt(encryptionKey);
		return new EncryptedNacPrivateKey(encryptedData.rawData);
	}

	@NonNull
	public PrivateKey toPrivateKey() {
		return PrivateKey.fromBytes(this.rawData);
	}
}
