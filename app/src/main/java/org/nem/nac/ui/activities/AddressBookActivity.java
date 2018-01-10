package org.nem.nac.ui.activities;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.helpers.ContactsHelper;
import org.nem.nac.loaders.ContactsAsyncLoader;
import org.nem.nac.models.Contact;
import org.nem.nac.providers.AddressInfoProvider;
import org.nem.nac.providers.NemContactsProvider;
import org.nem.nac.ui.adapters.AddressBookAdapter;
import org.nem.nac.ui.dialogs.ContactEditDialog;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class AddressBookActivity extends NacBaseActivity {

	private static final int SEARCH_DELAY_MS = 200;

	private View     _toolbarRightPanel;
	private TextView _toolbarRightLabel;
	private TextView _addNewBtn;
	private EditText _searchQueryInput;
	private ListView _contactsList;
	private Snackbar _undoSnackbar;
	private List<Contact> _contactsToDelete = new ArrayList<>();

	private AddressBookAdapter _adapter;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_address_book;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_address_book;
	}

	@Override
	public void onBackPressed() {
		if (_contactsList != null && _adapter != null && _adapter.getIsEditMode()) {
			toggleEditMode();
		}
		else {
			finish();
			startActivity(new Intent(this, DashboardActivity.class));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_addNewBtn = (TextView)findViewById(R.id.btn_add_contact);
		_addNewBtn.setOnClickListener(this::onAddContactClick);
		_searchQueryInput = (EditText)findViewById(R.id.input_search);
		_searchQueryInput.addTextChangedListener(SEARCH_TEXT_WATCHER);
		_contactsList = (ListView)findViewById(R.id.listview_contacts);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshContacts();
	}

	@Override
	protected void onStop() {
		super.onStop();
		boolean error = false;
		for (Contact c : _contactsToDelete) {
			try {
				ContactsHelper.deleteContact(this, c);
			} catch (Exception e) {
				Timber.e("Failed to delete contact! %s", c.getName());
				error = true;
			}
		}
		if (error) { Toaster.instance().show(R.string.errormessage_failed_to_delete_contact); }
		_contactsToDelete.clear();
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

	private void refreshContacts() {
		if (getLoaderManager().getLoader(ContactsAsyncLoader.ID) == null) {
			getLoaderManager().initLoader(ContactsAsyncLoader.ID, null, _contactLoaderCallbacks);
		}
		else {
			getLoaderManager().restartLoader(ContactsAsyncLoader.ID, null, _contactLoaderCallbacks);
		}
	}

	private void onEditBtnClick(final View view) {
		toggleEditMode();
	}

	private void toggleEditMode() {
		if (_adapter != null) {
			_adapter.toggleEditMode();
			_toolbarRightLabel.setText(_adapter.getIsEditMode()
					? R.string.toolbar_btn_done
					: R.string.toolbar_btn_edit);
		}
		if (_contactsList != null) {
			_contactsList.post(_contactsList::invalidateViews);
		}
	}

	private void onAddContactClick(final View clicked) {
		_addNewBtn.setEnabled(false);
		try {
			ContactEditDialog.create(null)
					.setOnEditedListener(this::onContactAdd)
					.setOnDismissListener(dialog -> {
						AppHost.SoftKeyboard.forceHideAsync(this);
					})
					.show(getFragmentManager(), null);
		} finally {
			_addNewBtn.setEnabled(true);
		}
	}

	private void onContactAdd(final Contact contact) {
		if (contact.hasValidAddress()) {
			try {
				final Contact existing = NemContactsProvider.instance().findByAddress(contact.getValidAddress().get());
				if (existing != null) {
					final String message = getString(R.string.errormessage_contact_already_exists, existing.getName());
					Toaster.instance().show(message);
				}
				else {
					ContactsHelper.addContact(this, contact.getName(), contact.getValidAddress().get());
					final String message = getString(R.string.message_contact_added, contact.getName());
					Toaster.instance().show(message);
					refreshContacts();
				}
			} catch (Exception e) {
				Timber.e(e, "Failed to add contact");
				Toaster.instance().showGeneralError();
			}
		}
		else {
			Timber.e("Failed to add contact, has no valid address: %s", contact.getRawAddress());
			Toaster.instance().show(R.string.errormessage_failed_to_add_contact);
		}
	}

	private void onItemDeleteClick(final Contact contact) {
		//
		_contactsToDelete.add(contact);
		_adapter.hideContact(contact.contactId);
		if (_undoSnackbar != null) {
			_undoSnackbar.setText(StringUtils.format(R.string.message_contact_deleted, contact.getName()));
		}
		else {
			_undoSnackbar = Snackbar.make(findViewById(R.id.layout_coordinator), StringUtils
					.format(R.string.message_contact_deleted, contact.getName()), Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.action_undo, this::onUndoDeleteClick)
					.setActionTextColor(getResources().getColor(R.color.official_green));
			_undoSnackbar.getView().setBackgroundColor(getResources().getColor(R.color.default_black));
		}
		_undoSnackbar.show();
		//
//		final String askMessage = getString(R.string.dialog_message_ask_delete_contact, contact.getName());
//		ConfirmDialogFragment.create(true, R.string.dialog_title_delete_contact, askMessage, null)
//				.setOnConfirmListener(d -> {
//					try {
//						ContactsHelper.deleteContact(this, contact);
//						final String deletedMessage = getString(R.string.message_contact_deleted, contact.getName());
//						Toaster.instance().show(deletedMessage);
//						refreshContacts();
//						AddressNamesProvider.instance().invalidateContacts();
//					} catch (RemoteException | OperationApplicationException e) {
//						ErrorUtils.sendSilentReport("Failed to delete contact", e);
//						Toaster.instance().show(R.string.errormessage_failed_to_delete_contact);
//					}
//				})
//				.show(getFragmentManager(), null);
	}

	private void onItemEditClick(final Contact contact) {
		ContactEditDialog.create(contact)
				.setOnEditedListener(editedContact -> {
					try {
						ContactsHelper.updateContact(this, editedContact);
						refreshContacts();
						AddressInfoProvider.instance().invalidateContacts();
					} catch (RemoteException | OperationApplicationException e) {
						Toaster.instance().show(R.string.errormessage_failed_to_update_contact);
					}
				})
				.show(getFragmentManager(), null);
	}

	private void onContactSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (_adapter.getIsEditMode()) { return; }
		final Contact selected = _adapter.getItem(position);
		if (selected.hasValidAddress()) {
			final Intent intent = new Intent(this, NewTransactionActivity.class)
					.putExtra(NewTransactionActivity.EXTRA_STR_ADDRESS, selected.getValidAddress().get().getRaw())
					.putExtra(NewTransactionActivity.EXTRA_STR_NAME, selected.getName());
			startActivity(intent);
			finish();
		}
//		else {
//			ContactEditDialog.create(selected)
//					.setOnEditedListener(editedContact -> {
//						try {
//							ContactsHelper.updateContact(this, editedContact);
//							refreshContacts();
//							AddressInfoProvider.instance().invalidateContacts();
//						} catch (RemoteException | OperationApplicationException e) {
//							Toaster.instance().show(R.string.errormessage_failed_to_update_contact);
//						}
//					})
//					.show(getFragmentManager(), null);
//		}
	}

	private void onUndoDeleteClick(final View clicked) {
		_contactsToDelete.clear();
		if (_adapter != null) { _adapter.showAll(); }
		if (_undoSnackbar != null) {
			_undoSnackbar.dismiss();
			_undoSnackbar = null;
		}
	}

	private void setSearchFilter() {
		final CharSequence query = _searchQueryInput.getText();
		Timber.d("filter query: %s", query);
		_adapter.getFilter().filter(query);
	}

	private TextWatcher SEARCH_TEXT_WATCHER = new TextWatcher() {
		private Handler _handler = new Handler(Looper.getMainLooper());

		@Override
		public void afterTextChanged(final Editable s) {
			_handler.removeCallbacks(AddressBookActivity.this::setSearchFilter);
			_handler.postDelayed(AddressBookActivity.this::setSearchFilter, SEARCH_DELAY_MS);
		}

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) { }

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) { }
	};

	private final LoaderManager.LoaderCallbacks<List<Contact>> _contactLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Contact>>() {
		@Override
		public Loader<List<Contact>> onCreateLoader(final int id, final Bundle args) {
			return new ContactsAsyncLoader(AddressBookActivity.this);
		}

		@Override
		public void onLoadFinished(final Loader<List<Contact>> loader, final List<Contact> data) {
			if (data != null) {
				final boolean wasInEditMode = _adapter != null && _adapter.getIsEditMode();
				_adapter = new AddressBookAdapter(AddressBookActivity.this, data);
				_adapter.setOnEditClickListener(AddressBookActivity.this::onItemEditClick);
				_adapter.setOnDeleteClickListener(AddressBookActivity.this::onItemDeleteClick);
				_contactsList.setAdapter(_adapter);
				if (wasInEditMode) {
					_adapter.toggleEditMode();
					_adapter.notifyDataSetChanged();
				}
				_contactsList.setOnItemClickListener(AddressBookActivity.this::onContactSelected);
			}
			else {
				Toaster.instance().show(R.string.errormessage_failed_to_get_contacts);
				finish();
			}
		}

		@Override
		public void onLoaderReset(final Loader<List<Contact>> loader) {
			_contactsList.setAdapter(null);
		}
	};
}
