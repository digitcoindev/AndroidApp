package org.nem.nac.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.models.MoreItem;

import java.util.ArrayList;
import java.util.List;

public final class MoreAdapter extends BaseAdapter {

	private final LayoutInflater _inflater;
	private final NacBaseActivity _activity;
	private final List<Item> _items = new ArrayList<>();

	public MoreAdapter(final NacBaseActivity activity, final List<org.nem.nac.ui.models.MoreItem> items) {
		_activity = activity;
		_inflater = LayoutInflater.from(activity);
		for (MoreItem rawItem : items) {
			_items.add(new Item(activity.getString(rawItem.nameRes),
				rawItem.actionOverride != null
					? rawItem.actionOverride
					: a -> a.startActivity(new Intent(a, rawItem.activity))));
		}
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final Item item = _items.get(position);
		Views views;
		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.list_item_more, parent, false);
			views = new Views();
			views.nameLabel = (TextView)convertView.findViewById(R.id.label_name);
			convertView.setTag(views);
		}
		else {
			views = (Views)convertView.getTag();
		}
		views.nameLabel.setText(item.name);
		convertView.setOnClickListener(v -> item.action.accept(_activity));

		return convertView;
	}

	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public Object getItem(final int position) {
		return _items.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	private static class Item {

		public final String                    name;
		public final Consumer<NacBaseActivity> action;

		public Item(final String name, final Consumer<NacBaseActivity> action) {
			this.name = name;
			this.action = action;
		}
	}

	private class Views {

		public TextView nameLabel;
	}
}
