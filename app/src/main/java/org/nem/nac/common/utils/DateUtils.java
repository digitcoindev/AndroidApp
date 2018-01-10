package org.nem.nac.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
	public static String format(final Date date, final boolean... timeOnNewLine) {
		final boolean wrap = timeOnNewLine != null && timeOnNewLine.length > 0 && timeOnNewLine[0];
		final String pattern = "dd/MM/yy" + (wrap ? '\n' : ' ') + "HH:mm";
		return date != null ? new SimpleDateFormat(pattern, Locale.US).format(date) : "null";
	}

	public static String formatWithShortMonth(final Date date, final boolean... timeOnNewLine) {
		final boolean wrap = timeOnNewLine != null && timeOnNewLine.length > 0 && timeOnNewLine[0];
		final String pattern = "MMM dd, yyyy" + (wrap ? '\n' : ' ') + "HH:mm:ss";
		return date != null ? new SimpleDateFormat(pattern, Locale.US).format(date) : "null";
	}
}
