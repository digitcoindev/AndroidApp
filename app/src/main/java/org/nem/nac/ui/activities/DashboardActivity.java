package org.nem.nac.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppHost;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.enums.LastTransactionType;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.datamodel.repositories.LastTransactionRepository;
import org.nem.nac.helpers.TransactionsHelper;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.TransferTransactionApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.AccountTransaction;
import org.nem.nac.models.transactions.LastTransaction;
import org.nem.nac.servers.ServerManager;
import org.nem.nac.tasks.GetAccountInfoAsyncTask;
import org.nem.nac.tasks.GetAllTransactionsAsyncTask;
import org.nem.nac.ui.IntervalCaller;
import org.nem.nac.ui.RequestCodes;
import org.nem.nac.ui.adapters.DashboardAdapter;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.utils.FragmentUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public final class DashboardActivity extends NacBaseActivity {

	public static final  String EXTRA_PARC_ACCOUNT_ADDRESS           = DashboardActivity.class.getCanonicalName() + "account-address";
	public static final  String EXTRA_BOOL_DONT_SHOW_UNSIGNED_PROMPT = DashboardActivity.class.getCanonicalName() + "dont-check-unconfirmed";
	private static final String FRAG_TAG_NO_NETWORK_DIALOG           = DashboardActivity.class.getCanonicalName() + ".wifi-settings";

	private View                       _toolbarRightPanel;
	private TextView                   _toolbarRightLabel;
	private TextView                   _nameLabel;
	private TextView                   _balanceLabel;
	private ListView                   _dashboardList;
	private DashboardAdapter           _adapter;
	private Account                    _account;
	private ConnectivityChangeReceiver _connectivityChangeReceiver;
	private final Map<AddressValue, MaybeConfirmedtransfer> _dataToDisplay                  = new HashMap<>();
	private final IntervalCaller                            _updateCaller                   =
			new IntervalCaller(AppConstants.DATA_AUTOREFRESH_INTERVAL, this::updateData);
	private final AtomicBoolean                             _updatingBalance                = new AtomicBoolean(false);
	private final AtomicBoolean                             _updatingTransactions           = new AtomicBoolean(false);
	private final AtomicBoolean                             _firstUpdate                    = new AtomicBoolean(true);
	private final AtomicBoolean                             _showUnsignedTransactionsDialog = new AtomicBoolean(true);

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_dashboard;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_dashboard;
	}

	@Override
	public void onBackPressed() {
		AccountListActivity.start(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Validate supplied address
		AddressValue address = getIntent().<AddressValue>getParcelableExtra(EXTRA_PARC_ACCOUNT_ADDRESS);
		if (address == null || !AddressValue.isValid(address)) {
			Timber.w("Address extra is invalid. %s. Trying get last used.", address);
			final Optional<AddressValue> lastAddress = AppSettings.instance().readLastUsedAccAddress();
			if (lastAddress.isPresent()) {
				address = lastAddress.get();
			}
			else {
				Timber.w("Last used address not present.");
				finish();
				AccountListActivity.start(this);
				return;
			}
		}
		AppSettings.instance().saveLastUsedAccAddress(address);
		// Init views
		_nameLabel = (TextView)findViewById(R.id.label_account_name);
		_balanceLabel = (TextView)findViewById(R.id.field_balance);
		_dashboardList = (ListView)findViewById(R.id.listview_dashboard);
		// Check if existing account
		final Optional<Account> account = new AccountRepository().find(address);
		if (account.isPresent()) {
			_account = account.get();
		}
		else {
			Timber.w("Local account not found: %s", address);
			_account = null;
			finish();
			AccountListActivity.start(this);
			return;
		}
		//
		_adapter = new DashboardAdapter(this);
		_dashboardList.setAdapter(_adapter);
		if (account.get().type == AccountType.MULTISIG) {
			_toolbarRightPanel.setVisibility(View.GONE);
			_toolbarRightPanel.setClickable(false);
		}
		_nameLabel.setText(account.get().name);

		_dashboardList.setOnItemClickListener(this::onMessageClick);
	}

	@Override
	protected void onResume() {
		super.onResume();
		_firstUpdate.set(true);
		//
		FragmentUtils.removeByTag(getFragmentManager(), FRAG_TAG_NO_NETWORK_DIALOG);
		//
		if (_connectivityChangeReceiver == null) {
			_connectivityChangeReceiver = new ConnectivityChangeReceiver();
		}
		registerReceiver(_connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		//
		if (AppHost.Network.isAvailable()) {
			_updateCaller.start(true);
			if (!getIntent().hasExtra(EXTRA_BOOL_DONT_SHOW_UNSIGNED_PROMPT)) {
				_showUnsignedTransactionsDialog.set(true);
			}
			else {
				_showUnsignedTransactionsDialog.set(false);
			}
		}
		else {
			ConfirmDialogFragment.create(true, null, R.string.dialog_message_no_network, R.string.btn_wifi_settings)
					.setOnConfirmListener(d -> {
						startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), RequestCodes.ACTION_WIFI_SETTINGS);
						return true;
					})
					.show(getFragmentManager(), FRAG_TAG_NO_NETWORK_DIALOG);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (_connectivityChangeReceiver != null) {
			unregisterReceiver(_connectivityChangeReceiver);
		}
		_updateCaller.stop();
	}

	@Override
	protected void setToolbarItems(@NonNull final Toolbar toolbar) {
		_toolbarRightPanel = findViewById(R.id.toolbar_right_panel);
		_toolbarRightPanel.setClickable(true);
		_toolbarRightPanel.setOnClickListener(v -> startActivity(new Intent(this, NewTransactionActivity.class)));
		_toolbarRightLabel = (TextView)findViewById(R.id.toolbar_right_text);
		_toolbarRightLabel.setText(R.string.toolbar_btn_new);
		_toolbarRightLabel.setVisibility(View.VISIBLE);
	}

	private void updateData() {
		if (!ServerManager.instance().hasServers()) {
			_updateCaller.stop();
			startActivity(new Intent(this, ServersListActivity.class));
			return;
		}
		//
		if (_account == null || !isNotDestroyed()) {
			_updateCaller.stop();
			onBackPressed();
			return;
		}
		//
		final boolean firstUpdate = _firstUpdate.compareAndSet(true, false);
		if (_updatingBalance.compareAndSet(false, true)) {
			Timber.d("Getting account info...");
			(firstUpdate ? new GetAccountInfoAsyncTask(this, _account.publicData.address) : new GetAccountInfoAsyncTask(_account.publicData.address))
					.withCompleteCallback(this::onAccountInfo)
					.execute();
		}
		if (_updatingTransactions.compareAndSet(false, true)) {
			Timber.i("Getting transactions...");
			(firstUpdate
					? new GetAllTransactionsAsyncTask(this, _account.publicData.publicKey)
					: new GetAllTransactionsAsyncTask(_account.publicData.publicKey))
					.withCompleteCallback(this::onAccountTransactions)
					.execute();
		}
	}

	private void onMessageClick(final AdapterView<?> adapterView, final View view, final int pos, final long id) {
		final DashboardAdapter.Item tran = (DashboardAdapter.Item)adapterView.getItemAtPosition(pos);
		final Intent intent = new Intent(DashboardActivity.this, MessagesActivity.class);
		intent.putExtra(MessagesActivity.EXTRA_STR_COMPANION_ADDRESS_RAW, tran.companion.getRaw());
		startActivity(intent);
	}

	private void onAccountInfo(final GetAccountInfoAsyncTask task, final AsyncResult<AccountMetaDataPairApiDto> result) {
		try {
			Timber.i("Got account info");
			if (!result.getResult().isPresent()) {
				Timber.d("Bad result");
				_balanceLabel.setText("");
				showBalance(false);
				return;
			}

			final String balanceStr =
					getString(R.string.label_balance_xems, NumberUtils.toAmountString(result.getResult().get().account.balance.getAsFractional()));
			_balanceLabel.setText(balanceStr);
			showBalance(true);
		} finally {
			_updatingBalance.set(false);
		}
	}

	private void onAccountTransactions(final GetAllTransactionsAsyncTask task, final AsyncResult<List<AccountTransaction>> result) {
		try {
			Timber.i("Got transactions");
			if (!result.getResult().isPresent()) {
				Timber.d("Bad result");
				Toast.makeText(this, R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
				return;
			}

			final List<AccountTransaction> transactions = result.getResult().get();
			saveLastIncomingConfirmedTransfer(_account.publicData.address, transactions);
			//
			final List<MaybeConfirmedtransfer> transfers = Stream.of(transactions)
					.filter(x -> x.transaction.unwrapTransaction().type == TransactionType.TRANSFER_TRANSACTION)
					.map(x -> new MaybeConfirmedtransfer(x.isConfirmed, (TransferTransactionApiDto)x.transaction.unwrapTransaction()))
					.collect(Collectors.toList());
			_dataToDisplay.clear();  //kwl  must clear, else not list update
			for (MaybeConfirmedtransfer tran : transfers) {
				final AddressValue companion = tran.transfer.getCompanion(_account.publicData.address);
				if (!_dataToDisplay.containsKey(companion)) {
					_dataToDisplay.put(companion, tran);
				}
			}
			final List<DashboardAdapter.Item> items = Stream.of(_dataToDisplay.values())
					.sorted((lhs, rhs) -> -lhs.transfer.timeStamp.compareTo(rhs.transfer.timeStamp))
					.map(x -> {
						final boolean isSigner = x.transfer.isSigner(_account.publicData.publicKey);
						return new DashboardAdapter.Item(isSigner, x.transfer.signer.toAddress().equals(x.transfer.recipient), x.transfer.hasMessage(),
								x.transfer.getCompanion(_account.publicData.address), x.transfer.message, x.transfer.amount, x.transfer
								.getDate(), x.isConfirmed);
					})
					.collect(Collectors.toList());
			//
			_adapter.setItems(items);
			_adapter.notifyDataSetChanged();
			//
			checkShowUnsignedDialog(transactions);
			//
		} finally {
			_updatingTransactions.set(false);
		}
	}

	private void checkShowUnsignedDialog(final List<AccountTransaction> transactions) {
		try {
			final List<AccountTransaction> unconfirmed = Stream.of(transactions)
					.filter(x -> !x.isConfirmed)
					.collect(Collectors.toList());
			if (unconfirmed == null || unconfirmed.isEmpty()) {
				_showUnsignedTransactionsDialog.set(false);
				return;
			}
			if (Stream.of(unconfirmed)
					.filter(x -> TransactionsHelper.needToSign(x, _account.publicData.address))
					.findFirst().isPresent()) {
				if (isMeResumed() && _showUnsignedTransactionsDialog.compareAndSet(true, false)) {
					ConfirmDialogFragment.create(true, null, R.string.dialog_message_have_unconfirmed, R.string.btn_show_unconfirmed)
							.setOnConfirmListener(d -> {
								startActivity(new Intent(this, UnconfirmedListActivity.class));
								return true;
							})
							.show(getFragmentManager(), null);
				}
			}
		} catch (Throwable t) {
			throw t;
		}
	}

	private void showBalance(final boolean show) {
		_balanceLabel.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void saveLastIncomingConfirmedTransfer(final AddressValue recipient, final List<AccountTransaction> transactions) {
		final Optional<AccountTransaction> lastIncomingTransfer = Stream.of(transactions)
				.filter(t -> t.isConfirmed
						&& t.transaction.unwrapTransaction().type == TransactionType.TRANSFER_TRANSACTION
						&& !t.transaction.unwrapTransaction().isSigner(recipient))
				.findFirst();
		if (lastIncomingTransfer.isPresent()) {
			final LastTransactionRepository lastSeenRepo = new LastTransactionRepository();
			final LastTransaction lastSeen =
					lastSeenRepo.find(_account.publicData.address, LastTransactionType.SEEN)
							.orElse(new LastTransaction(_account.publicData.address, LastTransactionType.SEEN));
			lastSeen.transactionHash = lastIncomingTransfer.get().metadata.hash.data;
			lastSeenRepo.save(lastSeen);
		}
	}

	private final class ConnectivityChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (isNotDestroyed()) {
				if (!AppHost.Network.isAvailable()) {
					return;
				}
				//
				FragmentUtils.removeByTag(getFragmentManager(), FRAG_TAG_NO_NETWORK_DIALOG);
				_updateCaller.start(true);
			}
		}
	}

	private static final class MaybeConfirmedtransfer {

		public final boolean                   isConfirmed;
		public final TransferTransactionApiDto transfer;

		public MaybeConfirmedtransfer(final boolean isConfirmed, final TransferTransactionApiDto transfer) {
			this.isConfirmed = isConfirmed;
			this.transfer = transfer;
		}
	}
}
