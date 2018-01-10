package org.nem.nac.datamodel.mappers;

import android.support.annotation.Nullable;

import org.nem.nac.common.enums.AccountType;
import org.nem.nac.datamodel.entities.AccountEntity;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.account.PublicAccountData;
import org.nem.nac.models.primitives.AddressValue;

public final class AccountMapper {

	@Nullable
	public static Account toModel(AccountEntity src) {
		if (null == src) {
			return null;
		}
		final NacPublicKey publicKey = new NacPublicKey(src.publicKey);
		final AddressValue address = new AddressValue(src.address);
		final PublicAccountData pubData = new PublicAccountData(publicKey, address);
		final Account dst = new Account(pubData);
		dst.id = (src._id != null) ? src._id : 0L;
		dst.name = src.name;
		dst.privateKey = new EncryptedNacPrivateKey(src.privateKey);
		dst.type = AccountType.fromTypeId(src.type);
		dst.sortIndex = src.sortIndex;
		return dst;
	}

	@Nullable
	public static AccountEntity toEntity(Account src) {
		if (null == src) {
			return null;
		}
		final AccountEntity dst = new AccountEntity();
		dst._id = (src.id != 0) ? src.id : null;
		dst.name = src.name;
		dst.privateKey = src.privateKey.getRaw();
		dst.publicKey = src.publicData.publicKey.toHexStr();
		dst.address = src.publicData.address.getRaw();
		dst.type = src.type.id;
		dst.sortIndex = src.sortIndex;
		return dst;
	}
}
