package org.nem.nac.datamodel.repositories;

import com.annimon.stream.Optional;

import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.InvoiceMessageEntity;
import org.nem.nac.datamodel.mappers.InvoiceMessageMapper;
import org.nem.nac.models.InvoiceMessage;

public final class InvoiceMessageRepository {

	private static final long ID = 1L;

	public synchronized Optional<InvoiceMessage> get()
			throws NacPersistenceRuntimeException {
		try {
			final InvoiceMessageEntity entity = NemSQLiteHelper.getInstance()
					.get(InvoiceMessageEntity.class, ID);
			return Optional.ofNullable(InvoiceMessageMapper.toModel(entity));
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized InvoiceMessage save(final InvoiceMessage model)
			throws NacPersistenceRuntimeException {
		try {
			InvoiceMessageEntity entity = InvoiceMessageMapper.toEntity(model);
			entity._id = ID;
			NemSQLiteHelper.getInstance()
					.insertOrUpdate(entity);
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
