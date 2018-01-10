package org.nem.nac.tasks;

import com.annimon.stream.Optional;

import org.nem.nac.BuildConfig;
import org.nem.nac.R;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.providers.PredefinedAccountsProvider;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class AddAccountTask extends BaseAsyncTask<AddAccountTask, NacPrivateKey, Void, Account> {

	private final String _name;
	private volatile boolean _alreadyExists = false;

	public AddAccountTask(final String name) {
		_name = name;
	}

	public AddAccountTask(final NacBaseActivity activity, final String name) {
		super(activity, R.string.progress_dialog_wait_message);
		_name = name;
	}

	@Override
	protected Account doInBackground(final NacPrivateKey... params) {
		_alreadyExists = false;
		// Predefined
		// todo: remove this, testing only
		final AccountRepository repository = new AccountRepository();
		if (BuildConfig.DEBUG) {
			final int predefinedIndex = parsePredefinedIndex(_name);
			if (predefinedIndex > -1) {
				final Optional<Account> predefined = PredefinedAccountsProvider.get(predefinedIndex);
				if (predefined.isPresent()) {
					return repository.save(predefined.get());
				}
			}
		}
		try {
			final NacPrivateKey privateKey = params[0];
			if (repository.find(NacPublicKey.fromPrivateKey(privateKey)).isPresent()) {
				_alreadyExists = true;
				return null;
			}
			final Optional<BinaryData> eKey = EKeyProvider.instance().getKey();
			if (!eKey.isPresent()) {
				Timber.e("Key was not present!");
				return null;
			}
			final Account newAccount = new Account(_name, privateKey, eKey.get());
			return repository.save(newAccount);
		} catch (NacException e) {
			Timber.e(e, "Failed to create account: %s", e.getMessage());
			return null;
		}
	}

	@Override
	protected void onPostExecute(final Account account) {
		if (account == null) {
			if (_alreadyExists) { Toaster.instance().show(R.string.errormessage_account_already_exists); }
			else { Toaster.instance().show(R.string.errormessage_account_creation_failed); }
		}
		super.onPostExecute(account);
	}

	private int parsePredefinedIndex(final String name) {
		if (name.length() < 2 || !name.startsWith("-")) {
			return -1;
		}
		try {
			final int index = NumberUtils.parseInt(name.substring(1));
			return PredefinedAccountsProvider.contains(index) ? index : -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
