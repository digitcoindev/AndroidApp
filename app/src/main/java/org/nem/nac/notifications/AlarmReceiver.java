package org.nem.nac.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import timber.log.Timber;

public final class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Timber.d("Alarm received! Timestamp: %dms", SystemClock.elapsedRealtime());
		final Intent serviceIntent = new Intent(context, UpdatesNotificationService.class);
		context.startService(serviceIntent);
	}
}
