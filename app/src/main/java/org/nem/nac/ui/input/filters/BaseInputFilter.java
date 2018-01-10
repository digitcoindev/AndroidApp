package org.nem.nac.ui.input.filters;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

public abstract class BaseInputFilter implements InputFilter {

	@Override
	public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest, final int dstart, final int dend) {

		if (source instanceof SpannableStringBuilder) {
			SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
			for (int i = end - 1; i >= start; i--) {
				char currentChar = source.charAt(i);
				if (!isCharAllowed(currentChar)) {
					sourceAsSpannableBuilder.delete(i, i + 1);
				}
			}
			return source;
		}
		else {
			StringBuilder filteredStringBuilder = new StringBuilder();
			for (int i = start; i < end; i++) {
				char currentChar = source.charAt(i);
				if (isCharAllowed(currentChar)) {
					filteredStringBuilder.append(currentChar);
				}
			}
			return filteredStringBuilder.toString();
		}
	}

	protected abstract boolean isCharAllowed(final char character);
}
