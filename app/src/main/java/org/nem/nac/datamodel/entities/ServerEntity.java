package org.nem.nac.datamodel.entities;

public final class ServerEntity extends PersistentEntity {

	public String protocol;
	public String host;
	public int    port;

	@SuppressWarnings("unused")
	public ServerEntity() {}

	public ServerEntity(final long id, final String protocol, final String host, final int port) {
		this._id = id > 0 ? id : null;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}
}
