package org.nem.nac.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.nem.nac.common.Stopwatch;
import org.nem.nac.common.utils.ErrorUtils;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;

import timber.log.Timber;

public final class UpdatesNotificationService extends Service {

	private final        Stopwatch                                _stopwatch                  = new Stopwatch();

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Timber.i("Service start: updates checker");
		_stopwatch.reset();
		_stopwatch.start();
		new Thread(() -> {
			try {
				new TransactionUpdatesChecker(UpdatesNotificationService.this).check();
			} catch (InterruptedException e) {
				Timber.w(e, "Updates check interrupted");
			} catch (NacPersistenceRuntimeException e) {
				Timber.e(e, "Updates check failed!");
			} catch (Throwable throwable) {
				Timber.e(throwable, "Updates check failed!");
				ErrorUtils.sendSilentReport("Updates check failed!", throwable);
			}
		}, getClass().getSimpleName() + "-background thread").start();
		stopSelf();
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}
}

