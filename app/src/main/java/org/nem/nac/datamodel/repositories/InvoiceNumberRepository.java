package org.nem.nac.datamodel.repositories;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.InvoiceNumberEntity;
import org.nem.nac.datamodel.mappers.InvoiceNumberMapper;
import org.nem.nac.models.invoice.InvoiceNumber;

public final class InvoiceNumberRepository {

	private static final long ID = 1;

	@NonNull
	public Optional<InvoiceNumber> get()
			throws NacPersistenceRuntimeException {
		try {
			final InvoiceNumberEntity entity = NemSQLiteHelper.getInstance().get(InvoiceNumberEntity.class, ID);
			return Optional.ofNullable(InvoiceNumberMapper.toModel(entity));
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	@NonNull
	public InvoiceNumber save(final InvoiceNumber model)
			throws NacPersistenceRuntimeException {
		try {
			final InvoiceNumberEntity entity = InvoiceNumberMapper.toEntity(model);
			entity._id = ID;
			NemSQLiteHelper.getInstance()
					.insertOrUpdate(entity);
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
