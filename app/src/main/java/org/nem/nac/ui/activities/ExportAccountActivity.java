package org.nem.nac.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.exceptions.NacRuntimeException;
import org.nem.nac.common.exceptions.NacUserVisibleException;
import org.nem.nac.common.utils.IOUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.datamodel.repositories.AppPasswordRepository;
import org.nem.nac.models.AppPassword;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.models.qr.QrAccount;
import org.nem.nac.models.qr.QrDto;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.share.ShareHelper;
import org.nem.nac.ui.controls.QrImageView;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.dialogs.ExportAccountPasswordDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class ExportAccountActivity extends NacBaseActivity {

	private QrImageView _qrImageView;
	private TextView               _btnShareQr;
	private TextView    _btnShowPrivateKey;
	private TextView    _labelShowPrivKey;
	private TextView    _privateKeyField;
	private TextView _publicKeyField;
	private ScrollView  _scrollView;
	private ViewGroup              _mainLayout;
	private Account                _account;
	private EncryptedNacPrivateKey _exportedKey;
	private AppPassword            _appPwd;

	private Bitmap _qrBitmap;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_export_account;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_export_account;
	}

	@Override
	public void onBackPressed() {
		finish();
		startActivity(new Intent(this, MoreActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_scrollView = (ScrollView)findViewById(R.id.scroll_view);
		_qrImageView = (QrImageView)findViewById(R.id.imageview_qr);
		_qrImageView.setOnQrRenderedListener(this::onQrRendered);
		_mainLayout = (ViewGroup)findViewById(R.id.layout_main);
		_mainLayout.setVisibility(View.GONE);
		_btnShareQr = (TextView)findViewById(R.id.btn_share_qr);
		_btnShareQr.setOnClickListener(this::onShareClick);
		_btnShowPrivateKey = (TextView)findViewById(R.id.btn_show_private_key);
		_btnShowPrivateKey.setOnClickListener(this::onShowPrivateKey);
		_labelShowPrivKey = (TextView)findViewById(R.id.label_private_key);
		_privateKeyField = (TextView)findViewById(R.id.textview_private_key);
		_publicKeyField = (TextView)findViewById(R.id.textview_public_key);

		// Me
		final Optional<AddressValue> me = AppSettings.instance().readLastUsedAccAddress();
		if (!me.isPresent()) {
			Timber.e("Local account was not selected");
			Toast.makeText(this, R.string.errormessage_account_not_selected, Toast.LENGTH_SHORT).show();
			finish();
			AccountListActivity.start(this);
			return;
		}
		// Check if existing account
		final Optional<Account> account = new AccountRepository().find(me.get());
		if (!account.isPresent()) {
			Timber.w("Dashboard opened with bad address: %s", me.get());
			return;
		}
		_account = account.get();
		if (_account.publicData.publicKey != null) {
			_publicKeyField.setText(_account.publicData.publicKey.toHexStr());
		}
		else {
			_publicKeyField.setText(R.string.label_public_key_no_public_key);
		}
		final Optional<AppPassword> appPwd = new AppPasswordRepository().get();
		if (!appPwd.isPresent()) {
			Timber.e("App password not present!");
			throw new NacRuntimeException("App password not present!");
		}
		_appPwd = appPwd.get();
	}

	@Override
	protected void onStart() {
		super.onStart();
		_qrBitmap = null;
		//_qrImageView.setImageBitmap(null);
		//
		ExportAccountPasswordDialogFragment.create()
				.setOnPasswordEnteredListener(this::onPasswordEntered)
				.setOnCancelListener(dialog -> {
					_exportedKey = null;
					finish();
				})
				.show(getFragmentManager(), null);
	}

	private void onPasswordEntered(final ExportAccountPasswordDialogFragment dialog) {
		Timber.d("Export account confirmed");
		if (dialog.useAppPassword()) {
			_exportedKey = _account.privateKey;
			_mainLayout.setVisibility(View.VISIBLE);
		}
		else {
			if (!dialog.isValidPassword()) {
				return;
			}
			try {
				final BinaryData pwdKey = dialog.deriveKey(_appPwd.salt);
				final NacPrivateKey key = _account.privateKey.decryptKey(EKeyProvider.instance().getKey().get());
				_exportedKey = key.encryptKey(pwdKey);
				_mainLayout.setVisibility(View.VISIBLE);
			} catch (NacCryptoException e) {
				_exportedKey = null;
				Timber.e("Bad password");
				Toaster.instance().show(R.string.errormessage_failed_to_export_account);
				finish();
				return;
			}
		}
		//
		_qrImageView.setQrDto(new QrDto(QrDto.Type.ACCOUNT, new QrAccount(_account.name, _exportedKey, _appPwd.salt)));
	}

	private void onQrRendered(final Bitmap bitmap) {
		if (isNotDestroyed()) {
			if (bitmap == null) {
				_btnShareQr.setClickable(false);
				_btnShareQr.setEnabled(false);
				Toaster.instance().showGeneralError();
				return;
			}
			_qrBitmap = bitmap;
			_btnShareQr.setClickable(true);
			_btnShareQr.setEnabled(true);
			_mainLayout.setVisibility(View.VISIBLE);
		}
	}

	private void onShareClick(final View clicked) {
		_btnShareQr.setClickable(false);
		//
		try {
			if (_qrBitmap != null) {
				final String filePath = IOUtils.saveBitmapToFile(_qrBitmap, AppConstants.QR_IMAGE_STORE_FILE_NAME);
				final String body =
						StringUtils.format(R.string.email_body_share_account, _account.name);
				final String subject = StringUtils.format(R.string.email_subject_share_account, _account.name);
				final String chooserName = getString(R.string.action_chooser_share_account);
				ShareHelper.shareQr(this, subject, body, filePath, chooserName);
			}
		} catch (NacUserVisibleException e) {
			Toaster.instance().show(getString(R.string.text_error) + "\n" + e.getMessage());
		} finally {
			_btnShareQr.setClickable(true);
		}
	}

	private void onShowPrivateKey(final View clicked) {
		ConfirmDialogFragment.create(true, R.string.dialog_title_warning, R.string.message_private_key_exposure_warning_html, R.string.btn_i_understand)
				.setOnConfirmListener(dialog -> {
					if (_account != null) {
						Timber.d("Show private key warning accepted");
						_btnShowPrivateKey.setVisibility(View.GONE);
						final Optional<BinaryData> eKey = EKeyProvider.instance().getKey();
						if (!eKey.isPresent()) {
							Timber.wtf("Ekey not present!");
							Toaster.instance().showGeneralError();
							return true;
						}
						try {
							final String key = _account.privateKey.decryptKey(eKey.get()).toHexStr();
							_privateKeyField.setText(key);
							_labelShowPrivKey.setVisibility(View.VISIBLE);
							_privateKeyField.setVisibility(View.VISIBLE);
							_publicKeyField.setVisibility(View.VISIBLE);
							clicked.post(() -> _scrollView.fullScroll(View.FOCUS_DOWN));
						} catch (NacCryptoException e) {
							Timber.wtf("Error getting private key!");
							Toaster.instance().showGeneralError();
						}
					}
					return true;
				})
				.show(getFragmentManager(), null);
	}
}
