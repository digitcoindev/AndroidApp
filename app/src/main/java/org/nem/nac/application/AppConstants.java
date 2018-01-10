package org.nem.nac.application;

import org.nem.nac.BuildConfig;
import org.nem.nac.R;
import org.nem.nac.common.TimeSpan;
import org.nem.nac.common.enums.NetworkVersion;
import org.nem.nac.common.models.TimeValue;
import org.nem.nac.models.network.Port;

/**
 * Values which cannot be changed in the application.
 */
public final class AppConstants {

	//public static final NetworkVersion NETWORK_VERSION         = NetworkVersion.MAIN_NETWORK;
	public static final NetworkVersion NETWORK_VERSION = networkVersion();
	public static final TimeValue      DEFAULT_DEADLINE        = TimeValue.fromValue(24*3600); // 1 hour in seconds
	public static final Port           DEFAULT_PORT            = new Port(7890);
	public static final String         PREDEFINED_PROTOCOL     = "http";
	public static final TimeValue      NEMESIS_BLOCK_TIMESTAMP = new TimeValue(1427587585);
	public static final String         ENCODING_UTF8           = "UTF-8";
	// 4 seems to be minimum, IllegalArgumentException if lower (10-08-2015)
	public static final int            BCRYPT_LOG_ROUNDS       = BuildConfig.DEBUG ? 4 : 10;
	public static final int            SALT_SIZE_BYTES           = 256 / 8;
	public static final int            DERIVE_KEY_ITERATIONS     = 2000;
	public static final int            DERIVED_KEY_SIZE_BITS     = 256;
	public static final int            MIN_PASSWORD_LENGTH       = 6;
	public static final TimeSpan       DATA_AUTOREFRESH_INTERVAL = TimeSpan.fromSeconds(BuildConfig.DEBUG ? 10 : 30);
	public static final String         DEFAULT_LANGUAGE          = initDEFAULT_LANGUAGE(); //"en";
	public static final String         LOG_FILE_NAME             = "log.txt";

	public static final String REGEX_ADDRESS_INPUT_STRIPPABLE_CHARACTERS = "[^0-9A-Za-z]";
	public static final String NEM_CONTACT_TYPE                          = NacApplication.getAppContext()
			.getString(R.string.nem_contact_type);
	public static final String QR_IMAGE_STORE_FILE_NAME                  = "qr_share.png";
	public static final int    MAX_MESSAGE_LENGTH_BYTES = 160;
	public static final String REGEX_IP_ADDRESS         =
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	public static final String REGEX_HOSTNAME           =
			"^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?$";
	public static final int    SPLASH_DELAY_MS                           = 2000;

	public static final double MinimumFee_factor=0.05; // old 1
	public static final double MinimumFee_caped=1.25; // old 25
	public static final long MinimumFee_cap_amount=10000;
	public static final double MinimumFee_basic=3;  // new 6
	public static final int MinimumFee_msg_char=32;
	public static final int MinimumFee_max_input=10; // old 99
	public static final String secKey="1a2b3c4d5e6f7g8h9";

	private static NetworkVersion networkVersion(){
		if (BuildConfig.FLAVOR.equals("_testnet") || BuildConfig.FLAVOR.equals("_testnet_cn")){
			return NetworkVersion.TEST_NETWORK;
		} else {
			return NetworkVersion.MAIN_NETWORK;
		}
	}

	private static String initDEFAULT_LANGUAGE(){
		if (BuildConfig.FLAVOR.equals("_mainnet_cn") || BuildConfig.FLAVOR.equals("_testnet_cn"))
			return "zh";
		else
			return "en";
	}
}
