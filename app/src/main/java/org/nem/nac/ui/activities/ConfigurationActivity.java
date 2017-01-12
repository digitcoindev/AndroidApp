package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.datamodel.repositories.InvoiceMessageRepository;
import org.nem.nac.models.InvoiceMessage;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.ui.adapters.SingleSelectDialogAdapter;
import org.nem.nac.ui.dialogs.AboutDialogFragment;
import org.nem.nac.ui.dialogs.ChangeAppPasswordDialogFragment;
import org.nem.nac.ui.dialogs.InvoiceMessageDialogFragment;
import org.nem.nac.ui.dialogs.SingleSelectDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public final class ConfigurationActivity extends NacBaseActivity {

	private ScrollView   _scrollView;
	private View     _languageBtn;
	private TextView _languageLabel;
	private View     _changePwdBtn;
	private View     _serversBtn;
	private View     _updateIntervalBtn;
	private View     _aboutBtn;
	private View     _primaryAccBtn;
	private TextView _updateIntervalLabel;
	private TextView _invoiceMessageLabel;
	private View     _invoiceMessageBtn;
	private TextView _primaryAccLabel;
	private ViewGroup    _notificationDetailsLayout;
	private ViewGroup    _notificationLockScreenBtn;
	private SwitchCompat _notificationSound;
	private SwitchCompat _notificationVibro;
	private SwitchCompat _notificationLockScreen;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_configuration;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_configuration;
	}

	@Override
	public void onBackPressed() {
		finish();
		AccountListActivity.start(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_scrollView = (ScrollView)findViewById(R.id.scroll_view);
		//
		_languageBtn = findViewById(R.id.btn_language);
		_languageBtn.setOnClickListener(this::onLanguageClick);
		_languageLabel = (TextView)findViewById(R.id.label_language);
		_languageLabel.setText(getDisplayLanguage());
		//
		_changePwdBtn = findViewById(R.id.btn_change_password);
		_changePwdBtn.setOnClickListener(this::onChangePasswordClick);
		//
		_serversBtn = findViewById(R.id.btn_servers);
		_serversBtn.setOnClickListener(this::onServersClick);
		//
		_updateIntervalBtn = findViewById(R.id.btn_updates_interval);
		_updateIntervalBtn.setOnClickListener(this::onChangeUpdatesIntervalClick);
		_updateIntervalLabel = (TextView)findViewById(R.id.label_update_check_interval);
		//
		_invoiceMessageBtn = findViewById(R.id.btn_invoice_message);
		_invoiceMessageBtn.setOnClickListener(this::onInvoiceMessageClick);
		_invoiceMessageLabel = (TextView)findViewById(R.id.label_invoice_message);
		final Optional<InvoiceMessage> invoiceMessageOptional = new InvoiceMessageRepository().get();
		displayInvoiceMessage(invoiceMessageOptional.orElse(null));
		//
		_primaryAccBtn = findViewById(R.id.btn_primary_account);
		_primaryAccBtn.setOnClickListener(this::onPrimaryAccountClick);
		_primaryAccLabel = (TextView)findViewById(R.id.label_primary_account);
		final AppSettings appSettings = AppSettings.instance();
		final Optional<AddressValue> primaryAddress = appSettings.getPrimaryAddress();
		if (primaryAddress.isPresent()) {
			final String name = primaryAddress.get().toNameOrDashed();
			final String nameSubstr = name.substring(0, Math.min(name.length(), 10));
			_primaryAccLabel.setText(nameSubstr);
		}
		else {
			_primaryAccLabel.setText(R.string.primary_account_none);
		}
		//
		final TimeSpan updateInterval = appSettings.getUpdatesCheckInterval().orElse(TimeSpan.ZERO);
		displayUpdateInterval(updateInterval);
		//
		_aboutBtn = findViewById(R.id.btn_about);
		_aboutBtn.setOnClickListener(this::onAboutClick);
		//
		_notificationDetailsLayout = (ViewGroup)findViewById(R.id.layout_notification_details);
		_notificationLockScreenBtn = (ViewGroup)findViewById(R.id.btn_notification_lock_screen);
		_notificationSound = (SwitchCompat)findViewById(R.id.switch_notification_sound_enabled);
		_notificationVibro = (SwitchCompat)findViewById(R.id.switch_notification_vibro_enabled);
		_notificationLockScreen = (SwitchCompat)findViewById(R.id.switch_notification_show_on_lock_screen);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final AppSettings appSettings = AppSettings.instance();
		_notificationSound.setChecked(appSettings.getNotificationSoundEnabled());
		_notificationVibro.setChecked(appSettings.getNotificationVibeEnabled());
		_notificationLockScreen.setChecked(appSettings.getNotificationLockScreenEnabled());
		//
		showNotificationSettings(appSettings.getUpdatesCheckInterval().isPresent());
		if (Build.VERSION.SDK_INT < 21) {
			_notificationLockScreenBtn.setVisibility(View.GONE);
			_notificationLockScreen.setOnCheckedChangeListener(null);
		}
	}

	private void onPrimaryAccountClick(final View clicked) {
		final List<Account> accounts = new AccountRepository().getAllSorted();
		int selectedIndex = 0;
		final Optional<AddressValue> primaryAddress = AppSettings.instance().getPrimaryAddress();
		if (primaryAddress.isPresent()) {
			final ListIterator<Account> iterator = accounts.listIterator();
			while (iterator.hasNext()) {
				final int i = iterator.nextIndex(); // remember to call this !before! next().
				final Account acc = iterator.next();
				if (acc.publicData.address.equals(primaryAddress.get())) {
					selectedIndex = i + 1; // + 1 because of adding additional empty item
					break;
				}
			}
		}
		//
		List<SingleSelectDialogAdapter.Item<Account>> items = new ArrayList<>(accounts.size() + 1);
		items.add(new SingleSelectDialogAdapter.Item<>(null, getString(R.string.primary_account_none)));
		Stream.of(accounts)
				.forEach(acc -> items.add(new SingleSelectDialogAdapter.Item<>(acc, acc.name)));

		final SingleSelectDialogAdapter<Account> adapter = new SingleSelectDialogAdapter<>(this, items, selectedIndex);
		SingleSelectDialogFragment.<Account>create(R.string.dialog_title_select_primary_account)
				.setAdapter(adapter)
				.setOnSelectionChangedListener(item -> {
					if (item.value == null) {
						Timber.d("Cleared primary account");
						AppSettings.instance().clearPrimaryAddress();
						_primaryAccLabel.setText(R.string.primary_account_none);
					}
					else {
						Timber.d("Set primary account %s", item.value.publicData.address);
						AppSettings.instance().savePrimaryAddress(item.value.publicData.address);
						final String nameSubstr = item.value.name.substring(0, Math.min(item.value.name.length(), 10));
						_primaryAccLabel.setText(nameSubstr);
					}
				})
				.show(getFragmentManager(), null);
	}

	private void onInvoiceMessageClick(final View clicked) {
		InvoiceMessageDialogFragment.create()
				.setMessageUpdatedListener(this::displayInvoiceMessage)
				.show(getFragmentManager(), null);
	}

	private void onLanguageClick(final View clicked) {
		final Map<String, Integer> supportedLocales = AppSettings.instance().getSupportedLocales();
		final List<SingleSelectDialogAdapter.Item<String>> items = new ArrayList<>(supportedLocales.size() + 1);
		items.add(new SingleSelectDialogAdapter.Item<>("", getString(R.string.lang_auto)));
		Stream.of(supportedLocales)
				.map(entry -> new SingleSelectDialogAdapter.Item<>(entry.getKey(), getString(entry.getValue())))
				.forEach(items::add);

		final String appLang = AppSettings.instance().getAppLang();
		final ListIterator<SingleSelectDialogAdapter.Item<String>> iterator = items.listIterator();
		int selectedIndex = 0;
		while (iterator.hasNext()) {
			if (appLang.equalsIgnoreCase(iterator.next().value)) {
				selectedIndex = iterator.nextIndex() - 1;
				break;
			}
		}

		final SingleSelectDialogAdapter<String> adapter = new SingleSelectDialogAdapter<>(this, items, selectedIndex);

		SingleSelectDialogFragment.<String>create(R.string.dialog_title_language)
				.setAdapter(adapter)
				.setOnSelectionChangedListener(item -> {
					final String selectedLang = item.value;
					AppSettings.instance().setAppLang(selectedLang);
					recreate();
				})
				.show(getFragmentManager(), null);
	}

	private void onChangePasswordClick(final View clicked) {
		clicked.setClickable(false);
		try {
			ChangeAppPasswordDialogFragment.create(false).show(getFragmentManager(), null);
		} finally {
			clicked.setClickable(true);
		}
	}

	private void onServersClick(final View clicked) {
		finish();
		startActivity(new Intent(this, ServersListActivity.class));
	}

	private void onChangeUpdatesIntervalClick(final View clicked) {
		final List<SingleSelectDialogAdapter.Item<TimeSpan>> items = Stream.of(AppSettings.instance().getUpdateIntervals())
				.sorted((lhs, rhs) -> {
					if (lhs.isLessThan(rhs)) { return -1; }
					if (lhs.isGreaterThan(rhs)) { return 1; }
					return 0;
				})
				.map(interval -> {
					final String display;
					if (interval.isLessThan(TimeSpan.fromHours(1))) {
						display = getString(R.string.label_minutes, NumberUtils.toString(interval.toMinutes()));
					}
					else {
						display = getString(R.string.label_hours, NumberUtils.toString(interval.toHours()));
					}
					return new SingleSelectDialogAdapter.Item<>(interval, display);
				})
				.collect(Collectors.toList());
		items.add(0, new SingleSelectDialogAdapter.Item<>(TimeSpan.ZERO, getString(R.string.label_never)));

		final SingleSelectDialogAdapter<TimeSpan> adapter = new SingleSelectDialogAdapter<>(this, items, interval -> {
			final TimeSpan storedInterval = AppSettings.instance().getUpdatesCheckInterval().orElse(TimeSpan.ZERO);
			return storedInterval.equals(interval.value);
		});

		SingleSelectDialogFragment.<TimeSpan>create(R.string.dialog_title_updates_check_interval)
				.setAdapter(adapter)
				.setOnSelectionChangedListener(item -> {
					final TimeSpan interval = item.value;
					AppSettings.instance().setUpdatesCheckInterval(interval);
					final boolean zeroInterval = interval.equals(TimeSpan.ZERO);
					showNotificationSettings(!zeroInterval);
					if (!zeroInterval) {
						_notificationDetailsLayout.postDelayed(() -> _scrollView.fullScroll(View.FOCUS_DOWN), 200);
					}
					_updateIntervalLabel.setText(item.displayName);
				})
				.show(getFragmentManager(), null);
	}

	private void onAboutClick(final View clicked) {
		AboutDialogFragment.create().show(getFragmentManager(), null);
	}

	private void onNotificationSoundChanged(final CompoundButton button, final boolean checked) {
		AppSettings.instance().saveNotificationSoundEnabled(checked);
	}

	private void onNotificationVibroChanged(final CompoundButton button, final boolean checked) {
		AppSettings.instance().saveNotificationVibeEnabled(checked);
	}

	private void onNotificationLockScreenChanged(final CompoundButton button, final boolean checked) {
		AppSettings.instance().saveNotificationLockScreenEnabled(checked);
	}

	private String getDisplayLanguage() {
		//final String appLang = AppSettings.instance().getAppLang();
		final Locale locale = getResources().getConfiguration().locale;
		String display = getString(AppSettings.instance().getSupportedLocales().get(locale.getLanguage()));
		display = StringUtils.isNotNullOrEmpty(display) ? display : locale.getDisplayLanguage();
		final boolean isFallbackToDefault = AppSettings.instance().getAppLang().isEmpty() && !locale.getLanguage().equals(Locale.getDefault().getLanguage());
		return display + (isFallbackToDefault ? " (Default)" : "");
	}

	private void displayInvoiceMessage(final InvoiceMessage invoiceMsg) {
		if (invoiceMsg == null) {
			_invoiceMessageLabel.setText("");
			return;
		}
		final String message = invoiceMsg.getReadable(0);
		final String messageSubstr = message.substring(0, Math.min(message.length(), 10));
		_invoiceMessageLabel.setText(messageSubstr);
	}

	private void displayUpdateInterval(final TimeSpan interval) {
		if (interval == null) {
			_updateIntervalLabel.setText("");
			return;
		}
		final String display;
		if (interval.equals(TimeSpan.ZERO)) {
			display = getString(R.string.label_never);
		}
		else if (interval.isLessThan(TimeSpan.fromHours(1))) {
			display = getString(R.string.label_minutes, NumberUtils.toString(interval.toMinutes()));
		}
		else {
			display = getString(R.string.label_hours, NumberUtils.toString(interval.toHours()));
		}
		_updateIntervalLabel.setText(display);
	}

	private void showNotificationSettings(final boolean show) {
		if (_notificationDetailsLayout != null) {
			_notificationDetailsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
		}
		if (_notificationSound != null) {
			_notificationSound.setOnCheckedChangeListener(show ? this::onNotificationSoundChanged : null);
		}
		if (_notificationVibro != null) {
			_notificationVibro.setOnCheckedChangeListener(show ? this::onNotificationVibroChanged : null);
		}
		if (_notificationLockScreen != null) {
			_notificationLockScreen.setOnCheckedChangeListener(show ? this::onNotificationLockScreenChanged : null);
		}
	}
}
