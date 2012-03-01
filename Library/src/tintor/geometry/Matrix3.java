package tintor.geometry;

import tintor.patterns.Immutable;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

// Partialy based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

@Immutable
public final class Matrix3 {
	// Fields
	public final Vector3 a, b, c; // rows

	// Constants
	public final static Matrix3 Zero = new Matrix3(Vector3.Zero, Vector3.Zero, Vector3.Zero);
	public final static Matrix3 Identity = new Matrix3(Vector3.X, Vector3.Y, Vector3.Z);
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f,%f|%f,%f,%f|%f,%f,%f)");

	// Fatory Methods
	public static Matrix3 rotation(Vector3 axis, final float angle) {
		if (angle == 0) return Identity;
		axis = axis.unit();
		if (!axis.isFinite()) throw new RuntimeException();

		final float c = GMath.cos(angle), s = GMath.sin(angle);
		return axis.mul(1 - c).mul(axis).add(Identity, c).add(axis.mul(s).tilda());
	}

	// Constructors
	//public static long counter;

	public Matrix3(final Vector3 a, final Vector3 b, final Vector3 c) {
		this.a = a;
		this.b = b;
		this.c = c;
		//counter += 1;
	}

	public Matrix3(final float d) {
		this(d, d, d);
	}

	public Matrix3(final float ax, final float by, final float cz) {
		this(new Vector3(ax, 0, 0), new Vector3(0, by, 0), new Vector3(0, 0, cz));
	}

	public Matrix3(final float ax, final float ay, final float az, final float bx, final float by, final float bz,
			final float cx, final float cy, final float cz) {
		this(new Vector3(ax, ay, az), new Vector3(bx, by, bz), new Vector3(cx, cy, cz));
	}

	// Addition
	public Matrix3 add(final Matrix3 m) {
		return new Matrix3(a.add(m.a), b.add(m.b), c.add(m.c));
	}

	public Matrix3 add(final Matrix3 m, final float i) {
		return new Matrix3(a.add(i, m.a), b.add(i, m.b), c.add(i, m.c));
	}

	public Matrix3 sub(final Matrix3 m) {
		return new Matrix3(a.sub(m.a), b.sub(m.b), c.sub(m.c));
	}

	// Multiplication
	public Matrix3 mul(final float v) {
		return new Matrix3(a.mul(v), b.mul(v), c.mul(v));
	}

	public Vector3 mul(final Vector3 v) {
		return new Vector3(a.dot(v), b.dot(v), c.dot(v));
	}

	/** @return (this * v) . p */
	public float mulDot(final Vector3 v, final Vector3 p) {
		return a.dot(v) * p.x + b.dot(v) * p.y + c.dot(v) * p.z;
	}

	/** @return this * m 
	 *  27 mul */
	public Matrix3 mul(final Matrix3 m) {
		return new Matrix3(a.mul(m), b.mul(m), c.mul(m));
	}

	/** m must be diagonal!
	 *  @return this * m */
	public Matrix3 mulD(final Matrix3 m) {
		return new Matrix3(a.mulD(m), b.mulD(m), c.mulD(m));
	}

	/** @return this * m.transpose */
	public Matrix3 mulT(final Matrix3 m) {
		return new Matrix3(a.mulT(m), b.mulT(m), c.mulT(m));
	}

	/** @return this.transpose * m */
	public Matrix3 tMul(final Matrix3 m) {
		return new Matrix3(Vector3.mul(a.x, b.x, c.x, m), Vector3.mul(a.y, b.y, c.y, m), Vector3.mul(a.z, b.z, c.z, m));
	}

	public Matrix3 sqr() {
		return mul(this);
	}

	/** 36 mul, 1 div */
	public Matrix3 inv() {
		final float d = 1 / det();

		final Vector3 p = new Vector3(b.y * c.z - b.z * c.y, c.y * a.z - a.y * c.z, a.y * b.z - b.y * a.z, d);
		if (!p.isFinite()) return Matrix3.Zero;

		final Vector3 q = new Vector3(b.z * c.x - b.x * c.z, a.x * c.z - c.x * a.z, b.x * a.z - a.x * b.z, d);
		if (!q.isFinite()) return Matrix3.Zero;

		final Vector3 r = new Vector3(b.x * c.y - c.x * b.y, c.x * a.y - a.x * c.y, a.x * b.y - a.y * b.x, d);
		if (!r.isFinite()) return Matrix3.Zero;

		return new Matrix3(p, q, r);
	}

	public Matrix3 transpose() {
		return new Matrix3(colX(), colY(), colZ());
	}

	public float det() {
		return a.mixed(b, c);
	}

	public float trace() {
		return a.x + b.y + c.z;
	}

	/** converts to quaternion */
	public Quaternion quaternion() {
		if (a.x + b.y + c.z >= 0) {
			final float s = GMath.sqrt(1 + a.x + b.y + c.z) * 2;
			return new Quaternion(s / 4, (c.y - b.z) / s, (a.z - c.x) / s, (b.x - a.y) / s);
		}
		if (a.x >= b.y && a.x >= c.z) {
			final float s = GMath.sqrt(1 + a.x - b.y - c.z) * 2;
			return new Quaternion((c.y - b.z) / s, s / 4, (a.y + b.x) / s, (c.x + a.z) / s);
		}
		if (b.y >= c.z) {
			final float s = GMath.sqrt(1 - a.x + b.y - c.z) * 2;
			return new Quaternion((a.z - c.x) / s, (a.y + b.x) / s, s / 4, (b.z + c.y) / s);
		}

		final float s = GMath.sqrt(1 - a.x - b.y + c.z) * 2;
		return new Quaternion((b.x - a.y) / s, (c.x + a.z) / s, (b.z + c.y) / s, s / 4);
	}

	public boolean isFinite() {
		return a.isFinite() && b.isFinite() && c.isFinite();
	}

	// Columns
	public Vector3 colX() {
		return new Vector3(a.x, b.x, c.x);
	}

	public Vector3 colY() {
		return new Vector3(a.y, b.y, c.y);
	}

	public Vector3 colZ() {
		return new Vector3(a.z, b.z, c.z);
	}

	public float dotX(final Vector3 v) {
		return a.x * v.x + b.x * v.y + c.x * v.z;
	}

	public float dotY(final Vector3 v) {
		return a.y * v.x + b.y * v.y + c.y * v.z;
	}

	public float dotZ(final Vector3 v) {
		return a.z * v.x + b.z * v.y + c.z * v.z;
	}

	// Equals
	public boolean equals(final Matrix3 o) {
		return this == o ? true : a.equals(o.a) && b.equals(o.b) && c.equals(o.c);
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Matrix3 && equals((Matrix3) o);
	}

	@Override
	public int hashCode() {
		return Hash.hash(Matrix3.class.hashCode(), a.hashCode(), b.hashCode(), c.hashCode());
	}

	@Override
	public String toString() {
		return String.format("[%f %f %f / %f %f %f / %f %f %f]", a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);
	}
}