package org.nem.nac.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.nem.nac.common.models.TimeValue;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("")
public final class ErrorObjectApiDto {
	/**
	 * The number of seconds elapsed since the creation of the nemesis block.
	 */
	public TimeValue timestamp;
	/**
	 * The general description of the error.
	 */
	public String    error;
	/**
	 * The detailed error message.
	 */
	public String    message;
	/**
	 * The HTTP status.
	 */
	public int       status;

	@Override
	public String toString() {
		return String.format("%s: %d/%s - %s", timestamp, status, error, message);
	}
}
