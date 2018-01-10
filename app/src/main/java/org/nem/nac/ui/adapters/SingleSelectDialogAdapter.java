package org.nem.nac.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;

import org.nem.nac.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class SingleSelectDialogAdapter<T> extends BaseAdapter {

	private Context _context;
	private final List<Item<T>> _items = new ArrayList<>();
	private Consumer<Item<T>> _itemClickListener;

	public SingleSelectDialogAdapter(final Context context, final List<Item<T>> items, final int selectedIndex) {
		_context = context;
		_items.addAll(items);
		_items.get(selectedIndex).isSelected = true;
	}

	public SingleSelectDialogAdapter(final Context context, final List<Item<T>> items, final Predicate<Item<T>> selectedPredicate) {
		_context = context;
		_items.addAll(items);
		for (Item<T> item : _items) {
			if (selectedPredicate.test(item)) {
				item.isSelected = true;
				break;
			}
		}
	}

	public void setOnItemClickListener(final Consumer<Item<T>> listener) {
		_itemClickListener = listener;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final Item<T> item = getItem(position);

		final Views views;
		Timber.d("Position %d", position);
		if (convertView == null) {
			convertView = LayoutInflater.from(_context).inflate(R.layout.list_item_single_select, parent, false);
			views = new Views(convertView);
			convertView.setTag(views);
		}
		else {
			views = (Views)convertView.getTag();
		}
		convertView.setOnClickListener(v -> {
			if (_itemClickListener != null) {
				_itemClickListener.accept(item);
			}
		});
		views.checkedText.setText(item.displayName);
		views.checkedText.setChecked(item.isSelected);
		return convertView;
	}

	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public Item<T> getItem(final int position) {
		return _items.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	public static final class Item<T> {

		public final String  displayName;
		public final T       value;
		protected    boolean isSelected;

		public Item(final T value, final String displayName) {
			this.displayName = displayName;
			this.value = value;
		}

		public boolean isSelected() {
			return isSelected;
		}
	}

	private static class Views {

		public CheckedTextView checkedText;

		public Views(final View convertView) {
			checkedText = (CheckedTextView)convertView.findViewById(R.id.checkedtext_item);
		}
	}
}
