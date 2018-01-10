package org.nem.nac.http;

import org.nem.nac.common.exceptions.NacRuntimeException;

public final class ResponseParsingRuntimeException extends NacRuntimeException {

	/**
	 * Indicates what object failed to parse - response model (true) or error object (false)
	 */
	public final boolean successfulResponse;

	public ResponseParsingRuntimeException(final Throwable throwable, final boolean successfulResponse) {
		super(throwable);
		this.successfulResponse = successfulResponse;
	}
}
