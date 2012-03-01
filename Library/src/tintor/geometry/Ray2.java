package tintor.geometry;

import tintor.patterns.Immutable;
import tintor.util.Hash;

/** Infinite directed line in space. */
@Immutable public final class Ray2 {
	// Fields
	public final Vector2 origin;
	public final Vector2 dir;

	// Constructors
	public Ray2(final Vector2 origin, final Vector2 dir) {
		this.origin = origin;
		this.dir = dir.unit();
	}

	public Ray2(final Vector2 origin, final float angle) {
		this.origin = origin;
		dir = Vector2.polar(angle);
	}

	// Operations
	public Vector2 point(final float t) {
		return origin.add(t, dir);
	}

	public float nearest(final Vector2 p) {
		return p.sub(origin).dot(dir);
	}

	public float distance(final Vector2 p) {
		final Vector2 v = origin.sub(p);
		return dir.mul(v.dot(dir)).distance(v);
	}

	// Unique
	//	public Side side(Vector2 p, float ε) {
	//		return Side.classify(dir.det(origin.sub(p)));
	//	}

	// from Object
	public boolean equals(final Ray2 o) {
		return this == o ? true : o != null && origin == o.origin && dir == o.dir;
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Ray2 && equals((Ray2) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Ray3.class.hashCode(), origin.hashCode(), dir.hashCode());
	}
}