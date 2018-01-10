package org.nem.nac.common.exceptions;

public class NacRuntimeException extends RuntimeException {
	@SuppressWarnings("unused")
	public NacRuntimeException() {
	}

	@SuppressWarnings("unused")
	public NacRuntimeException(String detailMessage) {
		super(detailMessage);
	}

	@SuppressWarnings("unused")
	public NacRuntimeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	@SuppressWarnings("unused")
	public NacRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
