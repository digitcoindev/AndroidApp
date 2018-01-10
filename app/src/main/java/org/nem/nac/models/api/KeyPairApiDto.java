package org.nem.nac.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class KeyPairApiDto {
	public String privateKey;
	public String publicKey;
	public String address;
}
