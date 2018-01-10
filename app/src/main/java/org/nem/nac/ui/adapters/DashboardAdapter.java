package org.nem.nac.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.enums.MessageType;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.DateUtils;
import org.nem.nac.models.Xems;
import org.nem.nac.models.api.MessageApiDto;
import org.nem.nac.models.primitives.AddressValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class DashboardAdapter extends BaseAdapter {

	private final LayoutInflater _inflater;
	private final List<Item> _items = new ArrayList<>();

	public DashboardAdapter(final Context context) {
		AssertUtils.notNull(context);
		_inflater = LayoutInflater.from(context);
	}

	public void setItems(final List<Item> items) {
		_items.clear();
		_items.addAll(items);
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final Item item = getItem(position);

		final Views views;
		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.list_item_dashboard, parent, false);
			views = new Views(convertView);
			convertView.setTag(views);
		}
		else {
			views = (Views)convertView.getTag();
		}

		// name
		final String name = item.companion.toNameOrDashed();
		views.companionLabel.setText(name);
		// message
		if (item.hasMessage) {
			if (item.message.type == MessageType.NOT_ENCRYPTED) {
				final String msgText = MessageApiDto.toReadableString(item.message).get();
				views.messageLabel.setText(msgText);
			}
			else {
				views.messageLabel.setText(R.string.placeholder_encrypted_message);
			}
		}
		else {
			views.messageLabel.setText(null); // before trying to set visibility, check if layout won't break
		}
		// amount
		final boolean zeroAmount = item.amount.equals(Xems.ZERO);
		views.amountLabel.setText(String.format("%s%s", zeroAmount ? " " : (item.isFromToMyself ? "±" : (item.isOutgoing ? "-" : "+")), item.amount
				.toFractionalString()));
		final Resources resources = NacApplication.getAppContext()
				.getResources();
		final int amountColor = resources.getColor(item.isFromToMyself ? R.color.official_gray : (item.isOutgoing ? R.color.default_red : R.color.official_green));
		views.amountLabel.setTextColor(amountColor);
		views.xemsLabel.setTextColor(amountColor);
		// date
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -24);
		final Date oneDayBack = calendar.getTime();
		final Date date = item.date;
		if(!item.isConfirmed) {
			views.datetimeLabel.setText(R.string.label_unconfirmed_date);
		}
		else if (date.before(oneDayBack)) {
			views.datetimeLabel.setText(DateUtils.format(date, true));
		}
		else {
			views.datetimeLabel.setText(String.format("%d:%02d", date.getHours(), date.getMinutes()));
		}
		return convertView;
	}

	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public Item getItem(final int position) {
		return _items.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).hashCode();
	}

	public static class Item {
		public final boolean       isOutgoing;
		public final boolean       isFromToMyself;
		public final boolean       hasMessage;
		public final AddressValue  companion;
		public final MessageApiDto message;
		public final Xems          amount;
		public final Date date;
		public final boolean isConfirmed;

		public Item(final boolean isOutgoing, final boolean isFromToMyself, final boolean hasMessage, final AddressValue companion, final MessageApiDto message,
				final Xems amount, final Date date, final boolean isConfirmed) {
			this.isOutgoing = isOutgoing;
			this.isFromToMyself = isFromToMyself;
			this.hasMessage = hasMessage;
			this.companion = companion;
			this.message = message;
			this.amount = amount;
			this.date = date;
			this.isConfirmed = isConfirmed;
		}
	}

	private static class Views {

		public TextView companionLabel;
		public TextView messageLabel;
		public TextView datetimeLabel;
		public TextView amountLabel;
		public TextView xemsLabel;

		public Views(final View convert) {
			companionLabel = (TextView)convert.findViewById(R.id.label_companion);
			messageLabel = (TextView)convert.findViewById(R.id.label_message);
			datetimeLabel = (TextView)convert.findViewById(R.id.label_datetime);
			amountLabel = (TextView)convert.findViewById(R.id.label_amount);
			xemsLabel = (TextView)convert.findViewById(R.id.label_xems);
		}
	}
}
