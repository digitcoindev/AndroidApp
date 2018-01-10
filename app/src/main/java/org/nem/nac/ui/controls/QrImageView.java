package org.nem.nac.ui.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.common.models.Size;
import org.nem.nac.common.utils.LogUtils;
import org.nem.nac.log.LogTags;
import org.nem.nac.models.qr.QrDto;
import org.nem.nac.tasks.EncodeQrAsyncTask;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class QrImageView extends ImageView {

	private static final String LOG_TAG = QrImageView.class.getSimpleName();

	private QrDto _dto;
	private Consumer<Bitmap> _qrShowedListener;
	private boolean _execListener = false;
	private boolean _layoutComplete = false;
	private EncodeQrAsyncTask _encodeTask;

	public QrImageView(Context context) {
		super(context);
		init();
	}

	public QrImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public QrImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setOnQrRenderedListener(final Consumer<Bitmap> listener) {
		_qrShowedListener = listener;
	}

	@MainThread
	public void setQrDto(@Nullable final QrDto dto) {
		LogUtils.conditional(Log.DEBUG, LogTags.QR_CREATION.isLogged, LogTags.QR_CREATION.name, "QR Set");
		LogUtils.tagged(Log.DEBUG, LOG_TAG, "setQrDto(%s)", dto);
		if (dto == null) {
			setImageBitmap(null);
			return;
		}
		_dto = dto;
		_execListener = true;
		if (_layoutComplete) {
			showQr();
		}
	}

	public void invalidateQr() {
		setTag(R.id.tagkey_int_qr_hash, null);
	}

	private void showQr() {
		LogUtils.conditional(Log.DEBUG, LogTags.QR_CREATION.isLogged, LogTags.QR_CREATION.name, "showQr()");
		if (!_dto.data.validate()) {
			LogUtils.tagged(Log.ERROR, LOG_TAG, "Malformed QR supplied! %s", _dto.type);
			Toaster.instance().show(R.string.errormessage_malformed_qr);
			return;
		}

		final int width = getWidth();
		if (width == 0) {
			Timber.w("Skipping rendering QR with width 0");
			return;
		}
		if (_encodeTask != null) {
			_encodeTask.cancel(true);
			_encodeTask = null;
		}
		//noinspection SuspiciousNameCombination
		_encodeTask = (EncodeQrAsyncTask)new EncodeQrAsyncTask(_dto, new Size(width, width))
				.withCompleteCallback((task, result) -> {
					LogUtils.conditional(Log.DEBUG, LogTags.QR_CREATION.isLogged, LogTags.QR_CREATION.name, "Task completed");
					final Bitmap bitmap = result.getResult().isPresent() ? result.getResult().get() : null;
					if (bitmap != null) {
						setImageBitmap(bitmap);
						setTag(R.id.tagkey_int_qr_hash, _dto.hashCode());
						LogUtils.tagged(Log.DEBUG, LOG_TAG, "Rendered QR with size: %d", width);
					}
					else {
						LogUtils.tagged(Log.ERROR, LOG_TAG, "Failed to render QR! %s", _dto.type);
					}
					if (_qrShowedListener != null && _execListener) {
						_qrShowedListener.accept(bitmap);
						_execListener = false;
					}
				});
		_encodeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		LogUtils.conditional(Log.DEBUG, LogTags.QR_CREATION.isLogged, LogTags.QR_CREATION.name, "Task started");
	}

	private void init() {
		getViewTreeObserver().addOnGlobalLayoutListener(() -> {
			Timber.d("Layout complete");
			_layoutComplete = true;
			final Integer hashCode = (Integer)getTag(R.id.tagkey_int_qr_hash);
			if (_dto != null && (hashCode == null || hashCode != _dto.hashCode())) {
				showQr();
			}
		});
	}
}
