package org.nem.nac.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Stream;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

import org.nem.nac.application.AppHost;
import org.nem.nac.common.Stopwatch;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.common.exceptions.NoNetworkException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.JsonUtils;
import org.nem.nac.models.api.ErrorObjectApiDto;
import org.nem.nac.models.api.HarvestingInfoArrayApiDto;
import org.nem.nac.models.api.RequestAnnounceApiDto;
import org.nem.nac.models.api.RequestResultApiDto;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.AnnounceRequestResultApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairArrayApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairArrayApiDto;
import org.nem.nac.models.network.Server;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.servers.ServerFinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class NisApi {

	/**
	 * returns true if successful heartbeat, false otherwise.
	 *
	 * @throws NoNetworkException              when device is not connected to the network
	 * @throws ResponseParsingRuntimeException when failed to parse response (or error object) to model.
	 */
	public ServerResponse<Boolean> heartbeat(final Server server)
		throws NoNetworkException, ResponseParsingRuntimeException {
		try {
			Timber.d("Heartbeating %s", server);
			ServerResponse<RequestResultApiDto> heartbeatResponse =
				performGetRequest(server, Paths.HEARTBEAT, RequestResultApiDto.class, Paths.HEARTBEAT, null, false);
			return new ServerResponse<>(heartbeatResponse.server, heartbeatResponse.model
				.isSuccessful(), heartbeatResponse.responseTime);
		} catch (ServerErrorException | IOException e) {
			Timber.w("Heartbeat failed with exception: %s/%s", e.getClass().getName(), e.getMessage());
			return new ServerResponse<>(server, false, TimeSpan.ZERO);
		}
	}

	public ServerResponse<AccountMetaDataPairApiDto> getAccountInfo(final Server server, final AddressValue account)
			throws ServerErrorException, NoNetworkException, IOException {
		AssertUtils.notNull(account);
		Map<String, String> query = new HashMap<>();
		query.put("address", account.getRaw());
		return performGetRequest(server, Paths.ACCOUNT_GET, AccountMetaDataPairApiDto.class, Paths.ACCOUNT_GET, query, true);
	}

	public ServerResponse<TransactionMetaDataPairArrayApiDto> getTransactions(final Server server, final AddressValue account, @Nullable final Integer upToId)
			throws NoNetworkException, ServerErrorException, ResponseParsingRuntimeException, IOException {
		AssertUtils.notNull(account);
		Map<String, String> query = new HashMap<>();
		query.put("address", account.getRaw());
		if (upToId != null) {
			query.put("id", String.valueOf(upToId));
		}
		return performGetRequest(server, Paths.TRANSACTIONS_ALL, TransactionMetaDataPairArrayApiDto.class, Paths.TRANSACTIONS_ALL, query, true);
	}

	public ServerResponse<UnconfirmedTransactionMetaDataPairArrayApiDto> getUnconfirmedTransactions(final Server server, final AddressValue account)
			throws ServerErrorException, NoNetworkException, IOException {
		AssertUtils.notNull(account);
		Map<String, String> query = new HashMap<>();
		query.put("address", account.getRaw());
		return performGetRequest(server, Paths.TRANSACTIONS_UNCONFIRMED, UnconfirmedTransactionMetaDataPairArrayApiDto.class, Paths.TRANSACTIONS_UNCONFIRMED, query, true);
	}

	public ServerResponse<HarvestingInfoArrayApiDto> getHarvestInfo(final Server server, final AddressValue account)
			throws ServerErrorException, NoNetworkException, IOException {
		AssertUtils.notNull(server, account);
		Map<String, String> query = new HashMap<>();
		query.put("address", account.getRaw());
		return performGetRequest(server, Paths.HARVEST_INFO_GET, HarvestingInfoArrayApiDto.class, Paths.HARVEST_INFO_GET, query, true);
	}

	public ServerResponse<AnnounceRequestResultApiDto> announceTransaction(final Server server, final RequestAnnounceApiDto requestAnnounce)
			throws ServerErrorException, NoNetworkException, IOException {
		AssertUtils.notNull(requestAnnounce);

		return performPostRequest(server, Paths.ANNOUNCE_TRANSACTION, requestAnnounce, AnnounceRequestResultApiDto.class, Paths.ANNOUNCE_TRANSACTION, null, true);
	}

	private <TResponse> ServerResponse<TResponse> performGetRequest(final @NonNull Server server, final @NonNull String path,
		@NonNull final Class<? extends TResponse> modelClass, final Object tag, final @Nullable Map<String, String> queryParams, final boolean parseErrorObject)
			throws NoNetworkException, IOException, ServerErrorException, ResponseParsingRuntimeException {
		AssertUtils.notNull(server, "server is null");
		AssertUtils.notNull(path, "path is null");

		if (!AppHost.Network.isAvailable()) {
			throw new NoNetworkException();
		}
		final HttpRequest request = new HttpRequest(server, path).asHttpGet().setTag(tag);
		if (queryParams != null) {
			Stream.of(queryParams).forEach(x -> request.addQueryParameter(x.getKey(), x.getValue()));
		}
		final Call call = request.getCall(HttpClient.instance());
		final Stopwatch stopwatch = new Stopwatch(true);
		final Response response;
		try {
			response = call.execute();
		} finally {
			stopwatch.stop();
		}

		if (response.isSuccessful()) {
			try {
				final TResponse model = JsonUtils.fromJson(response.body().string(), modelClass);
				return new ServerResponse<>(server, model, stopwatch.getTimeSpan());
			} catch (JsonUtils.ParseException e) {
				throw new ResponseParsingRuntimeException(e, response.isSuccessful());
			}
		}
		else if (response.code() >= 500 && response.code() < 600) {
			ServerFinder.instance().clearBest();
		}
		if (parseErrorObject) {
			try {
				final ErrorObjectApiDto error = JsonUtils.fromJson(response.body().string(), ErrorObjectApiDto.class);
				throw new ServerErrorException(response.code(), response.message(), error);
			} catch (JsonUtils.ParseException e) {
				throw new ResponseParsingRuntimeException(e, response.isSuccessful());
			}
		}
		else {
			throw new ServerErrorException(response.code(), response.message());
		}
	}

	private <TResponse> ServerResponse<TResponse> performPostRequest(final @NonNull Server server, final @NonNull String path, final Object bodyModel,
		@NonNull final Class<? extends TResponse> responseModelClass, final Object tag, final @Nullable Map<String, String> queryParams,
		final boolean parseErrorObject)
			throws NoNetworkException, IOException, ServerErrorException, ResponseParsingRuntimeException {
		AssertUtils.notNull(server, path);

		if (!AppHost.Network.isAvailable()) {
			throw new NoNetworkException();
		}

		final HttpRequest request = new HttpRequest(server, path).asHttpPost(bodyModel).setTag(tag);
		if (queryParams != null) {
			Stream.of(queryParams).forEach(x -> request.addQueryParameter(x.getKey(), x.getValue()));
		}
		final Call call = request.getCall(HttpClient.instance());
		final Stopwatch stopwatch = new Stopwatch(true);
		final Response response;
		try {
			response = call.execute();
		} finally {
			stopwatch.stop();
		}

		if (response.isSuccessful()) {
			try {
				final TResponse model = JsonUtils.fromJson(response.body().string(), responseModelClass);
				return new ServerResponse<>(server, model, stopwatch.getTimeSpan());
			} catch (JsonUtils.ParseException e) {
				throw new ResponseParsingRuntimeException(e, response.isSuccessful());
			}
		}
		else if (response.code() >= 500 && response.code() < 600) {
			ServerFinder.instance().clearBest();
		}

		if (parseErrorObject) {
			try {
				final ErrorObjectApiDto error = JsonUtils.fromJson(response.body().string(), ErrorObjectApiDto.class);
				throw new ServerErrorException(response.code(), response.message(), error);
			} catch (JsonUtils.ParseException e) {
				throw new ResponseParsingRuntimeException(e, response.isSuccessful());
			}
		}
		else {
			throw new ServerErrorException(response.code(), response.message());
		}
	}

	private static final class Paths {
		public static final String HEARTBEAT                = "/heartbeat";
		public static final String TRANSACTIONS_ALL         = "/account/transfers/all";
		public static final String TRANSACTIONS_UNCONFIRMED = "/account/unconfirmedTransactions";
		public static final String ACCOUNT_GET              = "/account/get";
		public static final String ANNOUNCE_TRANSACTION = "/transaction/announce";
		public static final String HARVEST_INFO_GET     = "/account/harvests";
	}
}
