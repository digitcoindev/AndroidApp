package org.nem.nac.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;

/**
 * Base class for all popups
 */
public abstract class NacBaseDialogFragment extends DialogFragment {

	private static final String ARG_BOOL_CANCELABLE                  = "arg-cancelable";
	private static final String ARG_INT_STRRES_TITLE                 = "arg-title-res";
	private static final String ARG_BOOL_HAS_CONFIRM_BUTTON          = "arg-has-confirm-btn";
	private static final String ARG_INT_STRRES_CONFIRM_TEXT_OVERRIDE = "arg-confirm-text-res";

	/**
	 * Sets dialog arguments
	 *
	 * @param titleRes               Title text
	 * @param confirmBtn             True if dialog should have confirm button
	 * @param confirmTextOverrideRes If not null, overrides confirm button text
	 * @return Returns arguments bundle
	 */
	protected static Bundle setArgs(final boolean cancelable, final @StringRes Integer titleRes, final boolean confirmBtn,
			final @StringRes Integer confirmTextOverrideRes) {
		final Bundle args = new Bundle();
		if (titleRes != null) { args.putInt(ARG_INT_STRRES_TITLE, titleRes); }
		args.putBoolean(ARG_BOOL_HAS_CONFIRM_BUTTON, confirmBtn);
		if (confirmBtn && confirmTextOverrideRes != null) {
			args.putInt(ARG_INT_STRRES_CONFIRM_TEXT_OVERRIDE, confirmTextOverrideRes);
		}
		args.putBoolean(ARG_BOOL_CANCELABLE, cancelable);
		return args;
	}

	private   View      _closeBtn;
	private   ViewGroup _titleBar;
	private   TextView  _titleField;
	protected Button    confirmBtn;
	private   boolean   _hasConfirmBtn;
	protected boolean callOnCreateView = true;
	protected String                    title;
	private   String                    _confirmTextOverride;
	protected ConfirmListener           confirmClickListener;
	protected Consumer<DialogInterface> cancelListener;
	protected Consumer<DialogInterface> dismissListener;

	public NacBaseDialogFragment setOnConfirmListener(final ConfirmListener listener) {
		this.confirmClickListener = listener;
		return this;
	}

	public NacBaseDialogFragment setOnCancelListener(final Consumer<DialogInterface> listener) {
		this.cancelListener = listener;
		return this;
	}

	public NacBaseDialogFragment setOnDismissListener(final Consumer<DialogInterface> listener) {
		this.dismissListener = listener;
		return this;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		if (args != null) {
			// super method - because we don't need side effects here, such as access to non-initialized views
			super.setCancelable(args.getBoolean(ARG_BOOL_CANCELABLE, true));

			if (args.containsKey(ARG_INT_STRRES_TITLE)) {
				final int resId = args.getInt(ARG_INT_STRRES_TITLE);
				title = getString(resId);
			}

			_hasConfirmBtn = args.getBoolean(ARG_BOOL_HAS_CONFIRM_BUTTON, true);
			if (args.containsKey(ARG_INT_STRRES_CONFIRM_TEXT_OVERRIDE)) {
				_confirmTextOverride = getString(args.getInt(ARG_INT_STRRES_CONFIRM_TEXT_OVERRIDE));
			}
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		return new Dialog(getActivity(), R.style.AppDialogTheme);
	}

	@Nullable
	@CallSuper
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (callOnCreateView) {
			return inflateMasterLayout(inflater, container, getContentLayout());
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void setTitle(final String title) {
		this.title = title;
		enableTitleBarItems(isCancelable(), this.title);
	}

	@Override
	public void setCancelable(final boolean cancelable) {
		if (cancelable == isCancelable()) { return; }
		super.setCancelable(cancelable);
		enableTitleBarItems(cancelable, title);
	}

	@LayoutRes
	protected abstract int getContentLayout();

	protected void onCloseClick(final View clicked) {
		getDialog().cancel();
	}

	/**
	 * Override to implement custom click handling.
	 * Call super, otherwise external click handler won't work.
	 */
	@CallSuper
	protected void onConfirmClick(final View clicked) {
		if (confirmClickListener != null) {
			if (confirmClickListener.onConfirmClick(getDialog())) {
				dismiss();
			}
		}
		else {
			dismiss();
		}
	}

	@Override
	public void onCancel(final DialogInterface dialog) {
		if (cancelListener != null) {
			cancelListener.accept(dialog);
		}
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
		if (dismissListener != null) {
			dismissListener.accept(dialog);
		}
	}

	protected void enableTitleBarItems(final boolean closeBtn, @Nullable final String title) {
		final boolean hasTitle = title != null;
		if (_closeBtn != null) {
			_closeBtn.setOnClickListener(closeBtn ? this::onCloseClick : null);
			_closeBtn.setVisibility(closeBtn ? View.VISIBLE : View.INVISIBLE); // invisible to center title if dialog not closeable
		}
		if (_titleField != null) {
			_titleField.setText(title);
			_titleField.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
		}
		if (_titleBar != null) {
			_titleBar.setVisibility(closeBtn || hasTitle ? View.VISIBLE : View.GONE);
		}
	}

	protected void enableConfirmButton(final boolean enable) {
		confirmBtn.setOnClickListener(_hasConfirmBtn && enable ? this::onConfirmClick : null);
	}

	@NonNull
	private View inflateMasterLayout(final LayoutInflater inflater, final ViewGroup container, @LayoutRes final int contentRes) {
		final ViewGroup layout = ((ViewGroup)inflater.inflate(R.layout.master_dialog, container, false));
		_titleBar = (ViewGroup)layout.findViewById(R.id.dialog_title_bar);
		_closeBtn = layout.findViewById(R.id.btn_popup_close);
		_titleField = (TextView)layout.findViewById(R.id.label_dialog_title);
		enableTitleBarItems(isCancelable(), title);
		//
		final ViewGroup contentPanel = (ViewGroup)layout.findViewById(R.id.panel_content);
		final View content = inflater.inflate(contentRes, contentPanel, false);
		contentPanel.addView(content);
		//
		confirmBtn = (Button)layout.findViewById(R.id.btn_dialog_confirm);
		enableConfirmButton(_hasConfirmBtn);
		confirmBtn.setVisibility(_hasConfirmBtn ? View.VISIBLE : View.GONE);
		if (_hasConfirmBtn && _confirmTextOverride != null) {
			confirmBtn.setText(_confirmTextOverride);
		}
		//
		return layout;
	}

	public interface ConfirmListener {

		/**
		 * Should return true to allow dialog to be dismissed.
		 */
		boolean onConfirmClick(final DialogInterface dialog);
	}
}
