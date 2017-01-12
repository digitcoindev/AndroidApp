package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.ui.fragments.ScanFragment;

import timber.log.Timber;

public final class ScanQrActivity extends NacBaseActivity {

	private ScanFragment _scanFragment;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_scan_qr;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_scan_qr;
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, AddAccountActivity.class));
		finish();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!AppHost.Camera.hasCamera()) {
			Timber.w("Attempt to scan QR on device without camera!");
			Toast.makeText(this, R.string.errormessage_device_doesnt_have_camera, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (_scanFragment == null) {
			_scanFragment = ScanFragment.create(true);
			final FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
			tran.replace(R.id.layout_scan_fragment, _scanFragment);
			tran.commit();
			getHandler().postDelayed(() -> _scanFragment.setUserVisibleHint(true), 50);
		}
	}
}
