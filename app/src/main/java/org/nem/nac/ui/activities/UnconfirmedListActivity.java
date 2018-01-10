package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.enums.MultisigCosignatoryModificationType;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.helpers.TransactionsHelper;
import org.nem.nac.models.AnnounceResult;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.transactions.MultisigAggregateModificationTransactionApiDto;
import org.nem.nac.models.api.transactions.MultisigCosignatoryModificationApiDto;
import org.nem.nac.models.api.transactions.MultisigTransactionApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.drafts.MultisigSignatureTransactionDraft;
import org.nem.nac.tasks.GetUnconfirmedTransactionsAsyncTask;
import org.nem.nac.tasks.SendTransactionAsyncTask;
import org.nem.nac.ui.adapters.UnconfirmedToSignAdapter;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class UnconfirmedListActivity extends NacBaseActivity {

	public static final String EXTRA_PARC_ACCOUNT_ADDRESS = UnconfirmedListActivity.class.getCanonicalName() + "account-address";

	private ListView     _unconfirmedSignList;
	private AddressValue _address;
	private Account      _account;

	@Override
	public void onBackPressed() {
		finish();
		startActivity(new Intent(this, DashboardActivity.class).putExtra(DashboardActivity.EXTRA_BOOL_DONT_SHOW_UNSIGNED_PROMPT, true));
	}

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_unconfirmed_list;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_unconfirmed_list;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_unconfirmedSignList = (ListView)findViewById(R.id.unconfirmed_sign_list);
		//
		// Validate supplied address
		final AddressValue address = getIntent().<AddressValue>getParcelableExtra(EXTRA_PARC_ACCOUNT_ADDRESS);
		if (address != null && AddressValue.isValid(address)) {
			_address = address;
		}
		else {
			Timber.w("Address extra is invalid. %s. Trying get last used.", address);
			final Optional<AddressValue> lastAddress = AppSettings.instance().readLastUsedAccAddress();
			if (lastAddress.isPresent()) {
				_address = lastAddress.get();
			}
		}
		//
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (_address == null) {
			Timber.w("Last used address not present.");
			finish();
			AccountListActivity.start(this);
			return;
		}
		final Optional<Account> account = new AccountRepository().find(_address);
		if (!account.isPresent()) {
			Timber.w("Non-existing account.");
			finish();
			AccountListActivity.start(this);
			return;
		}
		_account = account.get();
		checkUnconfirmedTransactions();
	}

	private void onUnconfirmedTransactions(final GetUnconfirmedTransactionsAsyncTask task,
			final AsyncResult<List<UnconfirmedTransactionMetaDataPairApiDto>> result) {
		if (!result.getResult().isPresent()) {
			Toaster.instance().showGeneralError();
			return;
		}
		//
		try {
			final List<UnconfirmedTransactionMetaDataPairApiDto> unconfirmed = result.getResult().get();
			if (unconfirmed == null) {
				return;
			}
			final List<UnconfirmedTransactionMetaDataPairApiDto> toSign = Stream.of(unconfirmed)
					.filter(x -> TransactionsHelper.needToSign(x, _address))
					.collect(Collectors.toList());
			final UnconfirmedToSignAdapter adapter = new UnconfirmedToSignAdapter(this, toSign)
					.setOnConfirmListener(this::onConfirmTransaction)
					.setOnShowChangesListener(this::onShowChanges);
			_unconfirmedSignList.setAdapter(adapter);
		} catch (Throwable t) {
			throw t;
		}
	}

	private void onShowChanges(final UnconfirmedTransactionMetaDataPairApiDto unconfirmed) {
		final StringBuilder changes = new StringBuilder();
		changes.append(getString(R.string.unconfirmed_info_account, ((MultisigTransactionApiDto)unconfirmed.transaction).otherTrans.signer));
		final MultisigAggregateModificationTransactionApiDto aggregateModification =
				(MultisigAggregateModificationTransactionApiDto)((MultisigTransactionApiDto)unconfirmed.transaction).otherTrans;
		final List<String> toDelete = new ArrayList<>();
		final List<String> toAdd = new ArrayList<>();
		for (MultisigCosignatoryModificationApiDto modification : aggregateModification.modifications) {
			if (modification.modificationType == MultisigCosignatoryModificationType.ADD_NEW_COSIGNATORY) {
				toAdd.add(modification.cosignatoryAccount.toString());
			}
			if (modification.modificationType == MultisigCosignatoryModificationType.DELETE_EXISTING_COSIGNATORY) {
				toDelete.add(modification.cosignatoryAccount.toString());
			}
		}
		//
		if (!toDelete.isEmpty()) {
			changes.append("<br/>").append(getString(R.string.unconfirmed_info_deleted_colon));
			for (String del : toDelete) {
				changes.append("<br/>").append(del);
			}
		}
		//
		if (!toAdd.isEmpty()) {
			changes.append("<br/>").append(getString(R.string.unconfirmed_info_added_colon));
			for (String add : toAdd) {
				changes.append("<br/>").append(add);
			}
		}
		//
		ConfirmDialogFragment.create(true, R.string.dialog_title_unconfirmed_aggregate_modification_info, changes.toString(), null)
				.show(getFragmentManager(), null);
	}

	private void onConfirmTransaction(final UnconfirmedTransactionMetaDataPairApiDto unconfirmed) {
		if (_account == null) {
			Timber.e("Owner account was null!");
			Toaster.instance().showGeneralError();
			return;
		}
		final MultisigSignatureTransactionDraft draft =
				new MultisigSignatureTransactionDraft(_account.publicData.publicKey, unconfirmed.meta.data, ((MultisigTransactionApiDto)unconfirmed.transaction).otherTrans.signer
						.toAddress());
		new SendTransactionAsyncTask(this, draft, _account.privateKey)
				.withCompleteCallback(this::onTransactionConfirmed)
				.execute();
	}

	private void onTransactionConfirmed(final SendTransactionAsyncTask task, final AsyncResult<AnnounceResult> result) {
		if (!result.getResult().isPresent()) {
			Timber.e("Failed to confirm multisig transaction");
			Toaster.instance().showGeneralError();
			return;
		}
		final AnnounceResult announceResult = result.getResult().get();
		if (!announceResult.successful) {
			ConfirmDialogFragment.create(true, null, getString(R.string.dialog_transaction_announce_failed, announceResult.message), null)
					.show(getFragmentManager(), null);
			return;
		}
		ConfirmDialogFragment.create(true, null, R.string.dialog_message_transaction_announced, null)
				.setOnDismissListener(dialog -> {
					checkUnconfirmedTransactions();
				})
				.show(getFragmentManager(), null);
	}

	private void checkUnconfirmedTransactions() {
		new GetUnconfirmedTransactionsAsyncTask(this, _address)
				.withCompleteCallback(this::onUnconfirmedTransactions)
				.execute();
	}
}
