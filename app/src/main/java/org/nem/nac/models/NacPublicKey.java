package org.nem.nac.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PrivateKey;
import org.nem.core.crypto.PublicKey;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.primitives.AddressValue;

public final class NacPublicKey extends BinaryData {
	public NacPublicKey(@NonNull final byte[] raw) {
		super(raw);
	}

	public NacPublicKey(@NonNull final String rawHex) {
		super(rawHex);
	}

	@NonNull
	public static NacPublicKey fromPrivateKey(@NonNull final NacPrivateKey privateKey) {
		AssertUtils.notNull(privateKey);
		final PrivateKey key = PrivateKey.fromBytes(privateKey.getRaw());
		final KeyPair keyPair = new KeyPair(key);
		return new NacPublicKey(keyPair.getPublicKey().getRaw());
	}

	@NonNull
	public PublicKey toPublicKey() {
		return new PublicKey(this.rawData);
	}

	@NonNull
	public AddressValue toAddress() {
		return AddressValue.fromPublicKey(this);
	}

	// This is used in many adapters to show friendly account names to the user, so if making changes, do it carefully.
	@Override
	public String toString() {
		return toAddress().toName().orElse(this.toAddress().toString(true));
	}

	//region parcelable
	protected NacPublicKey(Parcel in) {
		super(in);
	}

	public static final Parcelable.Creator<NacPublicKey> CREATOR = new Parcelable.Creator<NacPublicKey>() {
		public NacPublicKey createFromParcel(Parcel source) {return new NacPublicKey(source);}

		public NacPublicKey[] newArray(int size) {return new NacPublicKey[size];}
	};
	//endregion
}
