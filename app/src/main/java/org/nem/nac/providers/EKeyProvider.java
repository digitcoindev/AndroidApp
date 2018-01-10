package org.nem.nac.providers;

import android.util.Log;

import com.annimon.stream.Optional;

import org.nem.nac.common.utils.LogUtils;
import org.nem.nac.log.LogTags;
import org.nem.nac.models.BinaryData;

public final class EKeyProvider {
	private static EKeyProvider _instance;

	public static synchronized EKeyProvider instance() {
		if (_instance == null) {
			_instance = new EKeyProvider();
		}
		return _instance;
	}

	private BinaryData _key;

	private EKeyProvider() {
	}

	public synchronized Optional<BinaryData> getKey() {
		LogUtils.conditional(Log.WARN, LogTags.EKEY_GET_SET.isLogged, LogTags.EKEY_GET_SET.name, "Getting key");
		return Optional.ofNullable(_key);
	}

	public synchronized void setKey(final BinaryData key) {
		_key = key;
		LogUtils.conditional(Log.WARN, LogTags.EKEY_GET_SET.isLogged, LogTags.EKEY_GET_SET.name, "Key set to: %s", key);
	}
}
