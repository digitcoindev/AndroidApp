package org.nem.nac.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.utils.NumberUtils;

/**
 * Represents amount of XEMs. Allows negative values.
 */
public final class Xems {

	public static final Xems ZERO         = Xems.fromMicro(0);
	private             long _amountMicro = 0;

	//region Constructors

	/**
	 * Constructs new object with zero amount
	 */
	public Xems() {
	}

	/**
	 * @param amountMicro amount in microXEMs
	 */
	private Xems(final long amountMicro) {
		_amountMicro = amountMicro;
	}
	//endregion

	//region Get-set

	/**
	 * Returns amount of XEMs as integer. MicroXEMs are ignored.
	 *
	 * @return
	 */
	public long getIntegerPart() {
		return _amountMicro / 1000_000;
	}

	public double getAsFractional() {
		return _amountMicro / 1000_000.0;
	}

	/**
	 * @return amount in microXEMs
	 */
	@JsonValue
	public long getAsMicro() {
		return _amountMicro;
	}

	/**
	 * Sets amount using XEMs.
	 *
	 * @param amount
	 */
	public void setAmount(final double amount) {
		_amountMicro = ((long)amount * 1000_000);
	}
	//endregion

	//region Actions

	public void addXems(final Xems xem) {
		_amountMicro += xem.getIntegerPart() * 1000_000;
	}

	public void addXems(final long amount) {
		_amountMicro += amount * 1000_000;
	}

	public void addXems(final double amount) {
		_amountMicro += (long)(amount * 1000_000);
	}

	public void addMicro(final long amount) {
		_amountMicro += amount;
	}

	public boolean isMoreThan(final Xems amount) {
		return this._amountMicro > amount._amountMicro;
	}

	public boolean isMoreOrEqual(final Xems amount) {
		return this._amountMicro >= amount._amountMicro;
	}
	//endregion

	public static Xems fromXems(final double amount) {
		final long micro = (long)(amount * 1000_000.0);
		return new Xems(micro);
	}

	public static Xems fromXems(final long amount) {
		return new Xems(amount * 1000_000);
	}

	@JsonCreator
	public static Xems fromMicro(final long amount) {
		return new Xems(amount);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Xems xems = (Xems)o;

		return _amountMicro == xems._amountMicro;
	}

	@Override
	public int hashCode() {
		return (int)(_amountMicro ^ (_amountMicro >>> 32));
	}

	public String toFractionalString() {
		return NumberUtils.toAmountString(getAsFractional());
	}

	@Override
	public String toString() {
		return NumberUtils.toAmountString(getAsFractional());
	}
}
