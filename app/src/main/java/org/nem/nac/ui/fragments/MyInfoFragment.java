package org.nem.nac.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.nem.nac.R;
import org.nem.nac.application.AppConstants;
import org.nem.nac.common.exceptions.NacUserVisibleException;
import org.nem.nac.common.utils.IOUtils;
import org.nem.nac.common.utils.IntentUtils;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.qr.QrDto;
import org.nem.nac.models.qr.QrUserInfo;
import org.nem.nac.share.ShareHelper;
import org.nem.nac.ui.controls.QrImageView;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.dialogs.EditFieldDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import timber.log.Timber;

public final class MyInfoFragment extends BaseTabFragment {

	private static final String ARG_PARC_ACCOUNT = "arg-account";

	public static MyInfoFragment create(final Account account) {
		MyInfoFragment fragment = new MyInfoFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARC_ACCOUNT, account);
		fragment.setArguments(args);
		return fragment;
	}

	private TextView    _myAddressField;
	private TextView    _myNameField;
	private QrImageView _qrImageView;
	private View        _btnShareQr;
	private Bitmap  _qrBitmap;
	private Account _account;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = inflater.inflate(R.layout.fragment_my_info, container, false);
		_myAddressField = (TextView)layout.findViewById(R.id.field_my_address);
		final View btnCopyAddress = layout.findViewById(R.id.btn_copy_address);
		btnCopyAddress.setOnClickListener(this::onCopyAddress);
		final View btnShareAddress = layout.findViewById(R.id.btn_share_address);
		btnShareAddress.setOnClickListener(this::onShareAddress);
		_myNameField = (TextView)layout.findViewById(R.id.field_my_name);
		final View btnEditName = layout.findViewById(R.id.btn_edit_name);
		btnEditName.setOnClickListener(this::onEditNameClick);
		_qrImageView = (QrImageView)layout.findViewById(R.id.imageview_qr);
		_qrImageView.setOnQrRenderedListener(this::onQrRendered);
		_btnShareQr = layout.findViewById(R.id.btn_share_qr);
		_btnShareQr.setOnClickListener(this::onShareQr);
		return layout;
	}

	@Override
	protected void onFullyVisible() {
		super.onFullyVisible();
		//
		if (_account == null) {
			if (getArguments() != null) {
				_account = getArguments().<Account>getParcelable(ARG_PARC_ACCOUNT);
			}
			else { _account = null; }
			//
			if (_account == null) {
				Timber.w("Last used address not present.");
				Toaster.instance().showGeneralError();
				return;
			}
			_myNameField.setText(_account.name);
			_myAddressField.setText(_account.publicData.address.toString(true));
		}
		//
		if (_qrBitmap == null) {
			_qrImageView.setQrDto(new QrDto(QrDto.Type.USER_INFO, new QrUserInfo(_account.name, _account.publicData.address)));
		}
		_myNameField.setText(_account.name);
		_myAddressField.setText(_account.publicData.address.toString(true));
	}

	private void onQrRendered(final Bitmap bitmap) {
		if (getActivity() != null && !getActivity().isFinishing()) {
			if (bitmap == null) {
				_btnShareQr.setClickable(false);
				_btnShareQr.setEnabled(false);
				Toaster.instance().showGeneralError();
			}
			_qrBitmap = bitmap;
			_btnShareQr.setClickable(true);
			_btnShareQr.setEnabled(true);
		}
	}

	private void onCopyAddress(final View clicked) {
		ClipboardManager clipboard = (ClipboardManager)
				getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

		final ClipData clip = ClipData.newPlainText("NEM address", _myAddressField.getText());
		clipboard.setPrimaryClip(clip);
		Toast.makeText(getActivity(), R.string.message_copy_complete, Toast.LENGTH_SHORT).show();
	}

	private void onShareAddress(final View clicked) {
		final String address = _myAddressField.getText().toString();
		IntentUtils
				.startSharingChooser(getActivity(), "text/plain", getString(R.string.action_chooser_share_address), getString(R.string.email_subject_share_address), address, null);
	}

	private void onEditNameClick(final View clicked) {
		EditFieldDialogFragment.create(R.string.dialog_title_edit_qr_name, getString(R.string.input_hint_name), _account.name, false)
				.setOnConfirmChangedValueListener(name -> {
					_account.name = name;
					_myNameField.setText(name);
					_qrImageView.setQrDto(new QrDto(QrDto.Type.USER_INFO, new QrUserInfo(_account.name, _account.publicData.address)));
				})
				.show(getActivity().getFragmentManager(), null);
	}

	private void onShareQr(final View clicked) {
		_btnShareQr.setClickable(false);
		try {
			final String filePath = IOUtils.saveBitmapToFile(_qrBitmap, AppConstants.QR_IMAGE_STORE_FILE_NAME);
			final String body =
					StringUtils.format(R.string.email_body_share_account, _account.name);
			final String subject = StringUtils.format(R.string.email_subject_share_account, _account.name);

			ShareHelper.shareQr(getActivity(), subject, body, filePath, getString(R.string.action_chooser_share_account));
		} catch (NacUserVisibleException e) {
			ConfirmDialogFragment.create(true, null, e.getMessage(), null).show(getActivity().getFragmentManager(), null);
		} finally {
			_btnShareQr.setClickable(true);
		}
	}
}
