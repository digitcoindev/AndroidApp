package org.nem.nac.datamodel.mappers;

import android.support.annotation.Nullable;

import org.nem.nac.datamodel.entities.InvoiceNumberEntity;
import org.nem.nac.models.invoice.InvoiceNumber;

public class InvoiceNumberMapper {

	@Nullable
	public static InvoiceNumber toModel(InvoiceNumberEntity src) {
		if (null == src) {
			return null;
		}
		return new InvoiceNumber(src.lastStored);
	}

	@Nullable
	public static InvoiceNumberEntity toEntity(InvoiceNumber src) {
		if (null == src) {
			return null;
		}
		return new InvoiceNumberEntity(src.lastStored);
	}
}
