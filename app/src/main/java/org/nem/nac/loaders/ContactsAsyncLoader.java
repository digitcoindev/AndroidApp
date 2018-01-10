package org.nem.nac.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nem.nac.models.Contact;
import org.nem.nac.providers.NemContactsProvider;

import java.util.List;

import timber.log.Timber;

public final class ContactsAsyncLoader extends AsyncTaskLoader<List<Contact>> {

	public static final int ID = 1598324;

	private List<Contact> _data;

	public ContactsAsyncLoader(final Context context) {
		super(context);
	}

	@Override
	protected void onStartLoading() {
		if (_data != null) {
			deliverResult(_data);
		}

		if (takeContentChanged() || _data == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();

		onStopLoading();

		if (_data != null) {
			_data = null;
		}
	}

	@Override
	public List<Contact> loadInBackground() {
		Timber.d("Loading contacts");
		return NemContactsProvider.instance().getAllSorted(false);
	}

	@Override
	public void deliverResult(final List<Contact> data) {
		if (isReset()) {
			return;
		}

		_data = data;

		if (isStarted()) {
			super.deliverResult(data);
		}
	}
}
