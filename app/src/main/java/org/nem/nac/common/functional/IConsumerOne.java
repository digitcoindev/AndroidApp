package org.nem.nac.common.functional;

import android.support.annotation.NonNull;

public interface IConsumerOne<T> {
	void accept(T arg);

	default IConsumerOne<T> andThen(@NonNull IConsumerOne<? super T> after) {
		return (T arg) -> {
			accept(arg);
			after.accept(arg);
		};
	}
}
