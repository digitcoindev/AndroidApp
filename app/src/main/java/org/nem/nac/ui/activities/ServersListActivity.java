package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.nem.nac.R;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.models.network.Server;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.servers.ServerManager;
import org.nem.nac.ui.adapters.ServerListAdapter;
import org.nem.nac.ui.dialogs.EditServerDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public final class ServersListActivity extends NacBaseActivity {

	private ListView          _serversListview;
	private TextView          _toolbarRightLabel;
	private ServerListAdapter _serversAdapter;
	private View              _toolbarRightPanel;
	private Snackbar          _undoSnackbar;
	private List<Long> _toDelete = new ArrayList<>();

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_servers_list;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_servers_list;
	}

	@Override
	public void onBackPressed() {
		if (_serversListview != null && _serversAdapter != null && _serversAdapter.getIsEditMode()) {
			toggleEditMode();
		}
		else {
			finish();
			startActivity(new Intent(this, ConfigurationActivity.class));
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Button addServerBtn = ((Button)findViewById(R.id.btn_add_server));
		addServerBtn.setOnClickListener(this::onAddBtnClick);
		_serversListview = (ListView)findViewById(R.id.listview_servers);
		_serversListview.setOnItemClickListener(this::onItemClick);
		_toolbarRightLabel = (TextView)findViewById(R.id.toolbar_right_text);

		final Map<Long, Server> servers = ServerManager.instance().getAllServers();
		try {
			setServersListViewAdapter(servers.values());
		} catch (NacPersistenceRuntimeException e) {
			Timber.e(e, "Failed to set account list adapter");
			Toast.makeText(this, R.string.errormessage_error_occured, Toast.LENGTH_LONG).show();
		}
		if (servers.size() < 1) {
			_toolbarRightPanel.setClickable(false);
			_toolbarRightPanel.setVisibility(View.INVISIBLE);
		}
		else {
			_toolbarRightPanel.setClickable(true);
			_toolbarRightPanel.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		final ServerManager serverManager = ServerManager.instance();
		serverManager.removeAll(_toDelete);
		_toDelete.clear();
	}

	@Override
	protected void setToolbarItems(@NonNull final Toolbar toolbar) {
		_toolbarRightPanel = findViewById(R.id.toolbar_right_panel);
		_toolbarRightPanel.setClickable(true);
		_toolbarRightPanel.setOnClickListener(this::onEditBtnClick);
		_toolbarRightLabel = (TextView)findViewById(R.id.toolbar_right_text);
		_toolbarRightLabel.setText(R.string.toolbar_btn_edit);
		_toolbarRightLabel.setVisibility(View.VISIBLE);
	}

	private void onEditBtnClick(final View clicked) {
		toggleEditMode();
	}

	private void toggleEditMode() {
		if (_serversAdapter != null) {
			_serversAdapter.toggleEditMode();
			_toolbarRightLabel.setText(_serversAdapter.getIsEditMode()
					? R.string.toolbar_btn_done
					: R.string.toolbar_btn_edit);
		}
		if (_serversListview != null) {
			_serversListview.post(_serversListview::invalidateViews);
		}
	}

	private void onAddBtnClick(final View clicked) {
		final EditServerDialogFragment editServerDialogFragment = EditServerDialogFragment.create(null);
		editServerDialogFragment.setOnChangedListener(this::onServerChanged);
		editServerDialogFragment.show(getFragmentManager(), EditServerDialogFragment.DIALOG_TAG);
	}

	private void onServerChanged(final Server server) {
		final ServerManager serverManager = ServerManager.instance();
		if (server.id != 0) {
			serverManager.update(server);
		}
		else {
			serverManager.addServer(server);
		}
		_serversListview.invalidateViews();
		((ServerListAdapter)_serversListview.getAdapter()).resetData(serverManager.getAllServers().values());
	}

	private void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
		if (_serversAdapter == null || _serversAdapter.getIsEditMode()) { return; }
		final Server chosen = (Server)adapterView.getItemAtPosition(position);
		ServerFinder.instance().setBest(chosen);
		_serversListview.invalidateViews();
	}

	private void onItemDeleteClick(final Server server) {
		_toDelete.add(server.id);
		_serversAdapter.hideServer(server.id);
		if (_undoSnackbar != null) {
			_undoSnackbar.setText(StringUtils.format(R.string.message_server_deleted, server.host));
		}
		else {
			_undoSnackbar = Snackbar.make(findViewById(R.id.layout_coordinator), StringUtils
					.format(R.string.message_server_deleted, server.host), Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.action_undo, this::onUndoDeleteClick)
					.setActionTextColor(getResources().getColor(R.color.official_green));
			_undoSnackbar.getView().setBackgroundColor(getResources().getColor(R.color.default_black));
			((CoordinatorLayout.LayoutParams)_undoSnackbar.getView().getLayoutParams()).setBehavior(null);
		}
		_undoSnackbar.show();
	}

	private void onItemEditClick(final Server server) {
		EditServerDialogFragment.create(server.id)
				.setOnChangedListener(s -> {
					ServerManager.instance().update(s);
					Toaster.instance().show(R.string.message_saved);
					if (isNotDestroyed() && _serversAdapter != null) {
						((ServerListAdapter)_serversListview.getAdapter()).resetData(ServerManager.instance().getAllServers().values());
					}
				})
				.show(getFragmentManager(), null);
	}

	private void onUndoDeleteClick(final View clicked) {
		_toDelete.clear();
		_serversAdapter.showAll();
		_undoSnackbar.dismiss();
		_undoSnackbar = null;
	}

	private void setServersListViewAdapter(final Collection<Server> servers)
			throws NacPersistenceRuntimeException {
		Timber.i("Got set of %d servers", servers.size());
		_serversAdapter = new ServerListAdapter(this, servers);
		_serversAdapter.setOnEditClickListener(this::onItemEditClick);
		_serversAdapter.setOnDeleteClickListener(this::onItemDeleteClick);
		_serversListview.setAdapter(_serversAdapter);
	}
}
