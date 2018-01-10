package org.nem.nac.datamodel.entities;

import org.nem.nac.models.BinaryData;

public final class AppPasswordEntity extends PersistentEntity {
	public String passwordHash;
	public byte[] salt;

	public AppPasswordEntity() { }

	public AppPasswordEntity(final String passwordHash, final BinaryData salt) {
		this.passwordHash = passwordHash;
		this.salt = salt.getRaw();
	}
}
