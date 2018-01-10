package org.nem.nac.common.utils;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

public final class JsonUtils {
	private static final String LOG_TAG = JsonUtils.class.getSimpleName();

	private static final ObjectMapper _mapper;

	static {
		_mapper = new ObjectMapper();
		_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static synchronized <TModel> TModel fromJson(final String json, final Class<TModel> modelClass) throws ParseException {
		Log.d(LOG_TAG, "Deserializing: " + json.substring(0, Math.min(json.length(), 150)) + "...");
		try {
			return _mapper.readValue(json, modelClass);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Failed to parse json to object: " + modelClass.getName());
			throw new ParseException(e);
		}
	}

	/**
	 * Serializes object to json string using compact formatting
	 *
	 * @throws ParseException
	 */
	public static synchronized <TModel> String toJson(final TModel model) throws ParseException {
		return toJson(model, false);
	}

	/**
	 * Serializes object to json string using pretty formatting
	 *
	 * @throws ParseException
	 */
	public static synchronized <TModel> String toJson(final TModel model, final boolean genPrettyOutput) throws ParseException {
		Log.d(LOG_TAG, "Serializing: " + (model != null ? model.getClass().getName() : "null"));
		try {
			ObjectWriter writer = _mapper.writer();
			if (genPrettyOutput) {
				writer = writer.withDefaultPrettyPrinter();
			}
			return writer.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			Log.e(LOG_TAG, "Failed to serialize");
			throw new ParseException(e);
		}
	}

	public static class ParseException extends IOException {

		public ParseException(Throwable throwable) {
			super(throwable);
		}
	}
}
