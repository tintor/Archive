package tintor.geometry;

import tintor.patterns.Immutable;

@Immutable
public final class Line3 {
	// Fields
	public final Vector3 a, b;

	// Constructors
	public static long counter;

	public Line3(final Vector3 a, final Vector3 b) {
		this.a = a;
		this.b = b;
		counter += 1;
	}

	// Operations
	public Vector3 point(final float t) {
		return Vector3.linear(a, b, t);
	}

	// With Plane
	/** Distance along the line, line is assumed infinite. */
	public float distance(final Plane3 p) {
		return -p.distance(a) / p.normal.dot(b.sub(a));
	}

	// With Point
	public static float distanceSquared(final Vector3 a, final Vector3 b, final Vector3 p) {
		final Vector3 d = b.sub(a);
		final float n = d.dot(p.sub(a)) / d.square();
		return (n >= 1 ? b : n <= 0 ? a : a.add(n, d)).distanceSquared(p);
	}

	private static float square(final float a) {
		return a * a;
	}

	public static float distanceSquaredInf(final Vector3 a, final Vector3 b, final Vector3 p) {
		//final Vector3 d = b.sub(a);
		//final float n = d.dot(p.sub(a)) / d.square();
		//return a.add(n, d).distanceSquared(p);

		final float dx = b.x - a.x, dy = b.y - a.y, dz = b.z - a.z;
		final float mx = p.x - a.x, my = p.y - a.y, mz = p.z - a.z;
		final float n = (dx * mx + dy * my + dz * mz) / (dx * dx + dy * dy + dz * dz);
		return square(dx * n - mx) + square(dy * n - my) + square(dz * n - mz);
	}

	public float nearestInf(final Vector3 p) {
		final Vector3 d = b.sub(a);
		return d.dot(p.sub(a)) / d.square();
	}

	public float nearest(final Vector3 p) {
		return GMath.clamp(nearestInf(p), 0, 1);
	}

	public float distanceSquaredInf(final Vector3 p) {
		return distanceSquaredInf(a, b, p);
	}

	public float distanceSquared(final Vector3 p) {
		return distanceSquared(a, b, p);
	}

	// distance from point to infinite line
	public float distanceInf(final Vector3 p) {
		return GMath.sqrt(distanceSquaredInf(p));
	}

	public float distance(final Vector3 p) {
		return GMath.sqrt(distanceSquared(p));
	}

	// With Line
	public Vector2 closestInf(final Line3 q) {
		final Vector3 A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final float aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		final float det = aa * bb - ab * ab;
		return new Vector2((ab * bc - bb * ac) / det, (aa * bc - ab * ac) / det);
	}

	public Vector2 nearestInf(final Line3 q) {
		final Vector2 k = closestInf(q);
		return k.isFinite() ? k : new Vector2(nearestInf(Vector3.Zero), q.nearestInf(Vector3.Zero));
	}

	public Vector2 nearest(final Line3 q) {
		final Vector2 k = closestInf(q);
		if (k.isFinite()) return new Vector2(GMath.clamp(k.x, 0, 1), GMath.clamp(k.y, 0, 1));
		final float pa = nearest(q.a), pb = nearest(q.b);
		return point(pa).distanceSquared(q.a) < point(pb).distanceSquared(q.b) ? new Vector2(pa, 0) : new Vector2(pb, 1);
	}

	public float distanceSquaredInf(final Line3 q) {
		final Vector2 k = nearestInf(q);
		return point(k.x).distanceSquared(q.point(k.y));
	}

	public float distanceSquared(final Line3 q) {
		final Vector2 k = closestInf(q);
		if (k.isFinite()) return point(GMath.clamp(k.x, 0, 1)).distanceSquared(q.point(GMath.clamp(k.y, 0, 1)));
		return Math.min(distanceSquared(q.a), distanceSquared(q.b));
	}

	public float distanceInf(final Line3 q) {
		return GMath.sqrt(distanceSquaredInf(q));
	}

	public float distance(final Line3 q) {
		return GMath.sqrt(distanceSquared(q));
	}

	public float closestPointInf(final Line3 q) {
		final Vector3 A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final float aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		return (ab * bc - bb * ac) / (aa * bb - ab * ab);
	}

	public float nearestPointInf(final Line3 q) {
		final float x = closestPointInf(q);
		return GMath.isFinite(x) ? x : 0; // if lines are parallel every point is nearest!
	}

	public float nearestPoint(final Line3 q) {
		final float x = closestPointInf(q);
		return GMath.isFinite(x) ? GMath.clamp(x, 0, 1) : q.distanceSquared(a) < q.distanceSquared(b) ? 1 : 0;
	}

	// Misc
	public Vector3 direction() {
		return b.sub(a);
	}

	/** Returns part of line in negative side of plane */
	public Line3 clip(final Plane3 p) {
		final float da = p.distance(a), db = p.distance(b);
		final Side sb = Side.classify(db);

		switch (Side.classify(da)) {
		case Positive:
			switch (sb) {
			case Positive:
				return null;
			case Zero:
				return new Line3(b, b);
			case Negative:
				return new Line3(Vector3.linear(a, b, da / (da - db)), b);
			}
			break;
		case Zero:
			return sb == Side.Positive ? new Line3(a, a) : this;
		case Negative:
			return sb == Side.Positive ? new Line3(a, Vector3.linear(a, b, da / (da - db))) : this;
		}
		throw new RuntimeException();
	}

	//	public boolean equals(final Line3 o) {
	//		return this == o ? true : a.equals(o.a) && b.equals(o.b);
	//	}

	// From Object
	@Override
	public final String toString() {
		return String.format("[%s %s]", a, b);
	}

	//	@Override public boolean equals(final Object o) {
	//		return o instanceof Line3 && equals((Line3) o);
	//	}
	//
	//	@Override public int hashCode() {
	//		return Hash.hash(Line3.class.hashCode(), a.hashCode(), b.hashCode());
	//	}
}