package org.nem.nac.models.api.transactions;

import android.support.annotation.Nullable;

import org.nem.nac.common.enums.ImportanceTransferMode;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPublicKey;

public class ImportanceTransferTransactionApiDto extends AbstractTransactionApiDto {
	/**
	 * The transaction signature (missing if part of a multisig transaction).
	 */
	@Nullable
	public BinaryData             signature;
	/**
	 * The mode.
	 */
	public ImportanceTransferMode mode;
	/**
	 * The public key of the receiving account as hexadecimal string.
	 */
	public NacPublicKey           remoteAccount;
}
