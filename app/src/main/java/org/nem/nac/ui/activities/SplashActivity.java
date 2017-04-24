package org.nem.nac.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import org.nem.nac.BuildConfig;
import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppHost;
import org.nem.nac.application.AppSettings;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.repositories.AppPasswordRepository;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.ui.dialogs.ChangeAppPasswordDialogFragment;
import org.nem.nac.ui.dialogs.MainNetStartupWarningDialogFragment;

import timber.log.Timber;

public final class SplashActivity extends NacBaseActivity {

	private boolean _visible    = false;
	private boolean _wasResumed = false;
	private View                                _coordinator;
	private ChangeAppPasswordDialogFragment     _pwdDialog;
	private MainNetStartupWarningDialogFragment _mainnetWarningDialog;
	private Handler _handler = new Handler(Looper.getMainLooper());

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_splash;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_splash;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_coordinator = findViewById(R.id.layout_coordinator);

		if (BuildConfig.DEBUG) {
			final TextView splash = ((TextView)findViewById(R.id.test));
			splash.setText("Density: " + AppHost.Screen.getDensityDpi() + "; w: " + AppHost.Screen.getSize().width / AppHost.Screen
					.getDensityLogical() + "; h: " + AppHost.Screen.getSize().height / AppHost.Screen.getDensityLogical());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (_mainnetWarningDialog != null) {
			if (_mainnetWarningDialog.isShown()) {
				_mainnetWarningDialog.dismiss();
			}
			_mainnetWarningDialog = null;
		}
		_handler = null;
		_pwdDialog = null;
	}

	@Override
	protected void onStart() {
		super.onStart();
		_visible = true;
	}

	@Override
	protected void onStop() {
		_visible = false;
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Timber.d("onResume()");

		if (!AppSettings.instance().getMainnetWarningAccepted() && (_mainnetWarningDialog == null || !_mainnetWarningDialog.isShown())) {
			_mainnetWarningDialog = MainNetStartupWarningDialogFragment.create();
			_mainnetWarningDialog.setOnConfirmListener(d -> {
				final boolean warningAccepted = _mainnetWarningDialog.isWarningAccepted();
				if (warningAccepted) {
					AppSettings.instance().setMainnetWarningAccepted();
					_handler.postDelayed(_proceedToNextActivity, AppConstants.SPLASH_DELAY_MS);
				}
				return warningAccepted;
			});
			_mainnetWarningDialog.show(getFragmentManager(), null);
		}

		ServerFinder.instance().clearBest();
		if (AppSettings.instance().getMainnetWarningAccepted()) {
			_handler.postDelayed(_proceedToNextActivity, AppConstants.SPLASH_DELAY_MS);
		}
	}

	@Override
	protected void onPause() {
		_handler.removeCallbacks(_proceedToNextActivity);
		super.onPause();
	}

	private void onPasswordSet() {
		finish();
		startActivity(LoginActivity.getIntent(this, null, false));
	}

	private Runnable _proceedToNextActivity = () -> {
		if (_visible && !isFinishing()) {
			try {
				if (new AppPasswordRepository().get().isPresent()) {
					startActivity(LoginActivity.getIntent(this, null, false));
					finish();
				}
				else {
					if (_pwdDialog == null) {
						_pwdDialog = ChangeAppPasswordDialogFragment.create(false)
								.setOnPasswordChangedListener(this::onPasswordSet);
						_pwdDialog.setOnCancelListener(d -> finish());
						_pwdDialog.show(getFragmentManager(), null);
					}
				}
			} catch (NacPersistenceRuntimeException e) {
				Timber.e(e, "Failed to get app password.");
				Snackbar.make(_coordinator, "Database problem\nPlease delete application data", Snackbar.LENGTH_INDEFINITE).show();
			}
		}
	};
}
