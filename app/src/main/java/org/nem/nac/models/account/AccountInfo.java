package org.nem.nac.models.account;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.api.account.AccountInfoApiDto;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;

import java.util.LinkedList;
import java.util.List;

public final class AccountInfo {
	public final AddressValue address;
	@Nullable
	public final NacPublicKey publicKey;
	public final Xems         balance;
	public final List<AccountInfo> cosignatories = new LinkedList<>();
	public final List<AccountInfo> cosignatoryOf = new LinkedList<>();
	@Nullable
	public final MultisigInfo multisigInfo;

	public AccountInfo(@NonNull final AddressValue address,@Nullable final NacPublicKey publicKey, final Xems balance, @Nullable final MultisigInfo multisigInfo) {
		this.address = address;
		this.publicKey = publicKey;
		this.balance = balance;
		this.multisigInfo = multisigInfo;
	}

	public AccountType getType() {
		final boolean isMultisig = !cosignatories.isEmpty();
		final boolean isCosignatory = !cosignatoryOf.isEmpty();
		if (isMultisig) {
			return AccountType.MULTISIG;
		}
		if (isCosignatory) {
			return AccountType.COSIGNATORY;
		}
		return AccountType.SIMPLE;
	}

	public static AccountInfo fromMetadataPairApiDto(@NonNull final AccountMetaDataPairApiDto dto) {
		AssertUtils.notNull(dto);
		final AccountInfo accountInfo = fromApiDto(dto.account);
		for (AccountInfoApiDto cosignatory : dto.meta.cosignatories) {
			accountInfo.cosignatories.add(fromApiDto(cosignatory));
		}
		for (AccountInfoApiDto cosignatory : dto.meta.cosignatoryOf) {
			accountInfo.cosignatoryOf.add(fromApiDto(cosignatory));
		}
		return accountInfo;
	}

	public static AccountInfo fromApiDto(@NonNull final AccountInfoApiDto dto) {
		return new AccountInfo(dto.address, dto.publicKey, dto.balance, dto.multisigInfo);
	}

	@Override
	public String toString() {
		return String.format("%s/%s", address, publicKey);
	}
}
