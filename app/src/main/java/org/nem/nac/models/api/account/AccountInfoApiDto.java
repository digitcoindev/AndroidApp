package org.nem.nac.models.api.account;

import android.support.annotation.Nullable;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.account.MultisigInfo;
import org.nem.nac.models.primitives.AddressValue;

public final class AccountInfoApiDto {
	public AddressValue address;
	public NacPublicKey publicKey;
	/**
	 * Always null according to specs.
	 */
	@Nullable
	public String       label;
	public Xems         balance;
	public Xems vestedBalance;
	public double       importance;
	public long         harvestedBlocks;
	@Nullable
	public MultisigInfo multisigInfo;

	@Override
	public String toString() {
		if(publicKey == null) {
			return NacApplication.getResString(R.string.errormessage_no_public_key);
		}
		return publicKey.toString();
	}
}
