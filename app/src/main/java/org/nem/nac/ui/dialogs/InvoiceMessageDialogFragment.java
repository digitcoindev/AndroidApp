package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.datamodel.repositories.InvoiceMessageRepository;
import org.nem.nac.models.InvoiceMessage;
import org.nem.nac.ui.utils.Toaster;

public class InvoiceMessageDialogFragment extends NacBaseDialogFragment {

	private static final int INVOICE_MAX_NUMBER = 9999;

	public static InvoiceMessageDialogFragment create() {
		InvoiceMessageDialogFragment fragment = new InvoiceMessageDialogFragment();
		Bundle args = setArgs(true, R.string.dialog_title_invoice_message, true, null);
		fragment.setArguments(args);
		return fragment;
	}

	private EditText _prefixInput;
	private EditText _postfixInput;
	private EditText _messageInput;
	private TextView _invoiceNumberLabel;
	private TextView _overallLengthLabel;
	private boolean _validMessage = false;
	@ColorInt
	private int _colorText, _colorError;
	private InvoiceMessage           _initialMessage;
	private Consumer<InvoiceMessage> _messageUpdatedListener;

	public InvoiceMessageDialogFragment setMessageUpdatedListener(final Consumer<InvoiceMessage> listener) {
		_messageUpdatedListener = listener;
		return this;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_invoice_message_dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = super.onCreateView(inflater, container, savedInstanceState);
		_prefixInput = (EditText)layout.findViewById(R.id.input_prefix);
		_postfixInput = (EditText)layout.findViewById(R.id.input_postfix);
		_messageInput = (EditText)layout.findViewById(R.id.input_message);
		_invoiceNumberLabel = (TextView)layout.findViewById(R.id.label_invoice_number_label);
		final String separator = getString(R.string.invoice_number_separator);
		_invoiceNumberLabel.setText(String.format("%s%d%s", separator, 0, separator));
		_overallLengthLabel = (TextView)layout.findViewById(R.id.label_invoice_message_overall_length);
		_colorText = _overallLengthLabel.getCurrentTextColor();
		_colorError = getResources().getColor(R.color.default_red);
		return layout;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Optional<InvoiceMessage> messageOptional = new InvoiceMessageRepository().get();
		if (messageOptional.isPresent()) {
			_initialMessage = messageOptional.get();
			_prefixInput.setText(_initialMessage.prefix);
			_postfixInput.setText(_initialMessage.postfix);
			_messageInput.setText(_initialMessage.message);
			validateMessage(_initialMessage);
		}
		else {
			_initialMessage = new InvoiceMessage("", "", "");
			validateMessage(_initialMessage);
		}
		_prefixInput.addTextChangedListener(_lengthWatcher);
		_postfixInput.addTextChangedListener(_lengthWatcher);
		_messageInput.addTextChangedListener(_lengthWatcher);
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		clicked.setClickable(false);
		try {
			if (_validMessage) {
				final InvoiceMessage message =
						new InvoiceMessage(_prefixInput.getText().toString(), _postfixInput.getText().toString(), _messageInput.getText().toString());
				new InvoiceMessageRepository().save(message);
				if (_messageUpdatedListener != null && !_initialMessage.equals(message)) {
					_messageUpdatedListener.accept(message);
				}
				super.onConfirmClick(clicked);
			}
			else {
				Toaster.instance().show(R.string.errormessage_message_too_long);
			}
		} finally {
			clicked.setClickable(true);
		}
	}

	private void setErrorState(boolean isError) {
		_overallLengthLabel.setTextColor(isError ? _colorError : _colorText);
	}

	private void validateMessage(final InvoiceMessage message) {
		final int cipherLength = message.getEncryptedBytesLength(INVOICE_MAX_NUMBER);
		final boolean isError = cipherLength > AppConstants.MAX_MESSAGE_LENGTH_BYTES;
		_validMessage = !isError;
		_overallLengthLabel.setText(getString(R.string.label_invoice_message_overall_length, cipherLength, AppConstants.MAX_MESSAGE_LENGTH_BYTES));
		setErrorState(isError);
	}

	private final TextWatcher _lengthWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			final InvoiceMessage message =
					new InvoiceMessage(_prefixInput.getText().toString(), _postfixInput.getText().toString(), _messageInput.getText().toString());
			validateMessage(message);
		}
	};
}
