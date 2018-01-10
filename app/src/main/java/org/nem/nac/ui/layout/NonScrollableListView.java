package org.nem.nac.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.nem.nac.ui.utils.ListViewUtils;

public final class NonScrollableListView extends ListView {
	public NonScrollableListView(final Context context) {
		super(context);
	}

	public NonScrollableListView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public NonScrollableListView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setAdapter(final ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter != null) {
			ListViewUtils.setHeightBasedOnChildren(this);
		}
	}
}
