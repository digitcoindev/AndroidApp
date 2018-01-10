package org.nem.nac.tasks;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.http.NisApi;
import org.nem.nac.http.ServerErrorException;
import org.nem.nac.http.ServerResponse;
import org.nem.nac.models.api.HarvestingInfoArrayApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;

import timber.log.Timber;

public final class GetHarvestInfoDataAsyncTask extends BaseAsyncTask<GetHarvestInfoDataAsyncTask, Void, Void, HarvestingInfoArrayApiDto> {

	private AddressValue _address;

	public GetHarvestInfoDataAsyncTask(final NacBaseActivity activity, final AddressValue address) {
		super(activity, R.string.progress_dialog_message_waiting_for_server);
		AssertUtils.notNull(address);

		_address = address;
	}

	@Override
	protected HarvestingInfoArrayApiDto doInBackground(final Void... params) {
		if (!populateServer()) {
			Toaster.instance().show(R.string.errormessage_no_server);
			return null;
		}

		final NisApi api = new NisApi();
		try {
			final ServerResponse<HarvestingInfoArrayApiDto> response = api.getHarvestInfo(server, _address);
			return response.model;
		} catch (ServerErrorException e) {
			String error = e.getReadableError(NacApplication.getResString(R.string.errormessage_error_occured));
			Timber.w("Server returned an error: %s", error);
			Toaster.instance().show(R.string.errormessage_server_error_occured);
		} catch (NoNetworkException e) {
			Timber.w("No network");
			return null;
		} catch (IOException e) {
			Timber.e(e, "Http request failed");
			Toaster.instance().show(R.string.errormessage_http_request_failed);
		}
		return null;
	}
}
