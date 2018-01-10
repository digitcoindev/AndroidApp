package org.nem.nac.common.models;

public class Size {
	public final int width;
	public final int height;

	public Size(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Size)) {
			return false;
		}
		Size s = (Size)o;
		return s.width == width && s.height == height;
	}

	@Override
	public int hashCode() {
		return width * 345 + height;
	}
}
