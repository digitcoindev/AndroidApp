package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.models.MessageDraft;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.helpers.Ed25519Helper;
import org.nem.nac.models.AnnounceResult;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.drafts.AbstractTransactionDraft;
import org.nem.nac.models.transactions.drafts.MultisigTransactionDraft;
import org.nem.nac.models.transactions.drafts.TransferTransactionDraft;
import org.nem.nac.tasks.EncryptMessageAsyncTask;
import org.nem.nac.tasks.GetAccountInfoAsyncTask;
import org.nem.nac.tasks.SendTransactionAsyncTask;
import org.nem.nac.ui.controls.AmountInput;
import org.nem.nac.ui.controls.AutocompleteAddressInput;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.utils.InputErrorUtils;
import org.nem.nac.ui.utils.Toaster;
import org.nem.nac.ui.utils.ViewUtils;

import java.util.List;

import timber.log.Timber;

public final class NewTransactionActivity extends NacBaseActivity {

	public static final String EXTRA_STR_ADDRESS   = NewTransactionActivity.class.getName() + ".e-address";
	public static final String EXTRA_DOUBLE_AMOUNT = NewTransactionActivity.class.getName() + ".e-amount";
	public static final String EXTRA_STR_MESSAGE   = NewTransactionActivity.class.getName() + ".e-message";
	public static final String EXTRA_BOOL_ENCRYPTED = NewTransactionActivity.class.getName() + ".e-encrypted";

	private Spinner                  _accountsSpinner;
	private TextView                 _balanceLabel;
	private ViewGroup                _fromPanel;
	private AutocompleteAddressInput _recipientInput;
	private AmountInput              _amountInput;
	private EditText                 _messageInput;
	private TextView                 _feeLabel;
	private AmountInput              _feeInput;
	private TextView                 _btnSend;
	private ImageView                _encryptBtn;
	private AddressValue             _meAddress;
	private Account                  _meAcc;
	private NacPublicKey             _initiator;
	private NacPublicKey             _signer;
	private boolean _encryptMsg     = false;
	private int     _displayWidthDp = AppHost.Screen.getSizeDpLogical().width;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_new_transaction;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_new_transaction;
	}

	@Override
	public void onBackPressed() {
		finish();
		startActivity(new Intent(this, DashboardActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Optional<AddressValue> lastAddress = AppSettings.instance().readLastUsedAccAddress();
		if (lastAddress.isPresent()) {
			_meAddress = lastAddress.get();
		}
		else {
			Timber.w("Last used address not present.");
			_meAddress = null;
			return;
		}
		// Init views
		final ScrollView scrollView = (ScrollView)findViewById(R.id.scroll_view);
		_accountsSpinner = (Spinner)findViewById(R.id.spinner_accounts);
		_balanceLabel = (TextView)findViewById(R.id.field_balance);
		showBalance(null);
		_fromPanel = (ViewGroup)findViewById(R.id.panel_from);
		_recipientInput = (AutocompleteAddressInput)findViewById(R.id.input_recipient);
		_amountInput = (AmountInput)findViewById(R.id.input_amount);
		_amountInput.setAllowZero(true);
		_amountInput.setTreatEmptyAsZero(true);
		_amountInput.addTextChangedListener(_feeChanger);
		_messageInput = (EditText)findViewById(R.id.input_message);
		_messageInput.addTextChangedListener(_feeChanger);
		_messageInput.addTextChangedListener(_lengthValidator);
		_feeLabel = (TextView)findViewById(R.id.label_fee);
		_feeInput = (AmountInput)findViewById(R.id.input_fee);
		_feeInput.setAllowZero(true);
		_feeInput.setTreatEmptyAsZero(true);
		_feeInput.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				_feeInput.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
			}
			return false;
		});
		_btnSend = (TextView)findViewById(R.id.btn_send);
		_btnSend.setOnClickListener(this::onSendClick);
		//
		_encryptBtn = (ImageView)findViewById(R.id.btn_encrypt);
		_encryptBtn.setOnClickListener(v -> {
			_encryptMsg = !_encryptMsg;
			refreshEncButtonState();
			updateFee();
			updateMessageErrorState(_messageInput.getText().toString());
		});
		//
		final Optional<Account> account = new AccountRepository().find(_meAddress);
		if (!account.isPresent()) {
			Timber.w("Local account not found: %s", _meAddress);
			_meAddress = null;
			return;
		}
		_meAcc = account.get();
		_initiator = _meAcc.publicData.publicKey;
		_signer = _meAcc.publicData.publicKey;

		getAccountInfo();
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String addressStr = getIntent().getStringExtra(EXTRA_STR_ADDRESS);
		final double amount = getIntent().getDoubleExtra(EXTRA_DOUBLE_AMOUNT, 0.0);
		final String messageStr = getIntent().getStringExtra(EXTRA_STR_MESSAGE);
		_encryptMsg = getIntent().getBooleanExtra(EXTRA_BOOL_ENCRYPTED, false);
		refreshEncButtonState();

		if (StringUtils.isNotNullOrEmpty(addressStr)) {
			_recipientInput.setText(addressStr);
		}
		try {
			final String textTag = (String)_amountInput.getTag();
			if (textTag != null) {
				_amountInput.setText(textTag);
			}
			else {
				_amountInput.setText(amount >= 0.01 ? Xems.fromXems(amount).toFractionalString() : "");
			}
		} catch (Throwable throwable) {
			_amountInput.setText(amount >= 0.01 ? Xems.fromXems(amount).toFractionalString() : "");
		}
		try {
			final String textTag = (String)_messageInput.getTag();
			if (textTag != null) {
				_messageInput.setText(textTag);
			}
			else {
				_messageInput.setText(messageStr);
			}
		} catch (Throwable throwable) {
			_messageInput.setText(messageStr);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		_amountInput.setTag(_amountInput.getText().toString());
		_messageInput.setTag(_messageInput.getText().toString());
	}

	private void onAccountInfo(final GetAccountInfoAsyncTask task, final AsyncResult<AccountMetaDataPairApiDto> result) {
		if (!result.getResult().isPresent()) {
			Toaster.instance().showGeneralError();
			return;
		}

		final AccountMetaDataPairApiDto accountInfo = result.getResult().get();
		showBalance(accountInfo.account.balance);

		switch (accountInfo.meta.getType()) {
			case MULTISIG: {
				Toast.makeText(this, R.string.errormessage_not_allowed, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			case COSIGNATORY: {
				_fromPanel.setVisibility(View.VISIBLE);
				final List<NacPublicKey> msigs = Stream.of(accountInfo.meta.cosignatoryOf)
						.map(a -> a.publicKey)
						.collect(Collectors.toList());
				msigs.add(0, _meAcc.publicData.publicKey);

				final ArrayAdapter<NacPublicKey> adapter =
						new ArrayAdapter<>(this, R.layout.list_item_spinner, msigs);
				_accountsSpinner.setAdapter(adapter);
				setOnAccountSelectedListener();
				break;
			}
		}
	}

	private void setOnAccountSelectedListener() {
		_accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				final NacPublicKey selected = (NacPublicKey)parent.getItemAtPosition(position);
				if (_meAcc.type == AccountType.COSIGNATORY) {
					_initiator = selected;
				}
				new GetAccountInfoAsyncTask(NewTransactionActivity.this, selected.toAddress())
						.withCompleteCallback((task, result) -> {
							if (!result.getResult().isPresent()) {
								Toaster.instance().showGeneralError();
								return;
							}
							final AccountMetaDataPairApiDto accountMetadata = result.getResult().get();
							showBalance(accountMetadata.account.balance);
							final AccountType accountType = AccountType.fromAccount(accountMetadata.meta);
							enableEncryptButton(accountType != AccountType.MULTISIG);
						})
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) { }
		});
	}

	// SEND click
	private void onSendClick(final View clicked) {
		Timber.i("Send transaction click");
		enableSendButton(false);

		if (_signer == null || _initiator == null) {
			Toast.makeText(this, R.string.errormessage_select_sender_account, Toast.LENGTH_SHORT).show();
			enableSendButton(true);
			return;
		}
		// Recipient
		final AddressValue recipient;
		try {
			recipient = _recipientInput.getAddress();
		} catch (NacException e) {
			Timber.d(e, "User input - bad address");
			_recipientInput.requestFocus();
			enableSendButton(true);
			return;
		}
		// Amount
		final Optional<Xems> amount = _amountInput.getAmount();
		if (!_amountInput.validate() || !amount.isPresent()) {
			enableSendButton(true);
			return;
		}
		// Message
		final String msgText = _messageInput.getText().toString();
		final byte[] msgData = msgText.getBytes();
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
			new EncryptMessageAsyncTask(this, _meAcc.privateKey, recipient, messageDraft)
					.withCompleteCallback(this::onMessageEncrypted)
					.execute();
		}
		else {
			announceTransaction(recipient, amount.get(), messageDraft);
		}
	}

	private void onMessageEncrypted(final EncryptMessageAsyncTask task, final AsyncResult<MessageDraft> result) {
		if (!result.getResult().isPresent()) {
			Timber.e("Failed to encrypt message. Task returned no result!");
			Toast.makeText(this, R.string.errormessage_failed_to_create_transaction, Toast.LENGTH_SHORT).show();
			enableSendButton(true);
			return;
		}
		if (!isNotDestroyed()) { return; }
		final MessageDraft messageDraft = result.getResult().get();
		final AddressValue recipient;
		try {
			recipient = _recipientInput.getAddress();
		} catch (NacException e) {
			Timber.d(e, "User input - bad address");
			enableSendButton(true);
			return;
		}
		// Amount
		final Optional<Xems> amount = _amountInput.getAmount();
		if (!_amountInput.validate() || !amount.isPresent()) {
			enableSendButton(true);
			return;
		}
		announceTransaction(recipient, amount.get(), messageDraft);
	}

	private void announceTransaction(final AddressValue recipient, final Xems amount, final MessageDraft msgData) {
		AbstractTransactionDraft draft =
				new TransferTransactionDraft(_initiator, recipient, amount, msgData);
		// Fee
		if (!_feeInput.getText().toString().isEmpty()) {
			final Xems minimumFee = draft.calculateMinimumFee();
			try {
				draft.setFee(_feeInput.getAmount().get());
			} catch (NacException e) {
				_feeInput.setText(minimumFee.toFractionalString());
				Toaster.instance().show(R.string.errormessage_fee_less_than_min);
				enableSendButton(true);
				return;
			}
		}

		if (!_signer.equals(_initiator)) {
			draft = new MultisigTransactionDraft(_signer, draft);
		}

		new SendTransactionAsyncTask(this, draft, _meAcc.privateKey)
				.withCompleteCallback((sendTask, sendResult) -> {
					if (!sendResult.getResult().isPresent()) {
						Timber.d("Bad result");
						enableSendButton(true);
						return;
					}
					final AnnounceResult announceResult = sendResult.getResult().get();
					if (!announceResult.successful) {
						ConfirmDialogFragment.create(true, null, getString(R.string.dialog_transaction_announce_failed, announceResult.message), null)
								.show(getFragmentManager(), null);
						enableSendButton(true);
						return;
					}
					_encryptMsg = false;
					refreshEncButtonState();
					_amountInput.setText("");
					_amountInput.setError(null);
					_messageInput.setText("");
					_messageInput.setError(null);
					_recipientInput.setText("");
					_recipientInput.setError(null);
					_feeInput.setText("");
					_feeInput.setError(null);
					ConfirmDialogFragment.create(true, null, R.string.dialog_message_transaction_announced, null)
							.setOnDismissListener(dialog -> {
								finish();
								startActivity(new Intent(this, DashboardActivity.class));
							})
							.show(getFragmentManager(), null);
					enableSendButton(true);
				})
				.execute();
	}

	private void getAccountInfo() {
		if (_meAddress == null) {
			finish();
			AccountListActivity.start(this);
			return;
		}
		new GetAccountInfoAsyncTask(this, _meAddress)
				.withCompleteCallback(this::onAccountInfo)
				.execute();
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

	private void enableSendButton(final boolean enable) {
		_btnSend.setClickable(enable);
		ViewUtils.setBgColor(_btnSend, this, enable ? R.color.official_green : R.color.official_gray);
	}

	private void showBalance(final Xems balance) {
		if (balance == null) {
			final String balanceStr = getString(R.string.label_amount_with_balance_placeholder, "");
			_balanceLabel.setText(balanceStr);
		}
		else {
			String amountFormat = getString(R.string.label_amount_with_balance_placeholder);
			String balanceHtml = getString(R.string.label_small_balance_inside_amount, balance.toFractionalString());
			if (_displayWidthDp < 360) {
				balanceHtml = "<small>" + balanceHtml + "</small>";
			}
			String amountStr = String.format(amountFormat, balanceHtml);
			_balanceLabel.setText(Html.fromHtml(amountStr));
			_balanceLabel.setSelected(true);
		}
	}

	private void refreshEncButtonState() {
		ViewUtils.setBgColor(_encryptBtn, this, _encryptMsg ? R.color.official_green : R.color.light_gray);
	}

	private void updateFee() {
		final Xems amount = _amountInput.getAmount().orElse(Xems.ZERO);
		final Editable message = _messageInput.getText();

		final int payloadLength =
				_encryptMsg ? Ed25519Helper.getEncryptedMessageLength(message.toString())
						: message.toString().getBytes().length;
		final Xems minFee = TransferTransactionDraft.calculateMinimumFee(amount, payloadLength);

		final String feeLabelStr = getString(R.string.label_fee_colon,
				Integer.toHexString(getResources().getColor(R.color.official_green) & 0x00ffffff),
				NumberUtils.toString(minFee.getAsFractional()));
		_feeLabel.setText(Html.fromHtml(feeLabelStr));

		if (!_feeInput.hasFocus()) {
			_feeInput.setAmount(minFee);
		}
	}

	private void updateMessageErrorState(String msg) {
		if (MessageDraft.isLengthValid(msg, _encryptMsg)) {
			InputErrorUtils.clearErrorState(_messageInput);
		}
		else {
			InputErrorUtils.setErrorState(_messageInput, null);
		}
	}

	private final TextWatcher _feeChanger = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			updateFee();
		}
	};

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
}
