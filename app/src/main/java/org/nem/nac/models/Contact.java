package org.nem.nac.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import org.nem.nac.common.exceptions.AddressFormatRuntimeException;
import org.nem.nac.models.primitives.AddressValue;

public class Contact implements Parcelable {

	public final Long   contactId;
	private Long _rawContactId;
	private      String _name;
	private      String _rawAddress;

	public Contact(final Long contactId, final Long rawContactId) {
		this.contactId = contactId;
		this._rawContactId = rawContactId;
	}

	public boolean existingContact() {
		return _rawContactId != null;
	}

	@NonNull
	public String getName() {
		return _name != null ? _name : "";
	}

	public void setName(final String name) {
		_name = name;
	}

	public Optional<Long> getRawContactId() {
		return Optional.ofNullable(_rawContactId);
	}

	public void setRawContactId(final Long rawId) {
		_rawContactId = rawId;
	}

	@Nullable
	public String getRawAddress() {
		return _rawAddress;
	}

	public void setRawAddress(final String rawAddress) {
		_rawAddress = rawAddress;
	}

	public boolean hasValidAddress() {
		return AddressValue.isValid(_rawAddress);
	}

	/**
	 * @return valid NEM address if present.
	 */
	@NonNull
	public Optional<AddressValue> getValidAddress() {
		if (_rawAddress == null) { return Optional.empty(); }
		try {
			return Optional.of(new AddressValue(_rawAddress));
		} catch (AddressFormatRuntimeException e) {
			return Optional.empty();
		}
	}

	//region parcelable

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(this.contactId);
		dest.writeValue(this._rawContactId);
		dest.writeString(this._name);
		dest.writeString(this._rawAddress);
	}

	protected Contact(Parcel in) {
		this.contactId = (Long)in.readValue(Long.class.getClassLoader());
		this._rawContactId = (Long)in.readValue(Long.class.getClassLoader());
		this._name = in.readString();
		this._rawAddress = in.readString();
	}

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
		public Contact createFromParcel(Parcel source) {return new Contact(source);}

		public Contact[] newArray(int size) {return new Contact[size];}
	};
	//endregion
}
