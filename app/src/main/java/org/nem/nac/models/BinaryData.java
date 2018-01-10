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

/**
 * Class represents immutable binary data.
 */
public class BinaryData implements Parcelable {

	/**
	 * BinaryData with zero length.
	 */
	public static final BinaryData EMPTY = new BinaryData(new byte[0]);

	protected final byte[] rawData;

	public BinaryData(@NonNull final byte[] rawData) {
		this.rawData = rawData;
	}

	@JsonCreator
	public BinaryData(@NonNull final String rawDataHex) {
		this.rawData = HexEncoder.getBytes(rawDataHex);
	}

	public int length() {
		return rawData.length;
	}

	@NonNull
	public byte[] getRaw() {
		return rawData;
	}

	@NonNull
	@JsonValue
	public String toHexStr() {
		return HexEncoder.getString(rawData);
	}

	public EncryptedBinaryData encrypt(@NonNull final BinaryData key)
			throws NacCryptoException {
		return new EncryptedBinaryData(AesCryptographer.instance().encrypt(this, key));
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(rawData);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BinaryData that = (BinaryData) o;

		return Arrays.equals(rawData, that.rawData);
	}

	@Override
	public String toString() {
		return toHexStr();
	}

	//region parcelable
	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {dest.writeByteArray(this.rawData);}

	protected BinaryData(Parcel in) {this.rawData = in.createByteArray();}

	public static final Parcelable.Creator<BinaryData> CREATOR = new Parcelable.Creator<BinaryData>() {
		public BinaryData createFromParcel(Parcel source) {return new BinaryData(source);}

		public BinaryData[] newArray(int size) {return new BinaryData[size];}
	};
	//endregion
}
