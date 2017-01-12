package org.nem.nac.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.annimon.stream.Optional;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.nem.nac.R;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.utils.LogUtils;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.log.LogTags;
import org.nem.nac.models.network.Server;
import org.nem.nac.notifications.AlarmsManager;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.servers.ServerManager;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.utils.Toaster;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import timber.log.Timber;

@ReportsCrashes(
		httpMethod = HttpSender.Method.PUT,
		reportType = HttpSender.Type.JSON,
		formUri = "http://193.84.22.102:8007/acra-nemapp/_design/acra-storage/_update/report",
		formUriBasicAuthLogin = "reporter1",
		formUriBasicAuthPassword = "123456",
		mode = ReportingInteractionMode.TOAST,
		buildConfigClass = org.nem.nac.BuildConfig.class,
		//applicationLogFile = AppConstants.LOG_FILE_NAME,
		//applicationLogFileLines = 150,
		resToastText = R.string.crash_report_toast_text
)
public final class NacApplication extends Application {

	private static volatile Context                _appContext  = null;
	private static volatile SoftReference<Handler> _mainHandler = new SoftReference<>(null);

	public static Context getAppContext() {
		if (null == _appContext) {
			throw new NacRuntimeException("App context has not been initialized");
		}
		return _appContext;
	}

	public static String getResString(@StringRes int resId, @Nullable Object... formatArgs) {
		return formatArgs != null && formatArgs.length > 0
				? getAppContext().getString(resId, formatArgs)
				: getAppContext().getString(resId);
	}

	/**
	 * Returns {@link Handler} for the main thread
	 */
	public static synchronized Handler getMainHandler() {
		Handler handler = _mainHandler.get();
		if (handler == null) {
			handler = new Handler(Looper.getMainLooper());
			_mainHandler = new SoftReference<>(handler);
		}
		return handler;
	}

	private static final Runnable ON_APP_PAUSE = () -> {
		LogUtils.conditional(Log.WARN, LogTags.EKEY_GET_SET.isLogged, LogTags.EKEY_GET_SET.name, "App Pause");
		EKeyProvider.instance().setKey(null);
		System.gc();
	};

	@Override
	protected void attachBaseContext(final Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
		ACRA.init(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// Note: this won't work with all ACRA configurations cause ACRA not always pass control to default handler
		//Thread.setDefaultUncaughtExceptionHandler(new ThreadExceptionHandler());
		_appContext = this.getApplicationContext();
		Timber.plant(new FileLoggingTree());

		if (!Charset.defaultCharset().equals(Charset.forName("utf-8"))) {
			Toaster.instance().show("Cannot continue!\nUtf-8 is not default charset", Toaster.Length.LONG);
			throw new NacRuntimeException("Utf-8 is not default charset!");
		}
		if (!checkUtf8support()) {
			Toaster.instance().show("Cannot continue!\nUtf-8 is not supported!", Toaster.Length.LONG);
			throw new NacRuntimeException("Utf-8 is not supported on this device!");
		}

		NemSQLiteHelper.setAppContext(_appContext);

		setActivityLifecycleCallbacks();

		final AppSettings appSettings = AppSettings.instance();
		final boolean switchedToMainnet = appSettings.getSwitchedToMainnet();
		if (!switchedToMainnet) {
			Timber.w("Removing preferences due to switching to mainnet");
			appSettings.deleteSharedPreferences();
			appSettings.setSwitchedToMainnet();
			try {
				NemSQLiteHelper.getInstance().getWritableDbCompartment(); // init/upgrade database
			} catch (NacException e) {
				Timber.e(e, "Failed to init database");
			}
		}
		//
		if (appSettings.getFirstStart() || !switchedToMainnet) {
			Timber.i("Adding default servers");
			final Server[] predefinedServers = appSettings.getPredefinedServers();
			final ServerManager serverManager = ServerManager.instance();
			for (Server server : predefinedServers) {
				serverManager.addServer(server);
			}
			appSettings.setFirstStart();
		}
	}

	private boolean checkUtf8support() {
		final String initial = "test";
		try {
			final byte[] bytes = initial.getBytes(AppConstants.ENCODING_UTF8);
			final String test = new String(bytes, AppConstants.ENCODING_UTF8);
			return test.equals(initial);
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	private void setActivityLifecycleCallbacks() {
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(final Activity a, final Bundle savedInstanceState) {
				Timber.d(" => ACTIVITY CREATE next: %s", a.getClass().getSimpleName());
			}

			@Override
			public void onActivityStarted(final Activity a) {
				Timber.d(" => ACTIVITY START next: %s", a.getClass().getSimpleName());
				if (a instanceof NacBaseActivity) {
					NacActivityManager.setCurrentActivity((NacBaseActivity)a);
				}
			}

			@Override
			public void onActivityResumed(final Activity a) {
				getMainHandler().removeCallbacks(ON_APP_PAUSE);
				Timber.d(" => ACTIVITY RESUME next: %s", a.getClass().getSimpleName());
				AlarmsManager.instance().disableUpdatesCheck();
			}

			@Override
			public void onActivityPaused(final Activity a) {
				getMainHandler().postDelayed(ON_APP_PAUSE, 3000);
				Timber.d(" => ACTIVITY PAUSE next: %s", a.getClass().getSimpleName());
				final Optional<TimeSpan> updatesCheckInterval = AppSettings.instance().getUpdatesCheckInterval();
				if (updatesCheckInterval.isPresent()) {
					AlarmsManager.instance().enableUpdatesCheck(updatesCheckInterval.get());
				}
			}

			@Override
			public void onActivityStopped(final Activity a) {
				Timber.d(" => ACTIVITY STOP next: %s", a.getClass().getSimpleName());
				NacActivityManager.getCurrentActivity().ifPresent(x -> {
					if (a.equals(x)) {
						NacActivityManager.setCurrentActivity(null);
					}
				});
			}

			@Override
			public void onActivitySaveInstanceState(final Activity a, final Bundle outState) { }

			@Override
			public void onActivityDestroyed(final Activity a) { }
		});
	}

	private static final class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

		private final Thread.UncaughtExceptionHandler _defaultExceptionHandler;

		public ThreadExceptionHandler() {
			_defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		}

		@Override
		public void uncaughtException(final Thread thread, final Throwable ex) {
			LogUtils.tagged(Log.DEBUG, "NacApplication", "Default exception handler");
			if (_defaultExceptionHandler != null) {
				_defaultExceptionHandler.uncaughtException(thread, ex);
			}
			else {
				System.exit(1);
			}
		}
	}

	private final class FileLoggingTree extends Timber.DebugTree {

		private final SimpleDateFormat     DATE_FORMAT = new SimpleDateFormat("d/MM/yy kk:mm:ss.SSS", Locale.US);
		private final Map<Integer, String> _logLevels  = new HashMap<>();
		private final Calendar _calendar;
		private       TimeSpan _calendarUpdate;

		public FileLoggingTree() {
			_logLevels.put(Log.VERBOSE, "VRB");
			_logLevels.put(Log.DEBUG, "DBG");
			_logLevels.put(Log.INFO, "INF");
			_logLevels.put(Log.WARN, "WRN");
			_logLevels.put(Log.ERROR, "ERR");
			_logLevels.put(Log.ASSERT, "WTF");

			_calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			_calendarUpdate = TimeSpan.now();
		}

		@Override
		protected void log(final int priority, final String tag, final String message, final Throwable t) {
			super.log(priority, tag, message, t);

			if (!LogTags.logToFile(tag)) {
				return;
			}

			final TimeSpan now = TimeSpan.now();
			final double offset = now.subtract(_calendarUpdate).toMilliSeconds();
			_calendarUpdate = now;
			_calendar.add(Calendar.MILLISECOND, ((int)offset));

			final String logString = String.format("=> %s / %s / %s: %s %s",
					DATE_FORMAT.format(_calendar.getTime()), _logLevels.get(priority), tag, message, t != null ? "\n" + t : "");

			try {
				LogFile.instance().write(logString);
			} catch (IOException e) {
				Log.e("LOG_FILE", "Failed to create/log to file");
			}
		}
	}
}

