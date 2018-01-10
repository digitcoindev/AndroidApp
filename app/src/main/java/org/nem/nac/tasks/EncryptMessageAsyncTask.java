package org.nem.nac.tasks;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.models.MessageDraft;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.crypto.NacCryptoException;
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
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;

import timber.log.Timber;

public final class EncryptMessageAsyncTask
		extends BaseAsyncTask<EncryptMessageAsyncTask, Void, Void, MessageDraft> {

	private final EncryptedNacPrivateKey _account1;
	private final AddressValue           _account2Addr;
	private final MessageDraft           _msg;

	public EncryptMessageAsyncTask(
			@NonNull NacBaseActivity activity,
			@NonNull EncryptedNacPrivateKey account1,
			@NonNull AddressValue account2,
			@NonNull final MessageDraft msg) {
		super(activity, R.string.progress_dialog_wait_message);
		AssertUtils.notNull(account1, account2, msg);

		_account1 = account1;
		_account2Addr = account2;
		_msg = msg;
	}

	@Override
	protected MessageDraft doInBackground(final Void... params) {
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		final NisApi api = new NisApi();
		try {
			final ServerResponse<AccountMetaDataPairApiDto> accountInfo =
				api.getAccountInfo(server, _account2Addr);
			final NacPublicKey recipientPubKey = accountInfo.model.account.publicKey;
			if (recipientPubKey == null) {
				Toaster.instance().show(R.string.errormessage_failed_to_encrypt_msg_no_public_key, Toaster.Length.LONG);
				return null;
			}
			final Optional<BinaryData> eKey = EKeyProvider.instance().getKey();
			final NacPrivateKey pKey = _account1.decryptKey(eKey.get());
			_msg.encryptPayload(pKey, recipientPubKey);
			return _msg;
			//
		} catch (ServerErrorException e) {
			String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
			Timber.w("Server returned an error: %s", error);
			Toaster.instance().show(R.string.errormessage_server_error_occured);
		} catch (NoNetworkException e) {
			Timber.w("No network");
		} catch (IOException e) {
			Timber.e(e, "Http request failed");
			Toaster.instance().show(R.string.errormessage_http_request_failed);
			ServerFinder.instance().clearBest();
		} catch (NacCryptoException e) {
			Timber.e(e, "Failed to decrypt private key!");
			Toaster.instance().show(R.string.errormessage_error_occured);
		}
		return null;
	}
}
