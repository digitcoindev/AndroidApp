package org.nem.nac.common.async;

public interface AsyncCallback<TResult> {
	void apply(AsyncResult<TResult> result);
}
