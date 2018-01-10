package org.nem.nac.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public final class ViewPagerAdapter extends FragmentPagerAdapter {

	private final List<Fragment> _fragments      = new ArrayList<>();
	private final List<String>   _fragmentTitles = new ArrayList<>();

	public ViewPagerAdapter(final FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	public void addFragment(Fragment fragment, String title) {
		_fragments.add(fragment);
		_fragmentTitles.add(title);
	}

	@Override
	public Fragment getItem(final int position) {
		return _fragments.get(position);
	}

	@Override
	public int getCount() {
		return _fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return _fragmentTitles.get(position);
	}
}
