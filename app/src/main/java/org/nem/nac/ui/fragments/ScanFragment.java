package org.nem.nac.ui.fragments;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.crypto.NacCryptoException;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.helpers.ContactsHelper;
import org.nem.nac.models.BinaryData;
import org.nem.nac.models.EncryptedNacPrivateKey;
import org.nem.nac.models.NacPrivateKey;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.account.PublicAccountData;
import org.nem.nac.models.qr.BaseQrData;
import org.nem.nac.models.qr.QrAccount;
import org.nem.nac.models.qr.QrDto;
import org.nem.nac.models.qr.QrInvoice;
import org.nem.nac.models.qr.QrUserInfo;
import org.nem.nac.providers.AddressInfoProvider;
import org.nem.nac.providers.EKeyProvider;
import org.nem.nac.qr.QrResult;
import org.nem.nac.qr.QrResultDecoder;
import org.nem.nac.qr.ScanResultStatus;
import org.nem.nac.ui.activities.AccountListActivity;
import org.nem.nac.ui.activities.NacBaseActivity;
import org.nem.nac.ui.activities.NewTransactionActivity;
import org.nem.nac.ui.dialogs.CheckPasswordDialogFragment;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.dialogs.UserInfoImportDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class ScanFragment extends BaseTabFragment implements QRCodeReaderView.OnQRCodeReadListener {

	private static final String ARG_BOOL_ACCOUNTS_ONLY = "accounts-only";

	private static final int RESTART_DELAY_SMALL = 300;
	private static final int RESTART_DELAY_BIG   = 1500;

	public static ScanFragment create(final boolean accountsOnly) {
		ScanFragment fragment = new ScanFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_BOOL_ACCOUNTS_ONLY, accountsOnly);
		fragment.setArguments(args);
		return fragment;
	}

	private QRCodeReaderView _qrView;

	private QrResultDecoder _qrResultDecoder;
	private boolean _accountsOnly = false;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		final Bundle args = getArguments();
		if (args != null) {
			_accountsOnly = args.getBoolean(ARG_BOOL_ACCOUNTS_ONLY, false);
		}
		//
		_qrResultDecoder = new QrResultDecoder(_accountsOnly);
		_qrResultDecoder.setCallbacks(this::onQrNotFound, this::onQrError, this::onQrScanSuccess);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_scan, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_qrView = (QRCodeReaderView)view.findViewById(R.id.qr_code_reader_view);
		_qrView.setOnQRCodeReadListener(this);
	}

	//endregion

	@Override
	protected void onFullyVisible() {
		super.onFullyVisible();
		_qrView.startCamera();
		_qrView.setQRDecodingEnabled(true);
	}

	@Override
	protected void onHiding() {
		super.onHiding();
		_qrView.stopCamera();
	}

	@Override
	public void onQRCodeRead(final ScanResultStatus status, final String text, final PointF[] points) {
		if (!((NacBaseActivity)getActivity()).isNotDestroyed()) {
			return;
		}
		if (_qrView.getQRDecodingEnabled() && _qrResultDecoder != null) {
			_qrView.setQRDecodingEnabled(false);
			_qrResultDecoder.decodeResult(new QrResult(status, text));
		}
	}

	//region QR Decode

	private void onQrScanSuccess(final BaseQrData qrData, final QrDto.Type type) {
		switch (type) {
			case ACCOUNT: {
				final QrAccount qrAccount = (QrAccount)qrData;
				CheckPasswordDialogFragment.create()
						.setOnPasswordConfirmListener(dialog -> {
							// Decrypting private key using entered pwd and salt from QR
							final NacPrivateKey privKey;
							try {
								final BinaryData eKey = dialog.deriveKey(qrAccount.salt);
								privKey = qrAccount.privateKey.decryptKey(eKey);
							} catch (NacCryptoException e) {
								Timber.e(e, "Decrypt failed");
								Toaster.instance().show(R.string.errormessage_failed_to_import_account);
								postCanDecodeFrames(RESTART_DELAY_BIG);
								return;
							}
							// Check if account already exists
							final NacPublicKey publicKey = NacPublicKey.fromPrivateKey(privKey);
							if (new AccountRepository().find(publicKey).isPresent()) {
								ConfirmDialogFragment.create(true, null, R.string.errormessage_account_already_exists, null)
										.setOnDismissListener(d -> postCanDecodeFrames(RESTART_DELAY_BIG))
										.show(getActivity().getFragmentManager(), null);
								return;
							}
							try {
								importQrAccount(qrAccount.name, privKey, publicKey);
							} catch (NacCryptoException e) {
								Toaster.instance().show(R.string.errormessage_failed_to_import_account);
								postCanDecodeFrames(RESTART_DELAY_BIG);
							}
						})
						.setOnCancelListener(dialog -> postCanDecodeFrames(RESTART_DELAY_BIG))
						.show(getActivity().getFragmentManager(), null);
				break;
			}
			case INVOICE: {
				final QrInvoice qrInvoice = (QrInvoice)qrData;
				final Intent intent = new Intent(getActivity(), NewTransactionActivity.class)
						.putExtra(NewTransactionActivity.EXTRA_STR_NAME, qrInvoice.name)
						.putExtra(NewTransactionActivity.EXTRA_STR_ADDRESS, qrInvoice.address.getRaw())
						.putExtra(NewTransactionActivity.EXTRA_STR_MESSAGE, qrInvoice.message)
						.putExtra(NewTransactionActivity.EXTRA_DOUBLE_AMOUNT, qrInvoice.amount.getAsFractional())
						.putExtra(NewTransactionActivity.EXTRA_BOOL_ENCRYPTED, true);
				startActivity(intent);
				getActivity().finish();
				break;
			}
			case USER_INFO: {
				final QrUserInfo qrUserInfo = (QrUserInfo)qrData;
				UserInfoImportDialogFragment.create(qrUserInfo.name, qrUserInfo.address)
						.setOnConfirmListener((add, sendTransaction, name) -> {
							boolean scanFurther = false;
							if (add) {
								try {
									ContactsHelper.addContact(getActivity(), name, qrUserInfo.address);
									scanFurther = true;
								} catch (Exception e) {
									Timber.e(e, "Failed to save contact %s", qrUserInfo);
									Toaster.instance().show(R.string.errormessage_failed_to_save_contact);
									scanFurther = true;
								}
							}
							if (sendTransaction) {
								final Intent intent = new Intent(getActivity(), NewTransactionActivity.class)
										.putExtra(NewTransactionActivity.EXTRA_STR_NAME, qrUserInfo.name)
										.putExtra(NewTransactionActivity.EXTRA_STR_ADDRESS, qrUserInfo.address.getRaw());
								startActivity(intent);
								scanFurther = false;
							}
							if (scanFurther) {
								postCanDecodeFrames(RESTART_DELAY_BIG);
							}
						})
						.setOnCancelListener(dialog -> postCanDecodeFrames(RESTART_DELAY_BIG))
						.show(getActivity().getFragmentManager(), null);
				break;
			}
		}
	}

	private void onQrError(final Integer msgRes) {
		AppHost.Vibro.vibrateTwoShort();
		Toaster.instance().show(msgRes);
		if (isFullyVisible) { postCanDecodeFrames(RESTART_DELAY_BIG); }
	}

	private void onQrNotFound() {
		Timber.d("QR not found, allowing further scans in %dms", RESTART_DELAY_SMALL);
		// no qr, restart fast to check again
		if (isFullyVisible) { postCanDecodeFrames(RESTART_DELAY_SMALL); }
	}

	//endregion

	private void importQrAccount(
			@NonNull final String name, @NonNull final NacPrivateKey privateKey, @NonNull final NacPublicKey publicKey)
			throws NacCryptoException {
		AssertUtils.notNull(name, privateKey, publicKey);

		final EncryptedNacPrivateKey encryptedKey =
				privateKey.encryptKey(EKeyProvider.instance().getKey().get());
		final Account account =
				new Account(name, encryptedKey, new PublicAccountData(publicKey));
		new AccountRepository().save(account);
		AddressInfoProvider.instance().invalidateLocal();
		Toast.makeText(getActivity(), StringUtils.format(R.string.message_account_imported, name), Toast.LENGTH_SHORT)
				.show();
		AccountListActivity.start(getActivity());
		getActivity().finish();
	}

	private void postCanDecodeFrames(final int delayMs) {
		_qrView.postDelayed(() -> _qrView.setQRDecodingEnabled(true), delayMs);
	}
}
