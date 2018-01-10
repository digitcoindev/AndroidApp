package org.nem.nac.models.invoice;

public final class InvoiceNumber {

	public int lastStored;

	public InvoiceNumber(final int lastStored) {
		this.lastStored = lastStored;
	}

	public int incrementByOne() {
		return ++lastStored;
	}
}
