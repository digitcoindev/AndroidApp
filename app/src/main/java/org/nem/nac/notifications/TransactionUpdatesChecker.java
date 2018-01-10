package org.nem.nac.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.application.AppSettings;
import org.nem.nac.broadcastreceivers.NotificationDeleteReceiver;
import org.nem.nac.common.ThreadPoolExecutorFactory;
import org.nem.nac.common.enums.LastTransactionType;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.utils.CollectionUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.datamodel.repositories.LastTransactionRepository;
import org.nem.nac.helpers.TransactionsHelper;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.transactions.AbstractTransactionApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.AccountTransaction;
import org.nem.nac.models.transactions.LastTransaction;
import org.nem.nac.models.transactions.NotificationDismissMetadata;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.tasks.GetAllTransactionsAsyncTask;
import org.nem.nac.ui.activities.AccountListActivity;
import org.nem.nac.ui.activities.DashboardActivity;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public final class TransactionUpdatesChecker {

	private static final int CHECK_TIMEOUT_MS = 30000;
	private static final int NOTIFICATION_ID  = 0xFC3A0001;

	private final ThreadPoolExecutor _executor = ThreadPoolExecutorFactory.createDefaultExecutor();
	private final Context _context;

	public TransactionUpdatesChecker(final Context context) {
		_context = context;
	}

	public void check()
			throws InterruptedException {
		Timber.d("Checking for transactions updates");

		final Map<NacPublicKey, Account> accountsByPubKey = new HashMap<>();
		final Map<AddressValue, Account> accountsByAddress = new HashMap<>();
		final List<Account> accounts = new AccountRepository().getAll();
		if (accounts.isEmpty()) {
			Timber.d("No accounts, skipping.");
			return;
		}
		for (Account acc : accounts) {
			accountsByPubKey.put(acc.publicData.publicKey, acc);
			accountsByAddress.put(acc.publicData.address, acc);
		}

		if (!ServerFinder.instance().getBest().isPresent()) {
			Timber.w("No server no update check");
			return;
		}

		final NotificationDataSummary notificationSummary = new NotificationDataSummary();
		final Map<NacPublicKey, AccountNotificationData> notificationDatas = new HashMap<>();

		final List<Callable<List<AccountTransaction>>> tasks = retrieveUpdateTasks(accountsByPubKey.keySet());
		Timber.d("Starting %d tasks", tasks.size());
		final List<Future<List<AccountTransaction>>> futures =
				_executor.invokeAll(tasks, CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		Timber.d("Finished tasks");

		for (Future<List<AccountTransaction>> future : futures) {
			final AccountNotificationData accountNotificationData;
			try {
				accountNotificationData = retrieveNotificationData(future);
				if (accountNotificationData.account != null) {
					notificationDatas.put(accountNotificationData.account, accountNotificationData);
					if (accountNotificationData.showTransfers && accountNotificationData.newIncomingTransfersCount > 0) {
						final NotificationDismissMetadata dismissMetadata =
								new NotificationDismissMetadata(accountNotificationData.account.toAddress(), accountNotificationData.newestTransferHash);
						notificationSummary.newTransfersPerAddress.put(accountNotificationData.account, accountNotificationData.newIncomingTransfersCount);
						notificationSummary.newTransfersPerName
								.put(accountsByPubKey.get(accountNotificationData.account).name, accountNotificationData.newIncomingTransfersCount);
						notificationSummary.dismissMetadatas.add(dismissMetadata);
					}
					if (accountNotificationData.newUnsignedTransactions > 0) {
						notificationSummary.unconfirmedPerAddress.put(accountNotificationData.account, accountNotificationData.newUnsignedTransactions);
						notificationSummary.unconfirmedPerName
								.put(accountsByPubKey.get(accountNotificationData.account).name, accountNotificationData.newUnsignedTransactions);
					}
				}
			} catch (ExecutionException | InterruptedException | CancellationException e) {
				Timber.w(e, "Future execution failed");
				return;
			}
		}
		//
		notificationSummary.newTransfersPerName.putAll(Stream.of(notificationSummary.newTransfersPerAddress)
				.collect(Collectors.toMap(u -> accountsByPubKey.get(u.getKey()).name, Map.Entry::getValue)));
		//
		final Intent notificationClickIntent;
		if (notificationSummary.newTransfersPerAddress.size() + notificationSummary.unconfirmedPerAddress.size() == 1) {
			if (!notificationSummary.newTransfersPerAddress.isEmpty()) {
				final AddressValue address = notificationSummary.newTransfersPerAddress.keySet().iterator().next().toAddress();
				notificationClickIntent = new Intent(_context.getApplicationContext(), DashboardActivity.class)
						.putExtra(DashboardActivity.EXTRA_PARC_ACCOUNT_ADDRESS, address);
			}
			else {
				final AddressValue address = notificationSummary.unconfirmedPerAddress.keySet().iterator().next().toAddress();
				notificationClickIntent = new Intent(_context.getApplicationContext(), DashboardActivity.class)
						.putExtra(DashboardActivity.EXTRA_PARC_ACCOUNT_ADDRESS, address);
			}
		}
		else {
			notificationClickIntent = new Intent(_context.getApplicationContext(), AccountListActivity.class);
		}
		notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//
		final Intent notificationDeleteIntent = new Intent(_context.getApplicationContext(), NotificationDeleteReceiver.class);
		Bundle extras = new Bundle();
		extras.putParcelableArrayList(NotificationDeleteReceiver.EXTRA_PARC_ARR_LAST_NOTIFIED_TRANSACTIONS, notificationSummary.dismissMetadatas);
		notificationDeleteIntent.putExtras(extras);
		showNotification(notificationSummary, notificationClickIntent, notificationDeleteIntent);
	}

	private AccountNotificationData retrieveNotificationData(final Future<List<AccountTransaction>> future)
			throws ExecutionException, InterruptedException, CancellationException {
		List<AccountTransaction> transactions = future.get();
		final TransactionLists transactionLists = new TransactionLists(transactions);
		final NacPublicKey account = transactionLists.account;
		final AccountNotificationData accountNotificationData = new AccountNotificationData(account);
		if (account == null) {
			return accountNotificationData;
		}
		//
		final AddressValue accountAddress = account.toAddress();
		accountNotificationData.newIncomingTransfersCount = getUnseenCount(accountAddress, transactionLists.incomingTransfers);
		accountNotificationData.showTransfers = haveUnnotifiedTransferUpdates(accountAddress, transactionLists.incomingTransfers);
		if (accountNotificationData.showTransfers && accountNotificationData.newIncomingTransfersCount > 0) {
			try {
				accountNotificationData.newestTransferHash = transactionLists.incomingTransfers.get(0).metadata.hash.data;
			} catch (NullPointerException npe) {
				Timber.e(npe, "retrieveNotificationData() metadata was null!");
			}
		}
		accountNotificationData.newUnsignedTransactions = transactionLists.unconfirmedToSign.size();
		return accountNotificationData;
	}

	private int getUnseenCount(final AddressValue account, final List<AccountTransaction> incomingTransfers) {
		final Optional<LastTransaction> lastSeen = getLastTransactionsRepository().find(account, LastTransactionType.SEEN);
		if (lastSeen.isPresent()) {
			try {
				final List<AccountTransaction> newIncoming =
						CollectionUtils.getWhileNotMatch(incomingTransfers, t -> lastSeen.get().transactionHash.equals(t.metadata.hash.data));
				return newIncoming.size();
			} catch (NullPointerException npe) {
				Timber.e(npe, "getUnseenCount");
				return 0;
			}
		}
		else {
			return incomingTransfers.size();
		}
	}

	private boolean haveUnnotifiedTransferUpdates(final AddressValue account, final List<AccountTransaction> incoming) {
		final Optional<LastTransaction> lastNotified = getLastTransactionsRepository().find(account, LastTransactionType.NOTIFIED);
		if (lastNotified.isPresent()) {
			try {
				final List<AccountTransaction> newIncoming =
						CollectionUtils.getWhileNotMatch(incoming, t -> lastNotified.get().transactionHash.equals(t.metadata.hash.data));
				return !newIncoming.isEmpty();
			} catch (NullPointerException npe) {
				Timber.e(npe, "haveUnnotifiedTransferUpdates");
				return false;
			}
		}
		else {
			return !incoming.isEmpty();
		}
	}

	private List<Callable<List<AccountTransaction>>> retrieveUpdateTasks(final Set<NacPublicKey> publicKeys) {
		return Stream.of(publicKeys)
				.map((publicKey) -> (Callable<List<AccountTransaction>>)new TransactionsGetter(publicKey)::get)
				.collect(Collectors.toList());
	}

	private SoftReference<LastTransactionRepository> _lastTransactionsRepository = new SoftReference<>(null);

	private synchronized LastTransactionRepository getLastTransactionsRepository() {
		LastTransactionRepository repository = _lastTransactionsRepository.get();
		if (repository == null) {
			repository = new LastTransactionRepository();
			_lastTransactionsRepository = new SoftReference<>(repository);
		}
		return repository;
	}

	private void showNotification(final NotificationDataSummary summary, final Intent clickIntent, final Intent deleteIntent) {
		Timber.d("showNotification(), updates: %d", summary.newTransfersPerName.size());
		final NotificationManager notificationManager = (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);

		final int updatesCount = Stream.of(summary.newTransfersPerName.values())
				.reduce(0, (x, y) -> x + y)
				+ Stream.of(summary.unconfirmedPerName.values())
				.reduce(0, (x, y) -> x + y);
		if (updatesCount == 0) {
			notificationManager.cancel(NOTIFICATION_ID);
			return;
		}

		final String lineSeparator = System.getProperty("line.separator", "\n");
		final String title = _context.getString(R.string.notification_updates_title);
		final StringBuilder notificationSb = new StringBuilder();
		// Unconfirmed
		if (!summary.unconfirmedPerName.isEmpty()) {
			for (String name : summary.unconfirmedPerName.keySet()) {
				final String text = _context.getString(R.string.notification_text_unsigned_transactions, name, summary.unconfirmedPerName.get(name));
				notificationSb.append(text).append(lineSeparator);
			}
		}
		// new transfers
		if (!summary.newTransfersPerName.isEmpty()) {
			for (String name : summary.newTransfersPerName.keySet()) {
				final String text = _context.getString(R.string.notification_updates_text, name, summary.newTransfersPerName.get(name));
				notificationSb.append(text).append(lineSeparator);
			}
		}
		final String notificationText = notificationSb.toString();
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(_context.getApplicationContext())
				.setAutoCancel(true)
				.setOnlyAlertOnce(true)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
				.setLargeIcon(BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.ic_notification_bar)
				.setContentTitle(title)
				.setContentText(notificationText);
		if (AppSettings.instance().getNotificationSoundEnabled()) {
			final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			builder.setSound(sound);
		}
		if (AppSettings.instance().getNotificationVibeEnabled()) {
			builder.setVibrate(new long[] { 0, AppHost.Vibro.MEDIUM_VIBE });
		}
		if (Build.VERSION.SDK_INT >= 21) {
			setLockScreenVisibility(builder);
		}

		final PendingIntent pendingIntent = PendingIntent.getActivity(_context, new Random().nextInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		final PendingIntent deletePendingIntent = PendingIntent.getBroadcast(_context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setDeleteIntent(deletePendingIntent);
		//
		final Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	@TargetApi(21)
	private void setLockScreenVisibility(final NotificationCompat.Builder builder) {
		builder.setVisibility(AppSettings.instance().getNotificationLockScreenEnabled() ? Notification.VISIBILITY_PUBLIC : Notification.VISIBILITY_SECRET);
	}

	private static class TransactionsGetter {

		private final NacPublicKey _publicKey;

		public TransactionsGetter(final NacPublicKey publicKey) {
			_publicKey = publicKey;
		}

		private List<AccountTransaction> get() {
			try {
				return new GetAllTransactionsAsyncTask(_publicKey).getSynchronous();
//				final List<TransactionMetaDataPairApiDto> transfersToMe = Stream.of(transactions)
//						.filter(t -> t.transaction.unwrapTransaction().type == TransactionType.TRANSFER_TRANSACTION
//								&& !t.transaction.unwrapTransaction().isSigner(_publicKey))
//						.collect(Collectors.toList());
				//return new KeyValuePair<>(_publicKey, Optional.of(transactions));
			} catch (IOException | NoNetworkException | ServerErrorException e) {
				Timber.w("Failed to check for updates %s", _publicKey);
				if (e instanceof IOException) {
					ServerFinder.instance().clearBest();
				}
				return null;
			}
		}
	}

	private static class TransactionLists {

		public final NacPublicKey account;
		public final List<AccountTransaction> incomingTransfers = new ArrayList<>();
		public final List<AccountTransaction> unconfirmedToSign = new ArrayList<>();

		public TransactionLists(final List<AccountTransaction> transactions) {
			NacPublicKey account = null;
			if (transactions == null) {
				this.account = null;
				return;
			}
			for (AccountTransaction transaction : transactions) {
				if (account == null) {
					account = transaction.account;
				}
				switch (transaction.getConfirmationStatus()) {
					case CONFIRMED: {
						final AbstractTransactionApiDto unwrapped = transaction.transaction.unwrapTransaction();
						if (unwrapped.type == TransactionType.TRANSFER_TRANSACTION
								&& !unwrapped.isSigner(transaction.account)) {
							incomingTransfers.add(transaction);
						}
						break;
					}
					case UNCONFIRMED: {
						if (transaction.unconfirmedMetadata == null) {
							throw new IllegalStateException("Unconfirmed transaction but empty metadata");
						}
						if (TransactionsHelper.needToSign(transaction, account.toAddress())) {
							unconfirmedToSign.add(transaction);
						}
						break;
					}
				}
			}
			this.account = account;
		}
	}

	private static class AccountNotificationData {

		public final NacPublicKey account;
		public       int          newIncomingTransfersCount;
		public       BinaryData   newestTransferHash;
		public       boolean      showTransfers;
		public       int          newUnsignedTransactions;

		private AccountNotificationData(final NacPublicKey account) {this.account = account;}
	}

	private static class NotificationDataSummary {

		public final Map<NacPublicKey, Integer>             newTransfersPerAddress = new HashMap<>();
		public final Map<String, Integer>                   newTransfersPerName    = new HashMap<>();
		public final ArrayList<NotificationDismissMetadata> dismissMetadatas       = new ArrayList<>();
		public final Map<NacPublicKey, Integer>             unconfirmedPerAddress  = new HashMap<>();
		public final Map<String, Integer>                   unconfirmedPerName     = new HashMap<>();
	}
}
