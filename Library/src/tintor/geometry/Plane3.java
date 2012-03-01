package tintor.geometry;

import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Plane3 {
	// Static
	public static float distance(final Vector3 p, final Vector3 a, final Vector3 b, final Vector3 c) {
		//		final Vector3 n = b.sub(a).cross(c.sub(a));
		//		return n.dot(p) - n.dot(a);
		final float cax = c.x - a.x, cay = c.y - a.y, caz = c.z - a.z;
		final float bax = b.x - a.x, bay = b.y - a.y, baz = b.z - a.z;
		return (bay * caz - baz * cay) * (p.x - a.x) + (baz * cax - bax * caz) * (p.y - a.y) + (bax * cay - bay * cax)
				* (p.z - a.z);
	}

	public static Vector3 closest(final Vector3 p, final Vector3 a, final Vector3 b, final Vector3 c) {
		// final Vector3 n = b.sub(a).cross(c.sub(a));
		// return p.sub(n, n.dot(p, a));
		final float cax = c.x - a.x, cay = c.y - a.y, caz = c.z - a.z;
		final float bax = b.x - a.x, bay = b.y - a.y, baz = b.z - a.z;

		final float nx = bay * caz - baz * cay;
		final float ny = baz * cax - bax * caz;
		final float nz = bax * cay - bay * cax;

		final float d = nx * (p.x - a.x) + ny * (p.y - a.y) + nz * (p.z - a.z);
		return new Vector3(p.x - nx * d, p.y - ny * d, p.z - nz * d);
	}

	// Fields
	public final Vector3 normal;
	public final float offset;

	// Constants
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f,%f|%f)");

	// Constructors
	/** Note: All methods assume that |normal| = 1! */
	public Plane3(final Vector3 normal, final float offset) {
		this.normal = normal;
		this.offset = offset;
	}

	/** Note: All methods assume that |normal| = 1! */
	public Plane3(final float a, final float b, final float c, final float d) {
		this(new Vector3(a, b, c), d);
	}

	/** Note: All methods assume that |normal| = 1! */
	public Plane3(final Vector3 normal, final Vector3 point) {
		this(normal, -normal.dot(point));
	}

	public Plane3(final Vector3[] a) {
		this(a[0], a[1], a[2]);
		// TODO find best fitting plane
		//		for (int i = 3; i < a.length; i++)
		//			if (side(a[i]) != Side.Zero) throw new RuntimeException();
	}

	public Plane3(final Vector3 a, final Vector3 b, final Vector3 c) { // counter-clockwise
		this(b.sub(a).cross(c.sub(a)).unit(), a);
	}

	// Factory Methods
	/** Plane is bisection of line AB<br>
	/*  a is in positive, b is in negative, normal points from b to a */
	public static Plane3 bisection(final Vector3 a, final Vector3 b) {
		return new Plane3(b.sub(a).unit(), Vector3.average(a, b));
	}

	// Operations
	public float distance(final Vector3 a) {
		return normal.dot(a) + offset;
	}

	public Side side(final Vector3 a) {
		return Side.classify(distance(a));
	}

	public Plane3 move(final float a) {
		return new Plane3(normal, offset - a);
	}

	public Plane3 flip() {
		return new Plane3(normal.neg(), -offset);
	}

	// Static Methods
	public static Vector3 intersection(final Plane3 a, final Plane3 b, final Plane3 c) {
		final Vector3 ab = a.normal.cross(b.normal);
		final Vector3 bc = b.normal.cross(c.normal);
		final Vector3 ca = c.normal.cross(a.normal);
		return ab.mul(c.offset).add(a.offset, bc).add(b.offset, ca).div(-a.normal.dot(bc));
	}

	public static Ray3 intersection(final Plane3 a, final Plane3 b) {
		final Vector3 dir = a.normal.cross(b.normal);
		final Vector3 origin = GMath.solveLinearRow(dir, a.normal, b.normal, new Vector3(0, -a.offset, -b.offset));
		return origin.isFinite() ? new Ray3(origin, dir) : null;
	}

	// From Object
	@Override
	public String toString() {
		return String.format("(%f %f %f %f)", normal.x, normal.y, normal.z, offset);
	}

	@Override
	public int hashCode() {
		return Hash.hash(normal.hashCode(), Hash.hash(offset));
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Plane3 && equals((Plane3) o);
	}

	public boolean equals(final Plane3 p) {
		return p == this || p.offset == offset && p.normal.equals(normal);
	}
}