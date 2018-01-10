package org.nem.nac.datamodel;

import org.nem.nac.common.exceptions.NacRuntimeException;

public final class NacPersistenceRuntimeException extends NacRuntimeException {

	public NacPersistenceRuntimeException(final String detailMessage, final Throwable throwable) {
		super(detailMessage, throwable);
	}
}
