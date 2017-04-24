package org.nem.nac.ui.input.filters;

public class AddressIllegalCharsStrippingFilter extends BaseInputFilter {

	@Override
	protected boolean isCharAllowed(final char character) {
		//final int codePoint = ((int)character);
		return ('A' <= character && character <= 'Z') || ('a' <= character && character <= 'z') || ('0' <= character && character <= '9');
	}
}
