package org.nem.nac.tasks;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.helpers.Ed25519Helper;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;

import timber.log.Timber;

public final class DecryptMessageAsyncTask extends BaseAsyncTask<DecryptMessageAsyncTask, Void, Void, BinaryData> {

	private final BinaryData             _cipher;
	private final EncryptedNacPrivateKey _account1;
	private final NacPublicKey           _account2;
	private final AddressValue           _account2Addr;

	public DecryptMessageAsyncTask(final BinaryData cipher, final EncryptedNacPrivateKey account1, final NacPublicKey account2) {
		AssertUtils.notNull(cipher, "cipher");
		AssertUtils.notNull(account1, "account1");
		AssertUtils.notNull(account2, "account2");

		_cipher = cipher;
		_account1 = account1;
		_account2 = account2;
		_account2Addr = null;
	}

	public DecryptMessageAsyncTask(final BinaryData cipher, final EncryptedNacPrivateKey account1, final AddressValue account2) {
		AssertUtils.notNull(cipher, "cipher");
		AssertUtils.notNull(account1, "account1");
		AssertUtils.notNull(account2, "account2");

		_cipher = cipher;
		_account1 = account1;
		_account2 = null;
		_account2Addr = account2;
	}

	@Override
	protected BinaryData doInBackground(final Void... params) {
		Timber.d("started");
		// Ekey
		final Optional<BinaryData> eKey = EKeyProvider.instance().getKey();
		if (!eKey.isPresent()) {
			Timber.e("Key was not present!");
			return null;
		}
		Timber.d("E-key ok");
		// Server
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}
		Timber.d("Server ok, %s", server);
		//
		final NacPublicKey acc2;
		if (_account2 != null) {
			acc2 = _account2;
			Timber.d("Have acc2 public key");
		}
		else {
			Timber.d("Requesting acc2 public key");
			final NisApi api = new NisApi();
			try {
				final ServerResponse<AccountMetaDataPairApiDto> accountInfo =
					api.getAccountInfo(server, _account2Addr);
				acc2 = accountInfo.model.account.publicKey;
				Timber.d("Got acc2 public key");
			} catch (ServerErrorException e) {
				String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
				Timber.w("Server returned an error: %s", error);
				Toaster.instance().show(R.string.errormessage_server_error_occured);
				return null;
			} catch (NoNetworkException e) {
				Timber.w("No network");
				return null;
			} catch (IOException e) {
				Timber.e(e, "Http request failed");
				Toaster.instance().show(R.string.errormessage_http_request_failed);
				ServerFinder.instance().clearBest();
				return null;
			}
		}

		if (acc2 == null) {
			Timber.e("acc2 public key is null");
			return null;
		}

		try {
			final NacPrivateKey privKey = _account1.decryptKey(eKey.get());
			final Optional<BinaryData> decrypted = Ed25519Helper.Ed25519BlockCipherDecrypt(_cipher, privKey, acc2);
			Timber.d("Returning, decrypted - %s", decrypted.isPresent());
			return decrypted.orElse(null);
		} catch (NacCryptoException e) {
			Timber.e(e, "Failed to decrypt message");
		}
		return null;
	}
}
