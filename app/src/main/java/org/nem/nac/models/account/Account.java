package org.nem.nac.models.account;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.NacPublicKey;

public final class Account implements Parcelable {

	public       long                   id;
	public int sortIndex = 0;
	public       String                 name;
	/**
	 * Encrypted private key
	 */
	public       EncryptedNacPrivateKey privateKey;
	@NonNull
	public final PublicAccountData      publicData;
	public AccountType type = AccountType.SIMPLE;

	/**
	 * @throws NacCryptoException If encrypting private key failed.
	 */
	public Account(
			@NonNull final String name,
			@NonNull final NacPrivateKey privateKey,
			@NonNull final BinaryData encryptionKey)
			throws NacCryptoException {
		AssertUtils.notNull(name);
		AssertUtils.notNull(privateKey);
		AssertUtils.notNull(encryptionKey);

		this.name = name;
		this.privateKey = privateKey.encryptKey(encryptionKey);
		this.publicData = new PublicAccountData(NacPublicKey.fromPrivateKey(privateKey));
	}

	public Account(
			@NonNull final String name,
			@NonNull final EncryptedNacPrivateKey privateKey,
			@NonNull final PublicAccountData publicData) {
		AssertUtils.notNull(name);
		AssertUtils.notNull(privateKey);
		AssertUtils.notNull(publicData);

		this.name = name;
		this.privateKey = privateKey;
		this.publicData = publicData;
	}

	/**
	 * @param publicData
	 * @apiNote This is for mapper only
	 */
	public Account(@NonNull PublicAccountData publicData) {
		AssertUtils.notNull(publicData);
		this.publicData = publicData;
	}

	@Override
	public String toString() {
		return name;
	}

	// Region Parcelable

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeInt(this.sortIndex);
		dest.writeString(this.name);
		dest.writeParcelable(this.privateKey, 0);
		dest.writeParcelable(this.publicData, 0);
		dest.writeInt(this.type == null ? -1 : this.type.ordinal());
	}

	protected Account(Parcel in) {
		this.id = in.readLong();
		this.sortIndex = in.readInt();
		this.name = in.readString();
		this.privateKey = in.readParcelable(EncryptedNacPrivateKey.class.getClassLoader());
		this.publicData = in.readParcelable(PublicAccountData.class.getClassLoader());
		int tmpType = in.readInt();
		this.type = tmpType == -1 ? null : AccountType.values()[tmpType];
	}

	public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
		public Account createFromParcel(Parcel source) {return new Account(source);}

		public Account[] newArray(int size) {return new Account[size];}
	};
	// Endregion
}
