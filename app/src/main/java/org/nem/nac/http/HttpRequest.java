package org.nem.nac.http;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.JsonUtils;
import org.nem.nac.models.network.Server;

public class HttpRequest {
	private static final MediaType       JSON_MEDIA_TYPE = MediaType.parse("application/json");
	private final        Uri.Builder     _uriBuilder     = new Uri.Builder();
	private              Request.Builder _requestBuilder = new Request.Builder();

	public HttpRequest(@NonNull Server server, @NonNull final String path) {
		AssertUtils.notNull(server, path);
		_uriBuilder.scheme(server.protocol);
		_uriBuilder.encodedAuthority(String.format("%s:%d", server.host, server.port.getValue()));
		_uriBuilder.path(path);
	}

	public Call getCall(@NonNull final HttpClient client) {
		AssertUtils.notNull(client);
		final Uri uri = _uriBuilder.build();
		return client.get().newCall(_requestBuilder.url(uri.toString()).build());
	}

	public HttpRequest addQueryParameter(@NonNull final String key, @Nullable final String param) {
		_uriBuilder.appendQueryParameter(key, param);
		return this;
	}

	public HttpRequest asHttpGet() {
		_requestBuilder.method("GET", null);
		return this;
	}

	public HttpRequest asHttpPost(@Nullable final Object bodyModel) {
		setMethodBody("POST", bodyModel);
		return this;
	}

	public HttpRequest setTag(final Object tag) {
		_requestBuilder.tag(tag);
		return this;
	}

	public HttpRequest setMethodBody(@NonNull final String method, @Nullable final Object bodyModel) {
		AssertUtils.notNull(method);
		if (bodyModel != null) {
			try {
				final RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, JsonUtils.toJson(bodyModel));
				_requestBuilder.method(method, requestBody);
			} catch (JsonUtils.ParseException e) {
				throw new NacRuntimeException("Failed to convert " + bodyModel.getClass().getName() + " to json");
			}
		}
		else {
			_requestBuilder.method(method, null);
		}
		return this;
	}
}
