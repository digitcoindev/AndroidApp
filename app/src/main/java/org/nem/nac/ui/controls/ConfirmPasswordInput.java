package org.nem.nac.ui.controls;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import org.nem.nac.R;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.ui.utils.InputErrorUtils;

public final class ConfirmPasswordInput extends EditText {

	public ConfirmPasswordInput(final Context context) {
		super(context);
		init();
	}

	public ConfirmPasswordInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ConfirmPasswordInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public boolean validate(@NonNull final String password, final boolean showMessage) {
		AssertUtils.notNull(password);
		if (!getText().toString().equals(password)) {
			final Integer msg = showMessage ? R.string.errormessage_passwords_not_match : null;
			InputErrorUtils.setErrorState(this, msg);
			return false;
		}
		InputErrorUtils.clearErrorState(this);
		return true;
	}

	private void init() {
		setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		setSelection(getText().length());
		addTextChangedListener(_errorClearer);
	}

	private final TextWatcher _errorClearer = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			InputErrorUtils.clearErrorState(ConfirmPasswordInput.this);
		}
	};
}
