package org.nem.nac.tasks;

import android.support.annotation.Nullable;

import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairArrayApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairArrayApiDto;
import org.nem.nac.models.transactions.AccountTransaction;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class GetAllTransactionsAsyncTask
		extends BaseAsyncTask<GetAllTransactionsAsyncTask, Void, Void, List<AccountTransaction>> {

	private final NacPublicKey _account;

	public GetAllTransactionsAsyncTask(final NacPublicKey account) {
		_account = account;
	}

	public GetAllTransactionsAsyncTask(final NacBaseActivity activity, final NacPublicKey account) {
		super(activity, R.string.progress_dialog_message_waiting_for_server);
		_account = account;
	}

	@Nullable
	public List<AccountTransaction> getSynchronous()
			throws NoNetworkException, ServerErrorException, IOException {
		if (server == null && !populateServer()) {
			return null;
		}
		final NisApi api = new NisApi();
		final List<AccountTransaction> allTransactions = new ArrayList<>();
		final ServerResponse<TransactionMetaDataPairArrayApiDto> confirmedResponse = api.getTransactions(server, _account.toAddress(), null);
		List<TransactionMetaDataPairApiDto> trans = new ArrayList<>(confirmedResponse.model.data.length);
		Collections.addAll(trans, confirmedResponse.model.data);
		// next pack
		if (!trans.isEmpty()) {
			final TransactionMetaDataPairApiDto last = trans.get(trans.size() - 1);
			final ServerResponse<TransactionMetaDataPairArrayApiDto> confirmedResponse2 = api.getTransactions(server, _account.toAddress(), last.meta.id);
			Collections.addAll(trans, confirmedResponse2.model.data);
		}
		Stream.of(trans)
				.map(x -> new AccountTransaction(_account, x))
				.forEach(allTransactions::add);
		// Unconfirmed
		final ServerResponse<UnconfirmedTransactionMetaDataPairArrayApiDto> unconfirmedResponse = api.getUnconfirmedTransactions(server, _account.toAddress());
		Stream.of(unconfirmedResponse.model.data)
				.map(x -> new AccountTransaction(_account, x))
				.forEach(allTransactions::add);
		Collections.sort(allTransactions);
		return allTransactions;
	}

	@Override
	protected List<AccountTransaction> doInBackground(final Void... params) {
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		try {
			final List<AccountTransaction> synchronousTrans = getSynchronous();
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
