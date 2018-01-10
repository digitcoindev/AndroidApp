package org.nem.nac.ui.adapters;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.models.network.Server;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.tasks.SetServerFlagAsyncTask;
import org.nem.nac.ui.activities.NacBaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ServerListAdapter extends BaseAdapter {

	private final NacBaseActivity _activity;
	private final LayoutInflater  _inflater;
	private final List<Server>  _servers         = new ArrayList<>();
	private final List<Integer> _hiddenPositions = new ArrayList<>();
	private Consumer<Server> _editListener;
	private Consumer<Server> _deleteListener;
	private boolean          _isEditMode;

	public ServerListAdapter(final NacBaseActivity activity, final Collection<Server> servers) {
		_activity = activity;
		_inflater = LayoutInflater.from(activity);
		_servers.addAll(servers);
	}

	public void resetData(final Collection<Server> servers) {
		_servers.clear();
		_servers.addAll(servers);
		notifyDataSetChanged();
	}

	public void setOnEditClickListener(Consumer<Server> listener) {
		_editListener = listener;
	}

	public void setOnDeleteClickListener(Consumer<Server> listener) {
		_deleteListener = listener;
	}

	public void toggleEditMode() {
		_isEditMode = !_isEditMode;
	}

	public boolean getIsEditMode() {
		return _isEditMode;
	}

	public void hideServer(final long id) {
		for (int i = 0; i < _servers.size(); i++) {
			if (((Server)getItem(i)).id == id) {
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
		final Server server = _servers.get(position);
		Views views;
		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.list_item_server, parent, false);
			views = new Views(convertView);
			views.editBtn.setOnClickListener(v -> {
				final Server editable = server;
				if (_editListener != null) {
					_editListener.accept(editable);
				}
			});
			convertView.setTag(views);
			// need to download and set image only the first time.
			new SetServerFlagAsyncTask(_activity, server, views.flagImg)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else {
			views = (Views)convertView.getTag();
		}
		final int hash = server.hashCode();
		views.editBtn.setTag(hash);
		views.deleteIcon.setTag(hash);
		views.deleteIcon.setOnClickListener(this::onDeleteClick);

		Optional<Server> current = ServerFinder.instance().peekBest();
		boolean isCurrent = false;
		if (current.isPresent()) {
			isCurrent = current.get().id == server.id;
			if (isCurrent) {
				views.checkMark.setImageDrawable(_activity.getResources().getDrawable(R.drawable.ic_hd_radio_button_active));
			}
			else {
				views.checkMark.setImageDrawable(_activity.getResources().getDrawable(R.drawable.ic_hd_radio_button));
			}
		}

		views.serverInput.setText(server.toString());

		views.checkMark.setVisibility(_isEditMode && !isCurrent ? View.GONE : View.VISIBLE);
		views.editBtn.setVisibility(_isEditMode && !isCurrent ? View.VISIBLE : View.GONE);
		views.deleteIcon.setVisibility(_isEditMode && !isCurrent ? View.VISIBLE : View.GONE);
		return convertView;
	}

	private int getRealPosition(int position) {
		for (Integer hiddenPos : _hiddenPositions) {
			if (hiddenPos <= position) {
				position = position + 1;
			}
		}
		return position;
	}

	@Override
	public int getCount() {
		return _servers.size() - _hiddenPositions.size();
	}

	@Override
	public Object getItem(final int position) {
		return _servers.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	private void onDeleteClick(final View clicked) {
		if (_deleteListener != null) {
			final int hash = (int)clicked.getTag();
			_deleteListener.accept(Stream.of(_servers).filter(a -> a.hashCode() == hash).findFirst().get());
		}
	}

	private static class Views {

		public TextView  editBtn;
		public ImageView flagImg;
		public TextView  serverInput;
		public ImageView checkMark;
		public ImageView deleteIcon;

		public Views(final View convert) {
			this.editBtn = (TextView)convert.findViewById(R.id.listitem_btn_edit);
			this.flagImg = (ImageView)convert.findViewById(R.id.listitem_img_flag);
			this.serverInput = (TextView)convert.findViewById(R.id.listitem_label_server);
			this.checkMark = (ImageView)convert.findViewById(R.id.listitem_check_mark);
			this.deleteIcon = (ImageView)convert.findViewById(R.id.listitem_delete_icon);
		}
	}
}
