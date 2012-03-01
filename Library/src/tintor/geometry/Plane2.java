package tintor.geometry;

import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Plane2 {
	public final Vector2 normal;
	public final float offset;

	// Constants
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f|%f)");

	// Factory Methods
	public static Plane2 bisection(final Vector2 a, final Vector2 b) {
		return pointAndNormal(Vector2.linear(a, b, 0.5f), a.direction(b));
	}

	/** angular bisection <br>
	  * plane contains intersection point of a and b <br>
	  * negative side of a and positive of b <br>
	  * negative side of b and positive of a <br>
	  * negative side of plane contains intersection of negative sides of a and b */
	public static Plane2 bisection(final Plane2 a, final Plane2 b) {
		return pointAndNormal(a.intersect(b), a.normal.add(b.normal).unit());
	}

	public static Plane2 pointAndDirection(final Vector2 a, final Vector2 d) {
		return pointAndNormal(a, d.unit());
	}

	public static Plane2 twoPoints(final Vector2 a, final Vector2 b) {
		final Vector2 n = new Vector2(b.y - a.y, a.x - b.x);
		return new Plane2(n, -n.dot(a));
	}

	public static Plane2 pointAndNormal(final Vector2 a, final Vector2 n) {
		assert 0.9999 < n.square() && n.square() < 1.0001;
		return new Plane2(n, -n.dot(a));
	}

	// Constructors
	public Plane2(final Vector2 a, final Vector2 b) {
		normal = a.direction(b).right();
		offset = -normal.dot(a);
	}

	Plane2(final Vector2 normal, final float offset) {
		this.normal = normal;
		this.offset = offset;
	}

	Plane2(final float a, final float b, final float c) {
		normal = new Vector2(a, b);
		offset = c;
	}

	// BASIC OPERATIONS
	public float distance(final Vector2 a) {
		return normal.dot(a) + offset;
	}

	public Plane2 move(final float a) {
		return new Plane2(normal, offset + a);
	}

	public Plane2 invert() {
		return new Plane2(normal.neg(), -offset);
	}

	// ako se poklapaju vektor ce biti NaN
	// ako su paralelne vektor ce biti Infinite
	public Vector2 intersect(final Plane2 p) {
		final float q = normal.det(p.normal);
		return new Vector2((offset * p.normal.y - p.offset * normal.y) / q, (offset * p.normal.x - p.offset * normal.x)
				/ q);
	}

	// intersect plane with line AB
	// returns T such that instersection point is AT + B(1-T)
	// returns NaN if both points are on plane
	// returns +Infinity if line is parallel above plane
	// returns -Infinity if line is parallel below plane
	public float intersect(final Vector2 a, final Vector2 b) {
		final float bn = b.dot(normal);
		return (bn + offset) / (bn - a.dot(normal));
	}

	@Override public int hashCode() {
		return Hash.hash(normal.hashCode(), Hash.hash(offset));
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Plane2 && equals((Plane2) o);
	}

	public boolean equals(final Plane2 p) {
		return p == this || p.offset == offset && p.normal.equals(normal);
	}

	@Override public String toString() {
		return String.format("(%s %s %s)", normal.x, normal.y, offset);
	}
}