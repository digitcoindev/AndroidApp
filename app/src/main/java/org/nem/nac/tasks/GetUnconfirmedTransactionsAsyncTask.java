package org.nem.nac.tasks;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairArrayApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public final class GetUnconfirmedTransactionsAsyncTask
		extends BaseAsyncTask<GetUnconfirmedTransactionsAsyncTask, Void, Void, List<UnconfirmedTransactionMetaDataPairApiDto>> {
	private final AddressValue _address;

	public GetUnconfirmedTransactionsAsyncTask(final AddressValue address) {
		_address = address;
	}

	public GetUnconfirmedTransactionsAsyncTask(final NacBaseActivity activity, final AddressValue address) {
		super(activity, R.string.progress_dialog_message_waiting_for_server);
		_address = address;
	}

	@Override
	protected List<UnconfirmedTransactionMetaDataPairApiDto> doInBackground(final Void... params) {
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		final NisApi api = new NisApi();
		try {
			final ServerResponse<UnconfirmedTransactionMetaDataPairArrayApiDto> response =
				api.getUnconfirmedTransactions(server, _address);
			final List<UnconfirmedTransactionMetaDataPairApiDto> trans = new ArrayList<>(response.model.data.length);
			Collections.addAll(trans, response.model.data);
			return trans;
		} catch (NoNetworkException e) {
			Toaster.instance().show(R.string.errormessage_no_network_connection);
		} catch (ServerErrorException e) {
			String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
			Timber.w("Server returned an error: %s", error);
			Toaster.instance().show(R.string.errormessage_server_error_occured);
		} catch (IOException e) {
			Toaster.instance().show(R.string.errormessage_http_request_failed);
			ServerFinder.instance().clearBest();
		}
		return null;
	}
}
