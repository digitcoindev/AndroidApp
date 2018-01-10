package org.nem.nac.models;

import android.support.annotation.NonNull;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;

import static org.nem.nac.helpers.Ed25519Helper.getEncryptedMessageLength;

public final class InvoiceMessage {

	@NonNull
	public final String separator = NacApplication.getResString(R.string.invoice_number_separator); // don't cache resources static - it can differ by locale
	@NonNull
	public final String prefix, postfix, message;

	public InvoiceMessage(final String prefix, final String postfix, final String message) {
		this.prefix = prefix != null ? prefix : "";
		this.postfix = postfix != null ? postfix : "";
		this.message = message != null ? message : "";
	}

	public boolean hasMessage() {
		return !message.isEmpty();
	}

	public boolean hasPrefix() {
		return !prefix.isEmpty();
	}

	public boolean hasPostfix() {
		return !postfix.isEmpty();
	}

	/**
	 * Returns overall encrypted invoice message length in bytes count.
	 *
	 * @param number Pass invoice number here for calculation
	 */
	public int getEncryptedBytesLength(final int number) {
		return getEncryptedMessageLength(getReadable(number));
	}

	/**
	 * Returns message with invoice number.
	 *
	 * @param number Pass invoice number here
	 */
	public String getReadable(final int number) {
		final StringBuilder msg = new StringBuilder();
		if (hasPrefix()) {
			msg.append(prefix).append(separator);
		}
		msg.append(number);
		if (hasPostfix()) {
			msg.append(separator).append(postfix);
		}

		msg.append(": ");

		if (hasMessage()) {
			msg.append(message);
		}

		return msg.toString();
	}

	@Override
	public int hashCode() {
		int result = separator.hashCode();
		result = 31 * result + prefix.hashCode();
		result = 31 * result + postfix.hashCode();
		result = 31 * result + message.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		InvoiceMessage that = (InvoiceMessage)o;

		if (!separator.equals(that.separator)) { return false; }
		if (!prefix.equals(that.prefix)) { return false; }
		if (!postfix.equals(that.postfix)) { return false; }
		return message.equals(that.message);
	}
}
