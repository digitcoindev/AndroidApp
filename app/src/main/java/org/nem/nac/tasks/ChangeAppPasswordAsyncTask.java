package org.nem.nac.tasks;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.crypto.KeyProvider;
import org.nem.nac.crypto.Mcrypto;
import org.nem.nac.crypto.PasswordHasher;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.datamodel.repositories.AppPasswordRepository;
import org.nem.nac.models.AppPassword;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.util.List;

import timber.log.Timber;

public final class ChangeAppPasswordAsyncTask extends BaseAsyncTask<ChangeAppPasswordAsyncTask, Void, Void, Boolean> {

	private       Optional<AppPassword> _appPassword;
	private final boolean               _forceRelogin;
	private       String                _newPwd;

	/**
	 * @param forceRelogin If true, e-key will be cleared. This should lead to Login activity opening.
	 *                     If false, e-key will be reset to reflect new password.
	 */
	public ChangeAppPasswordAsyncTask(final NacBaseActivity activity, final Optional<AppPassword> appPassword, final String newPassword,
			final boolean forceRelogin) {
		super(activity, R.string.progress_dialog_wait_message);

		_appPassword = appPassword;
		_forceRelogin = forceRelogin;
		_newPwd = newPassword;
	}

	@Override
	protected Boolean doInBackground(final Void... params) {
		// Assume new password inputs are validated here.
		if (_appPassword.isPresent()) {
			// 1. Pwd should be already validated.
			// 2. decrypt acc keys
			// 3. encrypt keys with new pwd
			// 4. if all ok - store keys and new password back
			try {
				changePassword(_newPwd);
				backupPW(_newPwd);
				return true;
			} catch (NacException e) {
				Timber.e(e, "Failed to change password!");
				Toaster.instance().show(R.string.errormessage_failed_to_change_password, Toaster.Length.LONG);
			}
			return false;
		}
		else { // First set password
			// 1. If any account present - throw error.
			// 2. Save new app password.
			if(new AccountRepository().any()) {
				Timber.wtf("Tried to set new password while already has accounts!");
				return false;
			}
			NemSQLiteHelper sqLiteHelper = null;
			try {
				final String hash = PasswordHasher.hash(_newPwd);
				sqLiteHelper = NemSQLiteHelper.getInstance();
				sqLiteHelper.beginTransaction();
				final BinaryData salt = KeyProvider.generateSalt();
				new AppPasswordRepository().save(new AppPassword(hash, salt));
				if(!_forceRelogin) {
					Timber.d("Not forcing relogin");
					final BinaryData newEkey = KeyProvider.deriveKey(_newPwd, salt);
					EKeyProvider.instance().setKey(newEkey);
				}else {
					Timber.d("Forcing relogin");
					EKeyProvider.instance().setKey(null);
				}
				sqLiteHelper.commitTransaction();
				backupPW(_newPwd);
				return true;
			} catch(Throwable throwable) {
				Timber.e(throwable, "Failed to change password");
				return false;
			}
			finally {
				if(sqLiteHelper != null) { sqLiteHelper.endTransaction(); }
			}
		}
	}

	private void changePassword(final String newPwd)
			throws NacException {
		AssertUtils.isTrue(newPwd != null && !newPwd.isEmpty());

		final NemSQLiteHelper sqLiteHelper = NemSQLiteHelper.getInstance();
		final AccountRepository accountRepository = new AccountRepository();
		final List<Account> accounts = accountRepository.getAllSorted();
		final Optional<BinaryData> eKeyOptional = EKeyProvider.instance().getKey();
		if (!eKeyOptional.isPresent()) {
			throw new NacException("Failed to change password!");
		}
		final BinaryData eKey = eKeyOptional.get();
		final BinaryData newSalt = KeyProvider.generateSalt();
		final BinaryData newEkey = KeyProvider.deriveKey(newPwd, newSalt);

		for (final Account acc : accounts) {
			final NacPrivateKey pKey = acc.privateKey.decryptKey(eKey);
			acc.privateKey = pKey.encryptKey(newEkey);
		}
		Timber.d("Keys encrypted");

		sqLiteHelper.beginTransaction();
		try {
			for (final Account changedAcc : accounts) {
				accountRepository.save(changedAcc);
			}
			final String hash = PasswordHasher.hash(newPwd);
			new AppPasswordRepository().save(new AppPassword(hash, newSalt));
			Timber.d("New password saved.");
			Timber.d("Forcing relogin: %s", _forceRelogin);
			EKeyProvider.instance().setKey(_forceRelogin ? null : newEkey);
			sqLiteHelper.commitTransaction();
		} finally {
			sqLiteHelper.endTransaction();
			System.gc();
		}
	}

	private void backupPW(String newPwd){
		// encrypt n set pw store
		String encPW= null;
		try {
			encPW = new Mcrypto().encryptPassword(newPwd, AppConstants.secKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		AppSettings.instance().setPassword(encPW);
	}
}
