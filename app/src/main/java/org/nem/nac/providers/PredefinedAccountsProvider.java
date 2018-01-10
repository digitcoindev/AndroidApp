package org.nem.nac.providers;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.nem.nac.BuildConfig;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.account.Account;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class PredefinedAccountsProvider {

	private static Map<Integer, Account> _accounts = new HashMap<>();

	static {
		if (BuildConfig.DEBUG) {

			try {
				final Account account =
						new Account("Alice", new NacPrivateKey("c7a10487f0c2be5cf691b42864e13be95d172f67aa1c8e018932ce09f700d962"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(1, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 1");
			}

			try {
				final Account account =
						new Account("Ljubomyr", new NacPrivateKey("5ccf739d9f40f981e100492632cf729ae7940980e677551684f4f309bac5c59d"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(2, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 2");
			}
			// Test Al
			try {
				final Account account =
						new Account("TestAl", new NacPrivateKey("3fc5a9cbc2b454edd716b6b98c8c015c133f569ab68ce7c339ad2ff581c4de94"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(3, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 3");
			}

			// Msig
			try {
				final Account account =
						new Account("Msig", new NacPrivateKey("282d1f064295b53ca36dd6a290fb8ab558aa1a101b04f3cf22c2d27bda652c5d"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(4, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 4");
			}
			// Cosig 1
			try {
				final Account account =
						new Account("Cosig 1", new NacPrivateKey("73f420543db15fb7969f86d28e0988f62f4b8afccbf3d08f47dbf0e025b93a61"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(5, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 5");
			}
			// Cosig 2
			try {
				final Account account =
						new Account("Cosig 2", new NacPrivateKey("245b491baaeab915564515c8c6c97d6a791c6977e942813ab1c183c59c57c151"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(6, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 6");
			}
			// Cosig 3
			try {
				final Account account =
						new Account("Cosig 3", new NacPrivateKey("4246fd7c0fff073caf238a716a72b44d25ee4631533b321e5e1353c9c7c0f7bb"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(7, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 7");
			}
			// Test msig
			try {
				final Account account =
						new Account("Test msig", new NacPrivateKey("73939c9834e0696791b4a5ab58988b4722f144bcae30f86132d79bfd1512a62e"), EKeyProvider
								.instance().getKey().get());
				_accounts.put(10, account);
			} catch (NacCryptoException e) {
				Timber.d(e, "Failed to add account 10");
			}
		}
	}

	public static boolean contains(final int index) {
		return _accounts.containsKey(index);
	}

	@NonNull
	public static Optional<Account> get(final int index) {
		if (BuildConfig.DEBUG) {
			return Optional.ofNullable(_accounts.get(index));
		}
		return Optional.empty();
	}
}
