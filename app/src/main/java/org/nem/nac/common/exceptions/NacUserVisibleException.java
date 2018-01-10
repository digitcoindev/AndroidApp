package org.nem.nac.common.exceptions;

import android.support.annotation.StringRes;

import org.nem.nac.application.NacApplication;

/**
 * Exception which message can be shown to user.
 */
public final class NacUserVisibleException extends NacException {
	public NacUserVisibleException(@StringRes final int detailMessageRes) {
		super(NacApplication.getAppContext().getString(detailMessageRes));
	}

	public NacUserVisibleException(@StringRes final int detailMessageRes, final Throwable throwable) {
		super(NacApplication.getAppContext().getString(detailMessageRes), throwable);
	}
}
