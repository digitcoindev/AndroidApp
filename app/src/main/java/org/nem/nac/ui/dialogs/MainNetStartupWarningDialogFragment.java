package org.nem.nac.ui.dialogs;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.nem.nac.R;

public final class MainNetStartupWarningDialogFragment extends NacBaseDialogFragment {

	private final ThreadLocal<Boolean> _shown = new ThreadLocal<>();
	private CheckBox _acceptCheckbox;

	public static MainNetStartupWarningDialogFragment create() {

		MainNetStartupWarningDialogFragment fragment = new MainNetStartupWarningDialogFragment();
		Bundle args = setArgs(false, R.string.dialog_title_mainnet_warning, true, R.string.text_OK);
		;
		fragment.setArguments(args);
		return fragment;
	}

	public MainNetStartupWarningDialogFragment() {
		_shown.set(false);
	}

	public boolean isWarningAccepted() {
		if (_acceptCheckbox == null) {
			throw new IllegalStateException("Checkbox is not initialized");
		}
		return _acceptCheckbox.isChecked();
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_main_net_startup_warning_dialog;
	}

	public boolean isShown() {
		return _shown.get();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		if (view == null) {
			return null;
		}
		confirmBtn.setBackgroundResource(R.drawable.shape_popup_btn_bottom_inactive);
		_acceptCheckbox = ((CheckBox)view.findViewById(R.id.checkbox_accept));
		_acceptCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> confirmBtn
				.setBackgroundResource(isChecked ? R.drawable.shape_popup_btn_bottom : R.drawable.shape_popup_btn_bottom_inactive));
		return view;
	}

	@Override
	public void show(final FragmentManager manager, final String tag) {
		if (_shown.get()) { return; }
		super.show(manager, tag);
		_shown.set(true);
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		_shown.set(false);
		super.onDismiss(dialog);
	}
}
