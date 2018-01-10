package org.nem.nac.ui.controls;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import com.annimon.stream.Optional;

import org.nem.core.crypto.CryptoException;
import org.nem.core.crypto.PrivateKey;
import org.nem.nac.R;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.ui.input.filters.HexStringIllegalCharsStrippingFilter;

public final class PrivateKeyInput extends EditText {

	public PrivateKeyInput(final Context context) {
		super(context);
		init();
	}

	public PrivateKeyInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PrivateKeyInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public boolean validate() {
		final String pKey = getText().toString();
		try {
			if (pKey.isEmpty()) {
				setError(getContext().getString(R.string.errortext_invalid_private_key));
				return false;
			}
			PrivateKey.fromHexString(pKey);
			return true;
		} catch (CryptoException e) {
			setError(getContext().getString(R.string.errortext_invalid_private_key));
			return false;
		}
	}

	public Optional<NacPrivateKey> getPrivateKey() {
		String text = getText().toString();
		try {
			final PrivateKey privateKey = PrivateKey.fromHexString(text);
			byte[] bytes = privateKey.getRaw().toByteArray();
			return Optional.of(new NacPrivateKey(bytes));
		} catch (CryptoException e) {
			return Optional.empty();
		}
	}

	private void init() {
		this.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		this.setFilters(new InputFilter[] { new HexStringIllegalCharsStrippingFilter() });
	}
}
