package org.nem.nac.models.api.transactions;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.enums.NetworkVersion;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.models.TimeValue;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.primitives.AddressValue;

import java.util.Date;

@SuppressWarnings("unused") // this is dto
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type",
		visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = TransferTransactionApiDto.class, name = "257"), // 0x0101
		@JsonSubTypes.Type(value = ImportanceTransferTransactionApiDto.class, name = "2049"), // 0x0801
		@JsonSubTypes.Type(value = MultisigAggregateModificationTransactionApiDto.class, name = "4097"), // 0x1001
		@JsonSubTypes.Type(value = MultisigSignatureTransactionApiDto.class, name = "4098"), // 0x1002
		@JsonSubTypes.Type(value = MultisigTransactionApiDto.class, name = "4100"), // 0x1004
		@JsonSubTypes.Type(value = ProvisionNamespaceTransactionApiDto.class, name = "8193"), // 0x2001
		@JsonSubTypes.Type(value = MosaicDefinitionCreationTransactionApiDto.class, name = "16385"), // 0x4001
		@JsonSubTypes.Type(value = MosaicSupplyChangeTransactionApiDto.class, name = "16386") // 0x4002
})
public abstract class AbstractTransactionApiDto {
	/**
	 * The transaction type.
	 */
	public TransactionType type;
	/**
	 * The number of seconds elapsed since the creation of the nemesis block.
	 */
	public TimeValue       timeStamp;
	/**
	 * The version of the structure.
	 */
	public NetworkVersion  version;
	/**
	 * The fee for the transaction.
	 * The higher the fee, the higher the priority of the transaction.
	 * Transactions with high priority get included in a block before transactions with lower priority.
	 */
	public Xems            fee;
	/**
	 * The deadline of the transaction.
	 * The deadline is given as the number of seconds elapsed since the creation of the nemesis block.
	 * If a transaction does not get included in a block before the deadline is reached, it is deleted.
	 */
	public TimeValue       deadline;
	/**
	 * The public key of the account that created the transaction.
	 */
	public NacPublicKey    signer;

	public boolean isSigner(final AddressValue account) {
		return AddressValue.fromPublicKey(signer).equals(account);
	}

	public boolean isSigner(final NacPublicKey account) {
		return signer.equals(account);
	}

	/**
	 * Returns inner transaction if object is Multisig transaction, and "this" if not
	 */
	@NonNull
	public AbstractTransactionApiDto unwrapTransaction() {
		return type == TransactionType.MULTISIG_TRANSACTION ? ((MultisigTransactionApiDto)this).otherTrans : this;
	}

	@JsonIgnore
	@NonNull

	public Date getDate() {
		return AppConstants.NEMESIS_BLOCK_TIMESTAMP.add(timeStamp).toDate();
	}

	@JsonIgnore
	@NonNull
	public Date getDeadlineDate() {
		final TimeValue deadlineTime = AppConstants.NEMESIS_BLOCK_TIMESTAMP.add(deadline);
		return deadlineTime.toDate();
	}

	@JsonIgnore
	@NonNull
	public AddressValue getSignerAddress() {
		return AddressValue.fromPublicKey(signer);
	}
}
