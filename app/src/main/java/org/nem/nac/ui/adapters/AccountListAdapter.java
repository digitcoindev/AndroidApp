package org.nem.nac.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.terlici.dragndroplist.DragNDropAdapter;
import com.terlici.dragndroplist.DragNDropListView;

import org.nem.nac.R;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AccountListAdapter extends BaseAdapter implements DragNDropAdapter {

	private int mPosition[];
	private final LayoutInflater    _inflater;
	private final List<AccountItem> _accounts;
	private final List<Integer> _hiddenPositions = new ArrayList<>();
	private Consumer<AccountItem> _editListener;
	private Consumer<AccountItem> _deleteListener;
	private boolean               _isEditMode;

	public AccountListAdapter(@NonNull final Context context, @NonNull final List<Account> accounts) {
		_inflater = LayoutInflater.from(context);
		_accounts = Stream.of(accounts)
				.sorted((lhs, rhs) -> lhs.sortIndex - rhs.sortIndex)
				.map(a -> new AccountItem(a.id, a.name, a.publicData.address.getRaw(), a.sortIndex))
				.collect(Collectors.toList());
		setup(_accounts.size());
	}

	public void setOnEditClickListener(Consumer<AccountItem> listener) {
		_editListener = listener;
	}

	public void setOnDeleteClickListener(Consumer<AccountItem> listener) {
		_deleteListener = listener;
	}

	public void toggleEditMode() {
		_isEditMode = !_isEditMode;
	}

	public boolean getIsEditMode() {
		return _isEditMode;
	}

	public void hideAccount(final long id) {
		for (int i = 0; i < _accounts.size(); i++) {
			if (getItem(i).id == id) {
				int posToHide = i;
				for (int pos : _hiddenPositions) {
					if (pos <= i) { posToHide--; }
				}
				_hiddenPositions.add(posToHide);
				notifyDataSetChanged();
				return;
			}
		}
	}

	public void showAllAccounts() {
		_hiddenPositions.clear();
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		try {
			final Map<Long, Account> accountsById = Stream.of(new AccountRepository().getAllSorted())
					.collect(Collectors.toMap(acc -> acc.id, acc -> acc));
			Stream.of(_accounts)
					.forEach(acc -> {
						acc.name = accountsById.get(acc.id).name;
						acc.sortIndex = accountsById.get(acc.id).sortIndex;
					});
		} catch (Exception e) {
			Timber.w("Failed to update account names");
		}
		setup(_accounts.size());
		super.notifyDataSetChanged();
	}

	private int getRealPosition(final int position) {
		int realPos = position;
		for (Integer hiddenPos : _hiddenPositions) {
			if (hiddenPos <= position) {
				realPos = realPos + 1;
			}
		}
		return realPos;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		position = getRealPosition(position);
		position = mPosition[position];
		final AccountItem account = _accounts.get(position);

		Views views;
		if (convertView == null) {
			views = new Views();
			convertView = _inflater.inflate(R.layout.list_item_account, parent, false);
			views.editBtn = (TextView)convertView.findViewById(R.id.listitem_btn_edit);
			views.accountNameInput = (TextView)convertView.findViewById(R.id.listitem_label_account_name);
			views.arrowIcon = (ImageView)convertView.findViewById(R.id.listitem_arrow_icon);
			views.deleteIcon = (ImageView)convertView.findViewById(R.id.listitem_delete_icon);
			views.editBtn.setOnClickListener(this::onEditClick);
			views.deleteIcon.setOnClickListener(this::onDeleteClick);
			views.dragHandle = (ImageView)convertView.findViewById(R.id.imgview_drag_handle);
			convertView.setTag(views);
		}
		else {
			views = (Views)convertView.getTag();
		}

		views.editBtn.setTag(account.id);
		views.deleteIcon.setTag(account.id);

		views.accountNameInput.setText(account.name);
		views.arrowIcon.setVisibility(_isEditMode ? View.GONE : View.VISIBLE);
		views.editBtn.setVisibility(_isEditMode ? View.VISIBLE : View.GONE);
		views.deleteIcon.setVisibility(_isEditMode ? View.VISIBLE : View.GONE);
		views.dragHandle.setVisibility(_isEditMode ? View.VISIBLE : View.GONE);

		return convertView;
	}

	@Override
	public int getCount() {
		return _accounts.size() - _hiddenPositions.size();
	}

	@Override
	public AccountItem getItem(final int position) {
		return _accounts.get(mPosition[position]);
	}

	@Override
	public boolean isEnabled(int position) {
		return super.isEnabled(mPosition[position]);
	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(mPosition[position]);
	}

	@Override
	public long getItemId(final int position) {
		return _accounts.get(mPosition[getRealPosition(position)]).id;
	}

	@Override
	public int getDragHandler() {
		return R.id.imgview_drag_handle;
	}

	@Override
	public void onItemDrag(final DragNDropListView parent, final View view, final int position, final long id) {}

	@Override
	public void onItemDrop(final DragNDropListView parent, final View view, final int startPosition, final int endPosition, final long id) {}

	@Override
	public View getDropDownView(int position, View view, ViewGroup group) {
		return super.getDropDownView(mPosition[position], view, group);
	}


	private void onDeleteClick(final View clicked) {
		if (_deleteListener != null) {
			final long id = (long)clicked.getTag();
			_deleteListener.accept(Stream.of(_accounts).filter(a -> a.id == id).findFirst().get());
		}
	}

	private void onEditClick(final View clicked) {
		if (_editListener != null) {
			final long id = (long)clicked.getTag();
			_editListener.accept(Stream.of(_accounts).filter(a -> a.id == id).findFirst().get());
		}
	}

	private void setup(int size) {
		mPosition = new int[size];

		for (int i = 0; i < size; ++i) { mPosition[i] = i; }
	}

	private class Views {

		public TextView  editBtn;
		public TextView  accountNameInput;
		public ImageView arrowIcon;
		public ImageView deleteIcon;
		public ImageView dragHandle;
	}

	public static class AccountItem {

		public long   id;
		public String name;
		public String rawAddress;
		public int sortIndex;

		public AccountItem(final long id, final String name, final String rawAddress, final int sortIndex) {
			this.id = id;
			this.name = name;
			this.rawAddress = rawAddress;
			this.sortIndex = sortIndex;
		}
	}
}
