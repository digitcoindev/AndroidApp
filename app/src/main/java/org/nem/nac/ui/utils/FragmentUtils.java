package org.nem.nac.ui.utils;

import android.app.Fragment;
import android.app.FragmentManager;

public final class FragmentUtils {

	public static void removeByTag(final FragmentManager fragmentManager, final String tag) {
		final Fragment noNetworkDialog = fragmentManager.findFragmentByTag(tag);
		if (noNetworkDialog != null) {
			fragmentManager.beginTransaction().remove(noNetworkDialog).commit();
		}
	}
}
