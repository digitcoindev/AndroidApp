package org.nem.nac.datamodel.repositories;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.nem.nac.common.enums.AccountType;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.entities.AccountEntity;
import org.nem.nac.datamodel.mappers.AccountMapper;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.primitives.AddressValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AccountRepository {

	public synchronized boolean any()
			throws NacPersistenceRuntimeException {
		Cursor cursor = null;
		try {
			cursor = NemSQLiteHelper.getInstance()
					.getReadableDbCompartment()
					.query(AccountEntity.class)
					.getCursor();
			return cursor.getCount() > 0;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		} finally {
			if (cursor != null) { cursor.close(); }
		}
	}

	public synchronized List<Account> getAll() {
		try {
			final List<AccountEntity> entities = NemSQLiteHelper.getInstance().getAll(AccountEntity.class);
			List<Account> models = new ArrayList<>(entities.size());
			for (AccountEntity entity : entities) {
				models.add(AccountMapper.toModel(entity));
			}
			return models;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized List<Account> getAllSorted()
			throws NacPersistenceRuntimeException {
		try {
			final List<Account> accounts = getAll();
			Collections.sort(accounts, (lhs, rhs) -> lhs.sortIndex - rhs.sortIndex);
			return accounts;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized Account get(long id)
			throws NacPersistenceRuntimeException {
		try {
			final AccountEntity entity = NemSQLiteHelper.getInstance().get(AccountEntity.class, id);
			return AccountMapper.toModel(entity);
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized Account save(Account model)
			throws NacPersistenceRuntimeException {
		try {
			AccountEntity entity = AccountMapper.toEntity(model);
			if (entity._id == null) {
				final List<AccountEntity> all = NemSQLiteHelper.getInstance().getAll(AccountEntity.class);
				int maxSortIndex = 0;
				for (AccountEntity acc : all) {
					maxSortIndex = Math.max(maxSortIndex, acc.sortIndex);
				}
				entity.sortIndex = maxSortIndex + 1;
			}
			final NemSQLiteHelper.PersistentEntity<AccountEntity> persistentEntity = NemSQLiteHelper.getInstance()
					.insertOrUpdate(entity);
			model.id = persistentEntity._id;
			return model;
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	public synchronized void delete(long id)
			throws NacPersistenceRuntimeException {
		try {
			final Account account = get(id);
			if (account == null) { return; }
			final int sortIndex = account.sortIndex;
			NemSQLiteHelper sqLiteHelper = null;
			try {
				sqLiteHelper = NemSQLiteHelper.getInstance();
				sqLiteHelper.beginTransaction();
				NemSQLiteHelper.getInstance().delete(AccountEntity.class, id);
				final List<AccountEntity> all = sqLiteHelper.getAll(AccountEntity.class);
				for (AccountEntity acc : all) {
					if (acc.sortIndex > sortIndex) {
						acc.sortIndex = acc.sortIndex - 1;
						sqLiteHelper.insertOrUpdate(acc);
					}
				}
				sqLiteHelper.commitTransaction();
			} finally {
				if (sqLiteHelper != null) { sqLiteHelper.endTransaction(); }
			}
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	@NonNull
	public synchronized Optional<Account> find(final AddressValue address)
			throws NacPersistenceRuntimeException {
		try {
			final AccountEntity entity = NemSQLiteHelper.getInstance()
					.getReadableDbCompartment()
					.query(AccountEntity.class)
					.withSelection("address = ?", address.getRaw())
					.get();
			return Optional.ofNullable(AccountMapper.toModel(entity));
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	@NonNull
	public synchronized Optional<Account> find(final NacPublicKey publicKey)
			throws NacPersistenceRuntimeException {
		try {
			final AccountEntity entity = NemSQLiteHelper.getInstance()
					.getReadableDbCompartment()
					.query(AccountEntity.class)
					.withSelection("publicKey = ?", publicKey.toHexStr())
					.get();
			return Optional.ofNullable(AccountMapper.toModel(entity));
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}

	/**
	 * Sets specified multisig status if account with id is found. Does not throw if not found.
	 *
	 * @throws NacPersistenceRuntimeException in case of failure
	 */
	public synchronized void tryUpdateAccountType(final long id, final AccountType type)
			throws NacPersistenceRuntimeException {
		try {
			final AccountEntity entity = NemSQLiteHelper.getInstance().get(AccountEntity.class, id);
			if (entity != null) {
				entity.type = type.id;
				NemSQLiteHelper.getInstance().insertOrUpdate(entity);
			}
		} catch (Exception e) {
			throw new NacPersistenceRuntimeException("Persistence operation failed", e);
		}
	}
}
