package org.nem.nac.models;

public final class AppPassword {

	/**
	 * Password hash.
	 */
	public String     passwordHash;
	/**
	 * Application salt for deriving keys
	 */
	public BinaryData salt;

	public AppPassword(final String passwordHash, final BinaryData salt) {
		this.passwordHash = passwordHash;
		this.salt = salt;
	}
}
