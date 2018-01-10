package org.nem.nac.datamodel.repositories;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.nem.nac.common.enums.LastTransactionType;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.LastTransactionEntity;
import org.nem.nac.datamodel.mappers.LastTransactionMapper;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.transactions.LastTransaction;

import java.util.ArrayList;
import java.util.List;

public final class LastTransactionRepository {

	public synchronized List<LastTransaction> getAll()
			throws NacPersistenceRuntimeException {
		try {
			final List<LastTransactionEntity> entities = NemSQLiteHelper.getInstance().getAll(LastTransactionEntity.class);
			List<LastTransaction> models = new ArrayList<>(entities.size());
			for (LastTransactionEntity entity : entities) {
				models.add(LastTransactionMapper.toModel(entity));
			}
			return models;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	@NonNull
	public synchronized Optional<LastTransaction> find(final AddressValue address, final LastTransactionType type)
			throws NacPersistenceRuntimeException {
		try {
			final LastTransactionEntity entity = NemSQLiteHelper.getInstance()
					.getReadableDbCompartment()
					.query(LastTransactionEntity.class)
					.withSelection("address = ? AND type = ?", address.getRaw(), String.valueOf(type.getRaw()))
					.get();
			return Optional.ofNullable(LastTransactionMapper.toModel(entity));
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public LastTransaction save(LastTransaction model)
			throws NacPersistenceRuntimeException {
		try {
			LastTransactionEntity entity = LastTransactionMapper.toEntity(model);
			final NemSQLiteHelper.PersistentEntity<LastTransactionEntity> persistentEntity = NemSQLiteHelper.getInstance().insertOrUpdate(entity);
			model.id = persistentEntity._id;
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public void delete(long id)
			throws NacPersistenceRuntimeException {
		try {
			NemSQLiteHelper.getInstance().delete(LastTransactionEntity.class, id);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
