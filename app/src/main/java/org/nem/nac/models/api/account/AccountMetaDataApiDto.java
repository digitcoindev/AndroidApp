package org.nem.nac.models.api.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.common.enums.AccountType;
import org.nem.nac.common.exceptions.NacRuntimeException;

import java.util.ArrayList;
import java.util.List;

public final class AccountMetaDataApiDto {

	public AccountHarvestingStatus status;
	public RemoteHarvestingStatus remoteStatus;
	public List<AccountInfoApiDto> cosignatoryOf;
	public List<AccountInfoApiDto> cosignatories;

	public AccountMetaDataApiDto() {
		this.cosignatoryOf = new ArrayList<>();
		this.cosignatories = new ArrayList<>();
	}

	@JsonIgnore
	public AccountType getType() {
		return AccountType.fromAccount(this);
	}

	public enum AccountHarvestingStatus {
		UNKNOWN("UNKNOWN"),
		LOCKED("LOCKED"),
		UNLOCKED("UNLOCKED");

		private static final AccountHarvestingStatus[] _values = AccountHarvestingStatus.values();

		@JsonCreator
		public static AccountHarvestingStatus fromValue(final String value) {
			for (AccountHarvestingStatus obj : _values) {
				if (obj._value.equals(value)) { return obj; }
			}
			throw new NacRuntimeException("Unknown AccountHarvestingStatus found");
		}

		private String _value;

		AccountHarvestingStatus(final String value) {
			_value = value;
		}

		@JsonValue
		public String getValue() {
			return _value;
		}

		@JsonIgnore
		public String toCapitalizedString() {
			final String name = name();
			return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		}
	}

	public enum RemoteHarvestingStatus {
		REMOTE("REMOTE"),
		ACTIVATING("ACTIVATING"),
		ACTIVE("ACTIVE"),
		DEACTIVATING("DEACTIVATING"),
		INACTIVE("INACTIVE");

		private static final RemoteHarvestingStatus[] _values = RemoteHarvestingStatus.values();

		@JsonCreator
		public static RemoteHarvestingStatus fromValue(final String value) {
			for (RemoteHarvestingStatus obj : _values) {
				if (obj._value.equals(value)) { return obj; }
			}
			throw new NacRuntimeException("Unknown RemoteHarvestingStatus found");
		}

		private String _value;

		RemoteHarvestingStatus(final String value) {
			_value = value;
		}

		@JsonValue
		public String getValue() {
			return _value;
		}

		@JsonIgnore
		public String toCapitalizedString() {
			final String name = name();
			return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		}
	}
}
