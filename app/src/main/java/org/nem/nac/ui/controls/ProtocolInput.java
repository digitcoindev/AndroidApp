package org.nem.nac.ui.controls;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import org.nem.nac.R;

public final class ProtocolInput extends EditText {
	public ProtocolInput(final Context context) {
		super(context);
		init();
	}

	public ProtocolInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProtocolInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public boolean validate() {
		final String text = getText().toString().toLowerCase();
		if (!text.equals("http")) {
			setError(getContext().getString(R.string.errormessage_invalid_protocol));
			setText("http");
			return false;
		}
		return true;
	}

	private void init() {
		this.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
}
