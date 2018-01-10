package org.nem.nac.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.annimon.stream.Optional;

import org.nem.nac.application.AppSettings;
import org.nem.nac.models.primitives.AddressValue;

import timber.log.Timber;

public abstract class NacBaseFragment extends Fragment {

	@Nullable
	protected AddressValue lastUsedAddress;

	@CallSuper
	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		Timber.d("# Fragment viewCreated %s", getClass().getSimpleName());
		Timber.d("# Fragment createView %s", getClass().getSimpleName());
		final Optional<AddressValue> lastAddress = AppSettings.instance().readLastUsedAccAddress();
		if (lastAddress.isPresent()) {
			lastUsedAddress = lastAddress.get();
		}
		else {
			Timber.d("Last used address not present.");
		}
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onStart() {
		Timber.d("# Fragment start %s", getClass().getSimpleName());
		super.onStart();
	}

	@Override
	public void onResume() {
		Timber.d("# Fragment resume %s", getClass().getSimpleName());
		super.onResume();
	}

	@Override
	public void onPause() {
		Timber.d("# Fragment pause %s", getClass().getSimpleName());
		super.onPause();
	}

	@Override
	public void onStop() {
		Timber.d("# Fragment stop %s", getClass().getSimpleName());
		super.onStop();
	}
}
