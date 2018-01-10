package org.nem.nac.models.account;

public final class MultisigInfo {
	/**
	 * When setting minCosignatories to 0, it means all cosignatories needed to sign transactions from multisig account.
	 */
	public static final int MIN_COSIGNATORIES_ALL = 0;
	public int cosignatoriesCount;
	public int minCosignatories;
}
