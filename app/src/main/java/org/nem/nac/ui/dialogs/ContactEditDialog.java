package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.models.Contact;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.NemContactsProvider;
import org.nem.nac.ui.controls.AutocompleteAddressInput;
import org.nem.nac.ui.utils.InputErrorUtils;
import org.nem.nac.ui.utils.Toaster;

public final class ContactEditDialog extends NacBaseDialogFragment {

	private static final String ARG_PARC_CONTACT_TO_EDIT = "arg-contact-to-edit";

	public static ContactEditDialog create(final @Nullable Contact contactToEdit) {
		ContactEditDialog fragment = new ContactEditDialog();
		final Bundle args = setArgs(true, R.string.dialog_title_add_contact, true, null);
		if (contactToEdit != null) { args.putParcelable(ARG_PARC_CONTACT_TO_EDIT, contactToEdit); }
		fragment.setArguments(args);
		return fragment;
	}

	private EditText                 _nameInput;
	private AutocompleteAddressInput _addressInput;
	private Contact                  _contact;
	private Consumer<Contact>        _editedListener;

	public ContactEditDialog setOnEditedListener(final Consumer<Contact> listener) {
		_editedListener = listener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			_contact = getArguments().getParcelable(ARG_PARC_CONTACT_TO_EDIT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = super.onCreateView(inflater, container, savedInstanceState);
		_nameInput = (EditText)layout.findViewById(R.id.input_name);
		_nameInput.addTextChangedListener(new InputErrorUtils.RemoveErrorWatcher(_nameInput));
		_addressInput = (AutocompleteAddressInput)layout.findViewById(R.id.input_address);
		return layout;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (_contact != null) {
			_nameInput.setText(_contact.getName());
			_addressInput.setText(_contact.hasValidAddress() ? _contact.getValidAddress().get().toString() : "");
			setTitle(getString(R.string.dialog_title_edit_contact));
		}
		AppHost.SoftKeyboard.forceShowAsync();
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		if (_nameInput.length() < 1) {
			InputErrorUtils.setErrorState(_nameInput, R.string.errormessage_contact_name_empty);
			_nameInput.requestFocus();
			return;
		}
		final Optional<AddressValue> address = _addressInput.getAddressIfValid();
		if (!address.isPresent()) {
			_addressInput.requestFocus();
			return;
		}
		//
		Contact existing;
		if (_contact != null && _contact.getName().equals(_nameInput.getText().toString())
				&& !_contact.hasValidAddress() && (existing = NemContactsProvider.instance().findByAddress(address.get())) != null) {
			Toaster.instance().show(getString(R.string.errormessage_contact_already_exists, existing.getName()));
			return;
		}
		//
		if (_editedListener != null) {
			final Contact edited = _contact != null ? _contact : new Contact(null, null);
			edited.setName(_nameInput.getText().toString());
			edited.setRawAddress(address.get().getRaw());
			_editedListener.accept(edited);
			super.onConfirmClick(clicked);
		}
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_contact_edit_dialog;
	}
}
