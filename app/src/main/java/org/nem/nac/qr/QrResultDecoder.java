package org.nem.nac.qr;

import android.support.annotation.StringRes;

import com.annimon.stream.function.BiConsumer;
import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.common.utils.JsonUtils;
import org.nem.nac.models.qr.BaseQrData;
import org.nem.nac.models.qr.QrDto;

import timber.log.Timber;

public final class QrResultDecoder {

	private boolean _accountsOnly;
	private       Runnable                           _notFoundListener;
	private       Consumer<Integer>                  _errorListener;
	private       BiConsumer<BaseQrData, QrDto.Type> _successListener;

	public QrResultDecoder(final boolean accountsOnly) {
		_accountsOnly = accountsOnly;
	}

	public void setCallbacks(final Runnable notFoundListener, final Consumer<Integer> errorListener,
			BiConsumer<BaseQrData, QrDto.Type> successListener) {
		_notFoundListener = notFoundListener;
		_errorListener = errorListener;
		_successListener = successListener;
	}

	private String checkInvoice(String json){
		//QrDto dto;
		if (!json.contains("name")){
			String jsonBegin = json.substring(0,json.indexOf("}}"));
			String jsonEnd = json.substring(json.indexOf("}}"));
			json= jsonBegin + ",\"name\":\" \"" + jsonEnd;
			Timber.d("%s", json);
		}

		return json;
	}

	public void decodeResult(final QrResult qrResult) {
		if (qrResult.status != ScanResultStatus.OK) {
			Timber.d("Qr not found");
			onQrNotFound();
			return;
		}
		String json = qrResult.text;
		if (json.contains("\"type\":2")){
			json=checkInvoice(json);
		}
		QrDto dto;
		try {
			dto = JsonUtils.fromJson(json, QrDto.class);
			if (!dto.data.validate()) {
				Timber.e("Malformed QR of type %s", dto.type);
				Timber.d("%s", json);
				onError(R.string.errormessage_malformed_qr);
				return;
			}
		} catch (JsonUtils.ParseException e) {
			Timber.d(e, "Not a NEM QR");
			// found wrong qr, wait a little longer while user changes it, don't waste resources
			onError(R.string.errormessage_not_nem_compatible_qr);
			return;
		}

		Timber.i("QR type: %s", dto.type);
		AppHost.Vibro.vibrateOneMedium();

		if (dto.version < QrDto.VERSION) {
			Timber.w("Outdated QR version! Expected: %d, actual: %d", QrDto.VERSION, dto.version);
			onError(R.string.message_qr_older_version);
			return;
		}
		else if (dto.version > QrDto.VERSION) {
			Timber.w("Newer QR version! Expected: %d, actual: %d", QrDto.VERSION, dto.version);
			onError(R.string.message_qr_newer_version);
			return;
		}

		if (_accountsOnly && dto.type != QrDto.Type.ACCOUNT) {
			Timber.i("Only accounts allowed");
			onError(R.string.errormessage_not_an_account_qr);
			return;
		}

		onSuccess(dto.type, dto.data);
	}

	private void onSuccess(final QrDto.Type type, final BaseQrData data) {
		Timber.i("Decoded %s", type);
		if (_successListener != null) {
			_successListener.accept(data, type);
		}
	}

	private void onError(@StringRes int msgToUserRes) {
		if (_errorListener != null) {
			_errorListener.accept(msgToUserRes);
		}
	}

	private void onQrNotFound() {
		if (_notFoundListener != null) {
			_notFoundListener.run();
		}
	}
}
