package org.nem.nac.common.utils;

import org.acra.ACRA;

import timber.log.Timber;

public final class ErrorUtils {

	public static void sendSilentReport(final String message, final Throwable throwable){
		try {
			ACRA.getErrorReporter().handleSilentException(new Throwable(String.format("Error report: %s", message), throwable));
		} catch (Throwable t) {
			Timber.e("Failed to send report");
		}
	}
}
