package org.nem.nac.datamodel.mappers;

import android.support.annotation.Nullable;

import org.nem.nac.datamodel.entities.ServerEntity;
import org.nem.nac.models.network.Port;
import org.nem.nac.models.network.Server;

public final class ServerMapper {

	@Nullable
	public static Server toModel(ServerEntity src) {
		if (null == src) {
			return null;
		}
		final Server model = new Server(src.protocol, src.host, new Port(src.port));
		model.id = src._id != null ? src._id : 0;
		return model;
	}

	@Nullable
	public static ServerEntity toEntity(Server src) {
		if (null == src) {
			return null;
		}
		return new ServerEntity(src.id, src.protocol, src.host, src.port.getValue());
	}
}
