package org.nem.nac.log;

import java.util.HashMap;
import java.util.Map;

public final class LogTags {

	public static final LogTag TRANSACTIONS  = new LogTag("TRANSACTION", false);
	public static final LogTag CAMERA_FRAMES          = new LogTag("CAM_FRAMES", false);
	public static final LogTag CAMERA_INIT   = new LogTag("CAM_INIT", false);
	public static final LogTag SCAN_FRAGMENT = new LogTag("SCAN_FRAGMENT", false);
	public static final LogTag EKEY_GET_SET  = new LogTag("PASS_CHANGE", false);
	public static final LogTag TAB_FRAGMENT_LIFECYCLE = new LogTag("TAB_FRAGMENT_LIFECYCLE", false);
	public static final LogTag QR_CREATION            = new LogTag("QR_CREATION", true);

	private static final Map<String, Boolean> _logToFile = new HashMap<>();

	static {
		_logToFile.put(TRANSACTIONS.name, false);
		_logToFile.put(CAMERA_FRAMES.name, false);
		_logToFile.put(CAMERA_INIT.name, false);
		_logToFile.put(SCAN_FRAGMENT.name, false);
		_logToFile.put(EKEY_GET_SET.name, false);
		_logToFile.put(TAB_FRAGMENT_LIFECYCLE.name, false);
		_logToFile.put(QR_CREATION.name, false);
	}

	public static boolean logToFile(final String tag) {
		final Boolean log = _logToFile.get(tag);
		return log != null && log;
	}

	public static class LogTag {

		public final int     id;
		public final String  name;
		public final boolean isLogged;

		LogTag(final String name, final boolean isLogged) {
			this.name = name;
			this.isLogged = isLogged;
			this.id = name.hashCode();
		}
	}
}
