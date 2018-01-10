package org.nem.nac.models.transactions.drafts;

import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.SizeOf;
import org.nem.nac.common.enums.MultisigCosignatoryModificationType;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.transactions.AggregateModification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AggregateModificationTransactionDraft extends AbstractTransactionDraft {

	private static final int VERSION = 2;

	public final int minCosignatoriesRelativeChange;
	private final Set<AggregateModification> _modifications = new HashSet<>();

	public AggregateModificationTransactionDraft(final NacPublicKey signer,
			@NonNull final Collection<? extends NacPublicKey> cosignatoriesToAdd, final NacPublicKey cosignatoryToRemove,
			final int minCosignatoriesRelativeChange) {
		super(VERSION, signer);
		AssertUtils.notNull(cosignatoriesToAdd);
		this.minCosignatoriesRelativeChange = minCosignatoriesRelativeChange;
		final List<AggregateModification> addModifications = Stream.of(cosignatoriesToAdd)
				.map(x -> new AggregateModification(MultisigCosignatoryModificationType.ADD_NEW_COSIGNATORY, x))
				.collect(Collectors.toList());
		_modifications.addAll(addModifications);
		if (cosignatoryToRemove != null) {
			_modifications.add(new AggregateModification(MultisigCosignatoryModificationType.DELETE_EXISTING_COSIGNATORY, cosignatoryToRemove));
		}
	}

	@NonNull
	@Override
	public TransactionType getType() {
		return TransactionType.MULTISIG_AGGREGATE_MODIFICATION_TRANSACTION;
	}

	@NonNull
	@Override
	public Xems calculateMinimumFee() {
		final Xems fee = new Xems();
		final int modificationsCount = _modifications.size();
		final boolean minCosigsModificationPresent = this.minCosignatoriesRelativeChange != 0;
		//fee.addXems(10 + 6 * modificationsCount);
		fee.addXems(10* AppConstants.MinimumFee_factor + AppConstants.MinimumFee_basic *
				AppConstants.MinimumFee_factor * modificationsCount);
		if (minCosigsModificationPresent) {
			//fee.addXems(6);
			fee.addXems(AppConstants.MinimumFee_basic*AppConstants.MinimumFee_factor);
		}
		return fee;
	}

	@Override
	protected void serializeAdditional(@NonNull final ByteArrayOutputStream os)
			throws IOException {
		// Number of cosignatory modifications
		final int modificationsNumber = _modifications.size();
		writeAsLeBytes(os, modificationsNumber);
		final List<AggregateModification> sortedModifications = Stream.of(_modifications)
				.sorted()
				.collect(Collectors.toList());
		// Modifications
		for (AggregateModification mod : sortedModifications) {
			mod.serialize(os);
		}
		writeAsLeBytes(os, minCosignatoriesRelativeChange != 0 ? SizeOf.INT : 0);
		if (minCosignatoriesRelativeChange != 0) {
			writeAsLeBytes(os, minCosignatoriesRelativeChange);
		}
		// Note: BloodyRookie:
		// It should be possible to leave out the min cosignatories part in case there is no change for min cosignatories
		// (leave out both, the 'Length" field and the 'relative change' field).
		// Note 2(2015/08/17): Stopped working, gives "signature not verifiable", reverted to tested behaviour.
	}
}
