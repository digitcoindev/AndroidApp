package org.nem.nac.common.async;

import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

public interface AsyncResult<TResult> {
	AsyncResult<TResult> setResult(final TResult result);

	AsyncResult<TResult> setException(final Exception exception);

	Optional<? extends TResult> getResult();

	Optional<? extends Exception> getException();

	/**
	 * Invokes callback with this result. Implementations must check if supplied callback is not null before invoking.
	 */
	void applyToCallbackSafe(@Nullable final AsyncCallback<TResult> asyncCallback);
}
