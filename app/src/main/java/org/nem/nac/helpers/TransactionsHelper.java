package org.nem.nac.helpers;

import com.annimon.stream.function.Predicate;

import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.utils.CollectionUtils;
import org.nem.nac.models.api.transactions.AbstractTransactionApiDto;
import org.nem.nac.models.api.transactions.MultisigAggregateModificationTransactionApiDto;
import org.nem.nac.models.api.transactions.MultisigTransactionApiDto;
import org.nem.nac.models.api.transactions.TransferTransactionApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.AccountTransaction;

public class TransactionsHelper {

	public static Predicate<AbstractTransactionApiDto> IS_TRANSFER =
			tran ->
					tran.type == TransactionType.TRANSFER_TRANSACTION
							|| (tran.type == TransactionType.MULTISIG_TRANSACTION
							&& ((MultisigTransactionApiDto)tran).otherTrans.type == TransactionType.TRANSFER_TRANSACTION);

	public static boolean needToSign(UnconfirmedTransactionMetaDataPairApiDto transaction, AddressValue account) {
		if (!isMultisig(transaction)) { return false; }
		final MultisigTransactionApiDto msigTransaction = (MultisigTransactionApiDto)transaction.transaction;
		return needToSign(account, msigTransaction);
	}

	public static boolean needToSign(AccountTransaction transaction, AddressValue account) {
		if (!isMultisig(transaction)) { return false; }
		final MultisigTransactionApiDto msigTransaction = (MultisigTransactionApiDto)transaction.transaction;
		return needToSign(account, msigTransaction);
	}

	private static boolean needToSign(final AddressValue account, final MultisigTransactionApiDto transaction) {
		if (transaction.otherTrans.type == TransactionType.MOSAIC_DEFINITION_CREATION_TRANSACTION
				|| transaction.otherTrans.type == TransactionType.MOSAIC_SUPPLY_CHANGE_TRANSACTION
				|| transaction.otherTrans.type == TransactionType.UNKNOWN) {
			return false;
		}

		if (transaction.isSigner(account)) {
			return false; // me is cosig that created transaction
		}
		if (CollectionUtils.any(transaction.signatures, s -> s.isSigner(account))) { return false; } // me already signed
		if (transaction.otherTrans.isSigner(account)) {
			return false; // me is multisig that transaction is from
		}
		if (transaction.otherTrans instanceof TransferTransactionApiDto) {
			final TransferTransactionApiDto transfer = (TransferTransactionApiDto)transaction.otherTrans;
			if (transfer.recipient.equals(account)) {
				return false; // me is recipient of this transaction
			}
		}
		else if (transaction.otherTrans instanceof MultisigAggregateModificationTransactionApiDto) {
			final MultisigAggregateModificationTransactionApiDto aggregateModificationTran =
					(MultisigAggregateModificationTransactionApiDto)transaction.otherTrans;
			if (CollectionUtils.any(aggregateModificationTran.modifications, m -> m.cosignatoryAccount.toAddress().equals(account))) {
				return false; // me was deleted/added
			}
		}
		return true;
	}

	public static boolean isMultisig(final AccountTransaction transaction) {
		return transaction.transaction instanceof MultisigTransactionApiDto;
	}

	public static boolean isMultisig(final UnconfirmedTransactionMetaDataPairApiDto transaction) {
		return transaction.meta.data != null && transaction.transaction instanceof MultisigTransactionApiDto;
	}
}
