package org.nem.nac.ui.controls;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.EditText;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.models.network.Port;

public class PortInput extends EditText {
	private boolean _allowEmpty = false;

	public PortInput(final Context context) {
		super(context);
	}

	public PortInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public PortInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public Port getPort()
			throws NacException {
		try {
			final String text = getText().toString();
			if (text.isEmpty()) {
				if (_allowEmpty) {
					return AppConstants.DEFAULT_PORT;
				}
				throw new NacException("Port is empty");
			}
			return new Port(NumberUtils.parseInt(getText().toString()));
		} catch (NumberFormatException e) {
			throw new NacException("Invalid port", e);
		}
	}

	public void setAllowEmpty(final boolean allowEmpty) {
		_allowEmpty = allowEmpty;
	}

	public void setPort(final int port) {
		setText(NumberUtils.toString(port));
	}

	public void setPort(@NonNull final Port port) {
		setPort(port.getValue());
	}

	public boolean isEmpty() {
		return getText().length() == 0;
	}

	public boolean validate() {
		final String text = getText().toString();
		if (!_allowEmpty && text.isEmpty()) {
			setError(getContext().getString(R.string.errormessage_port_required));
			return false;
		}
		else if (_allowEmpty && text.isEmpty()) {
			return true;
		}

		try {
			final int port = NumberUtils.parseInt(text);
			if (port > 0 && port < 65536) {
				return true;
			}
			setError(getContext().getString(R.string.errormessage_invalid_port_range));
			return false;
		} catch (NumberFormatException e) {
			setError(getContext().getString(R.string.errormessage_invalid_port_range));
			return false;
		}
	}
}
