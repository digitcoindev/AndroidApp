package org.nem.nac.ui.controls;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import org.nem.nac.common.utils.LogUtils;

public final class CameraSurfaceView extends SurfaceView {

	private static final String LOG_TAG = "CamearSurfaceView";

	private Float _ar = null;

	public CameraSurfaceView(final Context context) {
		super(context);
	}

	public CameraSurfaceView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public CameraSurfaceView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Sets surface aspect ratio (surface height = width * ar)
	 */
	public void setAr(final float ar) {
		_ar = ar;
		if (Build.VERSION.SDK_INT < 18) {
			requestLayout();
		}
		else if (!isInLayout()) {
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		if (_ar != null) {
			height = (int)Math.ceil(width * _ar);
		}
		setMeasuredDimension(width, height);
		LogUtils.tagged(Log.VERBOSE, LOG_TAG, "Measured size: %d*%d", width, height);
	}
}
