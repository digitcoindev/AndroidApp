package org.nem.nac.ui.fragments;

import android.util.Log;

import org.nem.nac.common.utils.LogUtils;
import org.nem.nac.log.LogTags;

public abstract class BaseTabFragment extends NacBaseFragment {

	protected boolean isResumed       = false;
	protected boolean userVisibleHint = false;
	protected boolean isFullyVisible  = false;

	@Override
	public void onResume() {
		super.onResume();
		isResumed = true;
		checkStartFullVisibilityActions();
	}

	@Override
	public void onPause() {
		super.onPause();
		isResumed = false;
		checkStartFullVisibilityActions();
	}

	@Override
	public void setUserVisibleHint(final boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		this.userVisibleHint = isVisibleToUser;
		LogUtils.conditional(Log.DEBUG, LogTags.TAB_FRAGMENT_LIFECYCLE.isLogged, LogTags.TAB_FRAGMENT_LIFECYCLE.name, "setUserVisibleHint(%s)+", isVisibleToUser);
		checkStartFullVisibilityActions();
		LogUtils.conditional(Log.DEBUG, LogTags.TAB_FRAGMENT_LIFECYCLE.isLogged, LogTags.TAB_FRAGMENT_LIFECYCLE.name, "setUserVisibleHint(%s)-", isVisibleToUser);
	}

	protected void onFullyVisible() {
		Log.d("FRAGMENT", "FULLY VISIBLE " + this.getClass().getSimpleName());
	}

	protected void onHiding() {
		Log.d("FRAGMENT", "HIDDEN " + this.getClass().getSimpleName());
	}

	private void checkStartFullVisibilityActions() {
		final boolean prevFullyVisible = this.isFullyVisible;
		this.isFullyVisible = isResumed && userVisibleHint;
		if (isFullyVisible && !prevFullyVisible) {
			onFullyVisible();
		}
		else if (!isFullyVisible && prevFullyVisible) { onHiding(); }
	}
}
