package org.nem.nac.tasks;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Optional;

import org.nem.core.crypto.DsaSigner;
import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.ed25519.Ed25519DsaSigner;
import org.nem.core.utils.HexEncoder;
import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.ErrorUtils;
import org.nem.nac.common.utils.IOUtils;
import org.nem.nac.common.utils.LogUtils;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.log.LogTags;
import org.nem.nac.models.AnnounceResult;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.SignedBinaryData;
import org.nem.nac.models.api.ApiResultCode;
import org.nem.nac.models.api.RequestAnnounceApiDto;
import org.nem.nac.models.api.transactions.AnnounceRequestResultApiDto;
import org.nem.nac.models.transactions.drafts.AbstractTransactionDraft;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

public final class SendTransactionAsyncTask
		extends BaseAsyncTask<SendTransactionAsyncTask, Void, Void, AnnounceResult> {
	private final AtomicReference<byte[]>                   _serializedTransaction = new AtomicReference<>();
	private final AtomicReference<AbstractTransactionDraft> _transaction           = new AtomicReference<>();
	@NonNull
	private final EncryptedNacPrivateKey _tranSigner;

	public SendTransactionAsyncTask(
			final NacBaseActivity activity,
			@NonNull final AbstractTransactionDraft transaction,
			@NonNull EncryptedNacPrivateKey tranSigner) {
		super(activity, R.string.progress_dialog_message_sending_in_progress);
		_transaction.set(transaction);
		_tranSigner = tranSigner;
	}

	public SendTransactionAsyncTask(
			final NacBaseActivity activity,
			@NonNull final byte[] serializedTransaction,
			@NonNull EncryptedNacPrivateKey tranSigner) {
		super(activity, R.string.progress_dialog_message_sending_in_progress);
		AssertUtils.notNull(tranSigner, serializedTransaction);

		_tranSigner = tranSigner;
		_serializedTransaction.set(serializedTransaction);
	}

	@Override
	protected AnnounceResult doInBackground(final Void... params) {
		Optional<BinaryData> eKey = EKeyProvider.instance().getKey();
		if (!eKey.isPresent()) {
			Timber.e("No key present");
			return null;
		}

		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		final NacPrivateKey privateKey;
		try {
			privateKey = _tranSigner.decryptKey(eKey.get());
		} catch (NacCryptoException e) {
			Toaster.instance().show(R.string.errormessage_account_error, Toaster.Length.LONG);
			Timber.e("Decryption failed!");
			return null;
		}
		final KeyPair signerKeyPair = new KeyPair(privateKey.toPrivateKey());
		final DsaSigner dsaSigner = new Ed25519DsaSigner(signerKeyPair);
		byte[] serializedTransaction = null;
		// Serializing transaction
		if (_serializedTransaction.get() == null) {
			final AbstractTransactionDraft tran = _transaction.get();
			final ByteArrayOutputStream tranStream = new ByteArrayOutputStream();
			try {
				tran.serialize(tranStream);
				serializedTransaction = tranStream.toByteArray();
			} catch (NacException e) {
				final String message = NacApplication.getResString(R.string.errormessage_failed_to_prepare_transaction);
				ErrorUtils.sendSilentReport("Preparing transaction failed", e);
				return new AnnounceResult(false, message, new NacException(message, e));
			} finally {
				IOUtils.closeSilently(tranStream);
			}
		}
		else {
			serializedTransaction = _serializedTransaction.get();
		}

		LogUtils.conditional(Log.WARN, LogTags.TRANSACTIONS.isLogged, LogTags.TRANSACTIONS.name, "Sending transaction:" + HexEncoder
				.getString(serializedTransaction));

		final SignedBinaryData signedTransaction = new SignedBinaryData(serializedTransaction, dsaSigner);
		Timber.d("Transaction signed");
		final RequestAnnounceApiDto announceDto = new RequestAnnounceApiDto(signedTransaction);

		final NisApi api = new NisApi();
		try {
			final ServerResponse<AnnounceRequestResultApiDto> response =
				api.announceTransaction(server, announceDto);
			final AnnounceRequestResultApiDto announceResult = response.model;
			final String message;
			if (announceResult.code.getCode() != ApiResultCode.UNKNOWN) {
				message = NacApplication.getResString(announceResult.code.getMessageRes());
			}
			else {
				message = announceResult.message;
			}
			Timber.d("Tran announced with message: %s", message);
			return new AnnounceResult(announceResult.isSuccessful(), message, null);
		} catch (NoNetworkException e) {
			Timber.d("No network");
			Toaster.instance().show(R.string.errormessage_no_network_connection);
			return null;
		} catch (ServerErrorException e) {
			String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
			Timber.w("Server returned an error: %s", error);
			Toaster.instance().show(R.string.errormessage_server_error_occured);
		} catch (IOException e) {
			Timber.d("Request failed!");
			Toaster.instance().show(R.string.errormessage_http_request_failed);
			ServerFinder.instance().clearBest();
		}
		return null;
	}
}
