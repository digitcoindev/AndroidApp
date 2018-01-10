package org.nem.nac.datamodel.mappers;

import android.support.annotation.Nullable;

import org.nem.nac.common.enums.LastTransactionType;
import org.nem.nac.datamodel.entities.LastTransactionEntity;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.LastTransaction;

public final class LastTransactionMapper {

	@Nullable
	public static LastTransaction toModel(LastTransactionEntity src) {
		if (null == src || !AddressValue.isValid(src.address)) {
			return null;
		}
		final LastTransaction model = new LastTransaction(AddressValue.fromValue(src.address), LastTransactionType.fromValue(src.type));
		model.id = (src._id != null) ? src._id : 0L;
		model.transactionHash = new BinaryData(src.hash != null ? src.hash : new byte[0]);
		return model;
	}

	@Nullable
	public static LastTransactionEntity toEntity(LastTransaction src) {
		if (null == src) {
			return null;
		}
		return new LastTransactionEntity(src.id != 0 ? src.id : null, src.address.getRaw(), src.transactionHash.getRaw(), src.type);
	}
}
