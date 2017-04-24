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
		String msg = BuildConfig.VERSION_NAME;
		msg += "\nWarning - this is development version and it can send to developers some sensitive information like transactions and/or encryption keys.\n" +
				"No private keys or passwords are being sent.";
		version.setText(msg);
		return layout;
	}
}
