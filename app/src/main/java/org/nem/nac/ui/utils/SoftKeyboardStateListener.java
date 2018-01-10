package org.nem.nac.ui.utils;

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.MainThread;
import android.view.View;
import android.view.ViewTreeObserver;

import com.annimon.stream.function.Consumer;

import org.nem.nac.application.AppHost;

import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

@MainThread
public final class SoftKeyboardStateListener {

	private LayoutListener _layoutListener;
	private View           _activityRoot;
	private boolean _installed = false;

	public boolean isInstalled() {
		return _installed;
	}

	/**
	 * Call it after view is created
	 */
	public void install(final View activityRoot, final StateChangeListener listener) {
		_activityRoot = activityRoot;
		_layoutListener = new LayoutListener(activityRoot, listener);
		activityRoot.getViewTreeObserver().addOnGlobalLayoutListener(_layoutListener);
		_installed = true;
		Timber.d("Keyboard listener installed");
	}

	/**
	 * Call this before activity gets destroyed
	 */
	public void uninstallIfInstalled() {
		if (!_installed) {
			return;
		}
		if (Build.VERSION.SDK_INT < 16) {
			_activityRoot.getViewTreeObserver().removeGlobalOnLayoutListener(_layoutListener);
		}
		else {
			_activityRoot.getViewTreeObserver().removeOnGlobalLayoutListener(_layoutListener);
		}
		_installed = false;
		Timber.d("Keyboard listener uninstalled");
	}

	public interface StateChangeListener extends Consumer<Boolean> {

	}

	private final class LayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

		private final View                _view;
		private final StateChangeListener _listener;
		private final float                    _density     = AppHost.Screen.getDensityLogical();
		private final AtomicReference<Boolean> _lastVisible = new AtomicReference<>(null);

		public LayoutListener(final View view, StateChangeListener listener) {
			_view = view;
			_listener = listener;
		}

		@Override
		public void onGlobalLayout() {
			Rect r = new Rect();
			//r will be populated with the coordinates of your view that area still visible.
			_view.getWindowVisibleDisplayFrame(r);

			final int heightDiffPx = _view.getRootView().getHeight() - (r.bottom - r.top);
			final int heightDiffDp = (int)Math.ceil(heightDiffPx / _density);
			if (heightDiffDp > 120) { // if more than this difference, its probably a keyboard...
				final Boolean prev = _lastVisible.getAndSet(true);
				if (_listener != null && (prev == null || !prev)) {
					_listener.accept(true);
				}
			}
			else {
				final Boolean prev = _lastVisible.getAndSet(false);
				if (_listener != null && (prev == null || prev)) {
					_listener.accept(false);
				}
			}
		}
	}
}
