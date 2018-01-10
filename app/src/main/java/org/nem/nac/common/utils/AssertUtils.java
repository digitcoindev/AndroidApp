package org.nem.nac.common.utils;

import android.support.annotation.Nullable;

import org.nem.nac.BuildConfig;

public class AssertUtils {
	public static void isTrue(final boolean expr) {
		if (BuildConfig.DEBUG) {
			if (!expr) {
				throw new AssertionError();
			}
		}
	}

	public static void notNull(@Nullable Object object, @Nullable String message) {
		if (BuildConfig.DEBUG) {
			if (null == object) {
				throw new AssertionError(message);
			}
		}
	}

	/**
	 * Throws an assertion error if <b>any</b> of arguments is null
	 */
	public static void notNull(final Object... objects) {
		if (BuildConfig.DEBUG) {
			for (Object obj : objects) {
				if (null == obj) {
					throw new AssertionError("Object was null");
				}
			}
		}
	}
}
