package org.nem.nac.models.transactions.drafts;

import android.support.annotation.NonNull;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.SizeOf;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.primitives.AddressValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class MultisigSignatureTransactionDraft extends AbstractTransactionDraft {
	private static final int VERSION = 1;

	/**
	 * The hash of the inner transaction of the corresponding multisig transaction.
	 */
	public final BinaryData   otherHash;
	/**
	 * The address of the corresponding multisig account.
	 */
	public final AddressValue otherAccount;

	/**
	 * @param signer       Transaction signer.
	 * @param otherHash    The hash of the inner transaction of the corresponding multisig transaction.
	 * @param otherAccount The address of the corresponding multisig account.
	 */
	public MultisigSignatureTransactionDraft(
			@NonNull final NacPublicKey signer, final BinaryData otherHash, final AddressValue otherAccount) {
		super(VERSION, signer);
		this.otherHash = otherHash;
		this.otherAccount = otherAccount;
	}

	@NonNull
	@Override
	public TransactionType getType() {
		return TransactionType.MULTISIG_SIGNATURE_TRANSACTION;
	}

	@NonNull
	@Override
	public Xems calculateMinimumFee() {
		//return Xems.fromXems(6);
		//return Xems.fromXems(3*AppConstants.MinimumFee_factor);
		return Xems.fromXems(AppConstants.MinimumFee_basic*AppConstants.MinimumFee_factor);
	}

	@Override
	protected void serializeAdditional(@NonNull final ByteArrayOutputStream os)
			throws IOException {
		// Length of hash object (hash of the corresponding multisig transaction) Always: 0x24, 0x00, 0x00, 0x00
		writeAsLeBytes(os, otherHash.length() + SizeOf.INT);
		// Length of hash: 4 bytes (integer). Always: 0x20, 0x00, 0x00, 0x00
		writeAsLeBytes(os, otherHash.length());
		// SHA3 hash bytes: 32 bytes.
		os.write(otherHash.getRaw());
		// Length of address of the corresponding multisig account (always 40): 4 bytes (integer).
		writeAsLeBytes(os, otherAccount.length());
		// Multisig account address: 40 bytes (using UTF8 encoding).
		os.write(otherAccount.getRaw().getBytes(AppConstants.ENCODING_UTF8));
	}
}
