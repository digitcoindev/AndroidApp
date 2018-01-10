package org.nem.nac.tasks;

import android.support.annotation.Nullable;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairArrayApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public final class GetAccountTransactionsAsyncTask
		extends BaseAsyncTask<GetAccountTransactionsAsyncTask, Void, Void, List<TransactionMetaDataPairApiDto>> {

	private final AddressValue _address;

	public GetAccountTransactionsAsyncTask(final AddressValue address) {
		super();
		_address = address;
	}

	public GetAccountTransactionsAsyncTask(final NacBaseActivity activity, final AddressValue address) {
		super(activity, R.string.progress_dialog_message_waiting_for_server);
		_address = address;
	}

	@Nullable
	public List<TransactionMetaDataPairApiDto> getSynchronous()
			throws NoNetworkException, ServerErrorException, IOException {
		if (server == null && !populateServer()) {
			return null;
		}
		final NisApi api = new NisApi();
		final ServerResponse<TransactionMetaDataPairArrayApiDto> confirmedResponse = api.getTransactions(server, _address, null);
		List<TransactionMetaDataPairApiDto> trans = new ArrayList<>(confirmedResponse.model.data.length);
		Collections.addAll(trans, confirmedResponse.model.data);
		// next pack
		if (!trans.isEmpty()) {
			final TransactionMetaDataPairApiDto last = trans.get(trans.size() - 1);
			final ServerResponse<TransactionMetaDataPairArrayApiDto> confirmedResponse2 = api.getTransactions(server, _address, last.meta.id);
			Collections.addAll(trans, confirmedResponse2.model.data);
		}
		//
		return trans;
	}

	@Override
	protected List<TransactionMetaDataPairApiDto> doInBackground(final Void... params) {
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		try {
			final List<TransactionMetaDataPairApiDto> synchronousTrans = getSynchronous();
			return synchronousTrans;
		} catch (NoNetworkException e) {
			Toaster.instance().show(R.string.errormessage_no_network_connection);
			return null;
		} catch (ServerErrorException e) {
			String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
			Timber.w("Server returned an error: %s", error);
			Toaster.instance().show(R.string.errormessage_server_error_occured);
		} catch (IOException e) {
			Toaster.instance().show(R.string.errormessage_http_request_failed);
		}
		return null;
	}
}
