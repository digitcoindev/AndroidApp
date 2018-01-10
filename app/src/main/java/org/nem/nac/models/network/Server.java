package org.nem.nac.models.network;

import org.nem.nac.application.AppConstants;

/**
 * NEM Infrastructure Server
 */
public final class Server {

	public       long   id;
	public String protocol;
	public String host;
	public Port   port;

	public Server(String protocol, String host, Port port) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Server server = (Server)o;

		if (protocol != null ? !protocol.equalsIgnoreCase(server.protocol) : server.protocol != null) { return false; }
		if (host != null ? !host.equalsIgnoreCase(server.host) : server.host != null) { return false; }
		return !(port != null ? !port.equals(server.port) : server.port != null);
	}

	@Override
	public int hashCode() {
		int result = protocol != null ? protocol.hashCode() : 0;
		result = 31 * result + (host != null ? host.hashCode() : 0);
		result = 31 * result + (port != null ? port.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		final boolean defaultProtocol = AppConstants.PREDEFINED_PROTOCOL.equalsIgnoreCase(protocol);
		final boolean defaultPort = AppConstants.DEFAULT_PORT.equals(port);
		return String.format("%s%s%s", defaultProtocol ? "" : protocol + "://", host, defaultPort ? "" : ":" + port);
	}
}
