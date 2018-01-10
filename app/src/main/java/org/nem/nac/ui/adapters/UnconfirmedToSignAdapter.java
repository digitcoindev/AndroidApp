package org.nem.nac.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.common.enums.MessageType;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.models.api.transactions.MultisigAggregateModificationTransactionApiDto;
import org.nem.nac.models.api.transactions.MultisigTransactionApiDto;
import org.nem.nac.models.api.transactions.TransferTransactionApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class UnconfirmedToSignAdapter extends BaseAdapter {

	private static final List<TransactionType> TYPES = new ArrayList<>();

	static {
		TYPES.add(TransactionType.TRANSFER_TRANSACTION);
		TYPES.add(TransactionType.MULTISIG_AGGREGATE_MODIFICATION_TRANSACTION);
	}

	private final LayoutInflater                                     _inflater;
	private final List<UnconfirmedTransactionMetaDataPairApiDto>     _items;
	private       Consumer<UnconfirmedTransactionMetaDataPairApiDto> _confirmListener;
	private       Consumer<UnconfirmedTransactionMetaDataPairApiDto> _showChangesListener;

	public UnconfirmedToSignAdapter(final Context context, List<UnconfirmedTransactionMetaDataPairApiDto> items) {
		_inflater = LayoutInflater.from(context);
		_items = new ArrayList<>(items);
	}

	public UnconfirmedToSignAdapter setOnConfirmListener(final Consumer<UnconfirmedTransactionMetaDataPairApiDto> listener) {
		_confirmListener = listener;
		return this;
	}

	public UnconfirmedToSignAdapter setOnShowChangesListener(final Consumer<UnconfirmedTransactionMetaDataPairApiDto> listener) {
		_showChangesListener = listener;
		return this;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final UnconfirmedTransactionMetaDataPairApiDto item = getItem(position);

		Views views = null;
		final TransactionType type = TYPES.get(getItemViewType(position));
		switch (type) {
			case TRANSFER_TRANSACTION: {
				if (convertView == null) {
					convertView = _inflater.inflate(R.layout.list_item_unconfirmed_transfer_to_sign, parent, false);
					views = new Views(convertView);
				}
				else {
					views = (Views)convertView.getTag();
				}
				break;
			}
			case MULTISIG_AGGREGATE_MODIFICATION_TRANSACTION: {
				if (convertView == null) {
					convertView = _inflater.inflate(R.layout.list_item_unconfirmed_aggregate_modification_to_sign, parent, false);
					views = new Views(convertView);
				}
				else {
					views = (Views)convertView.getTag();
				}
				break;
			}
		}
		convertView.setTag(views);
		//
		switch (type) {
			case TRANSFER_TRANSACTION: {
				final TransferTransactionApiDto transfer = ((TransferTransactionApiDto)((MultisigTransactionApiDto)item.transaction).otherTrans);
				views.from.setText(transfer.signer.toString());
				views.to.setText(transfer.recipient.toNameOrDashed());
				//
				if (transfer.hasMessage()) {
					views.message.setVisibility(View.VISIBLE);
					if (transfer.message.type == MessageType.NOT_ENCRYPTED) {
						views.message.setText(transfer.message.toString());
					}
					else {
						views.message.setText(R.string.placeholder_encrypted_message);
					}
				}
				else {
					views.message.setVisibility(View.GONE);
					views.message.setText(null);
				}
				//
				views.amount.setText(transfer.amount.toFractionalString());
				views.btnConfirm.setTag(item);
				views.btnConfirm.setOnClickListener(this::onConfirmClick);
				break;
			}
			case MULTISIG_AGGREGATE_MODIFICATION_TRANSACTION: {
				final MultisigAggregateModificationTransactionApiDto modification =
						((MultisigAggregateModificationTransactionApiDto)((MultisigTransactionApiDto)item.transaction).otherTrans);
				views.from.setText(item.transaction.signer.toString());
				//views.to.setText(modification..toNameOrDashed());
				views.btnShowChanges.setTag(item);
				views.btnShowChanges.setOnClickListener(this::onShowChangesClick);
				views.btnConfirm.setTag(item);
				views.btnConfirm.setOnClickListener(this::onConfirmClick);
				break;
			}
		}
		//
		return convertView;
	}

	private void onShowChangesClick(final View clicked) {
		clicked.setClickable(false);
		try {
			final UnconfirmedTransactionMetaDataPairApiDto unconfirmed = (UnconfirmedTransactionMetaDataPairApiDto)clicked.getTag();
			if (unconfirmed == null) {
				Timber.e("Transaction to confirm was null!");
				Toaster.instance().showGeneralError();
				return;
			}
			//
			if (_showChangesListener != null) {
				_showChangesListener.accept(unconfirmed);
			}
		} finally {
			clicked.setClickable(true);
		}
	}

	private void onConfirmClick(final View clicked) {
		clicked.setClickable(false);
		final UnconfirmedTransactionMetaDataPairApiDto unconfirmed = (UnconfirmedTransactionMetaDataPairApiDto)clicked.getTag();
		if (unconfirmed == null) {
			Timber.e("Transaction to confirm was null!");
			Toaster.instance().showGeneralError();
			clicked.setClickable(true);
			return;
		}
		if (_confirmListener != null) {
			_confirmListener.accept(unconfirmed);
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(final int position) {
		final UnconfirmedTransactionMetaDataPairApiDto item = getItem(position);
		return TYPES.indexOf(((MultisigTransactionApiDto)item.transaction).otherTrans.type);
	}

	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public UnconfirmedTransactionMetaDataPairApiDto getItem(final int position) {
		return _items.get(position);
	}

	@Override
	public long getItemId(final int i) {
		return i;
	}

	private static class Views {

		public final TextView from;
		public final TextView to;
		public final TextView message;
		public final TextView amount;
		public final TextView btnConfirm;
		public final TextView btnShowChanges;

		public Views(final View convertView) {
			from = (TextView)convertView.findViewById(R.id.text_view_from);
			to = (TextView)convertView.findViewById(R.id.text_view_to);
			message = (TextView)convertView.findViewById(R.id.text_view_message);
			amount = (TextView)convertView.findViewById(R.id.text_view_amount);
			btnConfirm = (TextView)convertView.findViewById(R.id.btn_confirm);
			btnShowChanges = (TextView)convertView.findViewById(R.id.btn_show_changes);
		}
	}
}
