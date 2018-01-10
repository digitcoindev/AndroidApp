package org.nem.nac.models.account;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.primitives.AddressValue;

/**
 * Represents public account data
 */
public class PublicAccountData implements Parcelable {
	@NonNull
	public final NacPublicKey publicKey;
	@NonNull
	public final AddressValue address;

	public PublicAccountData(@NonNull final NacPublicKey publicKey) {
		this(publicKey, AddressValue.fromPublicKey(publicKey));
	}

	public PublicAccountData(@NonNull final NacPublicKey publicKey, @Nullable final AddressValue address) {
		AssertUtils.notNull(publicKey);
		this.publicKey = publicKey;
		this.address = address != null ? address : AddressValue.fromPublicKey(publicKey);
	}

	/**
	 * Validates if public key and address belong to same account
	 *
	 * @throws NacException if validation failed
	 */
	public void validate()
			throws NacException {
		if (!address.equals(AddressValue.fromPublicKey(publicKey))) {
			throw new NacException("Inconsistent account data");
		}
	}

	@Override
	public int hashCode() {
		return publicKey.hashCode();
	}

	public boolean equals(final NacPublicKey publicKey) {
		return this.publicKey.equals(publicKey);
	}

	public boolean equals(final AddressValue address) {
		return this.address.equals(address);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PublicAccountData that = (PublicAccountData)o;

		return publicKey.equals(that.publicKey) && address.equals(that.address);
	}

	//region parcelable
	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.publicKey, flags);
		dest.writeParcelable(this.address, 0);
	}

	protected PublicAccountData(Parcel in) {
		this.publicKey = in.readParcelable(NacPublicKey.class.getClassLoader());
		this.address = in.readParcelable(AddressValue.class.getClassLoader());
	}

	public static final Parcelable.Creator<PublicAccountData> CREATOR = new Parcelable.Creator<PublicAccountData>() {
		public PublicAccountData createFromParcel(Parcel source) {return new PublicAccountData(source);}

		public PublicAccountData[] newArray(int size) {return new PublicAccountData[size];}
	};
	//endregion
}
