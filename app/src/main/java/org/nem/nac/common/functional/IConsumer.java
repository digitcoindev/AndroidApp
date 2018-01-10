package org.nem.nac.common.functional;

import android.support.annotation.NonNull;

public interface IConsumer {
	void accept();

	default IConsumer andThen(@NonNull IConsumer after) {
		return () -> {
			accept();
			after.accept();
		};
	}
}
