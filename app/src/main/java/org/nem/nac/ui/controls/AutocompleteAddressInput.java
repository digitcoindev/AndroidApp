package org.nem.nac.ui.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.AddressInfoProvider;
import org.nem.nac.ui.input.filters.AddressIllegalCharsStrippingFilter;

import java.util.ArrayList;
import java.util.List;

public final class AutocompleteAddressInput extends AutoCompleteTextView {

	private boolean _disableSuggestions = false;

	public AutocompleteAddressInput(final Context context) {
		super(context);
		init(context, null);
	}

	public AutocompleteAddressInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public AutocompleteAddressInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public boolean validate() {
		final AddressValue address = (AddressValue)getTag(R.id.tagkey_selected_address);
		if (address != null) { return true; }
		final String addressStr = getText().toString();
		if (addressStr.isEmpty()) {
			setError(getContext().getString(R.string.errortext_empty_address));
			return false;
		}
		final String strippedAddress = AddressValue.stripIllegalChars(addressStr);
		if (!AddressValue.isValid(strippedAddress)) {
			setError(getContext().getString(R.string.errortext_invalid_address));
			return false;
		}
		return true;
	}

	public AddressValue getAddress()
			throws NacException {
		final AddressValue address = (AddressValue)getTag(R.id.tagkey_selected_address);
		if (address != null) { return address; }

		if (!validate()) {
			throw new NacException("Invalid address");
		}
		final String strippedText = AddressValue.stripIllegalChars(getText().toString());
		return AddressValue.fromValue(strippedText);
	}

	public Optional<AddressValue> getAddressIfValid() {

		if (!validate()) {
			return Optional.empty();
		}
		final String strippedText = AddressValue.stripIllegalChars(getText().toString());
		return Optional.of(AddressValue.fromValue(strippedText));
	}

	private void init(final Context context, final AttributeSet attrs) {
		this.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		if (attrs != null) {
			final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutocompleteAddressInput, 0, 0);
			_disableSuggestions = typedArray.getBoolean(R.styleable.AutocompleteAddressInput_disableSuggestions, false);
			typedArray.recycle();
		}
		if (_disableSuggestions) {
			setFilters(new InputFilter[] { new AddressIllegalCharsStrippingFilter() });
		}
		else {
			final List<Item> items = Stream.of(AddressInfoProvider.instance().getAll())
					.map(x -> new Item(x.getValue().getDisplayName(), x.getKey()))
					.collect(Collectors.toList());
			setAdapter(new Adapter(items));
			setThreshold(1);
			setMaxLines(6);
			setDropDownBackgroundResource(R.drawable.shape_default_white_rounded);
			setOnItemClickListener(this::onSuggestionSelected);
			addTextChangedListener(_removeTagWatcher);
		}
	}

	private void onSuggestionSelected(final AdapterView<?> adapterView, final View parent, final int position, final long id) {
		final Item item = (Item)adapterView.getAdapter().getItem(position);
		setTag(R.id.tagkey_selected_address, item.address);
	}

	private TextWatcher _removeTagWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

		}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

		}

		@Override
		public void afterTextChanged(final Editable s) {
			final AddressValue selectedAddress = (AddressValue)getTag(R.id.tagkey_selected_address);
			if (selectedAddress != null) {
				setTag(R.id.tagkey_selected_address, null);
				post(() -> {
					final String addressReplaceString = selectedAddress.toString(true);
					setText(addressReplaceString);
					setSelection(Math.max(0, addressReplaceString.length()));
				});
			}
		}
	};

	private class Item {

		public final String       name;
		public final AddressValue address;

		public Item(final String name, final AddressValue address) {
			this.name = name;
			this.address = address;
		}

		@Override
		public String toString() {
			return String.format("%s\n(%s)", name, address);
		}
	}

	private class Adapter extends BaseAdapter implements Filterable {

		private List<Item> _originalValues;
		private List<Item> _filtered;

		public Adapter(final List<Item> items) {
			_originalValues = new ArrayList<>(items);
			_filtered = new ArrayList<>(items);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			TextView view;
			if (convertView == null) {
				view = (TextView)LayoutInflater.from(getContext()).inflate(R.layout.list_item_autocomplete, parent, false);
			}
			else {
				view = (TextView)convertView;
			}
			final Item item = getItem(position);
			view.setText(item.toString());
			return view;
		}

		@Override
		public Filter getFilter() {
			return _filter;
		}

		@Override
		public int getCount() {
			return _filtered.size();
		}

		@Override
		public Item getItem(final int position) {
			return _filtered.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		private final Filter _filter = new Filter() {
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				FilterResults results = new FilterResults();
				if (constraint == null || constraint.length() == 0) {
					final ArrayList<Item> list = new ArrayList<>(_originalValues);
					results.values = list;
					results.count = list.size();
				}
				else {
					final String constraintString = constraint.toString().toLowerCase();
					final List<Item> values = Stream.of(_originalValues)
							.filter(x -> x.address.getRaw().toLowerCase().startsWith(constraintString) || x.name.toLowerCase().startsWith(constraintString))
							.collect(Collectors.toList());
					results.values = values;
					results.count = values.size();
				}
				return results;
			}

			@Override
			protected void publishResults(final CharSequence constraint, final FilterResults results) {
				_filtered = (List<Item>)results.values;
				if (results.count > 0) {
					notifyDataSetChanged();
				}
				else {
					notifyDataSetInvalidated();
				}
			}
		};
	}
}
