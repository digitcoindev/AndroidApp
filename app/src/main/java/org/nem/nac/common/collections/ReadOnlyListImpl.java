package org.nem.nac.common.collections;

import java.util.ArrayList;
import java.util.Collection;

public class ReadOnlyListImpl<T> extends ArrayList<T> implements ReadOnlyList<T> {
	public ReadOnlyListImpl() {
	}

	public ReadOnlyListImpl(final Collection<? extends T> collection) {
		super(collection);
	}
}
