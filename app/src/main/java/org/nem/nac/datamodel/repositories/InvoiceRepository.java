package org.nem.nac.datamodel.repositories;

import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.InvoiceEntity;
import org.nem.nac.datamodel.mappers.InvoiceMapper;
import org.nem.nac.models.Invoice;

public final class InvoiceRepository {
	public boolean any() throws NacPersistenceRuntimeException {
		try {
			return NemSQLiteHelper.getInstance().getReadableDbCompartment()
					.query(InvoiceEntity.class).getCursor().getCount() > 0;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public Invoice get(long id) throws NacPersistenceRuntimeException {
		try {
			final InvoiceEntity entity = NemSQLiteHelper.getInstance().get(InvoiceEntity.class, id);
			return InvoiceMapper.toModel(entity);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	/**
	 * Adds new or updates existing invoice
	 */
	public Invoice save(Invoice model) throws NacPersistenceRuntimeException {
		try {
			InvoiceEntity entity = InvoiceMapper.toEntity(model);
			final NemSQLiteHelper.PersistentEntity<InvoiceEntity> persistentEntity = NemSQLiteHelper.getInstance().insertOrUpdate(entity);
			model.id = persistentEntity._id;
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public void delete(long id) throws NacPersistenceRuntimeException {
		try {
			NemSQLiteHelper.getInstance().delete(InvoiceEntity.class, id);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
