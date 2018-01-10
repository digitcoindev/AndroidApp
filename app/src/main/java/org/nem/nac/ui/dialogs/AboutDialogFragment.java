package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nem.nac.BuildConfig;
import org.nem.nac.R;

public final class AboutDialogFragment extends NacBaseDialogFragment {

	public static AboutDialogFragment create() {
		AboutDialogFragment fragment = new AboutDialogFragment();
		final Bundle args = setArgs(true, R.string.dialog_title_about, true, null);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_about_dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = super.onCreateView(inflater, container, savedInstanceState);
		final TextView version = (TextView)layout.findViewById(R.id.field_version);
		String msg = String.format("%s%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.VERSION_CODE);
		msg += "\n\nNote - this is enhance NEM Wallet+ version, with revised transaction fee.\n" +
				"Crash report will send to developer. No private keys or passwords are being sent.\n";
		version.setText(msg);
		return layout;
	}
}
