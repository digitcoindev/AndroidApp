package org.nem.nac.common.utils;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import org.nem.nac.application.NacApplication;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils {

	public static boolean isNullOrEmpty(String s) {
		return null == s || 0 == s.length();
	}

	public static boolean isNotNullOrEmpty(String s) {
		return !isNullOrEmpty(s);
	}

	@NonNull
	public static String format(@StringRes final int formatRes, final Object... args) {
		final String format = NacApplication.getAppContext().getString(formatRes);
		return String.format(format, args);
	}

	public static List<String> split(final String src, final int partSize) {
		List<String> parts = new ArrayList<>();
		int len = src.length();
		for (int i = 0; i < len; i += partSize) {
			parts.add(src.substring(i, Math.min(len, i + partSize)));
		}
		return parts;
	}

	public static boolean equals(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}
}
