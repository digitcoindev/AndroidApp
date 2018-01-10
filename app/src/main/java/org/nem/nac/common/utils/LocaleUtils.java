package org.nem.nac.common.utils;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppSettings;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LocaleUtils {

	private static SoftReference<Map<String, List<Locale>>> _available = new SoftReference<>(new HashMap<>());

	/**
	 * Returns current application locale, chosen amongst available on device, based on stored lang setting.
	 * This method may be not thread-safe
	 *
	 * @return if useNewObjectWhenNotAvailable is true, this method always return non-null locale.
	 */
	public static Optional<Locale> getCurrentAvailable(final boolean useNewObjectWhenNotAvailable) {
		final String appLang = AppSettings.instance().getAppLang();
		final Locale defaultLocale = Locale.getDefault();
		//
		if (appLang.isEmpty()) {
			if (AppSettings.instance().getSupportedLocales().containsKey(defaultLocale.getLanguage())) { // known locale
				return Optional.of(defaultLocale);
			}
			//
			final Optional<Locale> availableForDefault = getFirstAvailableForLangCode(AppConstants.DEFAULT_LANGUAGE);
			if (availableForDefault.isPresent()) { return availableForDefault; }
			return Optional.ofNullable(useNewObjectWhenNotAvailable ? new Locale(AppConstants.DEFAULT_LANGUAGE) : null);
		}
		else {
			final Optional<Locale> available = getFirstAvailableForLangCode(appLang);
			if (available.isPresent()) { return available; }
			return Optional.ofNullable(useNewObjectWhenNotAvailable ? new Locale(appLang) : null);
		}
	}

	private static Locale _current;

	private static Optional<Locale> getFirstAvailableForLangCode(final String appLang) {
		if (_current != null && _current.getLanguage().equalsIgnoreCase(appLang)) {
			return Optional.of(_current);
		}
		final List<Locale> localesForLang = getAvailable().get(appLang.toLowerCase());
		if (localesForLang != null && !localesForLang.isEmpty()) {
			final Locale current = localesForLang.get(0);
			_current = current;
			return Optional.of(current);
		}
		return Optional.empty();
	}

	private static Map<String, List<Locale>> getAvailable() {
		Map<String, List<Locale>> current = _available.get();
		if (current == null) {
			final Map<String, List<Locale>> available = new HashMap<>();
			Stream.of(Locale.getAvailableLocales())
					.forEach(x -> {
						final String lang = x.getLanguage().toLowerCase();
						final List<Locale> langLocales = available.get(lang);
						if (langLocales == null) {
							available.put(lang, new ArrayList<>());
						}
						available.get(lang).add(x);
					});
			current = available;
			_available = new SoftReference<>(current);
		}
		return current;
	}
}
