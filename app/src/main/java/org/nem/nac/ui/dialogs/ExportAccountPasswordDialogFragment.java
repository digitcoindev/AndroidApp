package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.crypto.KeyProvider;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.models.BinaryData;
import org.nem.nac.ui.controls.PasswordInput;
import org.nem.nac.ui.utils.InputErrorUtils;

import timber.log.Timber;

public final class ExportAccountPasswordDialogFragment extends NacBaseDialogFragment {

	public static ExportAccountPasswordDialogFragment create() {
		ExportAccountPasswordDialogFragment fragment = new ExportAccountPasswordDialogFragment();
		Bundle args = setArgs(true, R.string.dialog_title_account_export_password, true, null);
		fragment.setArguments(args);
		return fragment;
	}

	private PasswordInput                                 _passwordInput;
	private CheckBox                                      _useAppPasswordCheckBox;
	private Consumer<ExportAccountPasswordDialogFragment> _onPasswordEnteredListener;

	public boolean useAppPassword() {
		return _useAppPasswordCheckBox.isChecked();
	}

	public ExportAccountPasswordDialogFragment setOnPasswordEnteredListener(final Consumer<ExportAccountPasswordDialogFragment> listener) {
		_onPasswordEnteredListener = listener;
		return this;
	}

	public BinaryData deriveKey(final BinaryData salt)
			throws NacCryptoException {
		return KeyProvider.deriveKey(_passwordInput.getText().toString(), salt);
	}

	public boolean isValidPassword() {
		return _passwordInput.validate(false);
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_export_account_password_dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		if (view == null) {
			return null;
		}
		_passwordInput = (PasswordInput)view.findViewById(R.id.input_password);
		setPwdChangeListener();
		_useAppPasswordCheckBox = (CheckBox)view.findViewById(R.id.checkbox_use_app_password);
		_useAppPasswordCheckBox.setOnCheckedChangeListener(this::onUseAppPasswordChange);
		_useAppPasswordCheckBox.setChecked(true);
		return view;
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		if (_onPasswordEnteredListener != null && (_useAppPasswordCheckBox.isChecked() || _passwordInput.validate(true))) {
			_onPasswordEnteredListener.accept(this);
			super.onConfirmClick(clicked);
		}
		else if (!_useAppPasswordCheckBox.isChecked() && !_passwordInput.validate(true)) {
			InputErrorUtils.setErrorState(_passwordInput, R.string.errortext_password_too_short);
		}
	}

	private void setPwdChangeListener() {
		_passwordInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

			@Override
			public void afterTextChanged(final Editable s) {
				if (s.length() > 0) {
					_useAppPasswordCheckBox.setChecked(false);
				}
			}
		});
	}

	private void onUseAppPasswordChange(final CompoundButton button, final boolean isChecked) {
		Timber.d("Use app password: %s", isChecked);
		enablePasswordInput(!isChecked);
	}

	private void enablePasswordInput(final boolean enable) {
		_passwordInput.setVisibility(enable ? View.VISIBLE : View.GONE);
		if (enable) {
			_passwordInput.requestFocus();
		}
		else {
			_passwordInput.setText("");
		}
	}
}
