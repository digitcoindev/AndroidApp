package org.nem.nac.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.AnnounceResult;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.account.AccountInfoApiDto;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.transactions.drafts.AggregateModificationTransactionDraft;
import org.nem.nac.models.transactions.drafts.MultisigTransactionDraft;
import org.nem.nac.tasks.GetAccountInfoAsyncTask;
import org.nem.nac.tasks.SendTransactionAsyncTask;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.controls.DeletableCosignatoriesLinearList;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

@SuppressWarnings("SpellCheckingInspection")
public final class MsigRemoveCosigsFragment extends MsigBaseFragment {

	public static MsigRemoveCosigsFragment create() {
		MsigRemoveCosigsFragment fragment = new MsigRemoveCosigsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	private DeletableCosignatoriesLinearList _cosigsList;
	private TextView                         _deleteCosigsBtn;
	private AccountMetaDataPairApiDto        _selectedMultisig;
	private Account                          _me;
	private NacPublicKey _cosigToDelete;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_msig_remove_cosigs;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final ViewGroup layout = (ViewGroup)super.onCreateView(inflater, container, savedInstanceState);
		_cosigsList = (DeletableCosignatoriesLinearList)layout.findViewById(R.id.linear_list_cosignatories);
		_cosigsList.setOnDeleteItemListener(this::onCosigDelete);
		_deleteCosigsBtn = (TextView)layout.findViewById(R.id.btn_do_delete_cosigs);
		_deleteCosigsBtn.setOnClickListener(this::onDeleteCosigsClick);
		setAccountSelectedListener();
		return layout;
	}

	private void onCosigDelete(final NacPublicKey cosig) {
		if (_cosigToDelete != null) {
			Timber.w("Tried to overwrite deletable cosig");
		}
		if (cosig == null) {
			Timber.w("Cosig to delete was null");
			return;
		}
		_cosigToDelete = cosig;
		_cosigsList.removeItem(cosig);
		_cosigsList.allowDeleting(false);
		_cosigsList.refreshViews();
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (lastUsedAddress == null) {
			Timber.e("Last address was null");
			getActivity().finish();
		}
		final Optional<Account> account = new AccountRepository().find(lastUsedAddress);
		if (!account.isPresent()) {
			Timber.e("Last address is not valid local account");
			getActivity().finish();
		}
		_me = account.get();
	}

	// DELETE COSIGS CLICK
	private void onDeleteCosigsClick(final View clicked) {
		if (_selectedMultisig == null || _selectedMultisig.meta == null) {
			Toaster.instance().show(R.string.errormessage_account_not_selected);
			return;
		}

		if (_cosigToDelete != null) {
			announceMultisigChanges(new HashSet<>(), _cosigToDelete, 0);
		}
		else {
			Toaster.instance().show(R.string.errormessage_no_cosigs_to_remove);
		}
	}

	private void setAccountSelectedListener() {
		accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				final AccountInfoApiDto msigAcc = (AccountInfoApiDto)parent.getItemAtPosition(position);
				try {
					_cosigToDelete = null;
					_cosigsList.allowDeleting(true);
					_cosigsList.clearItems();
					new GetAccountInfoAsyncTask((NacBaseActivity)getActivity(), msigAcc.address)
							.withCompleteCallback(MsigRemoveCosigsFragment.this::onMultisigInfo)
							.execute();
				} catch (ClassCastException e) {
					Timber.e("Activity %s is not %s!", getActivity().getClass(), NacBaseActivity.class);
				}
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {}
		});
	}

	private void onMultisigInfo(final GetAccountInfoAsyncTask task, final AsyncResult<AccountMetaDataPairApiDto> result) {
		if (!result.getResult().isPresent()) {
			Timber.d("Account info not present");
			Toast.makeText(getActivity(), R.string.errormessage_failed_to_get_account_info, Toast.LENGTH_SHORT).show();
			return;
		}

		final AccountMetaDataPairApiDto msigInfo = result.getResult().get();
		_selectedMultisig = msigInfo;
		final List<NacPublicKey> cosigKeys = Stream.of(msigInfo.meta.cosignatories)
				.map(cosig -> cosig.publicKey)
				.collect(Collectors.toList());
		_cosigsList.addItems(cosigKeys);
	}

	private void onTransactionAnnounced(final SendTransactionAsyncTask task, final AsyncResult<AnnounceResult> result) {
		if (!result.getResult().isPresent()) {
			Timber.w("Bad result");
			Toaster.instance().showGeneralError();
			return;
		}
		final AnnounceResult announceResult = result.getResult().get();
		if (!announceResult.successful) {
			ConfirmDialogFragment.create(true, null, getString(R.string.dialog_transaction_announce_failed, announceResult.message), null)
					.show(getActivity().getFragmentManager(), null);
			return;
		}

		ConfirmDialogFragment.create(false, null, R.string.dialog_message_transaction_announced, null)
				.setOnDismissListener(d -> {
					_cosigToDelete = null;
					_cosigsList.allowDeleting(true);
					_cosigsList.refreshViews();
				})
				.show(getActivity().getFragmentManager(), null);
	}

	private void announceMultisigChanges(final Set<NacPublicKey> cosigsAdded, final NacPublicKey cosigRemoved, final int minCosigsRelChange) {
		AssertUtils.notNull(_selectedMultisig, _me);
		final AggregateModificationTransactionDraft modificationTran =
				new AggregateModificationTransactionDraft(_selectedMultisig.account.publicKey, cosigsAdded, cosigRemoved, minCosigsRelChange);
		final MultisigTransactionDraft multisigTransaction = new MultisigTransactionDraft(_me.publicData.publicKey, modificationTran);

		try {
			new SendTransactionAsyncTask((NacBaseActivity)getActivity(), multisigTransaction, _me.privateKey)
					.withCompleteCallback(this::onTransactionAnnounced)
					.execute();
		} catch (ClassCastException e) {
			Timber.e("Activity %s is not %s!", getActivity().getClass(), NacBaseActivity.class);
		}
	}
}
