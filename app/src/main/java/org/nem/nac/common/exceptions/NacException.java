package org.nem.nac.common.exceptions;

public class NacException extends Exception {
	public NacException() {
	}

	public NacException(String detailMessage) {
		super(detailMessage);
	}

	public NacException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NacException(Throwable throwable) {
		super(throwable);
	}
}
