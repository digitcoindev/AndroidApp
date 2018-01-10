package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import org.nem.nac.R;
import org.nem.nac.application.AppSettings;
import org.nem.nac.ui.adapters.MoreAdapter;
import org.nem.nac.ui.models.MoreItem;

import java.util.List;

import timber.log.Timber;

public final class MoreActivity extends NacBaseActivity {

	public static final String EXTRA_CLASS_RETURN_TO_ACTIVITY = MoreActivity.class.getCanonicalName() + "extra-ret-to-activity";

	private Class<? extends NacBaseActivity> _returnTo;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_more;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_more;
	}

	@Override
	public void onBackPressed() {
		finish();
		final Class<? extends NacBaseActivity> returnTo = _returnTo != null ? _returnTo : DashboardActivity.class;
		startActivity(new Intent(this, returnTo));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			try {
				//noinspection unchecked
				_returnTo = (Class<? extends NacBaseActivity>)extras.getSerializable(EXTRA_CLASS_RETURN_TO_ACTIVITY);
			} catch (ClassCastException e) {
				Timber.w("Invalid return activity passed");
				_returnTo = null;
			}
		}

		ListView moreListview = (ListView)findViewById(R.id.listview_more);
		final List<MoreItem> items = AppSettings.instance().getMoreItems();
		moreListview.setAdapter(new MoreAdapter(this, items));
	}
}
