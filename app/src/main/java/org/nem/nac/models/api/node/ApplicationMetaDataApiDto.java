package org.nem.nac.models.api.node;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.nem.nac.common.models.TimeValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ApplicationMetaDataApiDto {
	public TimeValue currentTime;
	public String application;
	public TimeValue startTime;
	public String version;
	public String signer;
}
