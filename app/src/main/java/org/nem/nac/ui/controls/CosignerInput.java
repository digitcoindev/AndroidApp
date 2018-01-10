package org.nem.nac.ui.controls;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
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
import com.annimon.stream.Stream;

import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PublicKey;
import org.nem.nac.R;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.providers.AddressInfoProvider;
import org.nem.nac.ui.utils.InputErrorUtils;

import java.util.ArrayList;
import java.util.List;

public final class CosignerInput extends AutoCompleteTextView {

	public CosignerInput(final Context context) {
		super(context);
		init();
	}

	public CosignerInput(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CosignerInput(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public boolean isEmpty() {
		return getText().length() == 0;
	}

	public boolean validate() {
		final NacPublicKey key = (NacPublicKey)getTag(R.id.tagkey_selected_public_key);
		if (key != null) { return true; }
		String text = getText().toString();

		if (isEmpty()) {
			InputErrorUtils.setErrorState(this, R.string.errormessage_invalid_public_key_entered);
			return false;
		}
		try {
			// If core's public key structure thinks it is valid then it is valid.
			final KeyPair keyPair = new KeyPair(null, PublicKey.fromHexString(text));
			return true;
		} catch (Exception e) {
			InputErrorUtils.setErrorState(this, R.string.errormessage_invalid_public_key_entered);
			return false;
		}
	}

	@NonNull
	public NacPublicKey getPublicKey()
			throws NacException {
		final NacPublicKey publicKey = (NacPublicKey)getTag(R.id.tagkey_selected_public_key);
		if (publicKey != null) { return publicKey; }
		if (!validate()) {
			throw new NacException("Public key is not valid!");
		}
		return new NacPublicKey(getText().toString());
	}

	private void init() {
		this.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		final List<Item> items = new ArrayList<>();
		Stream.of(AddressInfoProvider.instance().getLocal())
				.filter(x -> x.getValue().getPublicKey().isPresent())
				.forEach(x -> {
					final NacPublicKey publicKey = x.getValue().getPublicKey().get();
					items.add(new Item(x.getValue().getDisplayName(), publicKey));
					items.add(new Item(x.getValue().address.toString(true), publicKey));
					items.add(new Item(publicKey.toHexStr(), publicKey));
				});

		setAdapter(new Adapter(items));
		setThreshold(1);
		setDropDownBackgroundResource(R.drawable.shape_default_white_rounded);
		setOnItemClickListener(this::onSuggestionSelected);
		addTextChangedListener(_removeTagAndErrorWatcher);
	}

	private void onSuggestionSelected(final AdapterView<?> adapterView, final View parent, final int position, final long id) {
		final Item item = (Item)adapterView.getAdapter().getItem(position);
		setTag(R.id.tagkey_selected_public_key, item.publicKey);
	}

	private TextWatcher _removeTagAndErrorWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

		}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

		}

		@Override
		public void afterTextChanged(final Editable s) {
			InputErrorUtils.clearErrorState(CosignerInput.this);
			setTag(R.id.tagkey_selected_public_key, null);
		}
	};

	private class Item {

		public final String display;
		public final NacPublicKey publicKey;

		public Item(final String display, final NacPublicKey publicKey) {
			this.display = display;
			this.publicKey = publicKey;
		}

		@Override
		public String toString() {
			return display;
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
							.filter(x -> x.display.toLowerCase().startsWith(constraintString))
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
