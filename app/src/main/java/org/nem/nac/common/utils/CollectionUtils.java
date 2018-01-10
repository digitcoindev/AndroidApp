package org.nem.nac.common.utils;

import android.support.annotation.NonNull;

import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CollectionUtils {

	public static <T> void addAllMatches(@NonNull final Collection<T> src, @NonNull final Collection<T> dst, @NonNull final Predicate<T> predicate) {
		AssertUtils.notNull(src, dst, predicate);
		for (T item : src) {
			if (predicate.test(item)) {
				dst.add(item);
			}
		}
	}

	/**
	 * Returns elements starting from position 0 while elements does not meet condition.
	 */
	public static <T> List<T> getWhileNotMatch(final List<T> collection, @NonNull final Predicate<T> condition) {
		AssertUtils.notNull(collection, condition);
		final ArrayList<T> result = new ArrayList<>(collection.size());
		for (T item : collection) {
			if (condition.test(item)) {
				break;
			}
			result.add(item);
		}
		return result;
	}

	public static class Diff<TCollection extends Collection<?>> {

		public final TCollection uniqueLeft;
		public final TCollection uniqueRight;

		public Diff(@NonNull final TCollection uniqueLeft, @NonNull final TCollection uniqueRight) {
			AssertUtils.notNull(uniqueLeft, uniqueRight);
			this.uniqueLeft = uniqueLeft;
			this.uniqueRight = uniqueRight;
		}
	}

	public static <T> String join(final Iterable<T> collection, final String separator, Function<T, ?> selector) {
		if (collection == null) { return ""; }
		final String safeSeparator = StringUtils.isNotNullOrEmpty(separator) ? separator : ",";
		StringBuilder sb = new StringBuilder();
		for (T element : collection) {
			sb.append(selector != null ? selector.apply(element) : element).append(safeSeparator);
		}
		sb.setLength(Math.max(0, sb.length() - safeSeparator.length()));
		return sb.toString();
	}

	public static <T> boolean any(final Iterable<T> collection, Predicate<T> predicate) {
		if (collection == null) { return false; }
		for (T element : collection) {
			if (predicate.test(element)) { return true; }
		}
		return false;
	}

	public static <T> boolean any(final T[] array, Predicate<T> predicate) {
		if (array == null) { return false; }
		for (T element : array) {
			if (predicate.test(element)) { return true; }
		}
		return false;
	}
}
