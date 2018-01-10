package org.nem.nac.common.collections;

import java.util.Collection;

public interface ReadOnlyCollection<T> extends Collection<T>, Iterable<T> {
	int size();
}
