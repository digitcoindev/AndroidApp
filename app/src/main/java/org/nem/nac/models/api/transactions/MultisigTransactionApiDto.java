package org.nem.nac.models.api.transactions;

import org.nem.nac.models.BinaryData;

public class MultisigTransactionApiDto extends AbstractTransactionApiDto {
	/**
	 * The transaction signature.
	 */
	public BinaryData                           signature;
	/**
	 * The inner transaction.
	 * The inner transaction can be a transfer transaction,
	 * an importance transfer transaction or a multisig aggregate modification transaction.
	 * <i>The inner transaction does not have a valid signature.</i>
	 */
	public AbstractTransactionApiDto            otherTrans;
	public MultisigSignatureTransactionApiDto[] signatures;
}
