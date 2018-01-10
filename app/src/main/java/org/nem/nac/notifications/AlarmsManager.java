package org.nem.nac.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import org.nem.nac.application.NacApplication;
import org.nem.nac.common.TimeSpan;

import java.lang.ref.SoftReference;

import timber.log.Timber;

public final class AlarmsManager {

	private static AlarmsManager _instance;

	public static synchronized AlarmsManager instance() {
		if (_instance == null) {
			_instance = new AlarmsManager();
		}
		return _instance;
	}

	private SoftReference<AlarmManager> _alarmManager = new SoftReference<>(null);

	private AlarmManager getAlarmManager() {
		AlarmManager alarmManager = _alarmManager.get();
		if (alarmManager == null) {
			alarmManager = (AlarmManager)NacApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
			_alarmManager = new SoftReference<>(alarmManager);
		}
		return alarmManager;
	}

	public synchronized void enableUpdatesCheck(final TimeSpan interval) {
		disableUpdatesCheck();
		if (interval == null || interval.equals(TimeSpan.ZERO)) {
			Timber.w("Interval passed was null or zero. Update check cancelled.");
			return;
		}
		final long intervalMs = (long)interval.toMilliSeconds();
		final long firstTrigger = SystemClock.elapsedRealtime() + intervalMs;
		final AlarmManager alarmManager = getAlarmManager();
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTrigger, intervalMs, getPendingIntent());
		Timber.i("Updates check alarm set to %dms", intervalMs);
	}

	public synchronized void disableUpdatesCheck() {
		final AlarmManager alarmManager = getAlarmManager();
		alarmManager.cancel(getPendingIntent());
		Timber.i("Updates check alarm canceled");
	}

	private PendingIntent getPendingIntent() {
		final Context appContext = NacApplication.getAppContext();
		return PendingIntent.getBroadcast(appContext, 0, new Intent(appContext, AlarmReceiver.class), 0);
	}
}
