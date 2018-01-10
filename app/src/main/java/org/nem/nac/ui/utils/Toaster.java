package org.nem.nac.ui.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;

/**
 * Handler class to show {@link Toast}s from any thread.
 */
public final class Toaster {

	private static Toaster _instance;

	public static synchronized Toaster instance() {
		if (_instance == null) {
			_instance = new Toaster();
		}
		return _instance;
	}

	private Handler _handler = new Handler(Looper.getMainLooper());

	private Toaster() {
	}

	public void showGeneralError() {
		show(R.string.errormessage_error_occured, Length.SHORT);
	}

	public void showGeneralErrorLong() {
		show(R.string.errormessage_error_occured, Length.LONG);
	}

	/**
	 * Shows toast with {@link Toast#LENGTH_SHORT} length.
	 */
	public void show(@StringRes int msgRes) {
		show(msgRes, Length.SHORT);
	}

	/**
	 * @param length acts as {@link Toast} length constants
	 */
	public void show(@StringRes int msgRes, Length length) {
		final String message = NacApplication.getAppContext().getString(msgRes);
		show(message, length);
	}

	/**
	 * Shows toast with {@link Toast#LENGTH_SHORT} length.
	 */
	public void show(String msg) {
		show(msg, Length.SHORT);
	}

	/**
	 * @param length acts as {@link Toast} length constants
	 */
	public void show(String msg, Length length) {
		_handler.post(() -> {
			//noinspection ResourceType
			final Toast toast = Toast.makeText(NacApplication.getAppContext(), msg, length != null ? length.getRaw() : Toast.LENGTH_SHORT);
			View v = toast.getView().findViewById(android.R.id.message);
			if (v != null && v instanceof TextView) { ((TextView)v).setGravity(Gravity.CENTER); }
			toast.show();
		});
	}

	public enum Length {
		SHORT(Toast.LENGTH_SHORT),
		LONG(Toast.LENGTH_LONG);

		private int _raw;

		Length(final int raw) {
			_raw = raw;
		}

		public int getRaw() {
			return _raw;
		}
	}
}
