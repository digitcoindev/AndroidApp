package org.nem.nac.models.api.node;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.nem.nac.models.api.EndpointApiDto;

public final class NodeApiDto {

	public MetaData       metaData;
	public EndpointApiDto endpoint;
	public Identity       identity;

	public static class MetaData {

		public int features;
		public int networkId;
		public String application;
		public String version;
		public String platform;
	}

	public static class Identity {

		public String name;
		@JsonProperty("public-key")
		public String publicKey;
	}
}
