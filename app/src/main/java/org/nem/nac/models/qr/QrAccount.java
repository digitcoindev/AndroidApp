package org.nem.nac.models.qr;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;

public final class QrAccount extends BaseQrData {

	@JsonProperty("name")
	public final String name;

	@JsonProperty("priv_key")
	public final EncryptedNacPrivateKey privateKey;

	@JsonProperty("salt")
	public final BinaryData salt;

	@SuppressWarnings("unused")
	public QrAccount() {
		name = null;
		privateKey = null;
		salt = null;
	}

	public QrAccount(final String name, final EncryptedNacPrivateKey privateKey, final BinaryData salt) {
		this.name = name;
		this.salt = salt;
		this.privateKey = privateKey;
	}

	@Override
	public boolean validate() {
		return StringUtils.isNotNullOrEmpty(name)
			&& privateKey != null && privateKey.length() > 0
			&& salt != null && salt.length() > 0;
	}

	@Override
	public String toString() {
		if (!validate()) {
			return "Invalid!";
		}
		final String pkStr = privateKey.toString();
		final String saltStr = salt.toHexStr();
		return String.format("Name: %s, Key: %s..., Salt: %s...",
				name, pkStr.substring(0, Math.min(4, pkStr.length())), saltStr.substring(0, Math.min(4, saltStr.length())));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		QrAccount qrAccount = (QrAccount)o;

		if (name != null ? !name.equals(qrAccount.name) : qrAccount.name != null) { return false; }
		if (privateKey != null ? !privateKey.equals(qrAccount.privateKey) : qrAccount.privateKey != null) { return false; }
		return !(salt != null ? !salt.equals(qrAccount.salt) : qrAccount.salt != null);
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (privateKey != null ? privateKey.hashCode() : 0);
		result = 31 * result + (salt != null ? salt.hashCode() : 0);
		return result;
	}
}
