package org.nem.nac.datamodel.repositories;

import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.ServerEntity;
import org.nem.nac.datamodel.mappers.ServerMapper;
import org.nem.nac.models.network.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ServerRepository {

	public synchronized boolean any()
			throws NacPersistenceRuntimeException {
		try {
			return NemSQLiteHelper.getInstance()
					.getReadableDbCompartment()
					.query(ServerEntity.class)
					.getCursor()
					.getCount() > 0;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized List<Server> getAll()
			throws NacPersistenceRuntimeException {
		try {
			final List<ServerEntity> entities = NemSQLiteHelper.getInstance().getAll(ServerEntity.class);
			List<Server> models = new ArrayList<>(entities.size());
			for (ServerEntity entity : entities) {
				models.add(ServerMapper.toModel(entity));
			}
			return models;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized Server get(long id)
			throws NacPersistenceRuntimeException {
		try {
			final ServerEntity entity = NemSQLiteHelper.getInstance().get(ServerEntity.class, id);
			return ServerMapper.toModel(entity);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized Server save(Server model)
			throws NacPersistenceRuntimeException {
		try {
			ServerEntity entity = ServerMapper.toEntity(model);
			final NemSQLiteHelper.PersistentEntity<ServerEntity> persistentEntity = NemSQLiteHelper.getInstance()
					.insertOrUpdate(entity);
			model.id = persistentEntity._id;
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized void delete(long id)
			throws NacPersistenceRuntimeException {
		try {
			NemSQLiteHelper.getInstance().delete(ServerEntity.class, id);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized void deleteAll(final Collection<Long> ids)
			throws NacPersistenceRuntimeException {
		try {
			NemSQLiteHelper.getInstance().deleteAll(ServerEntity.class, ids);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
