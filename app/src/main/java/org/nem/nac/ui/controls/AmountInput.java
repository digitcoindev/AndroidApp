package org.nem.nac.ui.controls;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.models.Xems;

import timber.log.Timber;

public final class AmountInput extends EditText {

	private static final String ALLOWED_CHARS = "1234567890.";
	private static final int MAX_DECIMAL_DIGITS = 6;
	@ColorInt
	private int _colorText;
	@ColorInt
	private int _colorHint;
	@ColorInt
	private int _colorError;
	private       boolean   _treatEmptyAsZero = false;
	private       boolean   _allowZero        = false;
	private final Character _decimalSeparator = NumberUtils.getDecimalSeparator().orElse('.');

	public AmountInput(final Context context) {
		super(context);
		init();
	}

	public AmountInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AmountInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * If input is empty, makes it return zero value rather than fail validation. False by default
	 */
	public void setTreatEmptyAsZero(final boolean allow) {
		_treatEmptyAsZero = allow;
	}

	/**
	 * Allows zero value. False by default.
	 */
	public void setAllowZero(final boolean allow) {
		_allowZero = allow;
	}

	public boolean validate() {
		final String text = getText().toString();
		if (_treatEmptyAsZero && text.isEmpty()) {
			setErrorState(false);
			return true;
		}
		//
		final int dotIndex = text.indexOf(_decimalSeparator);
		if (dotIndex >= 0) {
			if (text.lastIndexOf(_decimalSeparator) != dotIndex) {
				setErrorState(true);
				return false;
			}
			if (text.length() - (dotIndex + 1) > MAX_DECIMAL_DIGITS) {
				setErrorState(true);
				return false;
			}
		}
		//
		try {
			final double num = NumberUtils.parseDouble(text);
			if (num < 0.0 || !_allowZero && num == 0.0) {
				setErrorState(true);
				return false;
			}
			setErrorState(false);
			return true;
		} catch (NumberFormatException e) {
			setErrorState(true);
			return false;
		}
	}

	@NonNull
	public Optional<Xems> getAmount() {
		final String text = getText().toString();
		if (!validate()) {
			return Optional.empty();
		}
		if (_treatEmptyAsZero && text.isEmpty()) {
			return Optional.of(Xems.ZERO);
		}
		return Optional.of(Xems.fromXems(NumberUtils.parseDouble(text)));
	}

	public void setAmount(final Xems amount) {
		setText(amount.toFractionalString());
	}

	private void init() {
		//setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		setRawInputType(InputType.TYPE_CLASS_NUMBER);
		setFilters(new InputFilter[] { new MaxDecimalSignsFilter() });
		addTextChangedListener(_validationWatcher);
		_colorText = getCurrentTextColor();
		_colorHint = getCurrentHintTextColor();
		_colorError = getContext().getResources().getColor(R.color.default_red);
	}

	private void setErrorState(boolean isError) {
		setTextColor(isError ? _colorError : _colorText);
		setHintTextColor(isError ? _colorError : _colorHint);
	}

	private final TextWatcher _validationWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			validate();
		}
	};

	private class MaxDecimalSignsFilter implements InputFilter {
		@Override
		public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest, final int dstart, final int dend) {
			boolean keepOriginal = true;
			StringBuilder sb = new StringBuilder(end - start);
			for (int i = start; i < end; i++) {
				char c = source.charAt(i);
				if (isCharAllowed(c)) { sb.append(c); }
				else { keepOriginal = false; }
			}

			if (keepOriginal)
				return null;
			else {
				if (source instanceof Spanned) {
					SpannableString sp = new SpannableString(sb);
					try {
						TextUtils.copySpansFrom((Spanned)source, start, end, null, sp, 0);
						return sp;
					} catch (Throwable throwable) {
						Timber.e(throwable, String.format("copySpansFrom(%s, %d, %d, null, %s(%d), 0); failed", source, start, end, sp, sp.length()));
						return sb;
					}
				}
				else {
					return sb;
				}
			}
		}

		private boolean isCharAllowed(final char c) {
			return c == _decimalSeparator || ALLOWED_CHARS.indexOf(c) >= 0;
		}
	}
}
