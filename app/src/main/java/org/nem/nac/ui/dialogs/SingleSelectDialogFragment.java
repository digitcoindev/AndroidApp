package org.nem.nac.ui.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.ui.adapters.SingleSelectDialogAdapter;

public final class SingleSelectDialogFragment<TValue> extends NacBaseDialogFragment {

	public static <TItemValue> SingleSelectDialogFragment<TItemValue> create(final @StringRes Integer titleRes) {
		SingleSelectDialogFragment<TItemValue> fragment = new SingleSelectDialogFragment<>();
		Bundle args = setArgs(true, titleRes, false, null);
		fragment.setArguments(args);
		return fragment;
	}

	private ListView                                         _itemsListView;
	private SingleSelectDialogAdapter<TValue>                _adapter;
	private Consumer<SingleSelectDialogAdapter.Item<TValue>> _itemSelectListener;
	private Consumer<SingleSelectDialogAdapter.Item<TValue>> _selectionChangeListener;

	public SingleSelectDialogFragment setOnItemSelectListener(final Consumer<SingleSelectDialogAdapter.Item<TValue>> listener) {
		_itemSelectListener = listener;
		return this;
	}

	public SingleSelectDialogFragment setOnSelectionChangedListener(final Consumer<SingleSelectDialogAdapter.Item<TValue>> listener) {
		_selectionChangeListener = listener;
		return this;
	}

	public SingleSelectDialogFragment<TValue> setAdapter(final SingleSelectDialogAdapter<TValue> adapter) {
		adapter.setOnItemClickListener(this::onItemClick);
		if (_itemsListView != null) {
			_itemsListView.setAdapter(adapter);
		}
		else {
			_adapter = adapter;
		}
		return this;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_single_select_dialog;
	}

	@SuppressLint("MissingSuperCall")
	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(getContentLayout(), container, false);
		_itemsListView = (ListView)view.findViewById(R.id.listview_items);
		return view;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (_adapter != null) {
			_itemsListView.setAdapter(_adapter);
			_adapter = null;
		}
//		adapter.setLocaleSelectedListener(locale -> {
//			final Locale prev = AppSettings.instance().getAppLang().orElse(null);
//			AppSettings.instance().setAppLang(locale);
//			final String prevLang = prev != null ? prev.getLanguage() : "";
//			final String currentLang = locale != null ? locale.getLanguage() : "";
//			final boolean changed = !prevLang.equals(currentLang);
//			dismiss();
//			if (changed && _listener != null) {
//				_listener.accept(locale);
//			}
//		});
	}

	private void onItemClick(final SingleSelectDialogAdapter.Item<TValue> item) {
		if (_itemSelectListener != null) {
			_itemSelectListener.accept(item);
		}
		if (_selectionChangeListener != null && !item.isSelected()) {
			_selectionChangeListener.accept(item);
		}
		dismiss();
	}
}
