package org.nem.nac.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.models.Contact;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class AddressBookAdapter extends BaseAdapter implements Filterable {

	private LayoutInflater _inflater;
	private final List<Item> _items = new ArrayList<>();
	private List<Item>        _filteredItems;
	private Consumer<Contact> _editListener;
	private Consumer<Contact> _deleteListener;
	private boolean           _isEditMode;
	private final List<Integer> _hiddenPositions = new ArrayList<>();

	public AddressBookAdapter(final Context context, final List<Contact> contacts) {
		_inflater = LayoutInflater.from(context);
		final List<Item> items = Stream.of(contacts)
				.map(i -> new Item(i, i.hasValidAddress()))
				.collect(Collectors.toList());

		_items.addAll(items);
		_filteredItems = new ArrayList<>(items);
	}

	public void setOnEditClickListener(Consumer<Contact> listener) {
		_editListener = listener;
	}

	public void setOnDeleteClickListener(Consumer<Contact> listener) {
		_deleteListener = listener;
	}

	public void toggleEditMode() {
		_isEditMode = !_isEditMode;
	}

	public boolean getIsEditMode() {
		return _isEditMode;
	}

	public void hideContact(final long contactId) {
		for (int i = 0; i < _filteredItems.size(); i++) {
			if (contactId == getItem(i).contactId) {
				_hiddenPositions.add(i);
				notifyDataSetChanged();
				return;
			}
		}
	}

	public void showAll() {
		_hiddenPositions.clear();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		position = getRealPosition(position);
		final Item item = _filteredItems.get(position);

		Views views;
		if (convertView != null) {
			views = (Views)convertView.getTag();
		}
		else {
			convertView = _inflater.inflate(R.layout.list_item_address_book_contact, parent, false);
			views = new Views(convertView);
			views.editBtn.setOnClickListener(this::onEditClick);
			views.deleteBtn.setOnClickListener(item.isNemContact ? this::onDeleteClick : null);
			convertView.setTag(views);
		}
		views.editBtn.setTag(item);
		views.deleteBtn.setTag(item);

		views.contactName.setText(item.contact.getName());
		views.nemContactIcon.setVisibility(item.isNemContact ? View.VISIBLE : View.INVISIBLE);
		views.editBtn.setVisibility(_isEditMode ? View.VISIBLE : View.GONE);
		views.deleteBtn.setVisibility(_isEditMode && item.isNemContact ? View.VISIBLE : View.GONE);

		return convertView;
	}

	@Override
	public int getCount() {
		return _filteredItems.size() - _hiddenPositions.size();
	}

	@Override
	public Contact getItem(final int position) {
		return _filteredItems.get(position).contact;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public Filter getFilter() {
		return _filter;
	}

	private int getRealPosition(int position) {
		for (Integer hiddenPos : _hiddenPositions) {
			if (hiddenPos <= position) {
				position = position + 1;
			}
		}
		return position;
	}

	private void onDeleteClick(final View clicked) {
		if (_deleteListener != null) {
			final Item item = (Item)clicked.getTag();
			_deleteListener.accept(item.contact);
		}
	}

	private void onEditClick(final View clicked) {
		if (_editListener != null) {
			final Item item = (Item)clicked.getTag();
			_editListener.accept(item.contact);
		}
	}

	private final Filter _filter = new Filter() {
		@Override
		protected FilterResults performFiltering(final CharSequence constraint) {
			final FilterResults results = new FilterResults();
			if (constraint == null || constraint.length() == 0) {
				results.values = _items;
				return results;
			}

			final String filterStr = constraint.toString().toLowerCase();
			Timber.d("Filter string: %s", filterStr);
			final List<Item> filtered = Stream.of(_items)
					.filter(i -> i.contact.getName().toLowerCase().contains(filterStr))
					.collect(Collectors.toList());
			Timber.d("Filtered(visible) %d items", filtered.size());

			results.values = filtered;
			results.count = filtered.size();

			return results;
		}

		@Override
		protected void publishResults(final CharSequence constraint, final FilterResults results) {
			_filteredItems = (List<Item>)results.values;
			notifyDataSetChanged();
		}
	};

	private class Views {

		public final TextView  contactName;
		public final ImageView nemContactIcon;
		public final View      editBtn;
		public final View      deleteBtn;

		public Views(View convertView) {
			contactName = (TextView)convertView.findViewById(R.id.text_view_contact);
			nemContactIcon = (ImageView)convertView.findViewById(R.id.image_view_is_nem_contact);
			editBtn = convertView.findViewById(R.id.listitem_btn_edit);
			deleteBtn = convertView.findViewById(R.id.listitem_delete_icon);
		}
	}

	private static class Item {

		public final Contact contact;
		public final boolean isNemContact;

		public Item(final Contact contact, final boolean isNemContact) {
			this.contact = contact;
			this.isNemContact = isNemContact;
		}
	}
}
