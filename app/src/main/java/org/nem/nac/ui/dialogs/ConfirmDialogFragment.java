package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.utils.StringUtils;

public final class ConfirmDialogFragment extends NacBaseDialogFragment {

	private static final String ARG_STR_MESSAGE = "arg-message-str";

	@StringRes
	private String _message;

	public static ConfirmDialogFragment create(boolean isCancelable, final @StringRes Integer titleRes, final @StringRes int messageRes,
			@Nullable final @StringRes Integer confirmTextResOverride) {
		return create(isCancelable, titleRes, NacApplication.getResString(messageRes), confirmTextResOverride);
	}

	public static ConfirmDialogFragment create(boolean isCancelable, final @StringRes Integer titleRes, final String message,
			@Nullable final @StringRes Integer confirmTextResOverride) {
		final ConfirmDialogFragment fragment = new ConfirmDialogFragment();
		final Bundle args = setArgs(isCancelable, titleRes, true, confirmTextResOverride);
		args.putString(ARG_STR_MESSAGE, message);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			_message = getArguments().getString(ARG_STR_MESSAGE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final TextView messageLabel = (TextView)view.findViewById(R.id.label_message);
		messageLabel.setText(StringUtils.isNotNullOrEmpty(_message) ? fromHtml(_message) : "");
		return view;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_confirm_dialog;
	}

	@SuppressWarnings("deprecation")
	public static Spanned fromHtml(String html){
		Spanned result;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
		} else {
			result = Html.fromHtml(html);
		}

		return result;
	}
}
