package org.nem.nac.ui.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.models.network.Server;
import org.nem.nac.servers.ServerManager;
import org.nem.nac.ui.controls.HostInput;
import org.nem.nac.ui.controls.PortInput;
import org.nem.nac.ui.controls.ProtocolInput;

import timber.log.Timber;

public final class EditServerDialogFragment extends NacBaseDialogFragment {

	public static final  String DIALOG_TAG           = EditServerDialogFragment.class.getSimpleName();
	private static final String ARG_LONG_EDITABLE_ID = "arg-editable-id";

	public static EditServerDialogFragment create(@Nullable final Long editableServerId) {
		final EditServerDialogFragment fragment = new EditServerDialogFragment();
		final Bundle args = setArgs(true, R.string.title_dialog_add_server, true, null);
		if (editableServerId != null) { args.putLong(ARG_LONG_EDITABLE_ID, editableServerId); }
		fragment.setArguments(args);
		return fragment;
	}

	private ProtocolInput _protocolInput;
	private HostInput     _hostInput;
	private PortInput     _portInput;
	private Long          _editableId;
	private Server _editable = null;
	private Consumer<Server> _changedListener;
	private boolean _anyValueChanged = false;

	public EditServerDialogFragment setOnChangedListener(final Consumer<Server> listener) {
		_changedListener = listener;
		return this;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			if (getArguments().containsKey(ARG_LONG_EDITABLE_ID)) {
				_editableId = getArguments().getLong(ARG_LONG_EDITABLE_ID);
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		if (view == null) {
			return null;
		}
		_protocolInput = ((ProtocolInput)view.findViewById(R.id.input_protocol));
		_protocolInput.setText(AppConstants.PREDEFINED_PROTOCOL);
		_hostInput = (HostInput)view.findViewById(R.id.input_host);
		_hostInput.requestFocus();
		_portInput = (PortInput)view.findViewById(R.id.input_port);
		_portInput.setAllowEmpty(true);
		return view;
	}

	@SuppressLint("SetTextI18n") // Port doesn't need localization
	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_editable = null;
		if (_editableId != null) {
			final Optional<Server> editable = ServerManager.instance().getById(_editableId);
			if (editable.isPresent()) {
				_editable = editable.get();
			}
		}
		if (_editable == null) {
			_editable = new Server(AppConstants.PREDEFINED_PROTOCOL, "", AppConstants.DEFAULT_PORT);
		}
		_protocolInput.setText(_editable.protocol);
		_hostInput.setText(_editable.host);
		_portInput.setText(Integer.toString(_editable.port.getValue()));
		enableChangeWatcher(true);
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_add_server_dialog;
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		if (!_protocolInput.validate()) {
			_protocolInput.requestFocus();
			return;
		}
		if (!_hostInput.validate()) {
			_hostInput.requestFocus();
			return;
		}
		if (!_portInput.validate()) {
			_portInput.requestFocus();
			return;
		}

		if (_anyValueChanged) {
			try {
				final long id = _editable.id;
				_editable = new Server(_protocolInput.getText().toString(), _hostInput.getText().toString(), _portInput.getPort());
				_editable.id = id;
				if (_changedListener != null) {
					_changedListener.accept(_editable);
				}
			} catch (NacException e) {
				Timber.e(e, "Port input exception - forgot to call validate?");
				throw new NacRuntimeException(e);
			}
		}
		super.onConfirmClick(clicked);
	}

	private void enableChangeWatcher(final boolean enable) {
		if (enable) {
			_protocolInput.addTextChangedListener(_changeWatcher);
			_hostInput.addTextChangedListener(_changeWatcher);
			_portInput.addTextChangedListener(_changeWatcher);
		}
		else {
			_protocolInput.removeTextChangedListener(_changeWatcher);
			_hostInput.removeTextChangedListener(_changeWatcher);
			_portInput.removeTextChangedListener(_changeWatcher);
		}
	}

	private final TextWatcher _changeWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			_anyValueChanged = true;
			enableChangeWatcher(false);
		}
	};
}
