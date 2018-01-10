package org.nem.nac.datamodel.entities;

public final class InvoiceMessageEntity extends PersistentEntity {
	public String prefix;
	public String postfix;
	public String message;

	public InvoiceMessageEntity() {
	}

	public InvoiceMessageEntity(final String prefix, final String postfix, final String message) {
		this.prefix = prefix;
		this.postfix = postfix;
		this.message = message;
	}
}
