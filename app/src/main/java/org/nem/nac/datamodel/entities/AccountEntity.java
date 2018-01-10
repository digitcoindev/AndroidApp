package org.nem.nac.datamodel.entities;

public final class AccountEntity extends PersistentEntity {
	public String name;
	public byte[] privateKey;
	public String publicKey;
	public String address;
	public int    type;
	public int sortIndex;
}
