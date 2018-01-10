package org.nem.nac.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.AnnounceResult;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.account.AccountInfoApiDto;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.transactions.drafts.AggregateModificationTransactionDraft;
import org.nem.nac.models.transactions.drafts.MultisigTransactionDraft;
import org.nem.nac.tasks.SendTransactionAsyncTask;
import org.nem.nac.ui.activities.DashboardActivity;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.controls.CosignerInput;
import org.nem.nac.ui.controls.MinCosignatoriesInput;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.utils.InputErrorUtils;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

@SuppressWarnings("SpellCheckingInspection")
public final class MsigAddCosigsFragment extends MsigBaseFragment {

	public static MsigAddCosigsFragment create() {
		MsigAddCosigsFragment fragment = new MsigAddCosigsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	private ScrollView _scrollView;
	private ViewGroup             _newCosigsPanel;
	private View                  _addCosigInputBtn;
	private TextView              _confirmBtn;
	private LayoutInflater        _inflater;
	private MinCosignatoriesInput _minCosigsInput;
	private final List<CosignerInput> _inputs = new ArrayList<>();
	private AccountMetaDataPairApiDto _meInfo;
	private AccountInfoApiDto         _selectedMultisig;

	public void setMyInfo(final AccountMetaDataPairApiDto account) {
		_meInfo = account;
		if (_meInfo.meta.getType() == AccountType.SIMPLE) {
			if (_confirmBtn == null && getView() != null) {
				_confirmBtn = ((TextView)getView().findViewById(R.id.btn_do_add_cosigs));
			}
			if (_confirmBtn != null) {
				_confirmBtn.setText(R.string.btn_convert_to_multisig);
			}
			if (_meInfo.account.publicKey == null) {
				Timber.e("Account has no public key!");
				getActivity().finish();
			}
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_msig_add_cosigs;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final ViewGroup layout = (ViewGroup)super.onCreateView(inflater, container, savedInstanceState);
		_scrollView = (ScrollView)layout.findViewById(R.id.scroll_view);
		_newCosigsPanel = (ViewGroup)layout.findViewById(R.id.panel_new_cosigs);
		_minCosigsInput = (MinCosignatoriesInput)layout.findViewById(R.id.input_min_cosigners);
		_minCosigsInput.setAllowEmpty(true);
		_minCosigsInput.addTextChangedListener(_minCosigsErrorClearer);
		_addCosigInputBtn = layout.findViewById(R.id.btn_add_cosig);
		_addCosigInputBtn.setOnClickListener(this::onAddCosigInputClick);
		_confirmBtn = (TextView)layout.findViewById(R.id.btn_do_add_cosigs);
		_confirmBtn.setOnClickListener(this::onConfirmCosigsAdd);
		_inflater = inflater;
		_newCosigsPanel.addView(inflateNewCosigInput());
		setAccountSelectedListener();
		return layout;
	}

	private void onAddCosigInputClick(final View clicked) {
		final View input = inflateNewCosigInput();
		_newCosigsPanel.addView(input);
		_scrollView.post(() -> {
			_scrollView.fullScroll(View.FOCUS_DOWN);
			input.requestFocus();
		});
	}

	private void onConfirmCosigsAdd(final View clicked) {
		if (_meInfo == null) {
			Toaster.instance().showGeneralError();
			return;
		}
		if (!_minCosigsInput.validate()) {
			return;
		}

		View invalid = null;
		for (CosignerInput input : _inputs) {
			boolean valid = input.isEmpty() || input.validate();
			if (!valid) {
				if (invalid == null) { invalid = input; }
			}
		}
		if (invalid != null) {
			invalid.requestFocus();
			invalid.requestLayout();
			return;
		}

		final Set<NacPublicKey> publicKeys = Stream.of(_inputs)
				.filter(input -> !input.isEmpty())
				.map(input -> {
					try {
						return input.getPublicKey();
					} catch (NacException e) {
						throw new NacRuntimeException(e);
					}
				})
				.collect(Collectors.toSet());

		if (_meInfo.meta.getType() == AccountType.SIMPLE) {
			if (publicKeys.isEmpty()) {
				Toaster.instance().show(R.string.errormessage_no_cosigs_to_add);
				return;
			}
			try {
				final int minCosignatories = _minCosigsInput.isEmpty() ? 0 : _minCosigsInput.getMinCosignatories();
				if (minCosignatories != 0 && minCosignatories > publicKeys.size()) {
					InputErrorUtils.setErrorState(_minCosigsInput, R.string.errormessage_min_cosigs_too_big);
					_minCosigsInput.requestFocus();
					return;
				}
				convertToMultisig(minCosignatories, publicKeys);
			} catch (NacException e) {
				Timber.e(e, "Failed to read min cosigs");
				Toast.makeText(getActivity(), R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
			}
			return;
		}

		if (_selectedMultisig == null || _selectedMultisig.multisigInfo == null) {
			Toast.makeText(getActivity(), R.string.errormessage_account_not_selected, Toast.LENGTH_SHORT).show();
			return;
		}

		int minCosigsRel;
		try {
			minCosigsRel = _minCosigsInput.isEmpty()
					? 0
					: _minCosigsInput.getMinCosignatories() - _selectedMultisig.multisigInfo.minCosignatories;
		} catch (NacException e) {
			Timber.e(e, "Failed to read min cosigs");
			Toast.makeText(getActivity(), R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
			return;
		}

		if (publicKeys.isEmpty()) {
			if (minCosigsRel != 0) {
				announceMultisigChanges(publicKeys, null, minCosigsRel);
			}
			else {
				Toast.makeText(getActivity(), R.string.errormessage_no_cosigs_to_add, Toast.LENGTH_SHORT).show();
			}
		}
		else {
			announceMultisigChanges(publicKeys, null, minCosigsRel);
		}
	}

	// CONVERT TO MULTISIG
	private void convertToMultisig(final int minCosignatories, final Set<NacPublicKey> publicKeys) {
		AssertUtils.notNull(_meInfo);
		final AggregateModificationTransactionDraft draft =
				new AggregateModificationTransactionDraft(_meInfo.account.publicKey, publicKeys, null, minCosignatories);
		final Optional<Account> account = new AccountRepository().find(_meInfo.account.publicKey);
		if (!account.isPresent()) {
			Timber.e("Failed to send multisig modifications - bad account");
			Toast.makeText(getActivity(), R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			new SendTransactionAsyncTask((NacBaseActivity)getActivity(), draft, account.get().privateKey)
					.withCompleteCallback(this::onTransactionAnnounced)
					.execute();
		} catch (ClassCastException e) {
			Timber.e("Activity %s is not %s!", getActivity().getClass(), NacBaseActivity.class);
		}
	}

	private View inflateNewCosigInput() {
		final View view = _inflater.inflate(R.layout.layout_new_cosigner_input, _newCosigsPanel, false);
		final CosignerInput input = (CosignerInput)view.findViewById(R.id.input_new_cosigner);
		_inputs.add(input);
		return view;
	}

	private void onTransactionAnnounced(final SendTransactionAsyncTask task, final AsyncResult<AnnounceResult> result) {
		if (!result.getResult().isPresent()) {
			Timber.e("Failed to send multisig modifications");
			Toaster.instance().showGeneralError();
			return;
		}
		final AnnounceResult announceResult = result.getResult().get();
		if (!announceResult.successful) {
			ConfirmDialogFragment.create(true, null, getString(R.string.dialog_transaction_announce_failed, announceResult.message), null)
					.show(getActivity().getFragmentManager(), null);
			return;
		}
		ConfirmDialogFragment.create(true, null, R.string.dialog_message_transaction_announced, null)
				.setOnDismissListener(dialog -> {
					_newCosigsPanel.removeAllViews();
					_inputs.clear();
					getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
					getActivity().finish();
				})
				.show(getActivity().getFragmentManager(), null);
	}

	private void setAccountSelectedListener() {
		accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				_selectedMultisig = (AccountInfoApiDto)parent.getItemAtPosition(position);
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {}
		});
	}

	private void announceMultisigChanges(final Set<NacPublicKey> cosigsAdded, final NacPublicKey cosigRemoved, final int minCosigsRelChange) {
		final AggregateModificationTransactionDraft modificationTran =
				new AggregateModificationTransactionDraft(_selectedMultisig.publicKey, cosigsAdded, cosigRemoved, minCosigsRelChange);
		final MultisigTransactionDraft multisigTransaction = new MultisigTransactionDraft(_meInfo.account.publicKey, modificationTran);

		final Optional<Account> account = new AccountRepository().find(_meInfo.account.publicKey);
		if (!account.isPresent()) {
			Timber.e("Failed to send multisig modifications - bad account");
			Toast.makeText(getActivity(), R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			new SendTransactionAsyncTask((NacBaseActivity)getActivity(), multisigTransaction, account.get().privateKey)
					.withCompleteCallback(this::onTransactionAnnounced)
					.execute();
		} catch (ClassCastException e) {
			Timber.e("Activity %s is not %s!", getActivity().getClass(), NacBaseActivity.class);
		}
	}

	private final TextWatcher _minCosigsErrorClearer = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			InputErrorUtils.clearErrorState(_minCosigsInput);
		}
	};
}
