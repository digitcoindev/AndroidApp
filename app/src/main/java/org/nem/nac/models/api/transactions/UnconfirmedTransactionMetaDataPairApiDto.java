package org.nem.nac.models.api.transactions;

public final class UnconfirmedTransactionMetaDataPairApiDto {

	public UnconfirmedTransactionMetaDataApiDto meta;
	public AbstractTransactionApiDto            transaction;

	public boolean isMultisig() {
		return meta.data != null && transaction instanceof MultisigTransactionApiDto;
	}
}
