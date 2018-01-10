package org.nem.nac.models.api;

import org.nem.nac.models.SignedBinaryData;

public final class RequestAnnounceApiDto {
	/**
	 * Data in HEX string format
	 */
	public String data;
	/**
	 * Signature in HEX string format
	 */
	public String signature;

	public RequestAnnounceApiDto() {
	}

	public RequestAnnounceApiDto(final SignedBinaryData signedBinaryData) {
		data = signedBinaryData.toHexStr();
		signature = signedBinaryData.getSignatureHex();
	}

	public RequestAnnounceApiDto(final String data, final String signature) {
		this.data = data;
		this.signature = signature;
	}
}
