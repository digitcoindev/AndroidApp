package org.nem.nac.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

@SuppressWarnings("unused")
public class SnackbarPushBehavior extends CoordinatorLayout.Behavior<View> {

	public SnackbarPushBehavior(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(final CoordinatorLayout parent, final View child, final View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(final CoordinatorLayout parent, final View child, final View dependency) {
		final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)child.getLayoutParams();
		final int newMargin = dependency.getHeight();
		final boolean needLayout = newMargin != layoutParams.bottomMargin;
		layoutParams.bottomMargin = newMargin;
		if (needLayout) {
			child.requestLayout();
		}
		return true;
	}

	@Override
	public void onDependentViewRemoved(final CoordinatorLayout parent, final View child, final View dependency) {
		final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)child.getLayoutParams();
		layoutParams.bottomMargin = 0;
	}
}
