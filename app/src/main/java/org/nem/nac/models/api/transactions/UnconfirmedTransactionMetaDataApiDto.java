package org.nem.nac.models.api.transactions;

import org.nem.nac.models.BinaryData;

public final class UnconfirmedTransactionMetaDataApiDto {
	/**
	 * The hash of the inner transaction or null if the transaction is not a multisig transaction.
	 */
	public BinaryData data;
}
