package org.nem.nac.models.qr;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.primitives.AddressValue;

public final class QrUserInfo extends BaseQrData {

	@JsonProperty("name")
	public final String       name;
	@JsonProperty("addr")
	public final AddressValue address;

	@SuppressWarnings("unused")
	public QrUserInfo() {
		name = null;
		address = null;
	}

	public QrUserInfo(String name, AddressValue address) {
		this.name = name;
		this.address = address;
	}

	@Override
	public boolean validate() {
		return StringUtils.isNotNullOrEmpty(name)
			&& address != null && AddressValue.isValid(address);
	}

	@Override
	public String toString() {
		if (!validate()) {
			return "Invalid!";
		}
		return String.format("Name: %s, addr: %s", name, address);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		QrUserInfo that = (QrUserInfo)o;

		if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
		return !(address != null ? !address.equals(that.address) : that.address != null);
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (address != null ? address.hashCode() : 0);
		return result;
	}
}
