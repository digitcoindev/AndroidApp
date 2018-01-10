package org.nem.nac.common.functional;

import android.support.annotation.NonNull;

public interface CallbackWithResult<TResult> {
	void call(@NonNull final TResult result);
}
