package org.nem.nac.ui.input.filters;

public class HexStringIllegalCharsStrippingFilter extends BaseInputFilter {

	@Override
	protected boolean isCharAllowed(final char character) {
		return ('A' <= character && character <= 'F') || ('a' <= character && character <= 'f') || Character.isDigit(character);
	}
}
