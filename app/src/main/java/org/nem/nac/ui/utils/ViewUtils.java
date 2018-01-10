package org.nem.nac.ui.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.view.View;

import org.nem.nac.common.models.Size;
import org.nem.nac.common.utils.AssertUtils;

import timber.log.Timber;

public final class ViewUtils {

	public static void setBgColor(final View view, Context context, @ColorRes int color) {
		setBgColor(view, context.getResources().getColor(color));
	}

	public static void setBgColor(final View view, @ColorInt int color) {
		AssertUtils.notNull(view, "View was null");

		final Drawable bg = view.getBackground();
		if (bg == null) {
			Timber.w("Cannot set background color on %s - has null background!", view);
			return;
		}
		if (bg instanceof ColorDrawable) {
			((ColorDrawable)bg).setColor(color);
		}
		else if (bg instanceof GradientDrawable) {
			((GradientDrawable)bg).setColor(color);
		}
	}

	public static Size measureView(final View view, final int widthMeasureSpec, final int heightMeasureSpec) {
		view.measure(widthMeasureSpec, heightMeasureSpec);
		return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
	}
}
