package org.nem.nac.providers;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.primitives.AddressValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public final class AddressInfoProvider {

	private static AddressInfoProvider _instance;

	public static synchronized AddressInfoProvider instance() {
		if (_instance == null) {
			_instance = new AddressInfoProvider();
		}
		return _instance;
	}

	private final Map<AddressValue, Info> _local           = new HashMap<>();
	private final Map<AddressValue, Info> _contacts        = new HashMap<>();
	private final AtomicBoolean           _invalidateLocal = new AtomicBoolean(true);
	private final AtomicBoolean             _invalidateContacts = new AtomicBoolean(true);

	public synchronized void invalidateLocal() {
		_invalidateLocal.set(true);
		Timber.d("Invalidate local request");
	}

	public synchronized void invalidateContacts() {
		_invalidateContacts.set(true);
		Timber.d("Invalidate contacts request");
	}

	public synchronized Map<AddressValue, Info> getAll() {
		refreshAll();
		final HashMap<AddressValue, Info> result = new HashMap<>(_contacts);
		result.putAll(_local); // local has priority
		return result;
	}

	public synchronized Map<AddressValue, Info> getLocal() {
		refreshLocal();
		return new HashMap<>(_local);
	}

	public synchronized Optional<Info> find(final AddressValue address) {
		Timber.d("Finding address info");
		refreshAll();
		final Info local = _local.get(address);
		return local != null ? Optional.of(local) : Optional.ofNullable(_contacts.get(address));
	}

	private void refreshAll() {
		refreshLocal();
		refreshContacts();
	}

	private void refreshLocal() {
		if (_invalidateLocal.get()) {
			Timber.d("Invalidating local");
			_local.clear();
			Stream.of(new AccountRepository().getAllSorted())
					.forEach(x -> _local.put(x.publicData.address, new Info(x.publicData.address, x.publicData.publicKey, x.name)));
			_invalidateLocal.set(false);
		}
	}

	private void refreshContacts() {
		if (_invalidateContacts.get()) {
			Timber.d("Invalidating contacts");
			_contacts.clear();
			Stream.of(NemContactsProvider.instance().getAllSorted(true))
					.forEach(x -> {
						final Optional<AddressValue> addr = x.getValidAddress();
						if (addr.isPresent()) {
							_contacts.put(addr.get(), new Info(addr.get(), null, x.getName()));
						}
					});
			_invalidateContacts.set(false);
		}
	}

	public static class Info {

		private final String       _displayName;
		private final NacPublicKey _publicKey;
		public final  AddressValue address;

		public Info(final AddressValue address, final NacPublicKey publicKey, final String displayName) {
			this.address = address;
			this._publicKey = publicKey;
			this._displayName = displayName;
		}

		@NonNull
		public String getDisplayName() {
			return _displayName != null ? _displayName : "";
		}

		@NonNull
		public Optional<NacPublicKey> getPublicKey() {
			return Optional.ofNullable(_publicKey);
		}
	}
}
