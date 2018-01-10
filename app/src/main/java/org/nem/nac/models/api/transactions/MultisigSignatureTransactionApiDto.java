package org.nem.nac.models.api.transactions;

import org.nem.nac.common.models.HashValue;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.primitives.AddressValue;

public class MultisigSignatureTransactionApiDto extends AbstractTransactionApiDto {
	/**
	 * The transaction signature.
	 */
	public BinaryData signature;

	/**
	 * The hash of the inner transaction of the corresponding multisig transaction.
	 */
	public HashValue    otherHash;
	/**
	 * The address of the corresponding multisig account.
	 */
	public AddressValue otherAccount;
}
