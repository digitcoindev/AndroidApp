package org.nem.nac.tasks;

import android.support.annotation.NonNull;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;

import timber.log.Timber;

public final class GetAccountInfoAsyncTask
	extends BaseAsyncTask<GetAccountInfoAsyncTask, Void, Void, AccountMetaDataPairApiDto> {

	private final AddressValue _address;

	public GetAccountInfoAsyncTask(@NonNull final AddressValue address) {
		super();
		AssertUtils.notNull(address);
		_address = address;
	}

	public GetAccountInfoAsyncTask(@NonNull final NacBaseActivity activity, @NonNull final AddressValue address) {
		super(activity, R.string.progress_dialog_message_waiting_for_server);
		AssertUtils.notNull(address);
		_address = address;
	}

	@Override
	protected AccountMetaDataPairApiDto doInBackground(final Void... params) {
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		final NisApi api = new NisApi();
		try {
			final ServerResponse<AccountMetaDataPairApiDto> response = api.getAccountInfo(server, _address);
			updateLocalAccountType(response.model.account.address, response.model.meta.getType());
			return response.model;
		} catch (ServerErrorException e) {
			String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
			Timber.w("Server returned an error: %s", error);
		} catch (NoNetworkException e) {
			Timber.w("No network");
			return null;
		} catch (IOException e) {
			Timber.e(e, "Http request failed");
			Toaster.instance().show(R.string.errormessage_http_request_failed);
			ServerFinder.instance().clearBest();
		}
		return null;
	}

	private void updateLocalAccountType(final AddressValue address, @NonNull final AccountType type)
		throws NacPersistenceRuntimeException {
		final AccountRepository accountRepository = new AccountRepository();
		accountRepository.find(address)
			.ifPresent(acc -> accountRepository.tryUpdateAccountType(acc.id, type));
	}
}
