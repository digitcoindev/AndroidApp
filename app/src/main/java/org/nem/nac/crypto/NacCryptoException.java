package org.nem.nac.crypto;

import org.nem.nac.common.exceptions.NacException;

public class NacCryptoException extends NacException {
	public NacCryptoException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
