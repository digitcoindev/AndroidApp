package org.nem.nac.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Optional;

import org.nem.nac.BuildConfig;
import org.nem.nac.R;
import org.nem.nac.application.AppSettings;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.utils.LocaleUtils;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.ui.utils.AnimatedBottomToolbarHider;
import org.nem.nac.ui.utils.SoftKeyboardStateListener;

import java.lang.ref.SoftReference;
import java.util.Locale;

import timber.log.Timber;

public abstract class NacBaseActivity extends AppCompatActivity {

	private static SoftReference<Handler> _handler = new SoftReference<>(null);
	private volatile boolean _isDestroyed;
	private volatile boolean _isStarted = false;
	private volatile boolean _isResumed = false;
	private   ProgressDialog _progressDialog;
	protected ViewGroup      toolbarBottom;
	private final SoftKeyboardStateListener _softKeyboardStateListener = new SoftKeyboardStateListener();
	/**
	 * This is initialized during {@link #onCreate(Bundle)}
	 */
	@Nullable
	protected AddressValue lastUsedAddress;

	protected Handler getHandler() {
		Handler handler = _handler.get();
		if (handler == null) {
			handler = NacApplication.getMainHandler();
			_handler = new SoftReference<>(handler);
		}
		return handler;
	}

	public boolean isMeResumed() {
		return _isResumed;
	}

	/**
	 * Workaround method for localizing activity titles.
	 */
	@StringRes
	protected abstract int getActivityTitle();

	protected boolean listenToKeyboardVisibility() { return false; }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onStart() {
		super.onStart();
		_isStarted = true;
		if (this instanceof SplashActivity
				|| this instanceof LoginActivity) {
			return;
		}
		if (!EKeyProvider.instance().getKey().isPresent()) {
			Timber.i("Need to log in!");
			final Bundle extras = this instanceof DashboardActivity ? getIntent().getExtras() : null;
			startActivity(LoginActivity.getIntent(this, extras, true));
			//finish();
		}
	}

	@Override
	protected void onStop() {
		_isStarted = false;
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		_isResumed = true;
	}

	@Override
	protected void onPause() {
		_isResumed = false;
		super.onPause();
	}

	/**
	 * Use this to find out if activity is destroyed.
	 * This has fallback because {@link Activity#isDestroyed()} is valid only with API 17
	 */
	public final boolean isNotDestroyed() {
		if (Build.VERSION.SDK_INT >= 17) {
			return !super.isDestroyed();
		}
		return !_isDestroyed;
	}

	public void showProgressDialog(final @StringRes int msgRes) {
		showProgressDialog(getString(msgRes));
	}

	@MainThread
	public void showProgressDialog(final String msg) {
		Timber.d("%s: ShowProgressDialog", Thread.currentThread().getStackTrace()[1].getClassName());

		if (_progressDialog == null) {
			_progressDialog = new ProgressDialog(this);
			_progressDialog.setMessage(msg);
			_progressDialog.setIndeterminate(true);
		}
		else {
			_progressDialog.setMessage(msg);
		}
		if (this.isNotDestroyed() && !_progressDialog.isShowing()) {
			_progressDialog.show();
		}
	}

	public void dismissProgressDialog() {
		Timber.d("Activity destroyed: %s; Dialog is %s; Is showing: %s", !isNotDestroyed(), _progressDialog != null ? "exist" : "null",
				_progressDialog != null && _progressDialog.isShowing());
		if (_progressDialog != null && _progressDialog.isShowing()) {
			_progressDialog.dismiss();
			Timber.d("Progress dialog dismissed!");
		}
	}

	/**
	 * This getter should return layout id which will be passed into
	 * {@link NacBaseActivity#setContentView(int)}
	 * internally.
	 */
	@LayoutRes
	protected abstract int getLayoutId();

	protected void setToolbarItems(@NonNull final Toolbar toolbar) { }

	protected void setBottomToolbarHandlers(final ViewGroup tbarBottom) {
		if (tbarBottom == null) {
			return;
		}
		final TextView msgsBtn = (TextView)tbarBottom.findViewById(R.id.toolbar_btn_msgs);
		final TextView addrBookBtn = (TextView)tbarBottom.findViewById(R.id.toolbar_btn_address_book);
		final TextView qrBtn = (TextView)tbarBottom.findViewById(R.id.toolbar_btn_qr);
		final TextView moreBtn = (TextView)tbarBottom.findViewById(R.id.toolbar_btn_more);
		final int activeColor = getResources().getColor(R.color.official_green);
		//
		final boolean isDashboard = this instanceof DashboardActivity;
		final boolean isAddressBook = this instanceof AddressBookActivity;
		final boolean isQrTabs = this instanceof QrTabsActivity;
		final boolean isMore = this instanceof MoreActivity;
		// Messages
		if (isDashboard) {
			final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_hd_tbar_messages_active);
			msgsBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			msgsBtn.setTextColor(activeColor);
		}
		else {
			msgsBtn.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
		}
		// AddressBook
		if (isAddressBook) {
			final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_hd_tbar_address_book_active);
			addrBookBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			addrBookBtn.setTextColor(activeColor);
		}
		else {
			addrBookBtn.setOnClickListener(v -> startActivity(new Intent(this, AddressBookActivity.class)));
		}
		// QR
		if (isQrTabs) {
			final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_hd_tbar_qr_active);
			qrBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			qrBtn.setTextColor(activeColor);
		}
		else {
			qrBtn.setOnClickListener(v -> startActivity(new Intent(this, QrTabsActivity.class)));
		}
		// More
		if (isMore) {
			final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_hd_more_active);
			moreBtn.setCompoundDrawables(null, drawable, null, null);
			moreBtn.setTextColor(activeColor);
		}
		else {
			moreBtn.setOnClickListener(v -> {
				final Intent intent = new Intent(this, MoreActivity.class);
				if (isDashboard || isAddressBook || isQrTabs) {
					intent.putExtra(MoreActivity.EXTRA_CLASS_RETURN_TO_ACTIVITY, this.getClass());
				}
				startActivity(intent);
			});
		}
	}

	/**
	 * Called during onCreate before setContentView().
	 */
	protected void onBeforeSetContent(Bundle savedInstanceState) {}

	private void setLocale() {
		final Locale locale = LocaleUtils.getCurrentAvailable(true).get();
		Resources res = getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = locale;
		res.updateConfiguration(conf, dm);
	}

	protected void onKeyboardVisibilityChange(final boolean visible) {
		if (toolbarBottom != null) {
			//toolbarBottom.setVisibility(visible ? View.GONE : View.VISIBLE);
			if (visible && toolbarBottom.getVisibility() == View.VISIBLE) {
				AnimatedBottomToolbarHider.collapse(toolbarBottom);
			}
			else if (!visible && toolbarBottom.getVisibility() != View.VISIBLE) {
				AnimatedBottomToolbarHider.expand(toolbarBottom);
			}
			//toolbarBottom.postDelayed(() -> toolbarBottom.setVisibility(visible ? View.GONE : View.VISIBLE), 450);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects()
					.detectLeakedClosableObjects()
					.penaltyLog()
					.penaltyDeath()
					.setClassInstanceLimit(Cursor.class, 20)
					.build());
		}
		setLocale();
		super.onCreate(savedInstanceState);
		onBeforeSetContent(savedInstanceState);
		setContentView(getLayoutId());
		//
		final View appBarLayout = findViewById(R.id.appBarLayout);
		if (appBarLayout != null) {
			appBarLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
				CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)appBarLayout.getLayoutParams();
				if (params != null) {
					final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior)params.getBehavior();
					if (behavior != null) {
						behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
							@Override
							public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
								return false;
							}
						});
					}
				}
			});
		}
		//
		final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_top);
		if (toolbar != null) {
			final ImageView leftIcon = (ImageView)toolbar.findViewById(R.id.toolbar_left_icon);
			leftIcon.setOnClickListener(this::onBackClicked);
			setSupportActionBar(toolbar);
			toolbar.setOnMenuItemClickListener(this::menuItemClickHandler);
			final TextView titleField = (TextView)findViewById(R.id.toolbar_title);
			titleField.setText(getString(getActivityTitle()));
			setToolbarItems(toolbar);

			final ActionBar supportActionBar = getSupportActionBar();
			if (supportActionBar != null) {
				supportActionBar.setDisplayShowTitleEnabled(false);
				supportActionBar.setDisplayHomeAsUpEnabled(false);
			}
		}
		toolbarBottom = (ViewGroup)findViewById(R.id.toolbar_bottom);
		if (toolbarBottom != null) {
			setBottomToolbarHandlers(toolbarBottom);
		}
		//
		if (toolbarBottom != null || listenToKeyboardVisibility()) {
			final View root = findViewById(R.id.view_activity_root);
			if (root != null) {
				_softKeyboardStateListener.install(root, this::onKeyboardVisibilityChange);
			}
			else {
				Timber.w("Root view not found, forgot to add id to layout? Not listening to keyboard visibility.");
			}
		}
		//
		final Optional<AddressValue> lastAddress = AppSettings.instance().readLastUsedAccAddress();
		if (lastAddress.isPresent()) {
			lastUsedAddress = lastAddress.get();
		}
		else {
			Timber.d("Last used address not present.");
		}
	}

	@Override
	protected void onDestroy() {
		_softKeyboardStateListener.uninstallIfInstalled();
		dismissProgressDialog();
		_isDestroyed = true;
		super.onDestroy();
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		// Toolbar back button
		if (android.R.id.home == itemId) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onBackClicked(final View clicked) {
		dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
		dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
		//onBackPress();
	}

	private boolean menuItemClickHandler(final MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			default:
				return false;
		}
	}
}
