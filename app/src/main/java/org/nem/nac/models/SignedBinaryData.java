package org.nem.nac.models;

import android.support.annotation.NonNull;

import org.nem.core.crypto.DsaSigner;
import org.nem.core.utils.HexEncoder;

/**
 * Class represents immutable binary data that can be signed.
 */
public final class SignedBinaryData extends BinaryData {
	private final byte[] _signature;

	public SignedBinaryData(@NonNull final byte[] rawData, @NonNull final DsaSigner signer) {
		super(rawData);
		_signature = signer.sign(rawData).getBytes();
	}

	@NonNull
	public byte[] getSignature() {
		return _signature;
	}

	@NonNull
	public String getSignatureHex() {
		return HexEncoder.getString(_signature);
	}
}
