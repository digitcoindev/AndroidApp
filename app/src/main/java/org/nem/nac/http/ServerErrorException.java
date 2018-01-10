package org.nem.nac.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import org.nem.nac.common.exceptions.NacException;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.models.api.ErrorObjectApiDto;

/**
 * Thrown when NIS is returning response with unsuccessful HTTP Code
 */
public final class ServerErrorException extends NacException {
	private final ErrorObjectApiDto _errorDto;
	private final int               _httpCode;
	private final String            _httpMessage;

	public ServerErrorException(final int httpCode, @Nullable final String httpMessage) {
		_httpCode = httpCode;
		_httpMessage = httpMessage;
		_errorDto = null;
	}

	public ServerErrorException(final int httpCode,
			@Nullable final String httpMessage, @Nullable final ErrorObjectApiDto errorDto) {
		_httpCode = httpCode;
		_httpMessage = httpMessage;
		_errorDto = errorDto;
	}

	public int getHttpCode() {
		return _httpCode;
	}

	@NonNull
	public Optional<String> getHttpMessage() {
		return Optional.ofNullable(_httpMessage);
	}

	@NonNull
	public Optional<ErrorObjectApiDto> getErrorDto() {
		return Optional.ofNullable(_errorDto);
	}

	public String getReadableError(@NonNull final String orElse) {
		String error = null;
		if (_errorDto != null) {
			error = _errorDto.toString();
		}
		else if (_httpMessage != null) {
			error = _httpMessage;
		}
		return StringUtils.isNotNullOrEmpty(error) ? error : orElse;
	}
}
