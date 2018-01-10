package org.nem.nac.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.nem.nac.R;

public abstract class MsigBaseFragment extends NacBaseFragment {

	protected ViewGroup fromPanel;
	protected Spinner   accountsSpinner;

	@LayoutRes
	protected abstract int getLayoutId();

	public void setAccountsSpinnerAdapter(final SpinnerAdapter adapter) {
		if (accountsSpinner == null) { accountsSpinner = (Spinner)getView().findViewById(R.id.spinner_accounts); }
		accountsSpinner.setAdapter(adapter);
	}

	public void setAccountsSpinnerVisibility(final int visibility) {
		fromPanel.setVisibility(visibility);
	}

	@CallSuper
	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View layout = inflater.inflate(getLayoutId(), container, false);
		fromPanel = (ViewGroup)layout.findViewById(R.id.panel_from);
		accountsSpinner = (Spinner)layout.findViewById(R.id.spinner_accounts);
		return layout;
	}
}
