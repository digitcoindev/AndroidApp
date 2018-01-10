package org.nem.nac.models.transactions;

import org.nem.nac.common.enums.LastTransactionType;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.primitives.AddressValue;

public final class LastTransaction {

	public       long                id;
	public final AddressValue        address;
	public       BinaryData          transactionHash;
	public final LastTransactionType type;

	public LastTransaction(final AddressValue address, LastTransactionType type) {
		this.address = address;
		this.type = type;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		LastTransaction that = (LastTransaction)o;

		if (!address.equals(that.address)) { return false; }
		if (transactionHash != null ? !transactionHash.equals(that.transactionHash) : that.transactionHash != null) { return false; }
		return type == that.type;
	}

	@Override
	public int hashCode() {
		int result = address.hashCode();
		result = 31 * result + (transactionHash != null ? transactionHash.hashCode() : 0);
		result = 31 * result + type.hashCode();
		return result;
	}
}
