package org.nem.nac.common.collections;

import java.util.List;

public interface ReadOnlyList<T> extends ReadOnlyCollection<T>, List<T> {
	T get(int position);
}
