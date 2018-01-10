package org.nem.nac.models.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.nem.nac.common.enums.NemRequestResultType;

public class RequestResultApiDto {

	public NemRequestResultType type;
	/**
	 * The meaning of the code is dependent on the type.
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
		property = "type",
		visible = true
	)
	@JsonSubTypes({
		@JsonSubTypes.Type(name = "1", value = ValidationResultCode.class),
		@JsonSubTypes.Type(name = "2", value = HeartbeatResultCode.class),
		@JsonSubTypes.Type(name = "4", value = StatusResultCode.class)
	})
	public ApiResultCode        code;
	public String               message;

	public boolean isSuccessful() {
		return code.isSuccessful();
	}
}
