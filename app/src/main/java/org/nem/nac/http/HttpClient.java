package org.nem.nac.http;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.nem.nac.BuildConfig;
import org.nem.nac.common.utils.LogUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okio.Buffer;

public final class HttpClient {

	private static final String LOG_TAG = HttpClient.class.getSimpleName();
	private static HttpClient _instance;
	private final OkHttpClient _httpClient = new OkHttpClient();

	public static HttpClient instance() {
		if (_instance == null) {
			synchronized (HttpClient.class) {
				if (_instance == null) {
					_instance = new HttpClient();
				}
			}
		}
		return _instance;
	}

	public OkHttpClient get() {
		return _httpClient;
	}

	private HttpClient() {
		_httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
		_httpClient.setReadTimeout(10, TimeUnit.SECONDS);
		_httpClient.setWriteTimeout(10, TimeUnit.SECONDS);
		if (BuildConfig.DEBUG) {
			_httpClient.interceptors().add(new LoggingInterceptor());
		}
	}

	private class LoggingInterceptor implements Interceptor {

		@Override
		public Response intercept(final Chain chain)
				throws IOException {
			final Request request = chain.request();
			final Request requestCopy = request.newBuilder().build();
			final String requestBodyStr;
			if (requestCopy.body() != null) {
				final Buffer buffer = new Buffer();
				requestCopy.body().writeTo(buffer);
				requestBodyStr = buffer.readUtf8();
			}
			else {
				requestBodyStr = null;
			}

			LogUtils.splittedMessage(Log.DEBUG, LOG_TAG, "Request to %s; Body:%s", request.url(), requestBodyStr);

			final long startTime = System.nanoTime();

			try {
				final Response response = chain.proceed(request);

				final long endTime = System.nanoTime();
				ResponseBody body = response.body();
				final String bodyString = body.string();
				final byte[] bodyStringBytes = bodyString.getBytes();
				final double timeMs = (endTime - startTime) / 1e6d;
				LogUtils.splittedMessage(Log.DEBUG, LOG_TAG, "Response from %s in %.1fms: %s", response.request().url(), timeMs, bodyString);
				final ResponseBody bodyCopy = ResponseBody.create(body.contentType(), bodyStringBytes);
				return response.newBuilder().body(bodyCopy).build();
			} catch (IOException e) {
				Log.d(LOG_TAG, "Api response exception", e);
				throw e;
			}
		}
	}
}
