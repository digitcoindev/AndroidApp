package org.nem.nac.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.nem.nac.common.enums.LastTransactionType;
import org.nem.nac.datamodel.repositories.LastTransactionRepository;
import org.nem.nac.models.transactions.LastTransaction;
import org.nem.nac.models.transactions.NotificationDismissMetadata;

import java.util.ArrayList;

import timber.log.Timber;

public final class NotificationDeleteReceiver extends BroadcastReceiver {

	public static final String EXTRA_PARC_ARR_LAST_NOTIFIED_TRANSACTIONS = NotificationDeleteReceiver.class.getCanonicalName() + ".last-notified-transactions";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Timber.d("Notification dismissed");
		final ArrayList<NotificationDismissMetadata> dismissedTransactions =
				intent.<NotificationDismissMetadata>getParcelableArrayListExtra(EXTRA_PARC_ARR_LAST_NOTIFIED_TRANSACTIONS);
		if (dismissedTransactions == null || dismissedTransactions.isEmpty()) {
			Timber.w("Notification dismissed without supplied transaction information");
			return;
		}

		final PendingResult pendingResult = goAsync();
		new Thread(() -> {
			try {
				final LastTransactionRepository lastSeenRepo = new LastTransactionRepository();
				for (NotificationDismissMetadata transaction : dismissedTransactions) {
					if (transaction.address == null) {
						Timber.e("Address was null!");
						continue;
					}
					final LastTransaction lastNotified = lastSeenRepo.find(transaction.address, LastTransactionType.NOTIFIED)
							.orElse(new LastTransaction(transaction.address, LastTransactionType.NOTIFIED));
					lastNotified.transactionHash = transaction.transactionHash;
					lastSeenRepo.save(lastNotified);
				}
			} catch (Throwable throwable) {
				Timber.e(throwable, "Error has occurred while deleting notification");
			}
			pendingResult.finish();
		}).start();
	}
}
