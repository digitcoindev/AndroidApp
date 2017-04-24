package org.nem.nac.ui.controls;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.Xems;
import org.nem.nac.models.api.MessageApiDto;
import org.nem.nac.models.api.transactions.TransactionMetaDataPairApiDto;
import org.nem.nac.models.api.transactions.TransferTransactionApiDto;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.tasks.DecryptMessageAsyncTask;
import org.nem.nac.ui.SwipeDetect;
import org.nem.nac.ui.utils.Toaster;
import org.nem.nac.ui.utils.ViewUtils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class TransfersList extends LinearList<TransactionMetaDataPairApiDto> {

	private EncryptedNacPrivateKey _owner;
	private AddressValue           _ownerAddr;
	private String                 _decryptionFailedError;
	private final Map<Integer, String> _messagesByTranId = new HashMap<>();
	private final Handler              _handler          = new Handler();

	public TransfersList(final Context context) {
		super(context);
		init();
	}

	public TransfersList(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TransfersList(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setOwner(final EncryptedNacPrivateKey privateKey, final AddressValue address) {
		_owner = privateKey;
		_ownerAddr = address;
	}

	@NonNull
	@Override
	protected View getItemView(@NonNull final LayoutInflater inflater, @Nullable final TransactionMetaDataPairApiDto item) {
		View itemView = inflater.inflate(R.layout.list_item_message_confirmed, this, false);
		if (item == null) {
			return itemView;
		}

		final TransferTransactionApiDto transfer = ((TransferTransactionApiDto)item.transaction.unwrapTransaction());
		final boolean isOutgoing = transfer.isSigner(_ownerAddr);
		final boolean isFromToMyself = transfer.signer.toAddress().equals(transfer.recipient);
		final boolean isLeftAligned = !isOutgoing;

		final Views views;
		views = new Views(itemView);
		//
		setMessageSwipeListener(isLeftAligned, views);
		//
		final TextView infoView = isLeftAligned ? views.infoLabelLeft : views.infoLabelRight;
		final TextView hiddenView = !isLeftAligned ? views.infoLabelLeft : views.infoLabelRight;
		hiddenView.setVisibility(GONE);
		final String info = getContext().getString(R.string.tran_info_confirmed, item.meta.height, item.transaction.fee.toFractionalString());
		infoView.setText(info);
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(500, MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(500, MeasureSpec.AT_MOST);
		final Size infoViewSize = ViewUtils.measureView(infoView, widthMeasureSpec, heightMeasureSpec);
		views.msgPanel.setTranslationX(isLeftAligned ? -infoViewSize.width : infoViewSize.width);

		final Drawable bg = getContext().getResources()
				.getDrawable(isOutgoing
						? R.drawable.shape_msg_bubble_right
						: R.drawable.shape_msg_bubble_left);

		views.msgPanel.setBackgroundDrawable(bg);

		views.dateLabel.setText(DateUtils.format(transfer.getDate()));

		final boolean zeroAmount = transfer.amount.equals(Xems.ZERO);
		if (transfer.hasMessage()) {
			if (_messagesByTranId.containsKey(item.meta.id)) {
				views.messageLabel.setText(_messagesByTranId.get(item.meta.id));
			}
			else {
				views.messageLabel.setText(MessageApiDto.toReadableString(transfer.message).get());
				if (transfer.message.type != MessageType.ENCRYPTED) {
					final String message = MessageApiDto.toReadableString(transfer.message).get();
					views.messageLabel.setText(message);
					_messagesByTranId.put(item.meta.id, message);
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
						Timber.d("Examining decrypt task result");
						if (!result.getResult().isPresent()) {
							Timber.w("Decrypting returned failure");
							views.messageLabel.setText(_decryptionFailedError);
							return;
						}
						final Optional<String> text = MessageApiDto.toReadableString(result.getResult().get());
						Timber.d("Setting decrypted message text");
						if (text.isPresent()) {
							_messagesByTranId.put(item.meta.id, text.get());
						}
						views.messageLabel.setText(text.isPresent() ? text.get() : _decryptionFailedError);
					})
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
		}
		else if (zeroAmount) {
			views.messageLabel.setText(R.string.placeholder_empty_transaction);
		}
		else {
			views.messageLabel.setVisibility(GONE);
		}

		views.amountLabel.setText(
				String.format("%s%s %s", zeroAmount ? "" : (isFromToMyself ? "±" : (isOutgoing ? "-" : "+")), transfer.amount.toFractionalString(),
						getContext().getString(R.string.text_XEM)));
		views.amountLabel.setVisibility(zeroAmount ? View.GONE : View.VISIBLE);

		views.messageLabel.setGravity(isOutgoing ? Gravity.RIGHT : Gravity.LEFT);
		views.mainPanel.setGravity(isOutgoing ? Gravity.RIGHT : Gravity.LEFT);
		views.msgPanel.setGravity(isOutgoing ? Gravity.RIGHT : Gravity.LEFT);
		//
		final ViewGroup.LayoutParams layoutParams = views.msgPanel.getLayoutParams();
		if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
			final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)layoutParams;
			if (isOutgoing) {
				marginLayoutParams.rightMargin = 0;
			}
			else {
				marginLayoutParams.leftMargin = 0;
			}
			views.msgPanel.setLayoutParams(layoutParams);
		}
		return itemView;
	}

	private void setMessageSwipeListener(final boolean isLeftAligned, final Views views) {
		final SwipeDetect swipeDetect = new SwipeDetect(true, false);
		swipeDetect.withOnSwipeDetectedListener((view, direction) -> {
			final TextView infoView = isLeftAligned ? views.infoLabelLeft : views.infoLabelRight;
			final SwipeDetect.Direction toOpen = isLeftAligned ? SwipeDetect.Direction.LR : SwipeDetect.Direction.RL;
			final boolean showInfo = direction == toOpen;
			final boolean currentlyShown = infoView.getVisibility() == VISIBLE;
			if (showInfo == currentlyShown) { return; }
			views.msgPanel.animate()
					.translationX(showInfo ? 0 : (isLeftAligned ? -infoView.getWidth() : infoView.getWidth()))
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
					infoAnimator.withEndAction(() -> infoView.setVisibility(INVISIBLE));
				}
			}
			infoAnimator.start();
		});
		//
		final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public void onLongPress(final MotionEvent e) {
				int x = Math.round(e.getX());
				int y = Math.round(e.getY());
				if (x > views.messageLabel.getLeft() && x < views.messageLabel.getRight() && y > views.messageLabel.getTop() && y < views.messageLabel.getBottom()) {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData.newPlainText("Nem Message", views.messageLabel.getText().toString());
					clipboard.setPrimaryClip(clip);
					Toaster.instance().show(R.string.message_copy_complete);
				}
			}
		});
		//
		views.msgPanel.setOnTouchListener((v, event) -> {
			swipeDetect.onTouch(v, event);
			gestureDetector.onTouchEvent(event);
			return true;
		});
	}

	@Override
	protected View onItemViewInvalidate(final View view, final TransactionMetaDataPairApiDto item) {
		if (view == null) { return null; }
		return view;
	}

	private void init() {
		_decryptionFailedError = getContext().getString(R.string.errormessage_decryption_failed);
	}

	private static class Views {

		public View         itemView;
		public LinearLayout mainPanel;
		public TextView     dateLabel;
		public LinearLayout msgPanel;
		public TextView     messageLabel;
		public TextView     amountLabel;
		public TextView     infoLabelLeft;
		public TextView     infoLabelRight;

		public Views(final View convert) {
			itemView = convert;
			mainPanel = (LinearLayout)convert.findViewById(R.id.item_main);
			dateLabel = (TextView)convert.findViewById(R.id.label_date);
			msgPanel = (LinearLayout)convert.findViewById(R.id.panel_message);
			messageLabel = (TextView)convert.findViewById(R.id.label_message);
			amountLabel = (TextView)convert.findViewById(R.id.label_amount);
			infoLabelLeft = (TextView)convert.findViewById(R.id.label_info_left);
			infoLabelRight = (TextView)convert.findViewById(R.id.label_info_right);
		}
	}
}
