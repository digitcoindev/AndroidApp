package org.nem.nac.models.transactions.drafts;

import android.support.annotation.NonNull;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.IOUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class MultisigTransactionDraft extends AbstractTransactionDraft {
	private static final int VERSION = 1;

	@NonNull
	private final AbstractTransactionDraft innerTran;

	/**
	 * @throws NacRuntimeException If {@code innerTran} has unsupported type
	 */
	public MultisigTransactionDraft(final NacPublicKey signer, @NonNull final AbstractTransactionDraft innerTran)
			throws NacRuntimeException {
		super(VERSION, signer);
		AssertUtils.notNull(innerTran);
		if (!innerTranHasAllowedType(innerTran)) {
			throw new NacRuntimeException("Bad inner transaction type");
		}
		this.innerTran = innerTran;
	}

	@NonNull
	@Override
	public Xems calculateMinimumFee() {
		//return Xems.fromXems(6);
		//return Xems.fromXems(3* AppConstants.MinimumFee_factor);
		return Xems.fromXems(AppConstants.MinimumFee_basic* AppConstants.MinimumFee_factor);
	}

	@NonNull
	@Override
	public TransactionType getType() {
		return TransactionType.MULTISIG_TRANSACTION;
	}

	@Override
	protected void serializeAdditional(@NonNull final ByteArrayOutputStream os)
			throws IOException {
		final ByteArrayOutputStream innerTranOs = new ByteArrayOutputStream();
		serializeInnerTran(innerTranOs);

		// Length of inner transaction object.
		writeAsLeBytes(os, innerTranOs.size());
		// What follows here is the inner transaction object.
		innerTranOs.writeTo(os);
	}

	private void serializeInnerTran(final ByteArrayOutputStream os)
			throws IOException {
		try {
			innerTran.serialize(os);
		} catch (NacException e) {
			throw new IOException("Failed to serialize inner transaction!");
		} finally {
			IOUtils.closeSilently(os);
		}
	}

	private boolean innerTranHasAllowedType(@NonNull final AbstractTransactionDraft otherTrans) {
		final TransactionType type = otherTrans.getType();
		return type == TransactionType.TRANSFER_TRANSACTION || type == TransactionType.IMPORTANCE_TRANSFER_TRANSACTION || type == TransactionType.MULTISIG_AGGREGATE_MODIFICATION_TRANSACTION;
	}
}
