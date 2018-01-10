package org.nem.nac.ui.models;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.annimon.stream.function.Consumer;

import org.nem.nac.ui.activities.NacBaseActivity;

public final class MoreItem {
	public final Class<? extends NacBaseActivity> activity;
	@StringRes
	public final int                              nameRes;
	@Nullable
	public final Consumer<NacBaseActivity> actionOverride;

	public MoreItem(final Class<? extends NacBaseActivity> activity, final int nameRes) {
		this.activity = activity;
		this.nameRes = nameRes;
		this.actionOverride = null;
	}

	public MoreItem(final int nameRes, @Nullable final Consumer<NacBaseActivity> actionOverride) {
		this.activity = null;
		this.nameRes = nameRes;
		this.actionOverride = actionOverride;
	}
}
