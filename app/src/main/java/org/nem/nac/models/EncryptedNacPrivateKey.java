package org.nem.nac.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.nem.nac.crypto.NacCryptoException;

public final class EncryptedNacPrivateKey extends EncryptedBinaryData {
	public EncryptedNacPrivateKey(@NonNull final byte[] raw) {
		super(raw);
	}

	@JsonCreator
	public EncryptedNacPrivateKey(@NonNull final String rawDataHex) {
		super(rawDataHex);
	}

	public NacPrivateKey decryptKey(@NonNull final BinaryData encryptionKey)
			throws NacCryptoException {
		final BinaryData decrypted = super.decrypt(encryptionKey);
		return new NacPrivateKey(decrypted.rawData);
	}

	//region Parcelable
	protected EncryptedNacPrivateKey(Parcel in) {
		super(in);
	}

	public static final Parcelable.Creator<EncryptedNacPrivateKey> CREATOR = new Parcelable.Creator<EncryptedNacPrivateKey>() {
		public EncryptedNacPrivateKey createFromParcel(Parcel source) {return new EncryptedNacPrivateKey(source);}

		public EncryptedNacPrivateKey[] newArray(int size) {return new EncryptedNacPrivateKey[size];}
	};
	//endregion
}
