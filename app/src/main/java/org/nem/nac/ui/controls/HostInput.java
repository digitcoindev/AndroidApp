package org.nem.nac.ui.controls;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;

import java.net.IDN;
import java.util.regex.Pattern;

public final class HostInput extends EditText {
	private static final Pattern IP;
	private static final Pattern HOSTNAME;

	static {
		IP = Pattern.compile(AppConstants.REGEX_IP_ADDRESS);
		HOSTNAME = Pattern.compile(AppConstants.REGEX_HOSTNAME);
	}

	public HostInput(final Context context) {
		super(context);
		init();
	}

	public HostInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public HostInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public boolean validate() {
		final String text = getText().toString();
		if (text.isEmpty()) {
			setError(getContext().getString(R.string.errormessage_host_empty));
			return false;
		}
		// valid ip
		if (IP.matcher(text).matches()) {
			return true;
		}
		// valid host
		final String asciiHost = IDN.toASCII(text);
		if (!HOSTNAME.matcher(asciiHost).matches()) {
			setError(getContext().getString(R.string.errormessage_invalid_hostname));
			return false;
		}
		return true;
	}

	private void init() {
		this.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
}
