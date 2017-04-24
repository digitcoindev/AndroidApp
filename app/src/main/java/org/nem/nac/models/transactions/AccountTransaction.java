package org.nem.nac.models.transactions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.api.transactions.AbstractTransactionApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;

public final class AccountTransaction implements Comparable<AccountTransaction> {

	public final NacPublicKey                         account;
	public final AddressValue                         address;
	public final AbstractTransactionApiDto            transaction;
	public final boolean                              isConfirmed;
	@Nullable
	public final TransactionMetaDataApiDto            metadata;
	@Nullable
	public final UnconfirmedTransactionMetaDataApiDto unconfirmedMetadata;

	public AccountTransaction(final NacPublicKey account, final TransactionMetaDataPairApiDto confirmed) {
		this.account = account;
		this.address = account != null ? account.toAddress() : null;
		this.transaction = confirmed.transaction;
		this.isConfirmed = true;
		metadata = confirmed.meta;
		unconfirmedMetadata = null;
	}

	public AccountTransaction(final NacPublicKey account, final UnconfirmedTransactionMetaDataPairApiDto unconfirmed) {
		this.account = account;
		this.address = account != null ? account.toAddress() : null;
		this.transaction = unconfirmed.transaction;
		this.isConfirmed = false;
		this.metadata = null;
		this.unconfirmedMetadata = unconfirmed.meta;
	}

	@Override
	public int compareTo(@NonNull final AccountTransaction another) {
		AssertUtils.notNull(another);
		return -(this.transaction.timeStamp.compareTo(another.transaction.timeStamp));
	}

	public ConfirmationStatus getConfirmationStatus() {
		if (metadata != null) {
			return ConfirmationStatus.CONFIRMED;
		}
		else if (unconfirmedMetadata != null) {
			return ConfirmationStatus.UNCONFIRMED;
		}
		else { throw new IllegalStateException("Unknown transaction type"); }
	}

	public enum ConfirmationStatus {
		CONFIRMED,
		UNCONFIRMED
	}
}
