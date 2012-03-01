package tintor.geometry;

import tintor.patterns.Immutable;

@Immutable
public final class Interval {
	// Fields
	public float min, max;

	// Constructors
	public Interval(final float min, final float max) {
		this.min = min;
		this.max = max;
	}

	public Interval() {
		this(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);
	}

	public void set(final float min, final float max) {
		this.min = min;
		this.max = max;
	}

	public Interval include(final Vector3 normal, final Vector3[] points) {
		set(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);
		for (final Vector3 p : points)
			include(normal.dot(p));
		return this;
	}

	// Operations
	public void include(final float a) {
		if (a > max) max = a;
		if (a < min) min = a;
	}

	public Interval shift(final float a) {
		min += a;
		max += a;
		return this;
	}

	public float width() {
		return max - min;
	}

	public float center() {
		return (max + min) / 2;
	}

	public void union(final Interval a) {
		if (a.max > max) max = a.max;
		if (a.min < min) min = a.min;
	}

	public void intersect(final Interval a) {
		if (a.max < max) max = a.max;
		if (a.min > min) min = a.min;
	}

	public float distance(final Interval b) {
		return (Math.abs(min - b.min + max - b.max) + min - max + b.min - b.max) / 2;
	}

	//	public boolean equals(final Interval a) {
	//		return min == a.min && max == a.max;
	//	}

	// From Object
	@Override
	public String toString() {
		return String.format("[%f %f]", min, max);
	}

	//	@Override public boolean equals(final Object o) {
	//		return o instanceof Interval && equals((Interval) o);
	//	}
	//
	//	@Override public final int hashCode() {
	//		return Hash.hash(Interval.class.hashCode(), Hash.hash(min), Hash.hash(max));
	//	}
}