package org.nem.nac.helpers;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.Contact;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.AddressInfoProvider;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;

import timber.log.Timber;

public final class ContactsHelper {

	public static void addContact(final Context context, final String name, final AddressValue address)
			throws RemoteException, OperationApplicationException {
		ArrayList<ContentProviderOperation> ops = new ArrayList<>();
		int rawContactInsertIndex = ops.size();

		ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
		//Display name/Contact name
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
				.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
				.build());
		//IM details
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
				.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM)
				.withValue(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, AppConstants.NEM_CONTACT_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Im.DATA, address.getRaw())
				.build());

		context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

		AddressInfoProvider.instance().invalidateContacts();
	}

	public static void updateContact(final Context context, final Contact contact)
			throws RemoteException, OperationApplicationException {
		if (!contact.existingContact()) {
			return;
		}
		final ContentResolver contentResolver = context.getContentResolver();
		ArrayList<ContentProviderOperation> ops = new ArrayList<>();
		final long rawContactId = contact.getRawContactId().get();
		// Name
		boolean hasStoredAddressRecord = false;
		boolean hasStoredNameRecord = false;
		Contact stored = new Contact(contact.contactId, rawContactId);
		//
		Cursor dataCursor = null;
		try {
			final String[] projection = new String[] { };
			final String selection = ContactsContract.Data.RAW_CONTACT_ID + "=?";
			final String[] selectionArgs = { String.valueOf(rawContactId) };
			dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
			if (dataCursor != null) {
				while (dataCursor.moveToNext()) {
					final int mimeTypeIndex = dataCursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
					if (mimeTypeIndex < 0) { continue; }
					switch (dataCursor.getString(mimeTypeIndex)) {
						case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE: {
							final int nameIndex = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY);
							if (nameIndex < 0) { continue; }
							hasStoredNameRecord = true;
							stored.setName(dataCursor.getString(nameIndex));
							continue;
						}
						case ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE: {
							final int protoIndex = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
							final int addressIndex = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA);
							if (protoIndex < 0 || addressIndex < 0) { continue; }
							final boolean isNemContact = AppConstants.NEM_CONTACT_TYPE.equals(dataCursor.getString(protoIndex));
							if (isNemContact) {
								hasStoredAddressRecord = true;
								stored.setRawAddress(dataCursor.getString(addressIndex));
							}
							continue;
						}
					}
				}
			}
		} finally {
			if (dataCursor != null) { dataCursor.close(); }
		}
		//

		// Name
		if (!contact.getName().equals(stored.getName())) {
			if (hasStoredNameRecord) {
				String selection = ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.CommonDataKinds.StructuredName.MIMETYPE + "=?";
				String[] selectionArgs =
						new String[] { String.valueOf(rawContactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
				ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
						.withSelection(selection, selectionArgs)
						.withExpectedCount(1)
						.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
						.build());
			}
			else {
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
						.build());
			}
		}

		final String storedAddress = stored.getRawAddress();
		final String newAddress = contact.getRawAddress();

		if (!StringUtils.equals(storedAddress, newAddress)) {
			if (hasStoredAddressRecord) {
				String selection =
						ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.CommonDataKinds.Im.MIMETYPE + "=?" + " AND " + ContactsContract.CommonDataKinds.Im.PROTOCOL + "=?";
				String[] selectionArgs =
						new String[] { String
								.valueOf(rawContactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, AppConstants.NEM_CONTACT_TYPE };
				ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
						.withSelection(selection, selectionArgs)
						.withExpectedCount(1)
						.withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM)
						.withValue(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, AppConstants.NEM_CONTACT_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Im.DATA, contact.getRawAddress())
						.build());
			}
			else {
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
						.withValue(ContactsContract.CommonDataKinds.Im.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM)
						.withValue(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, AppConstants.NEM_CONTACT_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Im.DATA, contact.getRawAddress())
						.build());
			}
		}

		ContentProviderResult[] results = contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
	}

	public static void deleteContact(Context context, final Contact contact)
			throws RemoteException, OperationApplicationException {
		if (!contact.existingContact()) {
			Timber.e("Tried to delete non-existing contact! %s/%s", contact.getName(), contact.getRawAddress());
			Toaster.instance().show(R.string.errormessage_non_existing_contact);
			return;
		}
		final ContentResolver contentResolver = context.getContentResolver();

		ArrayList<ContentProviderOperation> ops = new ArrayList<>();

		final Optional<Long> rawId = contact.getRawContactId();
		// Delete name raw contact
		if (rawId.isPresent()) {
			final long id = rawId.get();
			final String selection = ContactsContract.RawContacts._ID + "=?";
			final String[] selectionArgs = new String[] { String.valueOf(id) };
			ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
					.withSelection(selection, selectionArgs)
					.withExpectedCount(1)
					.build());
		}

		contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
	}
}
