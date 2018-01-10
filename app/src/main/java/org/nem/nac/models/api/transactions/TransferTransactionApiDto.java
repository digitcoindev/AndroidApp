package org.nem.nac.models.api.transactions;

import android.support.annotation.Nullable;

import org.nem.nac.models.Xems;
import org.nem.nac.models.api.MessageApiDto;
import org.nem.nac.models.primitives.AddressValue;

public final class TransferTransactionApiDto extends AbstractTransactionApiDto {
	/**
	 * The amount of XEM that is transferred from sender to recipient.
	 */
	public Xems          amount;
	/**
	 * The transaction signature.
	 */
	public String        signature;
	/**
	 * The address of the recipient.
	 */
	public AddressValue  recipient;
	/**
	 * Optionally a transaction can contain a message. In this case the transaction contains a message substructure. If not the field is null.
	 */
	@Nullable
	public MessageApiDto message;

	public AddressValue getCompanion(final AddressValue address) {
		if (isSigner(address)) {
			return recipient;
		}
		else {
			return AddressValue.fromPublicKey(signer);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		TransferTransactionApiDto that = (TransferTransactionApiDto)o;

		if (amount != null ? !amount.equals(that.amount) : that.amount != null) { return false; }
		if (recipient != null ? !recipient.equals(that.recipient) : that.recipient != null) { return false; }
		return !(message != null ? !message.equals(that.message) : that.message != null);
	}

	@Override
	public int hashCode() {
		int result = amount != null ? amount.hashCode() : 0;
		result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
		result = 31 * result + (message != null ? message.hashCode() : 0);
		return result;
	}

	public boolean hasMessage() {
		return message != null && message.payload != null && message.type != null;
	}
}
