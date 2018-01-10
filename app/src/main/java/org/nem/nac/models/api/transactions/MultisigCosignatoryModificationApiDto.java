package org.nem.nac.models.api.transactions;

import android.support.annotation.NonNull;

import org.nem.nac.common.enums.MultisigCosignatoryModificationType;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.NacPublicKey;

public final class MultisigCosignatoryModificationApiDto {
	/**
	 * The type of  modification.
	 */
	public MultisigCosignatoryModificationType modificationType;
	/**
	 * The public key of the cosignatory account as hexadecimal string
	 */
	public NacPublicKey                        cosignatoryAccount;

	public MultisigCosignatoryModificationApiDto() {
	}

	public MultisigCosignatoryModificationApiDto(
			@NonNull final MultisigCosignatoryModificationType type, @NonNull final NacPublicKey cosignatory) {
		AssertUtils.notNull(type, cosignatory);
		this.modificationType = type;
		this.cosignatoryAccount = cosignatory;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MultisigCosignatoryModificationApiDto that = (MultisigCosignatoryModificationApiDto)o;

		if (modificationType != that.modificationType) return false;
		return cosignatoryAccount.equals(that.cosignatoryAccount);
	}

	@Override
	public int hashCode() {
		int result = modificationType.hashCode();
		result = 31 * result + cosignatoryAccount.hashCode();
		return result;
	}
}
