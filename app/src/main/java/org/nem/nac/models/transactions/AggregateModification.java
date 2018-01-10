package org.nem.nac.models.transactions;

import android.support.annotation.NonNull;

import org.nem.nac.common.SizeOf;
import org.nem.nac.common.enums.MultisigCosignatoryModificationType;
import org.nem.nac.common.utils.ConvertUtils;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.models.NacPublicKey;

import java.io.IOException;
import java.io.OutputStream;

public final class AggregateModification implements Comparable<AggregateModification> {

	public final MultisigCosignatoryModificationType type;
	public final NacPublicKey                        cosignatory;

	public AggregateModification(final MultisigCosignatoryModificationType type, final NacPublicKey cosignatory) {
		this.type = type;
		this.cosignatory = cosignatory;
	}

	public int getLength() {
		return SizeOf.INT + SizeOf.INT + cosignatory.length();
	}

	public void serialize(final OutputStream stream)
			throws IOException {
		// Length of cosignatory modification structure
		stream.write(ConvertUtils.toLeBytes(getLength()));
		// Modification type
		stream.write(ConvertUtils.toLeBytes(type.getValue()));
		// Length of cosignatory's public key byte array
		stream.write(ConvertUtils.toLeBytes(cosignatory.length()));
		// Public key bytes of cosignatory
		stream.write(cosignatory.getRaw());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		AggregateModification that = (AggregateModification)o;

		if (type != that.type) { return false; }
		return cosignatory.equals(that.cosignatory);
	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + cosignatory.hashCode();
		return result;
	}

	@Override
	public int compareTo(@NonNull final AggregateModification another) {
		final int typeCompareResult = NumberUtils.compare(this.type.getValue(), another.type.getValue());
		return 0 != typeCompareResult
				? typeCompareResult
				: this.cosignatory.toAddress().compareTo(another.cosignatory.toAddress());
	}
}
