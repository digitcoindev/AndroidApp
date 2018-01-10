package org.nem.nac.models.transactions.drafts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nem.nac.application.AppConstants;
import org.nem.nac.common.SizeOf;
import org.nem.nac.common.enums.MessageType;
import org.nem.nac.common.enums.TransactionType;
import org.nem.nac.common.models.MessageDraft;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.primitives.AddressValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class TransferTransactionDraft extends AbstractTransactionDraft {

	private static final int VERSION = 1;

	/**
	 * @param payloadLength message payload length (byte count)
	 */
	public static Xems calculateMinimumFee(final Xems amount, final int payloadLength) {
		final Xems fee = new Xems();
		final long amountXems = amount.getIntegerPart();

		// July 11
		if (amountXems < AppConstants.MinimumFee_cap_amount) {
			fee.addXems(AppConstants.MinimumFee_factor);
		} else {
			double feecalc = AppConstants.MinimumFee_factor * ((long)(amountXems / AppConstants.MinimumFee_cap_amount));
			if (feecalc>AppConstants.MinimumFee_caped)
				fee.addXems(AppConstants.MinimumFee_caped);
			else
				fee.addXems(feecalc);
		}

		if (payloadLength > 0 && payloadLength <= AppConstants.MinimumFee_msg_char) {
			fee.addXems(AppConstants.MinimumFee_factor);
		} else if (payloadLength > AppConstants.MinimumFee_msg_char){
			fee.addXems(AppConstants.MinimumFee_factor*((int)(payloadLength/AppConstants.MinimumFee_msg_char+1)));
		}

	  // June 13, revise fee
	/*	if (amountXems < 10000L) {
			fee.addXems(AppConstants.MinimumFee_factor);
		} else {
			double feecalc = AppConstants.MinimumFee_factor * ((long)(amountXems / 10000L));
			if (feecalc>1.25)
				fee.addXems(1.25);
			else
				fee.addXems(feecalc);
		}

		if (payloadLength > 0 && payloadLength <= 32) {
			fee.addXems(AppConstants.MinimumFee_factor);
		} else if (payloadLength > 32){
			fee.addXems(AppConstants.MinimumFee_factor*((int)(payloadLength/32+1)));
		} */

	/*	if (amountXems < 10000L) {
			fee.addXems(1);
		} else {
			int feecalc = (int) (amountXems / 10000L);
			if (feecalc>25)
				fee.addXems(25);
			else
				fee.addXems(feecalc);
		}

		if (payloadLength > 0 && payloadLength <= 32) {
			fee.addXems(1);
		} else if (payloadLength > 32){
			fee.addXems(1+ payloadLength/32);
		} */

	/*	// fee for transfer
		if (amountXems < 8) {
			fee.addXems(10 - amountXems);
		}
		else {
			fee.addXems((long)Math.max(2, 99 * Math.atan(amountXems / 150000.0)));
		}
		// fee for message
		if (payloadLength > 0) {
			fee.addXems(2 * Math.max(1, payloadLength / 16));
		} */
		return fee;
	}

	public final AddressValue recipientAddress;
	@NonNull
	public final Xems         amount;
	@Nullable
	public final MessageDraft message;

	public TransferTransactionDraft(
			@NonNull final NacPublicKey signer,
			@NonNull final AddressValue recipient, @Nullable final Xems amount, @Nullable final MessageDraft message) {
		super(VERSION, signer);
		this.recipientAddress = recipient;
		this.amount = amount != null ? amount : Xems.ZERO;
		this.message = message;
	}

	@NonNull
	@Override
	public TransactionType getType() {
		return TransactionType.TRANSFER_TRANSACTION;
	}

	@NonNull
	@Override
	public Xems calculateMinimumFee() {
		final int length = message != null && message.hasPayload() ? message.getPayload().length() : 0;
		return calculateMinimumFee(amount, length);
	}

	@Override
	protected void serializeAdditional(@NonNull final ByteArrayOutputStream os)
			throws IOException {
		final byte[] recipientBytes = recipientAddress.getRaw().getBytes(AppConstants.ENCODING_UTF8);
		writeAsLeBytes(os, recipientBytes.length);
		os.write(recipientBytes);
		writeAsLeBytes(os, amount.getAsMicro());
		if (message != null && message.hasPayload()) {
			//noinspection ConstantConditions // already checked if it has payload.
			final int messageFieldLength = // 4 bytes message type + 4 byte length of payload + payload bytes
					SizeOf.INT + SizeOf.INT + message.getPayload().length();
			writeAsLeBytes(os, messageFieldLength);
			writeAsLeBytes(os, (message.isEncrypted() ? MessageType.ENCRYPTED : MessageType.NOT_ENCRYPTED).getValue());
			writeAsLeBytes(os, message.getPayload().length());
			os.write(message.getPayload().getRaw());
		}
		else { writeAsLeBytes(os, 0); }
	}
}
