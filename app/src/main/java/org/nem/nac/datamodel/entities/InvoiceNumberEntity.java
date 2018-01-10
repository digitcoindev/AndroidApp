package org.nem.nac.datamodel.entities;

public class InvoiceNumberEntity extends PersistentEntity {

	public int lastStored;

	public InvoiceNumberEntity() {
	}

	public InvoiceNumberEntity(final int lastStored) {
		this.lastStored = lastStored;
	}
}
