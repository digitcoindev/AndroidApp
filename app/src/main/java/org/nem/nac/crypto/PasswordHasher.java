package org.nem.nac.crypto;

import org.mindrot.jbcrypt.BCrypt;
import org.nem.nac.application.AppConstants;

public final class PasswordHasher {
	public static String hash(final String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(AppConstants.BCRYPT_LOG_ROUNDS));
	}

	public static boolean check(final String password, final String hash) {
		return BCrypt.checkpw(password, hash);
	}
}
