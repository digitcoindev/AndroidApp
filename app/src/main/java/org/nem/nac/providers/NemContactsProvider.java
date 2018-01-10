package org.nem.nac.providers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.application.AppConstants;
import org.nem.nac.application.NacApplication;
import org.nem.nac.common.utils.CollectionUtils;
import org.nem.nac.models.Contact;
import org.nem.nac.models.primitives.AddressValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for getting device contacts that have NEM address.
 */
public final class NemContactsProvider {

	private static NemContactsProvider _instance;

	@NonNull
	public static synchronized NemContactsProvider instance() {
		if (_instance == null) {
			_instance = new NemContactsProvider();
		}
		return _instance;
	}

	private NemContactsProvider() {
	}

	/**
	 * Gets list of contacts with NEM addresses.
	 *
	 * @param nemOnly if true, only NEM contacts will be returned, otherwise all device contacts.
	 */
	@NonNull
	public synchronized List<Contact> getAllSorted(final boolean nemOnly) {
		return getFromDevice(nemOnly);
	}

	@Nullable
	public Contact findByAddress(@NonNull final AddressValue address) {
		for (Contact contact : getAllSorted(true)) {
			if (contact.hasValidAddress() && contact.getValidAddress().get().equals(address)) {
				return contact;
			}
		}
		return null;
	}

	/**
	 * Returns all contacts that have NEM addresses.
	 */
	@NonNull
	private synchronized List<Contact> getFromDevice(final boolean nemContactsOnly) {
		final ContentResolver contentResolver = NacApplication.getAppContext().getContentResolver();
		//
		Map<Long, Contact> contacts2 = new HashMap<>();
		// Addresses
		Cursor addressesCursor = null;
		try {
			String selection = ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL + "=?";
			String[] selectionArgs = new String[] { ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE, AppConstants.NEM_CONTACT_TYPE };
			String[] projection =
					new String[] { ContactsContract.Data.CONTACT_ID, ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.CommonDataKinds.Im.DATA };
			addressesCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
			if (addressesCursor != null) {
				while (addressesCursor.moveToNext()) {
					final long id = addressesCursor.getLong(addressesCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
					final long rawId = addressesCursor.getLong(addressesCursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
					final String rawAddress = addressesCursor.getString(addressesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
					Contact contact = contacts2.get(id);
					if (contact == null) { contact = new Contact(id, rawId); }
					contact.setRawAddress(rawAddress);
					contacts2.put(id, contact);
				}
			}
		} finally {
			if (addressesCursor != null) { addressesCursor.close(); }
		}
		// Names
		Cursor namesCursor = null;
		try {
			String selection = ContactsContract.Data.MIMETYPE + "=?";
			if (nemContactsOnly) {
				selection = selection + " AND " + ContactsContract.Data.CONTACT_ID + " IN(" + CollectionUtils.join(contacts2.keySet(), ",", null) + ")";
			}
			String[] selectionArgs = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
			String[] projection =
					new String[] { ContactsContract.Data.CONTACT_ID, ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY };
			namesCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
			if (namesCursor != null) {
				while (namesCursor.moveToNext()) {
					final long id = namesCursor.getLong(namesCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
					final long rawId = namesCursor.getLong(namesCursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
					final String name = namesCursor.getString(namesCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY));
					Contact contact = contacts2.get(id);
					if (contact == null) { contact = new Contact(id, rawId); }
					contact.setName(name);
					contacts2.put(id, contact);
				}
			}
		} finally {
			if (namesCursor != null) { namesCursor.close(); }
		}

		final List<Contact> sortedContacts = new ArrayList<>(contacts2.values());
		Collections.sort(sortedContacts, (lhs, rhs) -> {
			boolean rNem = rhs.hasValidAddress(), lNem = lhs.hasValidAddress();
			if (rNem && !lNem) { return 1; }
			if (lNem && !rNem) { return -1; }
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		});

		return sortedContacts;
	}
}
