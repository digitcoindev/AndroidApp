package org.nem.nac.models.api;

import android.support.annotation.StringRes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.nem.nac.R;

import timber.log.Timber;

public enum ValidationResultCode implements ApiResultCode {
	/**
	 * Stub for unknown results
	 */
	UNKNOWN(-1, R.string.server_result_code_unknown_result),
	/**
	 * Neutral result. A typical example would be that a node validates an incoming transaction and realizes that it already knows about the transaction.
	 * In this case it is neither a success (meaning the node has a new transaction) nor a failure (because the transaction itself is valid).
	 */
	NEUTRAL(0, R.string.server_result_code_neutral),
	/**
	 * Success result. A typical example would be that a node validates a new valid transaction.
	 */
	SUCCESS(1, R.string.server_result_code_success),
	/**
	 * Unknown failure. The validation failed for unknown reasons.
	 */
	UNKNOWN_FAILURE(2, R.string.server_result_code_unknown_failure),
	/**
	 * The entity that was validated has already past its deadline.
	 */
	PAST_DEADLINE(3, R.string.server_result_code_past_deadline),
	/**
	 * The entity used a deadline which lies too far in the future.
	 */
	DEADLINE_TOO_FAR(4, R.string.server_result_code_deadline_too_far),
	/**
	 * There was an account involved which had an insufficient balance to perform the operation.
	 */
	INSUFFICIENT_BALANCE(5, R.string.server_result_code_insufficient_balance),
	/**
	 * The message supplied with the transaction is too large.
	 */
	MESSAGE_TOO_LARGE(6, R.string.server_result_code_message_too_large),
	/**
	 * The hash of the entity which got validated is already in the database.
	 */
	HASH_ALREADY_IN_DB(7, R.string.server_result_code_hash_already_in_db),
	/**
	 * The signature of the entity could not be validated.
	 */
	CANNOT_VALIDATE_SIGNATURE(8, R.string.server_result_code_cannot_validate_signature),
	/**
	 * The entity used a timestamp that lies too far in the past.
	 */
	TIMESTAMP_TOO_FAR_IN_THE_PAST(9, R.string.errormessage_internal_error),
	/**
	 * The entity used a timestamp that lies in the future which is not acceptable.
	 */
	TIMESTAMP_IN_THE_FUTURE(10, R.string.errormessage_internal_error),
	/**
	 * The entity is unusable.
	 */
	UNUSABLE_ENTITY(11, R.string.server_result_code_unusable_entity),
	/**
	 * The score of the remote block chain is inferior (although a superior score was promised).
	 */
	SCORE_INFERIOR(12, R.string.server_result_code_score_inferior),
	/**
	 * The remote block chain failed validation.
	 */
	BLOCKCHAIN_VALIDATION_FAILED(13, R.string.server_result_code_blockchain_validation_failed),
	/**
	 * There was a conflicting importance transfer detected.
	 */
	CONFLICTING_IMPORTANCE_TRANSFER(14, R.string.server_result_code_conflicting_importance_transfer),
	/**
	 * There were too many transaction in the supplied block.
	 */
	TOO_MANY_TRANSACTIONS_IN_BLOCK(15, R.string.server_result_code_too_many_transactions_in_block),
	/**
	 * The block contains a transaction that was signed by the harvester.
	 */
	TRANSACTION_SIGNED_BY_HARVESTER(16, R.string.server_result_code_transaction_signed_by_harvester),
	/**
	 * A previous importance transaction conflicts with a new transaction.
	 */
	PREVIOUS_IMPORTANCE_TRANSACTION_CONFLICT(17, R.string.server_result_code_previous_importance_transaction_conflict),
	/**
	 * An importance transfer activation was attempted while previous one is active.
	 */
	PREVIOUS_IMPORTANCE_TRANSFER_ACTIVE(18, R.string.server_result_code_previous_importance_transfer_active),
	/**
	 * An importance transfer deactivation was attempted but is not active.
	 */
	IMPORTANCE_TRANSFER_NOT_ACTIVE(19, R.string.server_result_code_importance_transfer_not_active),

	FAILURE_MULTISIG_NOT_A_COSIGNER(71, R.string.server_result_code_failure_multisig_not_a_cosigner),

	FAILURE_TRANSACTION_NOT_ALLOWED_FOR_MULTISIG(74, R.string.server_result_code_failure_transaction_not_allowed_for_multisig),

	FAILURE_MULTISIG_ALREADY_A_COSIGNER(75, R.string.server_result_code_multisig_already_a_cosigner),

	FAILURE_MULTISIG_MODIFICATION_MULTIPLE_DELETES(77, R.string.server_result_code_failure_multisig_modification_multiple_deletes),

	FAILURE_MULTISIG_MIN_COSIGNATORIES_OUT_OF_RANGE(82, R.string.server_result_code_failure_multisig_min_cosignatories_out_of_range);

	@JsonCreator
	public static ValidationResultCode fromRaw(final int rawCode) {
		for (ValidationResultCode value : values()) {
			if (value._rawCode == rawCode) { return value; }
		}
		Timber.e("Unknown result code: %d", rawCode);
		return UNKNOWN;
	}

	private int _rawCode;
	@StringRes
	private int _msgRes;

	ValidationResultCode(final int rawCode, final @StringRes Integer friendlyMsgRes) {
		_rawCode = rawCode;
		_msgRes = friendlyMsgRes;
	}

	@JsonValue
	@Override
	public int getCode() {
		return _rawCode;
	}

	@Override
	public boolean isSuccessful() {
		return _rawCode == NEUTRAL._rawCode || _rawCode == SUCCESS._rawCode;
	}

	@Override
	@StringRes
	public int getMessageRes() {
		return _msgRes;
	}
}
