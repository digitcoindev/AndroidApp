package org.nem.nac.tasks;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppSettings;
import org.nem.nac.crypto.KeyProvider;
import org.nem.nac.crypto.Mcrypto;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.crypto.PasswordHasher;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.repositories.AppPasswordRepository;
import org.nem.nac.models.AppPassword;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public class CheckAppPasswordTask extends BaseAsyncTask<CheckAppPasswordTask, String, Void, Boolean> {

	public CheckAppPasswordTask(final NacBaseActivity activity) {
		super(activity, R.string.progress_dialog_message_password_checking);
	}

	@Override
	protected Boolean doInBackground(final String... params) {
		if (params.length < 1) {
			return null;
		}
		final String pwd = params[0];
		try {
			final Optional<AppPassword> appPassword = new AppPasswordRepository().get();
			final boolean pwdOk = PasswordHasher.check(pwd, appPassword.get().passwordHash);
			if (pwdOk) {
				EKeyProvider.instance().setKey(KeyProvider.deriveKey(pwd, appPassword.get().salt));
				String pwStoreIsEmpty= AppSettings.instance().getPassword();
				if (pwStoreIsEmpty==null){
					backupPW(pwd);
				}
			}
			return pwdOk;
		} catch (NacPersistenceRuntimeException e) {
			Timber.e(e, "Persistence operation failed");
			return null;
		} catch (NacCryptoException e) {
			Timber.e(e, "Failed to derive key");
			return null;
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
