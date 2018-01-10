package org.nem.nac.ui.controls;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import org.nem.nac.R;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.NumberUtils;

public final class MinCosignatoriesInput extends EditText {
	private boolean _allowEmpty = false;

	public MinCosignatoriesInput(final Context context) {
		super(context);
		init();
	}

	public MinCosignatoriesInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MinCosignatoriesInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setAllowEmpty(final boolean value) {
		_allowEmpty = value;
	}

	public boolean isEmpty() {
		return getText().toString().isEmpty();
	}

	public boolean validate() {
		final String text = getText().toString();
		if (_allowEmpty && text.isEmpty()) {
			return true;
		}
		try {
			final int num = NumberUtils.parseInt(text);
			if (!_allowEmpty && num < 1) {
				setError(getContext().getString(R.string.errormessage_invalid_min_cosignatories_number));
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			setError(getContext().getString(R.string.errormessage_error_occured));
			return false;
		}
	}

	public int getMinCosignatories()
			throws NacException {
		final String text = getText().toString();
		if (!validate()) {
			throw new NacException("Invalid number");
		}
		try {
			return NumberUtils.parseInt(text);
		} catch (NumberFormatException e) {
			throw new NacException("Failed to parse \"" + text + "\" to number", e);
		}
	}

	private void init() {
		setRawInputType(InputType.TYPE_CLASS_NUMBER);
	}
}
