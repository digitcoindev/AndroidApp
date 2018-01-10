package org.nem.nac.models;

import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.qr.QrDto;
import org.nem.nac.models.qr.QrInvoice;

public final class Invoice {
	public long id;
	public long accountId;
	public String name;
	public AddressValue address;
	public Xems amount;
	public String message;

	public Invoice() {

	}

	public Invoice(final String name, final AddressValue address, final Xems amount, final String message) {
		this.name = name;
		this.address = address;
		this.amount = amount;
		this.message = message;
	}

	public QrDto toQrDto() {
		return new QrDto(QrDto.Type.INVOICE, new QrInvoice(name, address, amount, message));
	}
}
