package org.nem.nac.common.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class LogUtils {

	public static void tagged(final int priority, final String tagOverride, final String message, final Object... args) {
		if (tagOverride != null) {
			Timber.tag(tagOverride);
		}
		Timber.log(priority, message, args);
	}

	/**
	 * Conditionally logs message produced by factory. Factory will be run only if logging on specified level is enabled and condition is true.
	 */
	public static void conditional(final int priority, final boolean condition, final MessageFactory messageFactory) {
		conditional(priority, condition, null, messageFactory);
	}

	/**
	 * Conditionally logs message produced by factory. Factory will be run only if logging on specified level is enabled and condition is true.
	 *
	 * @param tagOverride If this is not null, log tag will be overridden with it.
	 */
	public static void conditional(final int priority, final boolean condition, @Nullable final String tagOverride, final MessageFactory messageFactory) {
		if (condition && messageFactory != null) {
			if (tagOverride != null) {
				Timber.tag(tagOverride);
				Timber.log(priority, messageFactory.get());
			}
		}
	}

	/**
	 * Conditionally logs message. Message will be logged only if logging on specified level is enabled and condition is true.
	 */
	public static void conditional(final int priority, final boolean condition, final String message) {
		conditional(priority, condition, null, message);
	}

	/**
	 * Conditionally logs message produced by factory. Message will be logged only if logging on specified level is enabled and condition is true.
	 *
	 * @param tagOverride If this is not null, log tag will be overridden with it.
	 */
	public static void conditional(final int priority, final boolean condition, @Nullable final String tagOverride, final String message,
			final Object... args) {
		if (condition && message != null) {
			if (tagOverride != null) {
				Timber.tag(tagOverride);
				Timber.log(priority, message, args);
			}
		}
	}

	public static void splittedMessage(final int priority, final String tag, final String message, final Object... args) {
		final int maxChars = 180;
		int index = 0;
		final String assembledMessage = String.format(message, args);
		if (assembledMessage.length() <= maxChars) {
			Log.println(priority, tag, assembledMessage);
			return;
		}
		List<String> strings = new ArrayList<>();
		Log.println(priority, tag, "> Start");
		while (index < assembledMessage.length()) {
			final String substring = assembledMessage.substring(index, Math.min(index + maxChars, assembledMessage.length()));
			strings.add(substring);
			index += maxChars;
		}
		for (String str : strings) {
			Log.println(priority, tag, "> " + str);
		}
		Log.println(priority, tag, "< End");
	}

	public interface MessageFactory {

		String get();
	}
}
