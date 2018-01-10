package org.nem.nac.datamodel.mappers;

import android.support.annotation.Nullable;

import org.nem.nac.datamodel.entities.InvoiceMessageEntity;
import org.nem.nac.models.InvoiceMessage;

public final class InvoiceMessageMapper {
	@Nullable
	public static InvoiceMessage toModel(InvoiceMessageEntity src) {
		if (null == src) {
			return null;
		}

		return new InvoiceMessage(src.prefix, src.postfix, src.message);
	}

	@Nullable
	public static InvoiceMessageEntity toEntity(InvoiceMessage src) {
		if (null == src) {
			return null;
		}
		return new InvoiceMessageEntity(src.prefix, src.postfix, src.message);
	}
}
