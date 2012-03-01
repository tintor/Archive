/*
Copyright (C) 2007 Marko Tintor <tintor@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package tintor.geometry.doubleN;

import java.io.Serializable;

import tintor.geometry.GMath;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

// Partialy based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

public final class Quaterniond implements Serializable {
	// Constants
	public final static Quaterniond Identity = new Quaterniond(1, 0, 0, 0);
	public final static Quaterniond Zero = new Quaterniond(0, 0, 0, 0);
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f,%f:%f)");

	// Fields
	public final double w, x, y, z;

	// Constructors
	public Quaterniond(final double w, final double x, final double y, final double z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Factory Methods
	/** Creates quaternion that represents rotation around axis by angle (in radians).<br>
	 *  Right-hand rule is used for rotation direction.<br>
	 *  Returns Identity quaternion if angle = 0 or axis = 0. */
	public static Quaterniond make(final Vector3d axis, final double angle) {
		if (angle == 0) return Identity;
		final Vector3d v = axis.mul(Math.sin(angle / 2) / axis.length());
		return v.isFinite() ? new Quaterniond(Math.cos(angle / 2), v.x, v.y, v.z) : Identity;
	}

	/** Creates quaternion that rotates vector a to vector b. */
	public static Quaterniond make(final Vector3d a, final Vector3d b) {
		Vector3d axis = a.cross(b);
		if (axis.square() < 1e-10) axis = a.normal();
		return make(axis, Math.acos(a.dot(b) / Math.sqrt(a.square() * b.square())));
	}

	public static Quaterniond axisX(final double angle) {
		return new Quaterniond(Math.cos(angle / 2), Math.sin(angle / 2), 0, 0);
	}

	public static Quaterniond axisY(final double angle) {
		return new Quaterniond(Math.cos(angle / 2), 0, Math.sin(angle / 2), 0);
	}

	public static Quaterniond axisZ(final double angle) {
		return new Quaterniond(Math.cos(angle / 2), 0, 0, Math.sin(angle / 2));
	}

	// Addition
	/** this + q */
	public Quaterniond add(final Quaterniond q) {
		return new Quaterniond(w + q.w, x + q.x, y + q.y, z + q.z);
	}

	/** this + q */
	public Quaterniond sub(final Quaterniond q) {
		return new Quaterniond(w - q.w, x - q.x, y - q.x, z - q.z);
	}

	/** this + q * a */
	public Quaterniond add(final Quaterniond q, final double a) {
		return new Quaterniond(w + q.w * a, x + q.x * a, y + q.y * a, z + q.z * a);
	}

	/** this - q * a */
	public Quaterniond sub(final Quaterniond q, final double a) {
		return new Quaterniond(w - q.w * a, x - q.x * a, y - q.y * a, z - q.z * a);
	}

	// Multiplication
	/** this * a */
	public Quaterniond mul(final double a) {
		return new Quaterniond(w * a, x * a, y * a, z * a);
	}

	/** this / a */
	public Quaterniond div(final double a) {
		return mul(1 / a);
	}

	/** this . q */
	public double dot(final Quaterniond q) {
		return w * q.w + x * q.x + y * q.y + z * q.z;
	}

	/** this * q */
	public Quaterniond mul(final Quaterniond q) {
		if (q == Identity) return this;
		if (this == Identity) return q;

		final double iw = w * q.w - x * q.x - y * q.y - z * q.z;
		final double ix = w * q.x + x * q.w + y * q.z - z * q.y;
		final double iy = w * q.y - x * q.z + y * q.w + z * q.x;
		final double iz = w * q.z + x * q.y - y * q.x + z * q.w;
		return new Quaterniond(iw, ix, iy, iz);
	}

	/** inv(this) * q */
	public Quaterniond ldiv(final Quaterniond q) {
		if (this == Identity) return q;
		if (q == Identity) return new Quaterniond(w, -x, -y, -z);

		final double iw = w * q.w + x * q.x + y * q.y + z * q.z;
		final double ix = w * q.x - x * q.w - y * q.z + z * q.y;
		final double iy = w * q.y + x * q.z - y * q.w - z * q.x;
		final double iz = w * q.z - x * q.y + y * q.x - z * q.w;
		return new Quaterniond(iw, ix, iy, iz);
	}

	/** this * inv(q) */
	public Quaterniond rdiv(final Quaterniond q) {
		if (q == Identity) return this;
		if (this == Identity) return new Quaterniond(q.w, -q.x, -q.y, -q.z);

		final double iw = w * q.w + x * q.x + y * q.y + z * q.z;
		final double ix = -w * q.x + x * q.w - y * q.z + z * q.y;
		final double iy = -w * q.y + x * q.z + y * q.w - z * q.x;
		final double iz = -w * q.z - x * q.y + y * q.x + z * q.w;
		return new Quaterniond(iw, ix, iy, iz);
	}

	/** returns q such that this * q == q * this == Identity */
	public Quaterniond inv() {
		return this == Identity ? Identity : new Quaterniond(w, -x, -y, -z);
	}

	/** normalizes quaternion */
	public Quaterniond unit() {
		if (this == Identity) return Identity;
		final Quaterniond q = div(Math.sqrt(dot(this)));
		return q.isFinite() ? q : Quaterniond.Zero;
	}

	// Conversion
	/** returns axis from (axis, angle) */
	public Vector3d axis() {
		return new Vector3d(x, y, z);
	}

	/** returns angle from (axis, angle) */
	public double angle() {
		return Math.acos(w) * 2;
	}

	/** converts to matrix */
	public Matrix3 matrix() {
		if (this == Identity) return Matrix3.Identity;
		final double x2 = x * x, y2 = y * y, z2 = z * z;
		final double xy = x * y, xz = x * z, yz = y * z;
		final double wx = w * x, wy = w * y, wz = w * z;

		final Vector3d a = new Vector3d(1 - 2 * (y2 + z2), 2 * (xy - wz), 2 * (xz + wy));
		final Vector3d b = new Vector3d(2 * (xy + wz), 1 - 2 * (x2 + z2), 2 * (yz - wx));
		final Vector3d c = new Vector3d(2 * (xz - wy), 2 * (yz + wx), 1 - 2 * (x2 + y2));
		return new Matrix3(a, b, c);
	}

	// for unit quaternion this will be unit vector
	public Vector3d dirX() {
		if (this == Identity) return Vector3d.X;
		return new Vector3d(1 - 2 * (y * y + z * z), 2 * (x * y - w * z), 2 * (x * z + w * y));
	}

	public Vector3d dirY() {
		if (this == Identity) return Vector3d.Y;
		return new Vector3d(2 * (x * y + w * z), 1 - 2 * (x * x + z * z), 2 * (y * z - w * x));
	}

	public Vector3d dirZ() {
		if (this == Identity) return Vector3d.Z;
		return new Vector3d(2 * (x * z - w * y), 2 * (y * z + w * x), 1 - 2 * (x * x + y * y));
	}

	public Vector3d idirX() {
		if (this == Identity) return Vector3d.X;
		return new Vector3d(1 - 2 * (y * y + z * z), 2 * (x * y + w * z), 2 * (x * z - w * y));
	}

	public Vector3d idirY() {
		if (this == Identity) return Vector3d.Y;
		return new Vector3d(2 * (x * y - w * z), 1 - 2 * (x * x + z * z), 2 * (y * z + w * x));
	}

	public Vector3d idirZ() {
		if (this == Identity) return Vector3d.Z;
		return new Vector3d(2 * (x * z + w * y), 2 * (y * z - w * x), 1 - 2 * (x * x + y * y));
	}

	// Misc
	public boolean isFinite() {
		return GMath.isFinite(w) && GMath.isFinite(x) && GMath.isFinite(y) && GMath.isFinite(z);
	}

	/** rotate vector by quaternion, returns this * Quaternion4(0, a) * inv(this) */
	public Vector3d rotate(final Vector3d a) {
		// BUGY! return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(v.cross(a), 2 * w);
		if (this == Identity) return a;

		final double iw = x * a.x + y * a.y + z * a.z;
		final double ix = w * a.x + y * a.z - z * a.y;
		final double iy = w * a.y - x * a.z + z * a.x;
		final double iz = w * a.z + x * a.y - y * a.x;

		final double vx = iw * x + ix * w - iy * z + iz * y;
		final double vy = iw * y + ix * z + iy * w - iz * x;
		final double vz = iw * z - ix * y + iy * x + iz * w;
		return new Vector3d(vx, vy, vz);
	}

	/** rotate vector by inverse quaternion, returns inv(this) * Quaternion4(0, a) * this<BR>
	 *  24 mul, 17 add */
	public Vector3d irotate(final Vector3d a) {
		// BUGY! return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(a.cross(v), 2 * w);
		if (this == Identity) return a;

		final double iw = x * a.x + y * a.y + z * a.z;
		final double ix = w * a.x - y * a.z + z * a.y;
		final double iy = w * a.y + x * a.z - z * a.x;
		final double iz = w * a.z - x * a.y + y * a.x;

		final double vx = iw * x + ix * w + iy * z - iz * y;
		final double vy = iw * y - ix * z + iy * w + iz * x;
		final double vz = iw * z + ix * y - iy * x + iz * w;
		return new Vector3d(vx, vy, vz);
	}

	// Static Methods
	private static Quaterniond combine(final Quaterniond p, final double a, final Quaterniond q, final double b) {
		return new Quaterniond(p.w * a + q.w * b, p.x * a + q.x * b, p.y * a + q.y * b, p.z * a + q.z * b);
	}

	/** Linear Interpolation */
	public static Quaterniond lerp(final Quaterniond p, final Quaterniond q, final double t) {
		return combine(p, 1 - t, q, t);
	}

	/** Spherical Linear Interpolation */
	public static Quaterniond slerp(final Quaterniond p, final Quaterniond q, final double t) {
		// // a + quat(axis(b + neg(a))*t)
		// Quaternion4 q = a.ldiv(b);
		// double angle = Math.acos(q.w) * t;
		// Vector3 axis = q.v.unit();
		// return new Quaternion4(Math.cos(angle), axis.isFinite() ? axis.mul(Math.sin(angle)) : Vector3.Zero).mul(a);
		final double d = p.dot(q);
		if (d >= 0) {
			final double a = Math.acos(d), k = 1 / Math.sin(a);
			return combine(p, Math.sin(a - t * a) * k, q, Math.sin(t * a) * k);
		}
		final double a = Math.acos(-d);
		final double k = 1 / Math.sin(a);
		return combine(p, Math.sin(a - t * a) * k, q, Math.sin(t * a) * -k);
	}

	/** Returns q such that Q * M * transQ is diagonal. */
	public static Quaterniond diagonalize(final Matrix3 m, int steps) {
		// NOT WORKING!
		if (true) throw new RuntimeException();

		Quaterniond q = Quaterniond.Identity;
		while (steps-- > 0) {
			System.out.println(q);
			final Matrix3 d = m.similarT(q.matrix());
			System.out.println(d);
			final Vector3d p = new Vector3d(d.b.z, d.a.z, d.a.y);

			final double ax = Math.abs(p.x);
			final double ay = Math.abs(p.y);
			final double az = Math.abs(p.z);

			double x = 0, y = 0, z = 0;
			double f = 0;

			if (ax > ay && ax > az) {
				x = 1;
				f = (d.c.z - d.b.y) / (2 * p.x);
			} else if (ay > az) {
				y = 1;
				f = (d.a.x - d.c.z) / (2 * p.y);
			} else {
				z = 1;
				f = (d.b.y - d.a.x) / (2 * p.z);
			}
			if (!GMath.isFinite(f)) break;

			final double t = 1 / (Math.abs(f) + Math.sqrt(1 + f * f));
			final double cosa = 1 / Math.sqrt(1 + t * t);
			if (cosa >= 1) break;

			final double sina2 = Math.signum(f) * Math.sqrt((1 - cosa) / 2);
			final double cosa2 = Math.sqrt(1 - sina2 * sina2);
			if (cosa2 >= 1) break;

			q = new Quaterniond(cosa2, x * sina2, y * sina2, z * sina2).mul(q).unit();
		}
		return q;
	}

	public boolean equals(final Quaterniond a) {
		return GMath.sqr(x - a.x) + GMath.sqr(y - a.y) + GMath.sqr(z - a.z) + GMath.sqr(w - a.w) <= GMath.sqr(Sided.eps
				.get());
	}

	public String toString(final String format) {
		return String.format(format, w, x, y, z);
	}

	// From Object
	@Override public boolean equals(final Object o) {
		return o instanceof Quaterniond && equals((Quaterniond) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Quaterniond.class.hashCode(), Hash.hash(w), Hash.hash(x), Hash.hash(y), Hash.hash(z));
	}

	@Override public String toString() {
		return toString(defaultFormat.get());
	}
}