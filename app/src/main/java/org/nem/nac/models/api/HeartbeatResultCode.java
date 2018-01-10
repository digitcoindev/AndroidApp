package org.nem.nac.models.api;

import android.support.annotation.StringRes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.R;

import timber.log.Timber;

public enum HeartbeatResultCode implements ApiResultCode {
	/**
	 * Stub for unknown results
	 */
	UNKNOWN(-1, R.string.server_result_code_unknown_result),
	SUCCESSFUL(1, R.string.server_result_code_success);

	@JsonCreator
	public static HeartbeatResultCode fromRaw(final int rawCode) {
		for (HeartbeatResultCode value : values()) {
			if (value._rawCode == rawCode) { return value; }
		}
		Timber.e("Unknown heartbeat code: %d", rawCode);
		return UNKNOWN;
	}

	private int _rawCode;
	@StringRes
	private int _msgRes;

	HeartbeatResultCode(final int rawCode, final @StringRes Integer friendlyMsgRes) {
		_rawCode = rawCode;
		_msgRes = friendlyMsgRes;
	}

	@JsonValue
	@Override
	public int getCode() { return _rawCode; }

	@Override
	public boolean isSuccessful() { return true; }

	@Override
	@StringRes
	public int getMessageRes() { return _msgRes; }

}
