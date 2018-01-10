package org.nem.nac.ui.controls;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.nem.nac.common.utils.AssertUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class LinearList<TItem> extends LinearLayout {

	protected final List<TItem> items = new ArrayList<>();
	protected View _emptyView;

	public LinearList(final Context context) {
		super(context);
		init();
	}

	public LinearList(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LinearList(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setEmptyView(@Nullable final View view) {
		_emptyView = view;
	}

	public List<TItem> getItems() {
		return new ArrayList<>(items);
	}

	public TItem getItem(final int location) {
		return items.get(location);
	}

	public void addItems(@NonNull final Collection<? extends TItem> items) {
		AssertUtils.notNull(items);
		for (TItem item : items) {
			addItem(item);
		}
	}

	public void addItem(@NonNull final TItem item) {
		AssertUtils.notNull(item);
		final boolean removeEmptyView = items.isEmpty() && _emptyView != null;
		if (items.add(item)) {
			if (removeEmptyView) {
				removeAllViews();
			}
			addToListView(item);
		}
	}

	public boolean containsItem(final TItem item) {
		return items.contains(item);
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public void removeItem(@NonNull final TItem item) {
		AssertUtils.notNull(item);
		if (items.remove(item)) {
			removeFromListView(item);
		}
		if (items.isEmpty() && _emptyView != null) {
			addView(_emptyView);
		}
	}

	public void clearItems() {
		final Iterator<TItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			removeFromListView(iterator.next());
			iterator.remove();
		}
		AssertUtils.isTrue(items.isEmpty());
	}

	public void refreshViews() {
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			view = onItemViewInvalidate(view, items.get(i));
			removeViewAt(i);
			addView(view, i, view != null ? view.getLayoutParams() : null);
		}
		invalidate();
	}

	protected View onItemViewInvalidate(View view, TItem item) {
		return getItemView(LayoutInflater.from(getContext()), item);
	}

	@NonNull
	protected abstract View getItemView(@NonNull final LayoutInflater inflater, @Nullable final TItem item);

	private void init() {
	}

	private       LayoutInflater   _inflater  = LayoutInflater.from(getContext());
	private final Map<TItem, View> _viewCache = new HashMap<>();

	private void addToListView(@NonNull TItem item) {
		View itemView = _viewCache.get(item);
		if (itemView == null) {
			itemView = getItemView(_inflater, item);
			_viewCache.put(item, itemView);
		}

		addView(itemView);
	}

	private void removeFromListView(@NonNull final TItem item) {
		final View itemView = _viewCache.get(item);
		removeView(itemView);
		_viewCache.remove(item);
	}
}
