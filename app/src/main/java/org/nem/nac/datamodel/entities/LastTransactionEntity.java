package org.nem.nac.datamodel.entities;

import org.nem.nac.common.enums.LastTransactionType;

public final class LastTransactionEntity extends PersistentEntity {

	public String address;
	public byte[] hash;
	public int    type;

	@SuppressWarnings("unused")
	public LastTransactionEntity() {
	}

	public LastTransactionEntity(final Long id, final String address, final byte[] hash, final LastTransactionType type) {
		this._id = id;
		this.address = address;
		this.hash = hash;
		this.type = type.getRaw();
	}
}
