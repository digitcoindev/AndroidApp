package org.nem.nac.tasks;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.nem.nac.common.models.Size;
import org.nem.nac.common.utils.JsonUtils;
import org.nem.nac.models.qr.QrDto;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class EncodeQrAsyncTask extends BaseAsyncTask<EncodeQrAsyncTask, Void, Void, Bitmap> {

	private QrDto _dto;
	private Size  _size;

	public EncodeQrAsyncTask(final QrDto dto, final Size codeSize) {
		if (dto == null) {
			throw new IllegalArgumentException("Dto was null");
		}
		_dto = dto;
		_size = codeSize;
		if (_size == null) { _size = new Size(100, 100); }
	}

	@Override
	protected Bitmap doInBackground(final Void... params) {
		try {
			final String json = JsonUtils.toJson(_dto);
			Timber.v("Serializing to QR: %s", json);
			Map<EncodeHintType, Object> hints = new HashMap<>(2);
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.MARGIN, 1);
			if (isCancelled()) { return null; }
			final BitMatrix codeMatrix = new QRCodeWriter().encode(json, BarcodeFormat.QR_CODE, _size.width, _size.height, hints);
			if (isCancelled()) { return null; }
			return toBitmap2(codeMatrix);
		} catch (JsonUtils.ParseException | WriterException e) {
			Timber.e(e, "Failed to encode dto: %s", e.getMessage());
			return null;
		}
	}

//	private Bitmap toBitmap(final BitMatrix bitMatrix) {
//		final int height = bitMatrix.getHeight();
//		final int width = bitMatrix.getWidth();
//		final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
//			}
//		}
//		return bmp;
//	}

	private Bitmap toBitmap2(final BitMatrix bitMatrix) {
		final int height = bitMatrix.getHeight();
		final int width = bitMatrix.getWidth();
		final int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}
