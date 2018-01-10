package org.nem.nac.models.api;

import android.support.annotation.StringRes;

public interface ApiResultCode {

	int UNKNOWN = -1;

	int getCode();

	boolean isSuccessful();

	@StringRes
	int getMessageRes();
}
