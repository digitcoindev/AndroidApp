package org.nem.nac.models;

import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.http.ServerErrorException;

/**
 * @deprecated Use {@link ServerErrorException} instead.
 */
@Deprecated
public final class ApiError {
	public final int    httpStatus;
	public final String error;
	public final String description;

	public ApiError(final int status, final String error, final String description) {
		this.httpStatus = status;
		this.error = error;
		this.description = description;
	}

	/**
	 * Returns readable error.
	 */
	@Override
	public String toString() {
		return String.format(StringUtils.isNotNullOrEmpty(description)
				? "%s:\n%s"
				: "%s", error, description.replace('_', ' '));
	}
}
