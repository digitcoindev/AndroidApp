package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;

import timber.log.Timber;

public final class AddAccountActivity extends NacBaseActivity {

	@Override
	protected int getLayoutId() {
		return R.layout.activity_add_account;
	}

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_add_account;
	}

	@Override
	public void onBackPressed() {
		AccountListActivity.start(this);
		finish();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final View btnCreate = findViewById(R.id.linear_layout_btn_create_account);
		btnCreate.setOnClickListener(this::onCreateClick);
		final View btnScanQr = findViewById(R.id.linear_layout_btn_scan_qr);
		btnScanQr.setOnClickListener(this::onScanQrClick);
		final View btnImportKey = findViewById(R.id.linear_layout_btn_import_key);
		btnImportKey.setOnClickListener(this::onImportKeyClick);
	}

	private void onCreateClick(final View clicked) {
		startActivity(new Intent(this, CreateAccountActivity.class));
	}

	private void onScanQrClick(final View clicked) {
		if (AppHost.Camera.hasCamera()) {
			startActivity(new Intent(this, ScanQrActivity.class));
		}
		else {
			Timber.w("Attempt to scan QR on device without camera!");
			ConfirmDialogFragment.create(true, null, R.string.errormessage_device_doesnt_have_camera, null)
					.show(getFragmentManager(), null);
		}
	}

	private void onImportKeyClick(final View clicked) {
		startActivity(new Intent(this, CreateAccountActivity.class).putExtra(CreateAccountActivity.EXTRA_BOOL_IMPORT_PRIVATE_KEY, true));
		Snackbar.make(findViewById(R.id.layout_coordinator), "Import", Snackbar.LENGTH_LONG)
				.show();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
}
