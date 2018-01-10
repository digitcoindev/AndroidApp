package org.nem.nac.models.transactions.drafts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.models.TimeValue;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.ConvertUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.providers.NodeInfoProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Transaction draft that should be assembled, signed and then announced
 */
public abstract class AbstractTransactionDraft {
	protected final int version;
	protected TimeValue timestamp = TimeValue.INVALID;
	protected TimeValue deadline  = TimeValue.INVALID;
	@NonNull
	public NacPublicKey signer;
	@Nullable
	public Xems         fee;

	public AbstractTransactionDraft(final int transactionVersion, @NonNull final NacPublicKey signer) {
		AssertUtils.notNull(signer);
		this.version = transactionVersion | AppConstants.NETWORK_VERSION.get() << 24;
		this.signer = signer;
	}

	/**
	 * Sets transaction fee. If set to null, minimum calculated fee will be used.
	 *
	 * @throws NacException if fee is smaller than minimum fee.
	 */
	public void setFee(@Nullable Xems fee)
			throws NacException {
		if (fee == null) {
			this.fee = null;
			return;
		}
		if (calculateMinimumFee().isMoreThan(fee)) {
			throw new NacException("Fee is smaller than minimum!");
		}
		this.fee = fee;
	}

	/**
	 * Writes this transaction into outputStream as described in specs.
	 *
	 * @throws NacException if serialization failed
	 */
	public final void serialize(@NonNull final ByteArrayOutputStream outputStream)
			throws NacException {
		AssertUtils.notNull(signer);

		try {
			if (timestamp.equals(TimeValue.INVALID)) {
				timestamp = NodeInfoProvider.instance().getNetworkTime();
			}
			if (deadline.equals(TimeValue.INVALID)) {
				deadline = timestamp.addDefaultDeadline();
			}
			if (fee == null) {
				fee = calculateMinimumFee();
			}

			writeAsLeBytes(outputStream, getType().getValue());
			writeAsLeBytes(outputStream, version);
			writeAsLeBytes(outputStream, timestamp.getValue());
			writeAsLeBytes(outputStream, signer.length());
			outputStream.write(signer.getRaw());
			writeAsLeBytes(outputStream, fee.getAsMicro());
			writeAsLeBytes(outputStream, deadline.getValue());
			serializeAdditional(outputStream);
		} catch (IOException e) {
			throw new NacException("Failed to serialize transaction", e);
		}
	}

	/**
	 * Returns minimum transaction fee
	 */
	@NonNull
	public abstract Xems calculateMinimumFee();

	@NonNull
	public abstract TransactionType getType();

	/**
	 * This method will be called after serializing common transaction data
	 * when calling {@link AbstractTransactionDraft#serialize(java.io.ByteArrayOutputStream)}
	 * Any subclass implementing it should serialize only its own data here.
	 */
	protected abstract void serializeAdditional(@NonNull final ByteArrayOutputStream outputStream)
			throws IOException;

	protected void writeAsLeBytes(@NonNull final OutputStream outputStream, final int value)
			throws IOException {
		outputStream.write(ConvertUtils.toLeBytes(value));
	}

	protected void writeAsLeBytes(@NonNull final OutputStream outputStream, final long value)
			throws IOException {
		outputStream.write(ConvertUtils.toLeBytes(value));
	}
}
