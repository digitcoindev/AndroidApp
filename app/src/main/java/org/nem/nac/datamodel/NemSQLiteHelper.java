package org.nem.nac.datamodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.datamodel.entities.AccountEntity;
import org.nem.nac.datamodel.entities.AppPasswordEntity;
import org.nem.nac.datamodel.entities.InvoiceEntity;
import org.nem.nac.datamodel.entities.InvoiceMessageEntity;
import org.nem.nac.datamodel.entities.InvoiceNumberEntity;
import org.nem.nac.datamodel.entities.LastTransactionEntity;
import org.nem.nac.datamodel.entities.ServerEntity;

import java.util.Collection;
import java.util.List;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.DatabaseCompartment;
import timber.log.Timber;

public final class NemSQLiteHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG    = NemSQLiteHelper.class.getSimpleName();
	private static final int    DB_VERSION = 4;
	private static final String DB_NAME    = "nem_database.db";

	private static NemSQLiteHelper _instance;
	private static Context         _appContext;
	private static Cupboard        _cupboard;

	public static NemSQLiteHelper getInstance()
			throws NacException {
		if (null == _instance) {
			if (null == _appContext) {
				throw new NacException("Uninitialized context. call setAppContext before first use");
			}
			_instance = new NemSQLiteHelper(_appContext);
		}

		return _instance;
	}

	public static void setAppContext(Context appContext) {
		_appContext = appContext;
	}

	static {
		_cupboard = new CupboardBuilder().useAnnotations().build();
		// Register models
		_cupboard.register(AccountEntity.class);
		_cupboard.register(InvoiceEntity.class);
		_cupboard.register(InvoiceMessageEntity.class);
		_cupboard.register(InvoiceNumberEntity.class);
		_cupboard.register(AppPasswordEntity.class);
		_cupboard.register(LastTransactionEntity.class);
		_cupboard.register(ServerEntity.class);
	}

	private NemSQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		_cupboard.withDatabase(db).createTables();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(LOG_TAG, "Upgrading DB from version " + oldVersion + " to version " + newVersion);
		if (oldVersion < 4 && newVersion == 4) {
			// Version 4 is mainnet version
			_cupboard.withDatabase(db).dropAllTables();
		}
		// this line will upgrade database, adding columns and new tables.
		// Note that existing columns will not be converted from what they originally were
		_cupboard.withDatabase(db).upgradeTables();
	}

	public DatabaseCompartment getReadableDbCompartment() {
		return _cupboard.withDatabase(getReadableDatabase());
	}

	public DatabaseCompartment getWritableDbCompartment() {
		return _cupboard.withDatabase(getWritableDatabase());
	}

	public void beginTransaction() {
		Timber.d("Begin transaction");
		getWritableDatabase().beginTransaction();
	}

	public void endTransaction() {
		getWritableDatabase().endTransaction();
		Timber.d("Transaction ended");
	}

	/**
	 * Marks transaction as successful.
	 */
	public void commitTransaction() {
		getWritableDatabase().setTransactionSuccessful();
		Timber.d("Transaction committed");
	}

	public <T> List<T> getAll(Class<T> entityClass) {
		return getReadableDbCompartment().query(entityClass).list();
	}

	public <T> T get(Class<T> entityClass, long id) {
		return getReadableDbCompartment().get(entityClass, id);
	}

	public <T> PersistentEntity<T> insertOrUpdate(T entity) {
		long id = getWritableDbCompartment().put(entity);
		return new PersistentEntity<T>(id, entity);
	}

	public <T> boolean delete(Class<T> entityClass, long id) {
		return getWritableDbCompartment().delete(entityClass, id);
	}

	/**
	 * @return Should return number of rows affected (depends on what cupboard's {@link DatabaseCompartment#delete(Class, String, String...)} returns)
	 */
	public <T> int deleteAll(Class<T> entityClass, Collection<Long> ids) {
		if (ids == null || ids.isEmpty()) { return 0; }
		final StringBuilder sb = new StringBuilder();
		for (Long id : ids) {
			if (id == null) { continue; }
			sb.append(id).append(",");
		}
		if (sb.length() > 0) { sb.setLength(sb.length() - 1); }

		final String selection = String.format("_ID IN (%s)", sb.toString());
		return getWritableDbCompartment().delete(entityClass, selection);
	}

	public static class PersistentEntity<T> {

		public long _id;
		public T    entity;

		PersistentEntity(long _id, T entity) {
			this._id = _id;
			this.entity = entity;
		}
	}
}
