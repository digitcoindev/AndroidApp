package org.nem.nac.models.api;

import org.nem.nac.application.AppConstants;
import org.nem.nac.models.network.Port;

public final class EndpointApiDto {

	public String protocol;
	public String host;
	public Port   port;

	@Override
	public String toString() {
		final boolean defaultProtocol = AppConstants.PREDEFINED_PROTOCOL.equalsIgnoreCase(protocol);
		final boolean defaultPort = AppConstants.DEFAULT_PORT.equals(port);
		return String.format("%s%s%s", defaultProtocol ? "" : protocol + "://", host, defaultPort ? "" : ":" + port);
	}
}
