package org.nem.nac.servers;

import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.datamodel.repositories.ServerRepository;
import org.nem.nac.models.network.Server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class ServerManager {

	private static volatile ServerManager _instance;

	public static synchronized ServerManager instance() {
		if (_instance == null) {
			_instance = new ServerManager();
		}
		return _instance;
	}

	private final ServerRepository  _repository = new ServerRepository();
	private final Map<Long, Server> _servers    = new HashMap<>();

	public ServerManager() {
		final Map<Long, Server> allById = Stream.of(_repository.getAll())
				.collect(Collectors.toMap(s -> s.id, s -> s));
		_servers.clear();
		_servers.putAll(allById);
		Timber.d("created");
	}

	public boolean hasServers() {
		return !_servers.isEmpty();
	}

	public synchronized void update(final Server server)
			throws NacRuntimeException {
		if (server.id < 1) {
			Timber.e("Unknown server, use addServer to add new");
			throw new NacRuntimeException("Unknown server, use addServer to add new");
		}
		_repository.save(server);
		reloadData();
	}

	public synchronized void remove(final long id) {
		final Server removed = _servers.remove(id);
		if (removed == null) { return; }
		_repository.delete(id);
	}

	public synchronized void removeAll(final Collection<Long> ids) {
		AssertUtils.notNull(ids);
		for (Long id : ids) {
			_servers.remove(id);
		}
		_repository.deleteAll(ids);
	}

	public synchronized void addServer(@NonNull final Server server) {
		AssertUtils.notNull(server);
		final Server saved = _repository.save(server);
		_servers.put(saved.id, server);
		Timber.d("Stored server %s", server);
	}

	@NonNull
	public synchronized Optional<Server> getById(final long id) {
		return Optional.ofNullable(_servers.get(id));
	}

	public synchronized Map<Long, Server> getAllServers() {
		return _servers;
	}

	private void reloadData() {
		final Map<Long, Server> allById = Stream.of(_repository.getAll())
				.collect(Collectors.toMap(s -> s.id, s -> s));
		_servers.clear();
		_servers.putAll(allById);
	}
}
