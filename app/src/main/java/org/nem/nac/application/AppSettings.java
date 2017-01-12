package org.nem.nac.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import org.nem.nac.BuildConfig;
import org.nem.nac.R;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.common.utils.ErrorUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.network.Server;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.ui.activities.AccountListActivity;
import org.nem.nac.ui.activities.ExportAccountActivity;
import org.nem.nac.ui.activities.HarvestDetailsActivity;
import org.nem.nac.ui.activities.LogActivity;
import org.nem.nac.ui.activities.MultisigActivity;
import org.nem.nac.ui.dialogs.EditFieldDialogFragment;
import org.nem.nac.ui.models.MoreItem;
import org.nem.nac.ui.utils.Toaster;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public final class AppSettings {

	private static final String PREFS_FILE_NAME        = "application_settings";
	private static final String PREF_LAST_USED_ADDRESS = "LAST_USED_ADDRESS";
	private static final String PREF_PRIMARY_ADDRESS   = "PRIMARY_ADDRESS";

	private static final String PREF_BOOL_SWITCHED_TO_MAINNET      = "PREF_BOOL_MAINNET_RESET";
	private static final String PREF_BOOL_FIRST_START              = "FIRST_START";
	private static final String PREF_STR_CURRENT_LANG              = "CURRENT_LOCALE";
	private static final String PREF_INT_UPDATE_CHECK_INTERVAL_SEC = "PREF_INT_UPDATE_CHECK_INTERVAL_MS";
	private static final String PREF_BOOL_MAINNET_WARNING_ACCEPTED = "PREF_BOOL_MAINNET_WARNING_ACCEPTED";

	private static final String PREF_BOOL_NOTIFICATION_SOUND              = "PREF_BOOL_NOTIFICATION_SOUND";
	private static final String PREF_BOOL_NOTIFICATION_VIBRATION          = "PREF_BOOL_NOTIFICATION_VIBRATION";
	private static final String PREF_BOOL_NOTIFICATION_SHOW_ON_LOCKSCREEN = "PREF_BOOL_NOTIFICATION_SHOW_ON_LOCKSCREEN";

	private static AppSettings _instance;

	private static       List<MoreItem>       _moreItems           = new ArrayList<>();
	private static       Map<String, Integer> _supportedLocalesRes = new LinkedHashMap<>();
	private static final List<TimeSpan>       _updateIntervals     = new ArrayList<>();
	private static final Server[]             _predefinedServers   = new Server[] {
			new Server("http", "jusan.nem.ninja", AppConstants.DEFAULT_PORT),
			new Server("http", "nijuichi.nem.ninja", AppConstants.DEFAULT_PORT),
			new Server("http", "85.25.36.97", AppConstants.DEFAULT_PORT),
			new Server("http", "62.75.171.41", AppConstants.DEFAULT_PORT),
			new Server("http", "85.25.36.92", AppConstants.DEFAULT_PORT),
			new Server("http", "199.217.112.135", AppConstants.DEFAULT_PORT),
			new Server("http", "san.nem.ninja", AppConstants.DEFAULT_PORT),
			new Server("http", "go.nem.ninja", AppConstants.DEFAULT_PORT),
			new Server("http", "hachi.nem.ninja", AppConstants.DEFAULT_PORT),
			new Server("http", "108.61.182.27", AppConstants.DEFAULT_PORT),
			new Server("http", "104.238.161.61", AppConstants.DEFAULT_PORT),
			new Server("http", "108.61.168.86", AppConstants.DEFAULT_PORT)
	};

	static {
		_moreItems.add(new MoreItem(R.string.more_item_accounts, AccountListActivity::start));
		_moreItems.add(new MoreItem(HarvestDetailsActivity.class, R.string.more_item_harvest_details));
		//_moreItems.add(new MoreItem(MapActivity.class, R.string.more_item_map));
		_moreItems.add(new MoreItem(ExportAccountActivity.class, R.string.more_item_export_account));
		_moreItems.add(new MoreItem(MultisigActivity.class, R.string.more_item_multisig));
		if (BuildConfig.DEBUG) {
			_moreItems.add(new MoreItem(LogActivity.class, R.string.more_item_see_log));
		}
		_moreItems.add(new MoreItem(R.string.debug_send_report, a -> {
			final EditFieldDialogFragment dialog =
					EditFieldDialogFragment.create(R.string.debug_report_comment, NacApplication.getResString(R.string.debug_hint_report_comment), "", true);
			dialog.setOnConfirmListener(d -> {
				final String comment = dialog.getValue();
				ErrorUtils.sendSilentReport("Report sent: " + (comment != null ? comment : null), null);
				Toaster.instance().show("Sent");
				return true;
			})
					.show(a.getFragmentManager(), null);
		}));
		_moreItems.add(new MoreItem(R.string.debug_delete_log_file, a -> {
			final File filesDir = a.getFilesDir();
			final File file = new File(filesDir, AppConstants.LOG_FILE_NAME);
			if (file.exists() && file.delete()) {
				Toaster.instance().show(R.string.debug_log_file_deleted);
			}
			else {
				Toaster.instance().show(R.string.debug_log_file_deleted);
			}
		}));

		_supportedLocalesRes.put("en", R.string.lang_name_en);
		_supportedLocalesRes.put("de", R.string.lang_name_de);
		_supportedLocalesRes.put("es", R.string.lang_name_es);
		_supportedLocalesRes.put("fi", R.string.lang_name_fi);
		_supportedLocalesRes.put("fr", R.string.lang_name_fr);
		_supportedLocalesRes.put("hr", R.string.lang_name_hr);
		_supportedLocalesRes.put("in", R.string.lang_name_in);
		_supportedLocalesRes.put("it", R.string.lang_name_it);
		_supportedLocalesRes.put("ja", R.string.lang_name_ja);
		_supportedLocalesRes.put("lt", R.string.lang_name_lt);
		_supportedLocalesRes.put("nl", R.string.lang_name_nl);
		_supportedLocalesRes.put("pl", R.string.lang_name_pl);
		_supportedLocalesRes.put("pt", R.string.lang_name_pt);
		//_supportedLocalesRes.put("ru", R.string.lang_name_ru);
		_supportedLocalesRes.put("zh", R.string.lang_name_zh);
		//_supportedLocalesRes.put("ko", R.string.lang_name_ko);

		if (BuildConfig.DEBUG) {
			_updateIntervals.add(TimeSpan.fromMinutes(0.2));
			_updateIntervals.add(TimeSpan.fromMinutes(0.5));
		}
		_updateIntervals.add(TimeSpan.fromMinutes(10));
		_updateIntervals.add(TimeSpan.fromMinutes(30));
		_updateIntervals.add(TimeSpan.fromHours(1));
		_updateIntervals.add(TimeSpan.fromHours(2));
		_updateIntervals.add(TimeSpan.fromHours(4));
		_updateIntervals.add(TimeSpan.fromHours(12));
		_updateIntervals.add(TimeSpan.fromHours(24));
	}

	public static AppSettings instance() {
		if (_instance == null) {
			synchronized (AppSettings.class) {
				if (_instance == null) {
					_instance = new AppSettings(NacApplication.getAppContext());
				}
			}
		}
		return _instance;
	}

	private final SharedPreferences _sharedPreferences;

	AppSettings(final Context context) {
		_sharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
	}

	//region Settings

	//region LastUsedAccount

	public synchronized void saveLastUsedAccAddress(@NonNull final AddressValue address) {
		_sharedPreferences.edit().putString(PREF_LAST_USED_ADDRESS, address.getRaw()).apply();
	}

	public synchronized Optional<AddressValue> readLastUsedAccAddress() {
		final String address = _sharedPreferences.getString(PREF_LAST_USED_ADDRESS, null);
		return address != null && AddressValue.isValid(address)
				? Optional.of(AddressValue.fromValue(address))
				: Optional.empty();
	}

	public synchronized void clearLastUsedAccAddress() {
		_sharedPreferences.edit().remove(PREF_LAST_USED_ADDRESS).apply();
	}
	//endregion

	//region Primary Account

	public synchronized void savePrimaryAddress(@NonNull final AddressValue address) {
		_sharedPreferences.edit().putString(PREF_PRIMARY_ADDRESS, address.getRaw()).apply();
	}

	public synchronized Optional<AddressValue> getPrimaryAddress() {
		final String address = _sharedPreferences.getString(PREF_PRIMARY_ADDRESS, null);
		return address != null && AddressValue.isValid(address)
				? Optional.of(AddressValue.fromValue(address))
				: Optional.empty();
	}

	public synchronized void clearPrimaryAddress() {
		_sharedPreferences.edit().remove(PREF_PRIMARY_ADDRESS).apply();
	}
	//endregion

	//region firstStart

	public synchronized void setFirstStart() {
		_sharedPreferences.edit()
				.putBoolean(PREF_BOOL_FIRST_START, true)
				.apply();
	}

	public synchronized boolean getFirstStart() {
		return !_sharedPreferences.contains(PREF_BOOL_FIRST_START);
	}

	public void removeFirstStart() {
		_sharedPreferences.edit().remove(PREF_BOOL_FIRST_START).apply();
	}

	//endregion

	//region switched to mainnet
	public synchronized boolean getSwitchedToMainnet() {
		return _sharedPreferences.contains(PREF_BOOL_SWITCHED_TO_MAINNET);
	}

	public synchronized void setSwitchedToMainnet() {
		_sharedPreferences.edit()
				.putBoolean(PREF_BOOL_SWITCHED_TO_MAINNET, true)
				.apply();
	}
	//endregion

	//region mainnet warning confirm

	public void setMainnetWarningAccepted() {
		_sharedPreferences.edit().putBoolean(PREF_BOOL_MAINNET_WARNING_ACCEPTED, true).commit();
	}

	public boolean getMainnetWarningAccepted() {
		return _sharedPreferences.contains(PREF_BOOL_MAINNET_WARNING_ACCEPTED);
	}
	//endregion

	//region Locale

	public synchronized void setAppLang(@Nullable final String lang) {
		if (StringUtils.isNullOrEmpty(lang)) {
			_sharedPreferences.edit().remove(PREF_STR_CURRENT_LANG).apply();
			Timber.i("App language setting removed");
			return;
		}
		_sharedPreferences.edit()
				.putString(PREF_STR_CURRENT_LANG, lang)
				.apply();
		Timber.i("New app language: %s", lang);
	}

	@NonNull
	public synchronized String getAppLang() {
		return _sharedPreferences.getString(PREF_STR_CURRENT_LANG, "");
	}
	//endregion

	//region Update notifications

	public List<TimeSpan> getUpdateIntervals() {
		return _updateIntervals;
	}

	public synchronized void setUpdatesCheckInterval(@Nullable final TimeSpan interval) {
		if (interval == null) {
			Timber.i("Set update interval null - notifications disabled");
			_sharedPreferences.edit().remove(PREF_INT_UPDATE_CHECK_INTERVAL_SEC).apply();
			return;
		}
		final int intervalSec = (int)interval.toSeconds();
		Timber.i("New update check interval: %dsec", intervalSec);
		_sharedPreferences.edit()
				.putInt(PREF_INT_UPDATE_CHECK_INTERVAL_SEC, intervalSec)
				.apply();
	}

	public synchronized Optional<TimeSpan> getUpdatesCheckInterval() {
		final int interval = _sharedPreferences.getInt(PREF_INT_UPDATE_CHECK_INTERVAL_SEC, 0);
		return interval > 0 ? Optional.of(TimeSpan.fromSeconds(interval)) : Optional.empty();
	}

	public synchronized void saveNotificationSoundEnabled(final boolean enabled) {
		_sharedPreferences.edit().putBoolean(PREF_BOOL_NOTIFICATION_SOUND, enabled).apply();
	}

	public synchronized boolean getNotificationSoundEnabled() {
		return _sharedPreferences.getBoolean(PREF_BOOL_NOTIFICATION_SOUND, true);
	}

	public synchronized void saveNotificationVibeEnabled(final boolean enabled) {
		_sharedPreferences.edit().putBoolean(PREF_BOOL_NOTIFICATION_VIBRATION, enabled).apply();
	}

	public synchronized boolean getNotificationVibeEnabled() {
		return _sharedPreferences.getBoolean(PREF_BOOL_NOTIFICATION_VIBRATION, true);
	}

	public synchronized void saveNotificationLockScreenEnabled(final boolean enabled) {
		_sharedPreferences.edit().putBoolean(PREF_BOOL_NOTIFICATION_SHOW_ON_LOCKSCREEN, enabled).apply();
	}

	public synchronized boolean getNotificationLockScreenEnabled() {
		return _sharedPreferences.getBoolean(PREF_BOOL_NOTIFICATION_SHOW_ON_LOCKSCREEN, true);
	}
	//endregion

	/**
	 * Returns supported locales as language/display name pairs
	 */
	public Map<String, Integer> getSupportedLocales() {
		return _supportedLocalesRes;
	}

	/**
	 * Returns item list for "More" activity, with resource ids of item names.
	 */
	public List<MoreItem> getMoreItems() {
		return _moreItems;
	}

	public Server[] getPredefinedServers() { // Predefined, don't have to do read check
		return _predefinedServers;
	}

	/**
	 * Warning, it's dangerous!
	 */
	public void deleteSharedPreferences() {
		_sharedPreferences.edit().clear().commit();
	}
	//endregion
}
