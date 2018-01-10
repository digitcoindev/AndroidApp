package org.nem.nac.common.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.nem.nac.R;
import org.nem.nac.common.exceptions.NacUserVisibleException;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public final class IOUtils {

	private static final String QR_SAVE_LOG_TAG = "QR CODE SAVING";
	public static void closeSilently(@Nullable final Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			Timber.i(e, "Failed to close closeable");
		}
	}

	/**
	 * @param fileName filename (only) for stored file.
	 * @return absolute path of saved file.
	 * @throws NacUserVisibleException If something went wrong.
	 */
	public static String saveBitmapToFile(@NonNull final Bitmap bitmap, @NonNull final String fileName)
			throws NacUserVisibleException {
		AssertUtils.notNull(bitmap);

		LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Saving QR to file");
		final String externalStorageState = Environment.getExternalStorageState();
		if (!externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
			Timber.w(new Throwable(), "External storage state: %s", externalStorageState);
			throw new NacUserVisibleException(R.string.errormessage_media_not_accessible);
		}
		LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Got storage state");
		final File root = Environment.getExternalStorageDirectory();
		if (!root.canWrite()) {
			Timber.w(new Throwable(), "External storage dir not writable!");
			throw new NacUserVisibleException(R.string.errormessage_storage_dir_not_writable);
		}
		LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Have permission to write");
		final File file = new File(root, fileName);
		FileOutputStream os = null;
		try {
			LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Creating file output stream");
			os = new FileOutputStream(file);
			LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Compressing bitmap");
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Flushing stream");
			os.flush();
			LogUtils.tagged(Log.DEBUG, QR_SAVE_LOG_TAG, "Stream flushed");
			return file.getAbsolutePath();
		} catch (IOException e) {
			throw new NacUserVisibleException(R.string.errormessage_error_occured, e);
		} finally {
			IOUtils.closeSilently(os);
		}
	}
}
