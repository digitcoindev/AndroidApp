package org.nem.nac.datamodel.repositories;

import com.annimon.stream.Optional;

import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.AppPasswordEntity;
import org.nem.nac.datamodel.mappers.AppPasswordMapper;
import org.nem.nac.models.AppPassword;

public final class AppPasswordRepository {

	private static final long ID = 1L;

	public Optional<AppPassword> get()
		throws NacPersistenceRuntimeException {
		try {
			final AppPasswordEntity entity = NemSQLiteHelper.getInstance()
				.get(AppPasswordEntity.class, ID);
			return Optional.ofNullable(AppPasswordMapper.toModel(entity));
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public AppPassword save(final AppPassword model)
		throws NacPersistenceRuntimeException {
		try {
			AppPasswordEntity entity = AppPasswordMapper.toEntity(model);
			entity._id = ID;
			NemSQLiteHelper.getInstance()
				.insertOrUpdate(entity);
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
