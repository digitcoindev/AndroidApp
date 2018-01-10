package org.nem.nac.providers;

import com.annimon.stream.Optional;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.models.TimeValue;
import org.nem.nac.common.utils.JsonUtils;
import org.nem.nac.http.HttpClient;
import org.nem.nac.http.HttpRequest;
import org.nem.nac.models.ApiError;
import org.nem.nac.models.NodeInfo;
import org.nem.nac.models.api.ErrorObjectApiDto;
import org.nem.nac.models.api.node.NisNodeInfoApiDto;
import org.nem.nac.models.network.Server;
import org.nem.nac.servers.ServerFinder;

import java.io.IOException;

public final class NodeInfoProvider extends AbstractProvider<NodeInfo> {

	private static NodeInfoProvider _instance;

	public static synchronized NodeInfoProvider instance() {
		if (_instance == null) {
			_instance = new NodeInfoProvider();
		}
		return _instance;
	}

	public TimeValue getNetworkTime()
		throws NacException {
		int offset = getUpdateTimeOffsetMs() / 1000;
		return getData().time.addSeconds(offset);
	}

	@Override
	protected void refresh()
		throws IOException {
		final Optional<Server> server = ServerFinder.instance().getBest();
		if (!server.isPresent()) {
			throw new IOException("No server");
		}
		final Call call = new HttpRequest(server.get(), "/node/extended-info").setMethodBody("GET", null)
			.getCall(HttpClient.instance());

		final Response response = call.execute();
		if (response.isSuccessful()) {
			final NisNodeInfoApiDto apiDto = JsonUtils.fromJson(response.body().string(), NisNodeInfoApiDto.class);
			data.set(new NodeInfo(apiDto.nisInfo.currentTime));
		}
		else {
			final ErrorObjectApiDto error = JsonUtils.fromJson(response.body().string(), ErrorObjectApiDto.class);
			final ApiError apiError = new ApiError(error.status, error.error, error.message);
			throw new IOException(apiError.toString());
		}
	}
}
