package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.crypto.KeyProvider;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.models.BinaryData;

public final class CheckPasswordDialogFragment extends NacBaseDialogFragment {

	private Consumer<CheckPasswordDialogFragment> _passwordConfirmListener;

	public static CheckPasswordDialogFragment create() {
		final CheckPasswordDialogFragment fragment = new CheckPasswordDialogFragment();
		final Bundle args = setArgs(true, R.string.dialog_title_account_import_password, true, null);
		fragment.setArguments(args);
		return fragment;
	}

	private EditText _passwordInput;

	public BinaryData deriveKey(final BinaryData salt)
			throws NacCryptoException {
		return KeyProvider.deriveKey(_passwordInput.getText().toString(), salt);
	}

	public CheckPasswordDialogFragment setOnPasswordConfirmListener(final Consumer<CheckPasswordDialogFragment> listener) {
		_passwordConfirmListener = listener;
		return this;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_check_password_dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		if (view == null) {
			return null;
		}
		_passwordInput = (EditText)view.findViewById(R.id.input_password);
		_passwordInput.addTextChangedListener(_pwdTextWatcher);
		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AppHost.SoftKeyboard.forceShowAsync();
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		if (_passwordInput.getText().length() > 0) {
			if (_passwordConfirmListener != null) {
				_passwordConfirmListener.accept(this);
			}
			super.onConfirmClick(clicked);
		}
	}

	private final TextWatcher _pwdTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

		}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

		}

		@Override
		public void afterTextChanged(final Editable s) {
			enableConfirmButton(s.length() > 0);
		}
	};
}
