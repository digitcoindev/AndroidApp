package org.nem.nac.ui.controls;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.ui.utils.InputErrorUtils;

/**
 * Used to input new passwords.
 */
public final class PasswordInput extends EditText {

	private boolean _isValid = false;

	public PasswordInput(final Context context) {
		super(context);
		init();
	}

	public PasswordInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PasswordInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public boolean validate(final boolean showMessage) {
		if (_isValid) {
			InputErrorUtils.clearErrorState(PasswordInput.this);
		}
		else {
			Integer msg = showMessage ? R.string.errormessage_too_short_password_hint : null;
			InputErrorUtils.setErrorState(PasswordInput.this, msg, AppConstants.MIN_PASSWORD_LENGTH);
		}
		return _isValid;
	}

	private void init() {
		setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		setSelection(getText().length());
		addTextChangedListener(_watcher);
	}

	private final TextWatcher _watcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			_isValid = s.length() >= AppConstants.MIN_PASSWORD_LENGTH;
			validate(false);
		}
	};
}
