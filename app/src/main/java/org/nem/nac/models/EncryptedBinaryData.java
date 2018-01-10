package org.nem.nac.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.core.utils.HexEncoder;
import org.nem.nac.crypto.AesCryptographer;
import org.nem.nac.crypto.NacCryptoException;

import java.util.Arrays;

public class EncryptedBinaryData implements Parcelable {
	protected final byte[] rawData;

	public EncryptedBinaryData(@NonNull final BinaryData rawData) {
		this.rawData = rawData.getRaw();
	}

	public EncryptedBinaryData(@NonNull final byte[] rawData) {
		this.rawData = rawData;
	}

	@JsonCreator
	public EncryptedBinaryData(@NonNull final String rawDataHex) {
		this.rawData = HexEncoder.getBytes(rawDataHex);
	}

	public int length() {
		return rawData.length;
	}

	public byte[] getRaw() {
		return rawData;
	}

	@NonNull
	@JsonValue
	public String getAsHex() {
		return HexEncoder.getString(rawData);
	}

	public BinaryData decrypt(final BinaryData key)
			throws NacCryptoException {
		return AesCryptographer.instance().decrypt(this, key);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(rawData);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EncryptedBinaryData that = (EncryptedBinaryData) o;

		return Arrays.equals(rawData, that.rawData);
	}

	@Override
	public String toString() {
		return getAsHex();
	}

	@Override
	public int describeContents() { return 0; }

	//region Parcelable
	@Override
	public void writeToParcel(Parcel dest, int flags) {dest.writeByteArray(this.rawData);}

	protected EncryptedBinaryData(Parcel in) {this.rawData = in.createByteArray();}

	public static final Parcelable.Creator<EncryptedBinaryData> CREATOR = new Parcelable.Creator<EncryptedBinaryData>() {
		public EncryptedBinaryData createFromParcel(Parcel source) {return new EncryptedBinaryData(source);}

		public EncryptedBinaryData[] newArray(int size) {return new EncryptedBinaryData[size];}
	};
	//endregion
}
