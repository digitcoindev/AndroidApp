package org.nem.nac.models.qr;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.Xems;
import org.nem.nac.models.primitives.AddressValue;

public final class QrInvoice extends BaseQrData {

	@JsonProperty("name")
	public final String       name;
	@JsonProperty("addr")
	public final AddressValue address;
	@JsonProperty("amount")
	public final Xems         amount;
	@JsonProperty("msg")
	public       String       message;

	@SuppressWarnings("unused") // Needed to create from json
	public QrInvoice() {
		name = null;
		address = null;
		amount = null;
		message = null;
	}

	public QrInvoice(final String name, final AddressValue address, final Xems amount, final String message) {
		this.name = name;
		this.address = address;
		this.amount = amount;
		this.message = message;
	}

	@Override
	public boolean validate() {
		if (message == null) { message = ""; }
		return StringUtils.isNotNullOrEmpty(name)
			&& address != null && AddressValue.isValid(address)
			&& amount != null && amount.isMoreOrEqual(Xems.ZERO);
	}

	@Override
	public String toString() {
		if (!validate()) {
			return "Invalid!";
		}
		return String.format("Name: %s, amount: %s", name, amount.toFractionalString());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		QrInvoice qrInvoice = (QrInvoice)o;

		if (name != null ? !name.equals(qrInvoice.name) : qrInvoice.name != null) { return false; }
		if (address != null ? !address.equals(qrInvoice.address) : qrInvoice.address != null) { return false; }
		if (amount != null ? !amount.equals(qrInvoice.amount) : qrInvoice.amount != null) { return false; }
		return !(message != null ? !message.equals(qrInvoice.message) : qrInvoice.message != null);
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (address != null ? address.hashCode() : 0);
		result = 31 * result + (amount != null ? amount.hashCode() : 0);
		result = 31 * result + (message != null ? message.hashCode() : 0);
		return result;
	}
}
