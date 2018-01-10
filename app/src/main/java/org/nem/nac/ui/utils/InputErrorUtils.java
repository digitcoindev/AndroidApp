package org.nem.nac.ui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;

public final class InputErrorUtils {

	public static void setErrorState(final TextView input, final @Nullable @StringRes Integer msgRes, final Object... msgFormatArgs) {
		final Context context = input.getContext();
		if (input.getTag(R.id.tagkey_text_color) == null) {
			final int currentTextColor = input.getCurrentTextColor();
			input.setTag(R.id.tagkey_text_color, currentTextColor);
		}
		if (input.getTag(R.id.tagkey_hint_color) == null) {
			final int currentHintColor = input.getCurrentHintTextColor();
			input.setTag(R.id.tagkey_hint_color, currentHintColor);
		}
		final int errorColor = context.getResources().getColor(R.color.default_red);
		input.setTextColor(errorColor);
		input.setHintTextColor(errorColor);
		if (msgRes != null) {
			input.setError(context.getString(msgRes, msgFormatArgs));
		}
	}

	public static void clearErrorState(final TextView input) {
		final Object textColorTag = input.getTag(R.id.tagkey_text_color);
		final Object hintColorTag = input.getTag(R.id.tagkey_hint_color);
		//final Resources resources = input.getContext().getResources();  // NacApplication.getAppContext()
		final int textColor = (textColorTag != null && textColorTag instanceof Integer) ? (int)textColorTag :
				ContextCompat.getColor(input.getContext(), R.color.default_black);
		final int hintColor = (hintColorTag != null && hintColorTag instanceof Integer) ? (int)hintColorTag :
				ContextCompat.getColor(input.getContext(),R.color.hint_foreground_material_light);

		input.setTextColor(textColor);
		input.setHintTextColor(hintColor);
		input.setError(null);
	}

	public static class RemoveErrorWatcher implements TextWatcher {

		private EditText _input;

		public RemoveErrorWatcher(final EditText input) {
			_input = input;
		}

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

		}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

		}

		@Override
		public void afterTextChanged(final Editable s) {
			InputErrorUtils.clearErrorState(_input);
		}
	}
}
