package org.nem.nac.common.enums;

import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.models.api.account.AccountMetaDataApiDto;

public enum AccountType {
	SIMPLE(0),
	COSIGNATORY(1),
	MULTISIG(2);

	private static AccountType[] _values = values();

	public final int id;

	AccountType(final int id) {
		this.id = id;
	}

	public static AccountType fromTypeId(final int id) {
		for (AccountType obj : _values) {
			if (obj.id == id) return obj;
		}
		throw new NacRuntimeException(String.format("Unknown AccountType found: %X", id));
	}

	public static AccountType fromAccount(AccountMetaDataApiDto account) {
		final boolean isMultisig = !account.cosignatories.isEmpty();
		final boolean isCosignatory = !account.cosignatoryOf.isEmpty();
		if (isMultisig) {
			return AccountType.MULTISIG;
		}
		if (isCosignatory) {
			return AccountType.COSIGNATORY;
		}
		return AccountType.SIMPLE;
	}
}
