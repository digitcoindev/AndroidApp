package org.nem.nac.ui.controls;

import android.animation.Animator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.common.enums.MessageType;
import org.nem.nac.common.models.Size;
import org.nem.nac.common.utils.DateUtils;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.api.MessageApiDto;
import org.nem.nac.models.api.transactions.MultisigTransactionApiDto;
import org.nem.nac.models.api.transactions.TransferTransactionApiDto;
import org.nem.nac.models.api.transactions.UnconfirmedTransactionMetaDataPairApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.tasks.DecryptMessageAsyncTask;
import org.nem.nac.ui.SwipeDetect;
import org.nem.nac.ui.utils.ViewUtils;

public final class UnconfirmedTransfersList extends LinearList<UnconfirmedTransactionMetaDataPairApiDto> {

	private EncryptedNacPrivateKey _owner;
	private AddressValue           _ownerAddr;
	private String                 _decryptionFailedError;
	private Integer _cosignatories;
	private Integer _minCosignatories;

	public UnconfirmedTransfersList(final Context context) {
		super(context);
		init();
	}

	public UnconfirmedTransfersList(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UnconfirmedTransfersList(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setOwner(final EncryptedNacPrivateKey privateKey, final AddressValue address, final Integer cosignatories, final Integer minCosignatories) {
		_owner = privateKey;
		_ownerAddr = address;
		_cosignatories = cosignatories;
		_minCosignatories = minCosignatories;
	}

	@NonNull
	@Override
	protected View getItemView(@NonNull final LayoutInflater inflater, @Nullable final UnconfirmedTransactionMetaDataPairApiDto item) {
		View itemView = inflater.inflate(R.layout.list_item_message_unconfirmed, this, false);
		final TransferTransactionApiDto transfer = (TransferTransactionApiDto)item.transaction.unwrapTransaction();
		final boolean isOutgoing = transfer.isSigner(_ownerAddr);
		final boolean isFromToMyself = transfer.signer.toAddress().equals(transfer.recipient);

		final Views views;
		views = new Views(itemView);
		//
		setMessageSwipeListener(itemView, views);
		//
		final String infoText = makeInfoText(item);
		views.infoLabel.setText(infoText);
		//
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(500, MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(500, MeasureSpec.AT_MOST);
		final Size infoViewSize = ViewUtils.measureView(views.infoLabel, widthMeasureSpec, heightMeasureSpec);
		views.messagePanel.setTranslationX(infoViewSize.width);
		//

		views.dateLabel.setText(getContext().getString(R.string.text_due_to, DateUtils.format(transfer.getDeadlineDate())));

		final boolean zeroAmount = transfer.amount.equals(Xems.ZERO);
		if (transfer.hasMessage()) {
			views.messageLabel.setText(MessageApiDto.toReadableString(transfer.message).get());
			if (transfer.message.type != MessageType.ENCRYPTED) {
				views.messageLabel.setText(MessageApiDto.toReadableString(transfer.message).get());
			}
			else {
				views.messageLabel.setText(R.string.message_decrypting_in_progress);
				final DecryptMessageAsyncTask decryptTask;
				if (isOutgoing) {
					final AddressValue otherAcc = transfer.recipient;
					decryptTask = new DecryptMessageAsyncTask(transfer.message.getData(), _owner, otherAcc);
				}
				else {
					final NacPublicKey otherAcc = transfer.signer;
					decryptTask = new DecryptMessageAsyncTask(transfer.message.getData(), _owner, otherAcc);
				}
				decryptTask.withCompleteCallback((task, result) -> {
					if (!result.getResult().isPresent()) {
						views.messageLabel.setText(_decryptionFailedError);
						return;
					}
					final Optional<String> text = MessageApiDto.toReadableString(result.getResult().get());
					views.messageLabel.setText(text.isPresent() ? text.get() : _decryptionFailedError);
				})
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
		else if (zeroAmount) {
			views.messageLabel.setText(R.string.placeholder_empty_transaction);
		}
		else {
			views.messageLabel.setVisibility(GONE);
		}

		views.amountLabel.setText(String
				.format("%s%s %s", zeroAmount ? "" : (isFromToMyself ? "±" : (isOutgoing ? "-" : "+")), transfer.amount.toFractionalString(), getContext()
						.getString(R.string.text_XEM)));
		views.amountLabel.setVisibility(zeroAmount ? View.GONE : View.VISIBLE);
		//
		final ViewGroup.LayoutParams layoutParams = views.messagePanel.getLayoutParams();
		if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
			final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)layoutParams;
			marginLayoutParams.rightMargin = 0;

			views.messagePanel.setLayoutParams(layoutParams);
		}
		//
		return itemView;
	}

	private void setMessageSwipeListener(final View itemView, final Views views) {
		itemView.setOnTouchListener(new SwipeDetect(true, false)
				.withOnSwipeDetectedListener((view, direction) -> {
					final TextView infoView = views.infoLabel;
					final boolean showInfo = direction == SwipeDetect.Direction.RL;
					final boolean currentlyShown = infoView.getVisibility() == VISIBLE;
					if (showInfo == currentlyShown) { return; }
					views.messagePanel.animate()
							.translationX(showInfo ? 0 : infoView.getWidth())
							.setDuration(200)
							.start();
					infoView.setAlpha(showInfo ? 0.0f : 1.0f);
					if (showInfo) {
						infoView.setVisibility(VISIBLE);
					}
					final ViewPropertyAnimator infoAnimator = infoView.animate()
							.alpha(showInfo ? 1.0f : 0.0f)
							.setInterpolator(showInfo ? new AccelerateInterpolator() : new DecelerateInterpolator());
					if (!showInfo) {
						if (Build.VERSION.SDK_INT < 16) {
							infoAnimator.setListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(final Animator animation) {}

								@Override
								public void onAnimationEnd(final Animator animation) {
									infoView.setVisibility(INVISIBLE);
								}

								@Override
								public void onAnimationCancel(final Animator animation) {}

								@Override
								public void onAnimationRepeat(final Animator animation) {}
							});
						}
						else {
							infoAnimator.withEndAction(() -> { infoView.setVisibility(INVISIBLE); });
						}
					}
					infoAnimator.start();
				}));
	}

	private String makeInfoText(final @Nullable UnconfirmedTransactionMetaDataPairApiDto item) {
		String info = String.format("%s: %s %s", getContext().getString(R.string.label_unconfirmed_info_fee_colon),
				item.transaction.fee.toFractionalString(), getContext().getString(R.string.text_XEM));

		if (item.isMultisig()) {
			final MultisigTransactionApiDto msigTransaction = (MultisigTransactionApiDto)item.transaction;
			if (msigTransaction.otherTrans.isSigner(_ownerAddr)) {
				if (msigTransaction.signatures != null) {
					final String htmlStr = getContext()
							.getString(R.string.tran_info_signers_count, msigTransaction.signatures.length + 1, (_cosignatories != null ? NumberUtils
									.toString(_cosignatories) : "-"));
					info += '\n';
					info += fromHtml(htmlStr);
				}

				info += ('\n' + getContext()
						.getString(R.string.tran_info_min_signers_count, (_minCosignatories != null ? NumberUtils.toString(_minCosignatories) : "-")));
			}
		}
		return info;
	}

	@SuppressWarnings("deprecation")
	public static Spanned fromHtml(String html){
		Spanned result;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
		} else {
			result = Html.fromHtml(html);
		}

		return result;
	}

	private void init() {
		_decryptionFailedError = getContext().getString(R.string.errormessage_decryption_failed);
	}

	private static class Views {

		public final TextView dateLabel;
		public final LinearLayout messagePanel;
		public final TextView messageLabel;
		public final TextView amountLabel;
		public final TextView infoLabel;

		public Views(final View convertView) {
			dateLabel = (TextView)convertView.findViewById(R.id.label_date);
			messageLabel = (TextView)convertView.findViewById(R.id.label_message);
			amountLabel = (TextView)convertView.findViewById(R.id.label_amount);
			infoLabel = (TextView)convertView.findViewById(R.id.label_info);
			messagePanel = (LinearLayout)convertView.findViewById(R.id.panel_message);
		}
	}
}
