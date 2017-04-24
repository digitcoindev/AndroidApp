package org.nem.nac.models.qr;

import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.LogUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = QrDto.QrSerializer.class)
@JsonDeserialize(using = QrDto.QrDtoDeserializer.class)
public final class QrDto {

	/**
	 * Current dto version
	 */
	public static final int VERSION = 2;

	protected static final Map<Type, Class<? extends BaseQrData>> TYPES = new HashMap<>();

	static {
		TYPES.put(QrDto.Type.USER_INFO, QrUserInfo.class);
		TYPES.put(QrDto.Type.INVOICE, QrInvoice.class);
		TYPES.put(QrDto.Type.ACCOUNT, QrAccount.class);
	}

	/**
	 * Version of received QR goes here
	 */
	@JsonProperty("v")
	public int        version;
	public Type       type;
	public BaseQrData data;

	public QrDto() { }

	public QrDto(@NonNull final Type type, @NonNull final BaseQrData data) {
		this.type = type;
		this.data = data;
		this.version = VERSION;
	}

	@Override
	public String toString() {
		return "Type: " + type + ", " + data.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		QrDto dto = (QrDto)o;

		if (version != dto.version) { return false; }
		if (type != dto.type) { return false; }
		return !(data != null ? !data.equals(dto.data) : dto.data != null);
	}

	@Override
	public int hashCode() {
		int result = version;
		result = 31 * result + (type != null ? type._raw : 0);
		result = 31 * result + (data != null ? data.hashCode() : 0);
		return result;
	}

	public enum Type {
		USER_INFO(1),
		INVOICE(2),
		ACCOUNT(3);

		private static final Type[] values = Type.values();
		private int _raw;

		@JsonCreator
		Type(final int raw) {
			_raw = raw;
		}

		@JsonValue
		public int getRaw() {
			return _raw;
		}

		public static Type fromRaw(final int raw) {
			for (Type value : values) {
				if (value._raw == raw) {
					return value;
				}
			}
			throw new NacRuntimeException("Unknown QrDto.Type found");
		}
	}

	public static final class QrSerializer extends JsonSerializer<QrDto> {

		@Override
		public void serialize(final QrDto value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException {
			gen.writeStartObject();
			gen.writeNumberField("v", value.version);
			gen.writeNumberField("type", value.type.getRaw());
			gen.writeObjectField("data", value.data);
			gen.writeEndObject();
		}
	}

	public static class QrDtoDeserializer extends JsonDeserializer<QrDto> {

		private static final String LOG_TAG = QrDtoDeserializer.class.getSimpleName();

		@Override
		public QrDto deserialize(final JsonParser jp, final DeserializationContext ctxt)
				throws IOException {
			jp.getText();
			ObjectNode node = jp.readValueAsTree();
			QrDto dto = new QrDto();
			final JsonNode vNode = node.get("v");
			dto.version = vNode != null ? vNode.asInt(0) : 0;
			dto.type = QrDto.Type.fromRaw(node.get("type").asInt(0));
			Class<? extends BaseQrData> concreteType = TYPES.get(dto.type);
			if (concreteType == null) {
				final String msg = "Cannot deserialize data of type: " + dto.type;
				LogUtils.tagged(Log.WARN, LOG_TAG, msg);
				throw new IOException(msg);
			}
			final JsonNode dataNode = node.get("data");
			dto.data = jp.getCodec().treeToValue(dataNode, concreteType);
			return dto;
		}
	}
}
