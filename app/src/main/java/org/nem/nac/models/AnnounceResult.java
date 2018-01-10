package org.nem.nac.models;

import android.support.annotation.Nullable;

import org.nem.nac.common.exceptions.NacException;

public final class AnnounceResult {
	public final boolean      successful;
	@Nullable
	public       String       message;
	@Nullable
	public       NacException exception;

	public AnnounceResult(final boolean successful, @Nullable final String message, @Nullable final NacException exception) {
		this.successful = successful;
		this.message = message;
		this.exception = exception;
	}
}
