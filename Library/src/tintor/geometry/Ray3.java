package tintor.geometry;

import tintor.patterns.Immutable;
import tintor.util.Hash;

@Immutable
public final class Ray3 {
	// Fields
	public final Vector3 origin, dir;

	// Constructors
	public Ray3(final Vector3 origin, final Vector3 dir) {
		this.origin = origin;
		this.dir = dir.unit();
	}

	/** Returns point on the ray. */
	public Vector3 point(final float t) {
		return origin.add(t, dir);
	}

	// Factory Methods
	/** ASSUME |dir| = 1 AND |normal| = 1 */
	public static Ray3 reflect(final Vector3 dir, final Vector3 point, final Vector3 normal) {
		return new Ray3(point, dir.sub(2 * normal.dot(dir), normal));
	}

	// With Plane
	/** Distance along the ray.<br>
	 *  Returns +-Inf or NaN if ray is parallel to plane. */
	public float distance(final Plane3 p) {
		return -p.distance(origin) / p.normal.dot(dir);
	}

	// With Point
	public float nearest(final Vector3 p) {
		return dir.dot(p, origin);
	}

	public float distanceSquared(final Vector3 p) {
		return point(nearest(p)).distanceSquared(p);
	}

	public float distance(final Vector3 p) {
		return GMath.sqrt(distanceSquared(p));
	}

	// With Ray
	/** Returns coordinates of closest points beetwen this and q.<BR>
	 *  Returns non-finite Vector2 if this.dir is colinear with q.dir. */
	public Vector2 closest(final Ray3 q) {
		final Vector3 r = origin.sub(q.origin);
		final float a = dir.dot(q.dir), b = dir.dot(r), c = q.dir.dot(r), d = 1 - a * a;
		return new Vector2((a * c - b) / d, (a * b - c) / d);
	}

	public Vector2 nearest(final Ray3 q) {
		final Vector2 k = closest(q);
		return k.isFinite() ? k : new Vector2(-origin.dot(dir), -q.origin.dot(q.dir));
	}

	public float distanceSquared(final Ray3 q) {
		final Vector2 c = nearest(q);
		return point(c.x).distanceSquared(point(c.y));
	}

	public float distance(final Ray3 q) {
		return GMath.sqrt(distanceSquared(q));
	}

	public boolean equals(final Ray3 r) {
		return this == r ? true : origin.equals(r.origin) && dir.equals(r.dir);
	}

	// From Object
	@Override
	public final String toString() {
		return String.format("[%s %s]", origin, dir);
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Ray3 && equals((Ray3) o);
	}

	@Override
	public int hashCode() {
		return Hash.hash(Ray3.class.hashCode(), origin.hashCode(), dir.hashCode());
	}
}