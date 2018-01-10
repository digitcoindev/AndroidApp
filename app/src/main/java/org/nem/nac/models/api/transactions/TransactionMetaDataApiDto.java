package org.nem.nac.models.api.transactions;

import org.nem.nac.common.models.HashValue;

public final class TransactionMetaDataApiDto {
	/**
	 * The height of the block in which the transaction was included.
	 */
	public long      height;
	/**
	 * The id of the transaction.
	 */
	public int       id;
	/**
	 * The transaction hash.
	 */
	public HashValue hash;

	@Override
	public int hashCode() {
		return hash != null ? hash.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TransactionMetaDataApiDto that = (TransactionMetaDataApiDto) o;

		return !(hash != null ? !hash.equals(that.hash) : that.hash != null);
	}
}
