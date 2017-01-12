package org.nem.nac.models.primitives;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.core.crypto.Hashes;
import org.nem.core.utils.ArrayUtils;
import org.nem.core.utils.Base32Encoder;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.AddressFormatRuntimeException;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.providers.AddressInfoProvider;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public final class AddressValue implements Parcelable, Comparable<AddressValue> {

	private static final int NUM_CHECKSUM_BYTES       = 4;
	private static final int NUM_DECODED_BYTES_LENGTH = 40;
	private static final int NUM_ENCODED_BYTES_LENGTH = 25;

	public static String stripIllegalChars(final CharSequence source) {
		return source.toString().replaceAll(AppConstants.REGEX_ADDRESS_INPUT_STRIPPABLE_CHARACTERS, "");
	}

	private final String _value;

	@JsonCreator
	public static AddressValue fromValue(final String value) {
		return value != null ? new AddressValue(value) : null;
	}

	/**
	 * Determines if the address is valid.
	 *
	 * @return true if the address is valid.
	 */
	public static boolean isValid(@NonNull final AddressValue value) {
		return isValid(value._value);
	}

	/**
	 * Determines if the address is valid.
	 *
	 * @return true if the address is valid.
	 */
	public static boolean isValid(@Nullable final String value) {
		if (value == null) {
			return false;
		}
		// this check should prevent leading and trailing whitespace
		if (NUM_DECODED_BYTES_LENGTH != value.length()) {
			return false;
		}

		final byte[] encodedBytes;

		try {
			encodedBytes = Base32Encoder.getBytes(value);
		} catch (final IllegalArgumentException e) {
			return false;
		}
		if (NUM_ENCODED_BYTES_LENGTH != encodedBytes.length) {
			return false;
		}

		if (AppConstants.NETWORK_VERSION.get() != encodedBytes[0]) {
			return false;
		}

		final int checksumStartIndex = NUM_ENCODED_BYTES_LENGTH - NUM_CHECKSUM_BYTES;
		final byte[] versionPrefixedHash = Arrays.copyOfRange(encodedBytes, 0, checksumStartIndex);
		final byte[] addressChecksum = Arrays.copyOfRange(encodedBytes, checksumStartIndex, checksumStartIndex + NUM_CHECKSUM_BYTES);
		final byte[] calculatedChecksum = generateChecksum(versionPrefixedHash);
		return Arrays.equals(addressChecksum, calculatedChecksum);
	}

	/**
	 * Creates an Address from a public key.
	 *
	 * @param publicKey The public key.
	 * @return An address object.
	 */
	public static AddressValue fromPublicKey(@NonNull final NacPublicKey publicKey) {
		return new AddressValue(generateEncoded(AppConstants.NETWORK_VERSION.get(), publicKey.getRaw()));
	}

	/**
	 * Creates an Address from a public key.
	 *
	 * @param version   First byte of network version
	 * @param publicKey The public key.
	 * @return An address object.
	 */
	public static AddressValue fromPublicKey(byte version, @NonNull final NacPublicKey publicKey) {
		return new AddressValue(generateEncoded(version, publicKey.getRaw()));
	}

	/**
	 * Creates new {@link AddressValue} from encoded string.
	 *
	 * @throws AddressFormatRuntimeException if address is not valid.
	 */
	public AddressValue(@NonNull final String value)
			throws AddressFormatRuntimeException {
		_value = value;
		if (!isValid(value)) {
			throw new AddressFormatRuntimeException("Invalid address!");
		}
	}

	@JsonValue
	public String getRaw() {
		return _value;
	}

	@JsonIgnore
	public int length() {
		return _value.length();
	}

	public Optional<String> toName() {
		final Optional<AddressInfoProvider.Info> infoOptional = AddressInfoProvider.instance().find(this);
		return infoOptional.isPresent() ? Optional.of(infoOptional.get().getDisplayName()) : Optional.<String>empty();
	}

	public String toNameOrDashed() {
		return toName().orElse(toString(true));
	}

	@Override
	public int compareTo(@NonNull final AddressValue rhs) {
		return this.getRaw().compareTo(rhs.getRaw());
	}

	public String toString(final boolean dashed) {
		if (!dashed) {
			return _value;
		}
		StringBuilder sb = new StringBuilder(_value.length());
		final List<String> parts = StringUtils.split(_value, 6);
		final ListIterator<String> iterator = parts.listIterator();
		while (iterator.hasNext()) {
			final String part = iterator.next();
			sb.append(part);
			if (iterator.hasNext()) {
				sb.append('-');
			}
		}
		return sb.toString();
	}

	/**
	 * @return readable address.
	 */
	@Override
	public String toString() {
		return _value;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof AddressValue)) { return false; }
		final AddressValue av = (AddressValue)o;
		return _value.equalsIgnoreCase(av._value);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
	}

	private static String generateEncoded(final byte version, final byte[] publicKey) {
		// step 1: sha3 hash of the public key
		final byte[] sha3PublicKeyHash = Hashes.sha3_256(publicKey);

		// step 2: ripemd160 hash of (1)
		final byte[] ripemd160StepOneHash = Hashes.ripemd160(sha3PublicKeyHash);

		// step 3: store version byte in front of (2)
		final byte[] versionPrefixedRipemd160Hash = ArrayUtils.concat(new byte[] { version }, ripemd160StepOneHash);

		// step 4: get the checksum of (3)
		final byte[] stepThreeChecksum = generateChecksum(versionPrefixedRipemd160Hash);

		// step 5: concatenate (3) and (4)
		final byte[] concatStepThreeAndStepSix = ArrayUtils.concat(versionPrefixedRipemd160Hash, stepThreeChecksum);

		// step 6: base32 encode (5)
		return Base32Encoder.getString(concatStepThreeAndStepSix);
	}

	private static byte[] generateChecksum(final byte[] input) {
		// step 1: sha3 hash of (input
		final byte[] sha3StepThreeHash = Hashes.sha3_256(input);

		// step 2: get the first X bytes of (1)
		return Arrays.copyOfRange(sha3StepThreeHash, 0, NUM_CHECKSUM_BYTES);
	}

	// Region Parcelable

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {dest.writeString(this._value);}

	protected AddressValue(Parcel in) {this._value = in.readString();}

	public static final Parcelable.Creator<AddressValue> CREATOR = new Parcelable.Creator<AddressValue>() {
		public AddressValue createFromParcel(Parcel source) {return new AddressValue(source);}

		public AddressValue[] newArray(int size) {return new AddressValue[size];}
	};
	// Endregion
}
