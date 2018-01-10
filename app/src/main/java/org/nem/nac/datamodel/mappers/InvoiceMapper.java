package org.nem.nac.datamodel.mappers;

import org.nem.nac.datamodel.entities.InvoiceEntity;
import org.nem.nac.models.Invoice;
import org.nem.nac.models.Xems;
import org.nem.nac.models.primitives.AddressValue;

public final class InvoiceMapper {
	public static Invoice toModel(InvoiceEntity src) {
		if (null == src) {
			return null;
		}
		Invoice dst = new Invoice();
		dst.id = (src._id != null) ? src._id : 0L;
		dst.accountId = src.accountId;
		dst.name = src.name;
		dst.address = new AddressValue(src.address);
		dst.amount = Xems.fromMicro(src.amount);
		dst.message = src.message;
		return dst;
	}

	public static InvoiceEntity toEntity(Invoice src) {
		if (null == src) {
			return null;
		}
		InvoiceEntity dst = new InvoiceEntity();
		dst._id = (src.id != 0) ? src.id : null;
		dst.accountId = src.accountId;
		dst.name = src.name;
		dst.address = src.address.getRaw();
		dst.amount = src.amount.getAsMicro();
		dst.message = src.message;
		return dst;
	}
}
