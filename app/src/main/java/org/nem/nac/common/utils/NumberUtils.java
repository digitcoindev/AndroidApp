package org.nem.nac.common.utils;

import com.annimon.stream.Optional;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

public final class NumberUtils {

	/**
	 * Parses string to double, using current locale
	 */
	public static double parseDouble(final String s) {
		try {
			return getNFormatInstanceForCurrentLocale().parse(s).doubleValue();
		} catch (ParseException e) {
			throw new NumberFormatException("Failed to parse \"" + s + "\" to double");
		}
	}

	/**
	 * Parses string to int, using current locale
	 */
	public static int parseInt(final String s) {
		try {
			return NumberFormat.getIntegerInstance().parse(s).intValue();
		} catch (ParseException e) {
			throw new NumberFormatException("Failed to parse \"" + s + "\" to int");
		}
	}

	/**
	 * Returning number parsed to string, using current locale
	 */
	public static String toString(final double d) {
		final NumberFormat numberFormat = getNFormatInstanceForCurrentLocale();
		return numberFormat.format(d);
	}

	/**
	 * Returning number parsed to string, using current locale
	 *
	 * @param fractionDigits how many digits to show after decimal separator
	 */
	public static String toString(final double d, final int fractionDigits) {
		final NumberFormat format = getNFormatInstanceForCurrentLocale();
		format.setMinimumFractionDigits(fractionDigits);
		format.setMaximumFractionDigits(fractionDigits);
		return format.format(d);
	}

	/**
	 * Returning number in %.02f format parsed to string, using current locale
	 */
	public static String toAmountString(final double d) {
		final NumberFormat format = getNFormatInstanceForCurrentLocale();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(6);
		return format.format(d);
	}

	/**
	 * Returning number parsed to string, using current locale
	 */
	public static String toString(final int i) {
		final NumberFormat numberFormat = getNFormatInstanceForCurrentLocale();
		return numberFormat.format(i);
	}

	public static Optional<Character> getDecimalSeparator() {
		final NumberFormat df = DecimalFormat.getInstance(LocaleUtils.getCurrentAvailable(true).get());
		if (df instanceof DecimalFormat) {
			DecimalFormatSymbols formatSymbols = ((DecimalFormat)df).getDecimalFormatSymbols();
			return Optional.of(formatSymbols.getDecimalSeparator());
		}
		return Optional.empty();
	}

	public static int compare(final int lhs, final int rhs) {
		return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
	}

	private static NumberFormat getNFormatInstanceForCurrentLocale() {
		final NumberFormat instance = NumberFormat.getInstance(LocaleUtils.getCurrentAvailable(true).get());
		instance.setRoundingMode(RoundingMode.DOWN);
		return instance;
	}
}
