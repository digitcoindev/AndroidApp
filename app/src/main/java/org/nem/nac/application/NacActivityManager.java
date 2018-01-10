package org.nem.nac.application;

import com.annimon.stream.Optional;

import org.nem.nac.ui.activities.NacBaseActivity;

public final class NacActivityManager {
	private static NacBaseActivity _currentActivity;

	public static synchronized Optional<NacBaseActivity> getCurrentActivity() {
		return Optional.ofNullable(_currentActivity);
	}

	static void setCurrentActivity(final NacBaseActivity activity) {
		_currentActivity = activity;
	}
}
