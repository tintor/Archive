package geometry.base;

import java.util.Locale;

import geometry.GMath;
import util.Hash;

// Based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

@util.Immutable
public final class Quaternion {
	// Fields
	public final double w;
	public final Vector3 v;

	// Constants
	public final static Quaternion Identity = new Quaternion(1, Vector3.Zero);
	public final static Quaternion Zero = new Quaternion(0, Vector3.Zero);

	// Constructors
	public Quaternion(double w, Vector3 v) {
		this.w = w;
		this.v = v;
	}

	public Quaternion(double w, double x, double y, double z) {
		this(w, new Vector3(x, y, z));
	}

	// Factory Methods
	/** Creates quaternion that represents rotation around axis by angle (in radians).<br>
	 *  Right-hand rule is used for rotation direction.<br>
	 *  Returns Identity quaternion if angle = 0 or axis = 0. */
	public static Quaternion make(Vector3 axis, double angle) {
		if (angle == 0) return Identity;
		Vector3 v = axis.mul(Math.sin(angle / 2) / axis.length());
		return v.isFinite() ? new Quaternion(Math.cos(angle / 2), v) : Identity;
	}

	/** Creates quaternion that rotates vector a to vector b. */
	public static Quaternion make(Vector3 a, Vector3 b) {
		Vector3 axis = a.cross(b);
		if(axis.quad() < 1e-10) axis = a.normal();
		return make(axis, Math.acos(a.dot(b) / Math.sqrt(a.quad() * b.quad())));
	}

	public static Quaternion axisX(double angle) {
		return new Quaternion(Math.cos(angle / 2), Math.sin(angle / 2), 0, 0);
	}

	public static Quaternion axisY(double angle) {
		return new Quaternion(Math.cos(angle / 2), 0, Math.sin(angle / 2), 0);
	}

	public static Quaternion axisZ(double angle) {
		return new Quaternion(Math.cos(angle / 2), 0, 0, Math.sin(angle / 2));
	}

	// Addition
	/** this + q */
	public Quaternion add(Quaternion q) {
		return new Quaternion(w + q.w, v.add(q.v));
	}

	/** this + q */
	public Quaternion sub(Quaternion q) {
		return new Quaternion(w - q.w, v.sub(q.v));
	}

	/** this + q * a */
	public Quaternion add(Quaternion q, double a) {
		return new Quaternion(w + q.w * a, v.add(q.v, a));
	}

	/** this - q * a */
	public Quaternion sub(Quaternion q, double a) {
		return new Quaternion(w - q.w * a, v.sub(q.v, a));
	}

	// Multiplication
	/** this * a */
	public Quaternion mul(double a) {
		return new Quaternion(w * a, v.mul(a));
	}

	/** this / a */
	public Quaternion div(double a) {
		return mul(1 / a);
	}

	/** this . q */
	public double dot(Quaternion q) {
		return w * q.w + v.dot(q.v);
	}

	/** this * q */
	public Quaternion mul(Quaternion q) {
		if (q == Identity) return this;
		if (this == Identity) return q;
		return new Quaternion(w * q.w - v.dot(q.v), v.cross(q.v).add(q.v, w).add(v, q.w));
	}

	/** inv(this) * q */
	public Quaternion ldiv(Quaternion q) {
		if (this == Identity) return q;
		if (q == Identity) return inv();
		return new Quaternion(w * q.w + v.dot(q.v), q.v.cross(v).add(q.v, w).sub(v, q.w));
	}

	/** this * inv(q) */
	public Quaternion rdiv(Quaternion q) {
		if (q == Identity) return this;
		if (this == Identity) return q.inv();
		return new Quaternion(w * q.w + v.dot(q.v), q.v.cross(v).sub(q.v, w).add(v, q.w));
	}

	/** returns q such that this * q == q * this == Identity */
	public Quaternion inv() {
		if (this == Identity) return Identity;
		return new Quaternion(w, v.neg());
	}

	/** normalizes quaternion */
	public Quaternion unit() {
		if (this == Identity) return Identity;
		Quaternion q = div(Math.sqrt(dot(this)));
		return GMath.isFinite(q.w) && q.v.isFinite() ? q : Quaternion.Zero;
	}

	// Conversion
	/** returns axis from (axis, angle) */
	public Vector3 axis() {
		return v.unitz();
	}

	/** returns angle from (axis, angle) */
	public double angle() {
		return Math.acos(w) * 2;
	}

	/** converts to matrix */
	public Matrix3 matrix() {
		if (this == Identity) return Matrix3.Identity;

		double x2 = v.x * v.x, y2 = v.y * v.y, z2 = v.z * v.z;
		double xy = v.x * v.y, xz = v.x * v.z, yz = v.y * v.z;
		double wx = w * v.x, wy = w * v.y, wz = w * v.z;

		Vector3 a = new Vector3(1 - 2 * (y2 + z2), 2 * (xy - wz), 2 * (xz + wy));
		Vector3 b = new Vector3(2 * (xy + wz), 1 - 2 * (x2 + z2), 2 * (yz - wx));
		Vector3 c = new Vector3(2 * (xz - wy), 2 * (yz + wx), 1 - 2 * (x2 + y2));
		return new Matrix3(a, b, c);
	}

	// Misc
	/** same sa rotate(Vector3.Z) */
	public Vector3 direction() {
		if (this == Identity) return Vector3.Z;
		return new Vector3(2 * (v.x * v.z - w * v.y), 2 * (v.y * v.z + w * v.x), 1 - 2 * (v.x * v.x + v.y * v.y));
	}

	/** rotate vector by quaternion, returns this * Quaternion(0, a) * inv(this) */
	public Vector3 rotate(Vector3 a) {
		if (this == Identity) return a;
		//return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(v.cross(a), 2 * w);
		return mul(new Quaternion(0, a)).rdiv(this).v;
	}

	/** rotate point by quaternion, returns this * Quaternion(0, a) * inv(this) */
	public Point3 rotate(Point3 a) {
		if (this == Identity) return a;
		//return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(v.cross(a), 2 * w);
		return Point3.Zero.add(mul(new Quaternion(0, a.vector())).rdiv(this).v);
	}

	/** rotate vector by inverse quaternion, returns inv(this) * Quaternion(0, a) * this */
	public Vector3 irotate(Vector3 a) {
		if (this == Identity) return a;
		//		return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(a.cross(v), 2 * w);
		return ldiv(new Quaternion(0, a)).mul(this).v;
	}

	/** rotate vector by inverse quaternion, returns inv(this) * Quaternion(0, a) * this */
	public Point3 irotate(Point3 a) {
		if (this == Identity) return a;
		//		return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(a.cross(v), 2 * w);
		return Point3.Zero.add(ldiv(new Quaternion(0, a.vector())).mul(this).v);
	}

	// Static Methods
	/** Linear Interpolation */
	public static Quaternion lerp(Quaternion p, Quaternion q, double t) {
		return p.mul(1 - t).add(q, t);
	}

	/** Spherical Linear Interpolation */
	public static Quaternion slerp(Quaternion p, Quaternion q, double t) {
		// // a + quat(axis(b + neg(a))*t)
		// Quaternion q = a.ldiv(b);
		// double angle = Math.acos(q.w) * t;
		// Vector3 axis = q.v.unit();
		// return new Quaternion(Math.cos(angle), axis.isFinite() ? axis.mul(Math.sin(angle)) : Vector3.Zero).mul(a);
		double d = p.dot(q);
		if (d >= 0) {
			double a = Math.acos(d), k = 1 / Math.sin(a);
			return p.mul(Math.sin(a - t * a) * k).add(q, Math.sin(t * a) * k);
		} else {
			double a = Math.acos(-d), k = 1 / Math.sin(a);
			return p.mul(Math.sin(a - t * a) * k).sub(q, Math.sin(t * a) * k);
		}
	}

	/** Returns q such that Q * M * transQ is diagonal. */
	public static Quaternion diagonalize(Matrix3 m, int steps) {
		Quaternion q = Quaternion.Identity;
		while (steps-- > 0) {
			System.out.println(q);
			Matrix3 d = m.similarT(q.matrix());
			System.out.println(d);
			Vector3 p = new Vector3(d.b.z, d.a.z, d.a.y);

			double ax = Math.abs(p.x);
			double ay = Math.abs(p.y);
			double az = Math.abs(p.z);

			Vector3 v = null;
			double f = 0;

			if (ax > ay && ax > az) {
				v = Vector3.X;
				f = (d.c.z - d.b.y) / (2 * p.x);
			} else if (ay > az) {
				v = Vector3.Y;
				f = (d.a.x - d.c.z) / (2 * p.y);
			} else {
				v = Vector3.Z;
				f = (d.b.y - d.a.x) / (2 * p.z);
			}
			System.out.println(f);
			if (!GMath.isFinite(f)) break;

			double t = 1 / (Math.abs(f) + Math.sqrt(1 + f * f));

			double cosa = 1 / Math.sqrt(1 + t * t);
			System.out.println("cosa = " + cosa);
			if (cosa >= 1) break;

			double sina2 = Math.signum(f) * Math.sqrt((1 - cosa) / 2);
			System.out.println("sina2 = " + sina2);

			double cosa2 = Math.sqrt(1 - sina2 * sina2);
			System.out.println("cosa2 = " + cosa2);
			if (cosa2 >= 1) break;
			q = new Quaternion(cosa2, v.mul(sina2)).mul(q).unit();
		}
		return q;
	}

	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		Matrix3 m = new Matrix3(1, 0, 0, 0, 1, 0, 0, 0, 1);
		Quaternion q = diagonalize(m, 100);
		System.out.println(m.similarT(q.matrix()));
	}

	// From Object
	public boolean equals(Quaternion q) {
		return GMath.sqr(v.x - q.v.x) + GMath.sqr(v.y - q.v.y) + GMath.sqr(v.y - q.v.y) + GMath.sqr(w - q.w) <= GMath
				.sqr(Side.ε);
	}

	@Override
	public boolean equals(Object o) {
		Quaternion q = (Quaternion) o;
		return q != null && equals(q);
	}

	@Override
	public int hashCode() {
		return Hash.hash(Quaternion.class.hashCode(), v.hashCode(), Hash.hash(w));
	}

	@Override
	public String toString() {
		return String.format("(%.2f, %.2f, %.2f, %.2f)", w, v.x, v.y, v.z);
	}
};