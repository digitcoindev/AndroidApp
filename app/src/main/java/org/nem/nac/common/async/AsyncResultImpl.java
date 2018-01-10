package org.nem.nac.common.async;

import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

public final class AsyncResultImpl<TResult> implements AsyncResult<TResult> {
	private TResult   _result;
	private Exception _exception;

	public AsyncResultImpl() {
	}

	public AsyncResultImpl(final TResult result) {
		_result = result;
	}

	public AsyncResultImpl(final Exception exception) {
		_exception = exception;
	}

	public AsyncResult<TResult> setResult(@Nullable final TResult result) {
		_result = result;
		return this;
	}

	public AsyncResult<TResult> setException(@Nullable final Exception exception) {
		_exception = exception;
		return this;
	}

	@Override
	public Optional<TResult> getResult() {
		return Optional.ofNullable(_result);
	}

	@Override
	public Optional<Exception> getException() {
		return Optional.ofNullable(_exception);
	}

	public void applyToCallbackSafe(@Nullable final AsyncCallback<TResult> callback) {
		if (callback != null) {
			callback.apply(this);
		}
	}
}
