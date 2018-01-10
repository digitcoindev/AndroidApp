package org.nem.nac.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppHost;
import org.nem.nac.application.AppSettings;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.models.MessageDraft;
import org.nem.nac.common.models.TimeValue;
import org.nem.nac.common.utils.IOUtils;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.helpers.TransactionsHelper;
import org.nem.nac.models.AnnounceResult;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.account.AccountInfoApiDto;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.TransferTransactionApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.drafts.AbstractTransactionDraft;
import org.nem.nac.models.transactions.drafts.MultisigTransactionDraft;
import org.nem.nac.models.transactions.drafts.TransferTransactionDraft;
import org.nem.nac.servers.ServerManager;
import org.nem.nac.tasks.EncryptMessageAsyncTask;
import org.nem.nac.tasks.GetAccountInfoAsyncTask;
import org.nem.nac.tasks.GetAccountTransactionsAsyncTask;
import org.nem.nac.tasks.GetUnconfirmedTransactionsAsyncTask;
import org.nem.nac.tasks.SendTransactionAsyncTask;
import org.nem.nac.ui.IntervalCaller;
import org.nem.nac.ui.RequestCodes;
import org.nem.nac.ui.controls.AmountInput;
import org.nem.nac.ui.controls.TransfersList;
import org.nem.nac.ui.controls.UnconfirmedTransfersList;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.utils.FragmentUtils;
import org.nem.nac.ui.utils.InputErrorUtils;
import org.nem.nac.ui.utils.Toaster;
import org.nem.nac.ui.utils.ViewUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public final class MessagesActivity extends NacBaseActivity {

	public static final  String EXTRA_STR_COMPANION_ADDRESS_RAW =
			MessagesActivity.class.getCanonicalName() + "companion-address";
	private static final String FRAG_TAG_NO_NETWORK_DIALOG      = MessagesActivity.class.getCanonicalName() + ".wifi-settings";

	private static final int REFRESH_AFTER_SEND_DELAY_MS = 1500;

	private TextView                 _nameLabel;
	private TextView                 _balanceLabel;
	private TransfersList            _confirmedList;
	private UnconfirmedTransfersList _unconfirmedList;
	private TextView                 _activityTitleLabel;
	private View                     _unconfirmedSeparator;
	private ScrollView               _scrollView;
	// toolbar
	private View                     _toolbarRightPanel;
	private TextView                 _toolbarRightLabel;
	// Send message
	private LinearLayout             _sendMessagePanel;
	private AmountInput              _amountInput;
	private EditText                 _messageInput;
	private ImageView                _encryptBtn;
	private TextView                 _accountsBtn;
	private TextView                 _sendBtn;
	//
	private AddressValue             _address;
	private AddressValue             _companion;
	private Account                  _meAcc;
	private NacPublicKey             _sender;
	private boolean _encryptMsg = false;
	private List<AccountInfoApiDto>    _cosignatoryOf;
	private ConnectivityChangeReceiver _connectivityChangeReceiver;
	private       IntervalCaller _updateCaller            = new IntervalCaller(AppConstants.DATA_AUTOREFRESH_INTERVAL, this::updateData);
	private final AtomicBoolean  _firstUpdate             = new AtomicBoolean(true);
	private final AtomicBoolean  _updatingBalance         = new AtomicBoolean(false);
	private final AtomicBoolean  _updatingTransactions    = new AtomicBoolean(false);
	private final AtomicBoolean  _scrollToNewTransactions = new AtomicBoolean(false);
	private Integer _cosignatories;
	private Integer _minCosignatories;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_messages;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_messages;
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Me
		final Optional<AddressValue> address = AppSettings.instance().readLastUsedAccAddress();
		if (!address.isPresent()) {
			Timber.e("Local account was not selected");
			Toaster.instance().show(R.string.errormessage_account_not_selected);
			finish();
			AccountListActivity.start(this);
			return;
		}
		_address = address.get();
		// Init views
		initViews();
		// Check if existing account
		final Optional<Account> account = new AccountRepository().find(_address);
		if (!account.isPresent()) {
			Timber.w("Messages opened with bad address: %s", _address);
			_address = null;
			finish();
			AccountListActivity.start(this);
			return;
		}
		_meAcc = account.get();
		_confirmedList.setOwner(_meAcc.privateKey, _address);
		_sender = _meAcc.publicData.publicKey;
		_nameLabel.setText(account.get().name);
		// Companion
		final String rawCompanionAddr = getIntent().getStringExtra(EXTRA_STR_COMPANION_ADDRESS_RAW);
		if (AddressValue.isValid(rawCompanionAddr)) {
			_companion = AddressValue.fromValue(rawCompanionAddr);
			_toolbarRightPanel.setOnClickListener(this::onCopyAddressClick);
			_toolbarRightLabel.setVisibility(View.VISIBLE);
		}
		else {
			Timber.e("Companion was not selected");
			startActivity(new Intent(this, DashboardActivity.class));
			finish();
			return;
		}
		final String companionName = _companion.toNameOrDashed();
		_activityTitleLabel.setText(companionName);
		_activityTitleLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_small));
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshEncButtonState();
		_firstUpdate.set(true);

		if (_connectivityChangeReceiver == null) {
			_connectivityChangeReceiver = new ConnectivityChangeReceiver();
		}
		registerReceiver(_connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		//
		if (AppHost.Network.isAvailable()) {
			_updateCaller.start(true);
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
	protected void onStop() {
		super.onStop();
		_encryptMsg = false;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case RequestCodes.SELECT_SENDER_ACTIVITY: {
				if (resultCode == RESULT_OK) {
					final String selected = data.getStringExtra(SelectSenderActivity.EXTRA_STR_SELECTED_ACCOUNT);
					_sender = new NacPublicKey(selected);
					NacApplication.getMainHandler().postDelayed(() -> {
						new GetAccountInfoAsyncTask(this, _sender.toAddress())
								.withCompleteCallback((task, result) -> {
									if (!result.getResult().isPresent()) {
										showBalance(false);
										return;
									}
									final AccountMetaDataPairApiDto accountInfo = result.getResult().get();
									final AccountType accountType = AccountType.fromAccount(accountInfo.meta);
									enableEncryptButton(accountType != AccountType.MULTISIG);
									final String balanceStr = getString(R.string.label_balance_xems, NumberUtils
											.toAmountString(accountInfo.account.balance.getAsFractional()));
									_balanceLabel.setText(balanceStr);
									showBalance(true);
									_nameLabel.setText(_sender.toString());
								})
								.execute();
					}, 250); // hack to wait for resetting of currentActivity
				}
			}
		}
	}

	@Override
	protected void setToolbarItems(@NonNull final Toolbar toolbar) {
		_toolbarRightPanel = findViewById(R.id.toolbar_right_panel);
		_toolbarRightPanel.setClickable(true);
		_toolbarRightLabel = (TextView)findViewById(R.id.toolbar_right_text);
		_toolbarRightLabel.setText(R.string.toolbar_btn_copy);
	}

	private void updateData() {
		Timber.i("Updating transactions data...");
		_scrollToNewTransactions.set(false);

		if (!ServerManager.instance().hasServers()) {
			_updateCaller.stop();
			startActivity(new Intent(this, ServersListActivity.class));
			return;
		}
		//
		if (_address == null || !isNotDestroyed()) {
			_updateCaller.stop();
			onBackPressed();
			return;
		}
		//
		final boolean firstUpdate = _firstUpdate.compareAndSet(true, false);
		if (_updatingBalance.compareAndSet(false, true)) {
			Timber.d("Getting account info...");
			(firstUpdate ? new GetAccountInfoAsyncTask(this, _address) : new GetAccountInfoAsyncTask(_address))
					.withCompleteCallback(this::onAccountInfo)
					.execute();
		}
		//
		if (_updatingTransactions.compareAndSet(false, true)) {
			Timber.i("Getting transactions...");
			(firstUpdate ? new GetUnconfirmedTransactionsAsyncTask(this, _address) : new GetUnconfirmedTransactionsAsyncTask(_address))
					.withCompleteCallback(this::onUnconfirmedTransactions)
					.execute();
		}
	}

	private void onAccountInfo(final GetAccountInfoAsyncTask task, final AsyncResult<AccountMetaDataPairApiDto> result) {
		try {
			if (!result.getResult().isPresent()) {
				showBalance(false);
				return;
			}
			final AccountMetaDataPairApiDto accountInfo = result.getResult().get();
			_cosignatories = accountInfo.account.multisigInfo != null ? accountInfo.account.multisigInfo.cosignatoriesCount : null;
			_minCosignatories = accountInfo.account.multisigInfo != null ? accountInfo.account.multisigInfo.minCosignatories : null;

			final String balanceStr = getString(R.string.label_balance_xems, NumberUtils
					.toAmountString(accountInfo.account.balance.getAsFractional()));
			_balanceLabel.setText(balanceStr);
			showBalance(true);
			_sendMessagePanel.setVisibility(accountInfo.meta.getType() == AccountType.MULTISIG ? View.GONE : View.VISIBLE);
			if (accountInfo.meta.getType() == AccountType.COSIGNATORY) {
				_cosignatoryOf = accountInfo.meta.cosignatoryOf;
				Timber.d("I am cosignatory of %d multisigs, enabling accounts button", _cosignatoryOf.size());
				_accountsBtn.setVisibility(View.VISIBLE);
			}
		} finally {
			_updatingBalance.set(false);
		}
	}

	private void onAccountTransactions(final GetAccountTransactionsAsyncTask task,
			final AsyncResult<List<TransactionMetaDataPairApiDto>> result) {
		if (!result.getResult().isPresent()) {
			Toaster.instance().showGeneralError();
			_updatingTransactions.set(false);
			return;
		}
		try {
			final List<TransactionMetaDataPairApiDto> trans = result.getResult().get();
			final List<TransactionMetaDataPairApiDto> companionTransfers = new ArrayList<>();
			Stream.of(trans)
					.filter(t -> TransactionsHelper.IS_TRANSFER.test(t.transaction))
					.filter(t -> {
						final TransferTransactionApiDto unwrappedTransfer = (TransferTransactionApiDto)t.transaction.unwrapTransaction();
						final boolean companionIsMyself = _address.equals(_companion);
						if (companionIsMyself) {
							return unwrappedTransfer.isSigner(_companion) && unwrappedTransfer.recipient.equals(_companion);
						}
						return unwrappedTransfer.isSigner(_companion) || unwrappedTransfer.recipient
								.equals(_companion);
					})
					.sorted((lhs, rhs) -> lhs.transaction.timeStamp.compareTo(rhs.transaction.timeStamp))
					.forEach(companionTransfers::add);

			final TransactionMetaDataPairApiDto newest =
					_confirmedList.getItems().isEmpty() ? null : _confirmedList.getItem(_confirmedList.getItems().size() - 1);
			final TimeValue newestTimestamp = newest == null ? TimeValue.ZERO : newest.transaction.timeStamp;

			for (TransactionMetaDataPairApiDto tran : companionTransfers) {
				if (tran.transaction.timeStamp.compareTo(newestTimestamp) > 0) {
					_confirmedList.addItem(tran);
					_scrollToNewTransactions.set(true);
				}
			}

			if (_scrollToNewTransactions.compareAndSet(true, false)) {
				_confirmedList.post(() -> _scrollView.fullScroll(View.FOCUS_DOWN));
			}
		} finally {
			_updatingTransactions.set(false);
		}
	}

	private void onUnconfirmedTransactions(final GetUnconfirmedTransactionsAsyncTask task,
			final AsyncResult<List<UnconfirmedTransactionMetaDataPairApiDto>> result) {
		if (!result.getResult().isPresent()) {
			Toaster.instance().showGeneralError();
			_updatingTransactions.set(false);
			return;
		}
		try {
			final List<UnconfirmedTransactionMetaDataPairApiDto> unconfirmed = result.getResult().get();
			Collections.reverse(unconfirmed);
			final List<UnconfirmedTransactionMetaDataPairApiDto> companionUnconfirmed = new ArrayList<>();
			Stream.of(unconfirmed)
					.filter(u -> TransactionsHelper.IS_TRANSFER.test(u.transaction))
					.filter(u -> {
						final TransferTransactionApiDto unwrappedTransfer = (TransferTransactionApiDto)u.transaction.unwrapTransaction();
						final boolean companionIsMyself = _address.equals(_companion);
						if (companionIsMyself) {
							return unwrappedTransfer.isSigner(_companion) && unwrappedTransfer.recipient.equals(_companion);
						}
						return unwrappedTransfer.isSigner(_companion) || unwrappedTransfer.recipient.equals(_companion);
					})
					.forEach(companionUnconfirmed::add);
			//
			final UnconfirmedTransactionMetaDataPairApiDto newest =
					_unconfirmedList.getItems().isEmpty() ? null : _unconfirmedList.getItem(_unconfirmedList.getItems().size() - 1);
			final TimeValue newestTimestamp = newest == null ? TimeValue.ZERO : newest.transaction.timeStamp;

			_unconfirmedList.setVisibility(companionUnconfirmed.isEmpty() ? View.GONE : View.VISIBLE);
			_unconfirmedSeparator.setVisibility(companionUnconfirmed.isEmpty() ? View.GONE : View.VISIBLE);
			//
			_unconfirmedList.clearItems();
			_unconfirmedList.setOwner(_meAcc.privateKey, _address, _cosignatories, _minCosignatories);
			_unconfirmedList.addItems(companionUnconfirmed);
			//
			final UnconfirmedTransactionMetaDataPairApiDto updatedNewest =
					_unconfirmedList.getItems().isEmpty() ? null : _unconfirmedList.getItem(_unconfirmedList.getItems().size() - 1);
			if (updatedNewest != null && updatedNewest.transaction.timeStamp.compareTo(newestTimestamp) > 0) { // has new unconfirmed
				_scrollToNewTransactions.set(true);
			}
			//
			Timber.i("Getting transactions...");
			new GetAccountTransactionsAsyncTask(_address)
					.withCompleteCallback(this::onAccountTransactions)
					.execute();
		} catch (Throwable t) {
			_updatingTransactions.set(false);
			throw t;
		}
	}

	private void onAccountsClick(final View view) {
		Timber.d("Accounts button click");
		final Intent intent = new Intent(this, SelectSenderActivity.class);

		final List<String> msigList = Stream.of(_cosignatoryOf)
				.map(c -> c.publicKey.toHexStr())
				.collect(Collectors.toList());

		if (!msigList.isEmpty()) {
			String[] msigsArr = new String[msigList.size()];
			msigsArr = msigList.toArray(msigsArr);
			final String[] sendersHex = new String[msigsArr.length + 1];

			sendersHex[0] = _meAcc.publicData.publicKey.toHexStr();
			System.arraycopy(msigsArr, 0, sendersHex, 1, msigsArr.length);
			intent.putExtra(SelectSenderActivity.EXTRA_STR_ARR_ACCOUNTS_HEX, sendersHex);
		}

		startActivityForResult(intent, RequestCodes.SELECT_SENDER_ACTIVITY);
	}

	/**
	 * SEND CLICK
	 */
	private void onSendMessageClick_pre(final View clicked){

		final Optional<Xems> amount1 = _amountInput.getAmount();
		if (!amount1.isPresent()) {
			enableSendButton(true);
			return;
		}

		final String msgText = _messageInput.getText().toString();
		byte[] msgData = msgText.getBytes();

		final MessageDraft messageDraft = MessageDraft.create(msgData);
		if (messageDraft != null) {
			if (!MessageDraft.isLengthValid(msgText, _encryptMsg)) {
				InputErrorUtils.setErrorState(_messageInput, R.string.errormessage_message_too_long);
				_messageInput.requestFocus();
				enableSendButton(true);
				return;
			}
		}
		if (messageDraft != null && _encryptMsg) {
			new EncryptMessageAsyncTask(this, _meAcc.privateKey, _companion, messageDraft)
					.withCompleteCallback(this::onMessageEncrypted)
					.execute();
		}

		LayoutInflater inflater = getLayoutInflater();
		View views = inflater.inflate(R.layout.dialog_confirm_newtransaction, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		TextView title=(TextView) views.findViewById(R.id.confm_new_title);
		TextView amount=(TextView) views.findViewById(R.id.confm_new_amount);
		TextView msg=(TextView) views.findViewById(R.id.confm_new_msg);
		TextView address=(TextView) views.findViewById(R.id.confm_new_address);
		builder.setView(views);

		String msgStr = _messageInput.getText().toString();
		Optional<Xems> amountX = _amountInput.getAmount();
		String amountStr=amountX.get().toString();
		String AddressName = _companion.toNameOrDashed();
		String addressStr = _companion.toString();

		title.setText(AddressName);
		amount.setText(amountStr);
		msg.setText(msgStr);
		address.setText(addressStr);

		builder.setCancelable(true);

		builder.setPositiveButton(
				getString(R.string.btn_send),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onSendMessageClick();
						//Toast.makeText(MessagesActivity.this, "Send.......", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				});

		builder.setNegativeButton(
				getString(R.string.text_CANCEL),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//Toast.makeText(MessagesActivity.this, "Cancel.......", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				});

		AlertDialog alert11 = builder.create();
		alert11.show();
	}

	private void onSendMessageClick() {
		Timber.i("Send transaction click");
		enableSendButton(false);

		final Optional<Xems> amount = _amountInput.getAmount();
		if (!amount.isPresent()) {
			enableSendButton(true);
			return;
		}

		final String msgText = _messageInput.getText().toString();
		byte[] msgData = msgText.getBytes();

		final MessageDraft messageDraft = MessageDraft.create(msgData);
		if (messageDraft != null) {
			if (!MessageDraft.isLengthValid(msgText, _encryptMsg)) {
				InputErrorUtils.setErrorState(_messageInput, R.string.errormessage_message_too_long);
				_messageInput.requestFocus();
				enableSendButton(true);
				return;
			}
		}
		if (messageDraft != null && _encryptMsg) {
			new EncryptMessageAsyncTask(this, _meAcc.privateKey, _companion, messageDraft)
					.withCompleteCallback(this::onMessageEncrypted)
					.execute();
		}
		else {
			announceTransfer(amount.get(), messageDraft);
		}
	}

	private void onMessageEncrypted(final EncryptMessageAsyncTask task, final AsyncResult<MessageDraft> result) {
		if (!result.getResult().isPresent()) {
			Timber.e("Failed to encrypt message. Task returned no result!");
			Toast.makeText(this, R.string.errormessage_failed_to_create_transaction, Toast.LENGTH_SHORT).show();
			enableSendButton(true);
			return;
		}
		announceTransfer(_amountInput.getAmount().get(), result.getResult().get());
	}

	private void onCopyAddressClick(final View clicked) {
		clicked.setClickable(false);
		try {
			AppHost.Clipboard.copyText(null, _companion.toString(true));
			Toaster.instance().show(R.string.message_copy_complete);
		} finally {
			clicked.setClickable(true);
		}
	}

	/**
	 * Prepared transaction object
	 */
	private void announceTransfer(Xems amount, MessageDraft message) {

		final NacPublicKey myPublicKey = _meAcc.publicData.publicKey;
		final boolean isMultisig = !myPublicKey.equals(_sender);
		Timber.d("Announcing %s transfer from %s to %s", isMultisig ? "multisig" : "", isMultisig ? _sender : myPublicKey, _companion);

		AbstractTransactionDraft transfer =
				new TransferTransactionDraft(isMultisig ? _sender : myPublicKey, _companion, amount, message);
		if (isMultisig) {
			transfer = new MultisigTransactionDraft(myPublicKey, transfer);
		}
		final ByteArrayOutputStream tranStream = new ByteArrayOutputStream();
		try {
			transfer.serialize(tranStream);
		} catch (NacException e) {
			Timber.e("Failed to serialize transaction!");
			Toast.makeText(this, R.string.errormessage_failed_to_create_transaction, Toast.LENGTH_SHORT).show();
			enableSendButton(true);
			return;
		} finally {
			IOUtils.closeSilently(tranStream);
		}

		new SendTransactionAsyncTask(this, tranStream.toByteArray(), _meAcc.privateKey)
				.withCompleteCallback((sendTask, sendResult) -> {
					if (!sendResult.getResult().isPresent()) {
						Timber.d("Bad result");
						enableSendButton(true);
						return;
					}
					final AnnounceResult announceResult = sendResult.getResult().get();
					if (announceResult.successful) {
						//
						if (isMeResumed()) {
							_amountInput.setText("");
							_messageInput.setText("");
							//
							_updateCaller.stop();
							ConfirmDialogFragment.create(true, null, R.string.dialog_message_transaction_announced, null)
									.setOnDismissListener(d -> {
										_updateCaller =
												new IntervalCaller(TimeSpan.fromMilliSeconds(REFRESH_AFTER_SEND_DELAY_MS), this::updateData, 1, caller -> {
													_updateCaller = new IntervalCaller(AppConstants.DATA_AUTOREFRESH_INTERVAL, this::updateData);
													_updateCaller.start(false);
												});
										_updateCaller.start(true);
									})
//											}
//											getHandler().postDelayed(() ->
//													new GetAccountTransactionsAsyncTask(this, _address)
//															.withCompleteCallback(this::onAccountTransactions)
//															.execute(), 500))
									.show(getFragmentManager(), null);
						}
					}
					else {
						ConfirmDialogFragment.create(true, null, getString(R.string.dialog_transaction_announce_failed, announceResult.message), null)
								.show(getFragmentManager(), null);
					}
					enableSendButton(true);
				})
				.execute();
	}

	private void initViews() {
		_activityTitleLabel = (TextView)findViewById(R.id.toolbar_title);
		_nameLabel = (TextView)findViewById(R.id.label_account_name);
		_balanceLabel = (TextView)findViewById(R.id.field_balance);
		_scrollView = (ScrollView)findViewById(R.id.scroll_view);
		_confirmedList = (TransfersList)findViewById(R.id.list_confirmed);
		_unconfirmedList = (UnconfirmedTransfersList)findViewById(R.id.list_unconfirmed);
		_unconfirmedSeparator = findViewById(R.id.separator);
		_sendMessagePanel = (LinearLayout)findViewById(R.id.panel_send_message);
		_messageInput = (EditText)findViewById(R.id.input_message);
		_messageInput.addTextChangedListener(_lengthValidator);
		_accountsBtn = (TextView)findViewById(R.id.btn_accounts);
		_accountsBtn.setOnClickListener(this::onAccountsClick);

		_encryptBtn = (ImageView)findViewById(R.id.btn_encrypt);
		_encryptBtn.setOnClickListener(v -> {
			_encryptMsg = !_encryptMsg;
			refreshEncButtonState();
			updateMessageErrorState(_messageInput.getText().toString());
		});
		_amountInput = (AmountInput)findViewById(R.id.input_amount);
		_amountInput.setTreatEmptyAsZero(true);
		_amountInput.setAllowZero(true);
		_sendBtn = (TextView)findViewById(R.id.btn_send);
		_sendBtn.setOnClickListener(this::onSendMessageClick_pre);
	}

	private void enableEncryptButton(final boolean enable) {
		if (_encryptBtn.isEnabled() ^ enable) {
			if (!enable && _encryptMsg) {
				_encryptMsg = false;
			}
			_encryptBtn.setEnabled(enable);
			refreshEncButtonState();
		}
	}

	private void refreshEncButtonState() {
		ViewUtils.setBgColor(_encryptBtn, this, _encryptMsg ? R.color.official_green : R.color.light_gray);
	}

	private void enableSendButton(final boolean enable) {
		_sendBtn.setClickable(enable);
		ViewUtils.setBgColor(_sendBtn, this, enable ? R.color.official_green : R.color.light_gray);
	}

	private void showBalance(final boolean show) {
		_balanceLabel.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void updateMessageErrorState(String msg) {
		if (MessageDraft.isLengthValid(msg, _encryptMsg)) {
			InputErrorUtils.clearErrorState(_messageInput);
		}
		else {
			InputErrorUtils.setErrorState(_messageInput, null);
		}
	}

	private final TextWatcher _lengthValidator = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			updateMessageErrorState(s.toString());
		}
	};

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
}
