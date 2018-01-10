package org.nem.nac.models.api;

import android.support.annotation.StringRes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.R;

import timber.log.Timber;

public enum StatusResultCode implements ApiResultCode {
	/**
	 * Stub for unknown results
	 */
	UNKNOWN(-1, R.string.server_result_code_unknown_result),
	/**
	 * Unknown status.
	 */
	UNKNOWN_STATUS(0, R.string.server_result_code_unknown_status),
	/**
	 * NIS is stopped.
	 */
	NIS_STOPPED(1, R.string.server_result_code_nis_stopped),
	/**
	 * NIS is starting.
	 */
	NIS_STARTING(2, R.string.server_result_code_nis_starting),
	/**
	 * NIS is running.
	 */
	NIS_RUNNING(3, R.string.server_result_code_nis_running),
	/**
	 * NIS is booting the local node (implies NIS is running).
	 */
	NIS_BOOTING_LOCAL(4, R.string.server_result_code_nis_booting_local),
	/**
	 * The local node is booted (implies NIS is running).
	 */
	NIS_LOCAL_BOOTED(5, R.string.server_result_code_nis_local_booted),
	/**
	 * The local node is synchronized (implies NIS is running and the local node is booted)
	 */
	NIS_LOCAL_SYNCHRONIZED(6, R.string.server_result_code_nis_local_synchronized);

	@JsonCreator
	public static StatusResultCode fromRaw(final int rawCode) {
		for (StatusResultCode value : values()) {
			if (value._rawCode == rawCode) { return value; }
		}
		Timber.e("Unknown status code: %d", rawCode);
		return UNKNOWN;
	}

	private int     _rawCode;
	@StringRes
	private int _msgRes;

	StatusResultCode(final int rawCode, final @StringRes Integer friendlyMsgRes) {
		_rawCode = rawCode;
		_msgRes = friendlyMsgRes;
	}

	@JsonValue
	@Override
	public int getCode() {
		return _rawCode;
	}

	/**
	 * Always returns true
	 */
	@Override
	public boolean isSuccessful() {
		return true;
	}

	@Override
	@StringRes
	public int getMessageRes() {
		return _msgRes;
	}
}
