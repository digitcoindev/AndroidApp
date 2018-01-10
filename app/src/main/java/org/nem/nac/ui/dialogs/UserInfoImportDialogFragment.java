package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.models.Contact;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.NemContactsProvider;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class UserInfoImportDialogFragment extends NacBaseDialogFragment {

	private static final String ARG_STR_NAME    = "user-name";
	private static final String ARG_STR_ADDRESS = "user-address";

	public static UserInfoImportDialogFragment create(final String name, final AddressValue address) {
		UserInfoImportDialogFragment fragment = new UserInfoImportDialogFragment();
		Bundle args = setArgs(true, null, true, null);
		args.putString(ARG_STR_NAME, name);
		args.putString(ARG_STR_ADDRESS, address.getRaw());
		fragment.setArguments(args);
		return fragment;
	}

	private EditText _nameInput;
	private TextView _addressField;
	private CheckBox _sendTransactionCheckbox;
	private String   _name;
	private AddressValue    _address;
	private Contact         _existing;
	private ConfirmListener _confirmListener;

	public UserInfoImportDialogFragment setOnConfirmListener(final ConfirmListener listener) {
		_confirmListener = listener;
		return this;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_user_info_import_dialog;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			_name = getArguments().getString(ARG_STR_NAME, "");
			final String address = getArguments().getString(ARG_STR_ADDRESS);
			if (AddressValue.isValid(address)) {
				_address = new AddressValue(address);
			}
			else {
				Timber.e("Invalid address");
				Toaster.instance().show(R.string.errormessage_invalid_address);
				dismiss();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = super.onCreateView(inflater, container, savedInstanceState);
		_nameInput = (EditText)layout.findViewById(R.id.input_name);
		_addressField = (TextView)layout.findViewById(R.id.field_address);
		_sendTransactionCheckbox = (CheckBox)layout.findViewById(R.id.checkbox_send_transaction);
		return layout;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		setTitle(getString(R.string.dialog_title_import_contact));
		confirmBtn.setText(R.string.btn_add_contact);
		_nameInput.setText(_name);
		_addressField.setText(_address.getRaw());
		_existing = NemContactsProvider.instance().findByAddress(_address);
		if (_existing != null) {
			setTitle(getString(R.string.dialog_title_already_in_address_book, _existing.getName()));
			_nameInput.setEnabled(false);
			_nameInput.setTextColor(getActivity().getResources().getColor(R.color.official_gray));
			confirmBtn.setText(R.string.dialog_option_send_transaction);
			_sendTransactionCheckbox.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		boolean add = _existing == null;
		boolean send = _existing != null || _sendTransactionCheckbox.isChecked();
		if (_confirmListener != null) {
			_confirmListener.apply(add, send, _nameInput.getText().toString());
		}
		super.onConfirmClick(clicked);
	}

	public interface ConfirmListener {

		void apply(final boolean add, final boolean sendTransaction, final String name);
	}
}
