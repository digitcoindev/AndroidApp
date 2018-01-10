package org.nem.nac.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.crypto.Mcrypto;
import org.nem.nac.datamodel.repositories.AppPasswordRepository;
import org.nem.nac.models.AppPassword;
import org.nem.nac.tasks.ChangeAppPasswordAsyncTask;
import org.nem.nac.tasks.CheckAppPasswordTask;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.controls.ConfirmPasswordInput;
import org.nem.nac.ui.controls.PasswordInput;
import org.nem.nac.ui.utils.InputErrorUtils;

public final class ChangeAppPasswordDialogFragment extends NacBaseDialogFragment {

	private static final String ARG_FORCE_RELOGIN = "force-relogin";
	private EditText              _existingPasswordInput;
	private ConfirmPasswordInput  _confirmPasswordInput;
	private PasswordInput         _newPasswordInput;
	private Optional<AppPassword> _appPassword;
	private Runnable              _pwdChangedListener;
	private boolean _forceRelogin = true;

	public static ChangeAppPasswordDialogFragment create(final boolean forceRelogin) {
		ChangeAppPasswordDialogFragment fragment = new ChangeAppPasswordDialogFragment();
		Bundle args = setArgs(true, null, true, null);
		args.putBoolean(ARG_FORCE_RELOGIN, forceRelogin);
		fragment.setArguments(args);
		return fragment;
	}

	public ChangeAppPasswordDialogFragment setOnPasswordChangedListener(final Runnable listener) {
		_pwdChangedListener = listener;
		return this;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_change_app_password_dialog;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			_forceRelogin = getArguments().getBoolean(ARG_FORCE_RELOGIN, true);
		}
		_appPassword = new AppPasswordRepository().get();
		title = getString(_appPassword.isPresent() ? R.string.dialog_title_change_app_password : R.string.dialog_title_setup_app_password);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = super.onCreateView(inflater, container, savedInstanceState);
		_existingPasswordInput = (EditText)layout.findViewById(R.id.input_password_existing);
		_existingPasswordInput.addTextChangedListener(_clearErrorWatcher);
		_newPasswordInput = (PasswordInput)layout.findViewById(R.id.input_new_password);
		_confirmPasswordInput = (ConfirmPasswordInput)layout.findViewById(R.id.input_confirm_password);
		return layout;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!_appPassword.isPresent()) {
			_existingPasswordInput.setEnabled(false);
			_existingPasswordInput.setVisibility(View.GONE);
		}
	}

	@SuppressLint("MissingSuperCall")
	@Override
	protected void onConfirmClick(final View clicked) {
		confirmBtn.setClickable(false);
		if (_appPassword.isPresent()) {
			new CheckAppPasswordTask((NacBaseActivity)getActivity())
					.withCompleteCallback(this::onAppPasswordChecked)
					.execute(_existingPasswordInput.getText().toString());
		}
		else {
			onAppPasswordOk();
		}
	}

	private void onAppPasswordChecked(final CheckAppPasswordTask task, final AsyncResult<Boolean> result) {
		if (!result.getResult().isPresent() || !result.getResult().get()) {
			InputErrorUtils.setErrorState(_existingPasswordInput, R.string.errormessage_invalid_password);
			_existingPasswordInput.requestFocus();
			confirmBtn.setClickable(true);
			return;
		}
		onAppPasswordOk();
	}

	private void onAppPasswordOk() {
		final String newPwd = _newPasswordInput.getText().toString();
		if (!_newPasswordInput.validate(true)) {
			confirmBtn.setClickable(true);
			_newPasswordInput.requestFocus();
			return;
		}
		if (!_confirmPasswordInput.validate(newPwd, true)) {
			confirmBtn.setClickable(true);
			_confirmPasswordInput.requestFocus();
			return;
		}
		//
		new ChangeAppPasswordAsyncTask((NacBaseActivity)getActivity(), _appPassword, newPwd, _forceRelogin)
				.withCompleteCallback((task, result) -> {
					if (result.getResult().isPresent() && result.getResult().get()) {
						super.onConfirmClick(confirmBtn);
						if (_pwdChangedListener != null) {
							_pwdChangedListener.run();
						}
					}
					confirmBtn.setClickable(true);
				})
				.execute();
	}

	private final TextWatcher _clearErrorWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			InputErrorUtils.clearErrorState(_existingPasswordInput);
		}
	};
}
