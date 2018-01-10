package org.nem.nac.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.NumberUtils;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.repositories.InvoiceMessageRepository;
import org.nem.nac.datamodel.repositories.InvoiceNumberRepository;
import org.nem.nac.datamodel.repositories.InvoiceRepository;
import org.nem.nac.models.Invoice;
import org.nem.nac.models.InvoiceMessage;
import org.nem.nac.models.Xems;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.invoice.InvoiceNumber;
import org.nem.nac.ui.activities.InvoiceActivity;
import org.nem.nac.ui.controls.AmountInput;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class CreateInvoiceFragment extends BaseTabFragment {

	private static final String ARG_PARC_ACCOUNT = "arg-account";

	public static CreateInvoiceFragment create(final Account account) {
		CreateInvoiceFragment fragment = new CreateInvoiceFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARC_ACCOUNT, account);
		fragment.setArguments(args);
		return fragment;
	}

	private TextView                _nameField;
	private AmountInput             _amountInput;
	private EditText                _messageInput;
	private Button                  _createBtn;
	private Account                 _account;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View layout = inflater.inflate(R.layout.fragment_create_invoice, container, false);
		_nameField = (TextView)layout.findViewById(R.id.field_name);
		_amountInput = (AmountInput)layout.findViewById(R.id.input_amount);
		_messageInput = (EditText)layout.findViewById(R.id.input_message);
		_createBtn = (Button)layout.findViewById(R.id.btn_create);
		_createBtn.setOnClickListener(this::onCreateClick);
		return layout;
	}

	@Override
	public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//
		if (getArguments() != null) {
			_account = getArguments().<Account>getParcelable(ARG_PARC_ACCOUNT);
		}
		else { _account = null; }
		//
		if (_account == null) {
			Timber.w("Account not present.");
			Toaster.instance().showGeneralError();
			return;
		}
		//
		_nameField.setText(_account.name);
	}

	private boolean _messageInitialized = false;

	@Override
	protected void onFullyVisible() {
		super.onFullyVisible();
		//
		if (!_messageInitialized) {
			final InvoiceNumber number = new InvoiceNumberRepository().get().orElse(new InvoiceNumber(0));
			final Optional<InvoiceMessage> message = new InvoiceMessageRepository().get();
			if (message.isPresent()) {
				final String messageStr = message.get().getReadable(number.lastStored + 1);
				_messageInput.setText(messageStr);
			}
			_messageInitialized = true;
		}
	}

	private void onCreateClick(final View clicked) {
		_createBtn.setEnabled(false);
		try {
			if (!_amountInput.validate()) {
				_amountInput.requestFocus();
				return;
			}

			final Xems amount;
			try {
				amount = Xems.fromXems(NumberUtils.parseDouble(_amountInput.getText().toString()));
			} catch (NumberFormatException e) {
				Timber.e(e, "Failed to read amount!");
				Toast.makeText(getActivity(), R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
				return;
			}
			final String message = _messageInput.getText().toString();
			final Invoice invoice = new Invoice(_account.name, lastUsedAddress, amount, message);

			try {
				saveInvoice(invoice);
			} catch (NacPersistenceRuntimeException e) {
				Timber.e(e, "Failed to save invoice");
				Toast.makeText(getActivity(), R.string.errormessage_failed_to_save_invoice, Toast.LENGTH_SHORT).show();
			}
			final Intent intent = new Intent(getActivity(), InvoiceActivity.class)
					.putExtra(InvoiceActivity.EXTRA_INVOICE_ID, invoice.id);
			startActivity(intent);
			getActivity().finish();
		} finally {
			_createBtn.setEnabled(true);
		}
	}

	private void saveInvoice(Invoice invoice)
			throws NacPersistenceRuntimeException {
		final NemSQLiteHelper sqLiteHelper;
		try {
			sqLiteHelper = NemSQLiteHelper.getInstance();
		} catch (NacException e) {
			throw new NacPersistenceRuntimeException("Failed to get sqlite helper!", e);
		}

		try {
			sqLiteHelper.beginTransaction();
			new InvoiceRepository().save(invoice);
			final InvoiceNumber number = new InvoiceNumberRepository().get().orElse(new InvoiceNumber(0));
			number.incrementByOne();
			new InvoiceNumberRepository().save(number);
			sqLiteHelper.commitTransaction();
		} finally {
			sqLiteHelper.endTransaction();
		}
	}
}
