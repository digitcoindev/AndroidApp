package org.nem.nac.common.models;

import org.nem.nac.models.BinaryData;

public final class HashValue {
	public BinaryData data;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HashValue hashValue = (HashValue)o;

		return !(data != null ? !data.equals(hashValue.data) : hashValue.data != null);
	}

	@Override
	public int hashCode() {
		return data != null ? data.hashCode() : 0;
	}
}
