package org.nem.nac.models.api.transactions;

import android.support.annotation.Nullable;

import org.nem.nac.models.BinaryData;

public final class MultisigAggregateModificationTransactionApiDto extends AbstractTransactionApiDto {
	/**
	 * The transaction signature (missing if part of a multisig transaction).
	 */
	@Nullable
	public BinaryData                              signature;
	/**
	 * The JSON array of multisig modifications.
	 */
	public MultisigCosignatoryModificationApiDto[] modifications;

	/**
	 * object that holds the minimum cosignatories modification.
	 */
	public MinCosignatoriesApiDto minCosignatories;
}
