package org.nem.nac.qr;

public final class QrResult {

	public final ScanResultStatus status;
	public final String           text;

	public QrResult(final ScanResultStatus status) {
		this.status = status;
		text = null;
	}

	public QrResult(final ScanResultStatus status, final String text) {
		this.status = status;
		this.text = text;
	}
}
