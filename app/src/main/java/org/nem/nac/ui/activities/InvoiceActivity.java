package org.nem.nac.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.NacUserVisibleException;
import org.nem.nac.common.utils.IOUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.repositories.InvoiceRepository;
import org.nem.nac.models.Invoice;
import org.nem.nac.share.ShareHelper;
import org.nem.nac.ui.controls.QrImageView;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class InvoiceActivity extends NacBaseActivity {

	public static final String EXTRA_INVOICE_ID = InvoiceActivity.class.getCanonicalName() + "invoice-id";

	private QrImageView _qrImageView;
	private TextView _nameField, _amountField, _messageField;
	private Invoice  _invoice;
	private TextView _shareBtn;
	private TextView _numberField;
	private Bitmap _qrBitmap;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_invoice;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_invoice;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_nameField = (TextView)findViewById(R.id.field_name);
		_amountField = (TextView)findViewById(R.id.field_amount);
		_messageField = (TextView)findViewById(R.id.field_message);
		_qrImageView = (QrImageView)findViewById(R.id.imageview_qr);
		_qrImageView.setOnQrRenderedListener(this::onQrRendered);
		_shareBtn = (TextView)findViewById(R.id.btn_share_qr);
		_shareBtn.setOnClickListener(this::onShareQrClick);
		_numberField = (TextView)findViewById(R.id.field_number);

		final long invoiceId = getIntent().getLongExtra(EXTRA_INVOICE_ID, 0);
		try {
			_invoice = new InvoiceRepository().get(invoiceId);
			if (_invoice == null) {
				Timber.e("Invoice %d not found!", invoiceId);
				final String error = getString(R.string.errormessage_invoice_not_found, invoiceId);
				Toaster.instance().show(error);
				return;
			}
		} catch (NacPersistenceRuntimeException e) {
			Timber.e(e, "Failed to get invoice %d", invoiceId);
			Toaster.instance().showGeneralError();
			return;
		}
		_numberField.setText("#" + _invoice.id);
		_nameField.setText(_invoice.name);
		_amountField.setText(_invoice.amount.toFractionalString());
		_messageField.setText(_invoice.message);
		_qrImageView.setQrDto(_invoice.toQrDto());
	}

	private void onShareQrClick(final View clicked) {
		_shareBtn.setEnabled(false);
		try {
			if (_qrBitmap != null) {
				final String filePath = IOUtils.saveBitmapToFile(_qrBitmap, AppConstants.QR_IMAGE_STORE_FILE_NAME);
				final String body = StringUtils.format(R.string.email_body_share_invoice, _invoice.name,
						_invoice.message);
				final String subject = StringUtils.format(R.string.email_subject_share_invoice, _invoice.name);

				ShareHelper.shareQr(this, subject, body, filePath, getString(R.string.action_chooser_share_invoice));
			}
		} catch (NacUserVisibleException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} finally {
			_shareBtn.setEnabled(true);
		}
	}

	private void onQrRendered(final Bitmap bitmap) {
		if (isNotDestroyed()) {
			if (bitmap == null) {
				_shareBtn.setClickable(false);
				_shareBtn.setEnabled(false);
				Toaster.instance().showGeneralError();
			}
			_qrBitmap = bitmap;
			_shareBtn.setClickable(true);
			_shareBtn.setEnabled(true);
		}
	}
}
