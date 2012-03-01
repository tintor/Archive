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
package tintor.geometry.floatN;

import java.io.Serializable;

import tintor.geometry.GMath;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

// Partialy based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

public final class Quaternion implements Serializable {
	// Constants
	public final static Quaternion Identity = new Quaternion(1, 0, 0, 0);
	public final static Quaternion Zero = new Quaternion(0, 0, 0, 0);
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f,%f:%f)");

	// Fields
	public final float w, x, y, z;

	// Constructors
	public Quaternion(final float w, final float x, final float y, final float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Factory Methods
	/** Creates quaternion that represents rotation around axis by angle (in radians).<br>
	 *  Right-hand rule is used for rotation direction.<br>
	 *  Returns Identity quaternion if angle = 0 or axis = 0. */
	public static Quaternion make(final Vector3 axis, final float angle) {
		if (angle == 0) return Identity;
		final Vector3 v = axis.mul(GMath.sin(angle / 2) / axis.length());
		return v.isFinite() ? new Quaternion(GMath.cos(angle / 2), v.x, v.y, v.z) : Identity;
	}

	/** Creates quaternion that rotates vector a to vector b. */
	public static Quaternion make(final Vector3 a, final Vector3 b) {
		Vector3 axis = a.cross(b);
		if (axis.square() < 1e-10) axis = a.normal();
		return make(axis, GMath.acos(a.dot(b) / GMath.sqrt(a.square() * b.square())));
	}

	public static Quaternion axisX(final float angle) {
		return new Quaternion(GMath.cos(angle / 2), GMath.sin(angle / 2), 0, 0);
	}

	public static Quaternion axisY(final float angle) {
		return new Quaternion(GMath.cos(angle / 2), 0, GMath.sin(angle / 2), 0);
	}

	public static Quaternion axisZ(final float angle) {
		return new Quaternion(GMath.cos(angle / 2), 0, 0, GMath.sin(angle / 2));
	}

	// Addition
	/** this + q */
	public Quaternion add(final Quaternion q) {
		return new Quaternion(w + q.w, x + q.x, y + q.y, z + q.z);
	}

	/** this + q */
	public Quaternion sub(final Quaternion q) {
		return new Quaternion(w - q.w, x - q.x, y - q.x, z - q.z);
	}

	/** this + q * a */
	public Quaternion add(final Quaternion q, final float a) {
		return new Quaternion(w + q.w * a, x + q.x * a, y + q.y * a, z + q.z * a);
	}

	/** this - q * a */
	public Quaternion sub(final Quaternion q, final float a) {
		return new Quaternion(w - q.w * a, x - q.x * a, y - q.y * a, z - q.z * a);
	}

	// Multiplication
	/** this * a */
	public Quaternion mul(final float a) {
		return new Quaternion(w * a, x * a, y * a, z * a);
	}

	/** this / a */
	public Quaternion div(final float a) {
		return mul(1 / a);
	}

	/** this . q */
	public float dot(final Quaternion q) {
		return w * q.w + x * q.x + y * q.y + z * q.z;
	}

	/** this * q */
	public Quaternion mul(final Quaternion q) {
		if (q == Identity) return this;
		if (this == Identity) return q;

		final float iw = w * q.w - x * q.x - y * q.y - z * q.z;
		final float ix = w * q.x + x * q.w + y * q.z - z * q.y;
		final float iy = w * q.y - x * q.z + y * q.w + z * q.x;
		final float iz = w * q.z + x * q.y - y * q.x + z * q.w;
		return new Quaternion(iw, ix, iy, iz);
	}

	/** inv(this) * q */
	public Quaternion ldiv(final Quaternion q) {
		if (this == Identity) return q;
		if (q == Identity) return new Quaternion(w, -x, -y, -z);

		final float iw = w * q.w + x * q.x + y * q.y + z * q.z;
		final float ix = w * q.x - x * q.w - y * q.z + z * q.y;
		final float iy = w * q.y + x * q.z - y * q.w - z * q.x;
		final float iz = w * q.z - x * q.y + y * q.x - z * q.w;
		return new Quaternion(iw, ix, iy, iz);
	}

	/** this * inv(q) */
	public Quaternion rdiv(final Quaternion q) {
		if (q == Identity) return this;
		if (this == Identity) return new Quaternion(q.w, -q.x, -q.y, -q.z);

		final float iw = w * q.w + x * q.x + y * q.y + z * q.z;
		final float ix = -w * q.x + x * q.w - y * q.z + z * q.y;
		final float iy = -w * q.y + x * q.z + y * q.w - z * q.x;
		final float iz = -w * q.z - x * q.y + y * q.x + z * q.w;
		return new Quaternion(iw, ix, iy, iz);
	}

	/** returns q such that this * q == q * this == Identity */
	public Quaternion inv() {
		return this == Identity ? Identity : new Quaternion(w, -x, -y, -z);
	}

	/** normalizes quaternion */
	public Quaternion unit() {
		if (this == Identity) return Identity;
		final Quaternion q = div(GMath.sqrt(dot(this)));
		return q.isFinite() ? q : Quaternion.Zero;
	}

	// Conversion
	/** returns axis from (axis, angle) */
	public Vector3 axis() {
		return new Vector3(x, y, z);
	}

	/** returns angle from (axis, angle) */
	public float angle() {
		return GMath.acos(w) * 2;
	}

	/** converts to matrix */
	public Matrix3 matrix() {
		if (this == Identity) return Matrix3.Identity;
		final float x2 = x * x, y2 = y * y, z2 = z * z;
		final float xy = x * y, xz = x * z, yz = y * z;
		final float wx = w * x, wy = w * y, wz = w * z;

		final Vector3 a = new Vector3(1 - 2 * (y2 + z2), 2 * (xy - wz), 2 * (xz + wy));
		final Vector3 b = new Vector3(2 * (xy + wz), 1 - 2 * (x2 + z2), 2 * (yz - wx));
		final Vector3 c = new Vector3(2 * (xz - wy), 2 * (yz + wx), 1 - 2 * (x2 + y2));
		return new Matrix3(a, b, c);
	}

	// for unit quaternion this will be unit vector
	public Vector3 dirX() {
		if (this == Identity) return Vector3.X;
		return new Vector3(1 - 2 * (y * y + z * z), 2 * (x * y - w * z), 2 * (x * z + w * y));
	}

	public Vector3 dirY() {
		if (this == Identity) return Vector3.Y;
		return new Vector3(2 * (x * y + w * z), 1 - 2 * (x * x + z * z), 2 * (y * z - w * x));
	}

	public Vector3 dirZ() {
		if (this == Identity) return Vector3.Z;
		return new Vector3(2 * (x * z - w * y), 2 * (y * z + w * x), 1 - 2 * (x * x + y * y));
	}

	public Vector3 idirX() {
		if (this == Identity) return Vector3.X;
		return new Vector3(1 - 2 * (y * y + z * z), 2 * (x * y + w * z), 2 * (x * z - w * y));
	}

	public Vector3 idirY() {
		if (this == Identity) return Vector3.Y;
		return new Vector3(2 * (x * y - w * z), 1 - 2 * (x * x + z * z), 2 * (y * z + w * x));
	}

	public Vector3 idirZ() {
		if (this == Identity) return Vector3.Z;
		return new Vector3(2 * (x * z + w * y), 2 * (y * z - w * x), 1 - 2 * (x * x + y * y));
	}

	// Misc
	public boolean isFinite() {
		return GMath.isFinite(w) && GMath.isFinite(x) && GMath.isFinite(y) && GMath.isFinite(z);
	}

	/** rotate vector by quaternion, returns this * Quaternion4(0, a) * inv(this) */
	public Vector3 rotate(final Vector3 a) {
		// BUGY! return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(v.cross(a), 2 * w);
		if (this == Identity) return a;

		final float iw = x * a.x + y * a.y + z * a.z;
		final float ix = w * a.x + y * a.z - z * a.y;
		final float iy = w * a.y - x * a.z + z * a.x;
		final float iz = w * a.z + x * a.y - y * a.x;

		final float vx = iw * x + ix * w - iy * z + iz * y;
		final float vy = iw * y + ix * z + iy * w - iz * x;
		final float vz = iw * z - ix * y + iy * x + iz * w;
		return new Vector3(vx, vy, vz);
	}

	/** rotate vector by inverse quaternion, returns inv(this) * Quaternion4(0, a) * this<BR>
	 *  24 mul, 17 add */
	public Vector3 irotate(final Vector3 a) {
		// BUGY! return a.mul(1 - w * w).add(v, 2 * v.dot(a)).add(a.cross(v), 2 * w);
		if (this == Identity) return a;

		final float iw = x * a.x + y * a.y + z * a.z;
		final float ix = w * a.x - y * a.z + z * a.y;
		final float iy = w * a.y + x * a.z - z * a.x;
		final float iz = w * a.z - x * a.y + y * a.x;

		final float vx = iw * x + ix * w + iy * z - iz * y;
		final float vy = iw * y - ix * z + iy * w + iz * x;
		final float vz = iw * z + ix * y - iy * x + iz * w;
		return new Vector3(vx, vy, vz);
	}

	// Static Methods
	private static Quaternion combine(final Quaternion p, final float a, final Quaternion q, final float b) {
		return new Quaternion(p.w * a + q.w * b, p.x * a + q.x * b, p.y * a + q.y * b, p.z * a + q.z * b);
	}

	/** Linear Interpolation */
	public static Quaternion lerp(final Quaternion p, final Quaternion q, final float t) {
		return combine(p, 1 - t, q, t);
	}

	/** Spherical Linear Interpolation */
	public static Quaternion slerp(final Quaternion p, final Quaternion q, final float t) {
		// // a + quat(axis(b + neg(a))*t)
		// Quaternion4 q = a.ldiv(b);
		// float angle = Math.acos(q.w) * t;
		// Vector3 axis = q.v.unit();
		// return new Quaternion4(Math.cos(angle), axis.isFinite() ? axis.mul(Math.sin(angle)) : Vector3.Zero).mul(a);
		final float d = p.dot(q);
		if (d >= 0) {
			final float a = GMath.acos(d), k = 1 / GMath.sin(a);
			return combine(p, GMath.sin(a - t * a) * k, q, GMath.sin(t * a) * k);
		}
		final float a = GMath.acos(-d);
		final float k = 1 / GMath.sin(a);
		return combine(p, GMath.sin(a - t * a) * k, q, GMath.sin(t * a) * -k);
	}

	/** Returns q such that Q * M * transQ is diagonal. */
	public static Quaternion diagonalize(final Matrix3 m, int steps) {
		// NOT WORKING!
		if (true) throw new RuntimeException();

		Quaternion q = Quaternion.Identity;
		while (steps-- > 0) {
			System.out.println(q);
			final Matrix3 d = m.similarT(q.matrix());
			System.out.println(d);
			final Vector3 p = new Vector3(d.b.z, d.a.z, d.a.y);

			final float ax = Math.abs(p.x);
			final float ay = Math.abs(p.y);
			final float az = Math.abs(p.z);

			float x = 0, y = 0, z = 0;
			float f = 0;

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

			final float t = 1 / (Math.abs(f) + GMath.sqrt(1 + f * f));
			final float cosa = 1 / GMath.sqrt(1 + t * t);
			if (cosa >= 1) break;

			final float sina2 = Math.signum(f) * GMath.sqrt((1 - cosa) / 2);
			final float cosa2 = GMath.sqrt(1 - sina2 * sina2);
			if (cosa2 >= 1) break;

			q = new Quaternion(cosa2, x * sina2, y * sina2, z * sina2).mul(q).unit();
		}
		return q;
	}

	public boolean equals(final Quaternion a) {
		return GMath.sqr(x - a.x) + GMath.sqr(y - a.y) + GMath.sqr(z - a.z) + GMath.sqr(w - a.w) <= GMath.sqr(Side.eps
				.get());
	}

	public String toString(final String format) {
		return String.format(format, w, x, y, z);
	}

	// From Object
	@Override public boolean equals(final Object o) {
		return o instanceof Quaternion && equals((Quaternion) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Quaternion.class.hashCode(), Hash.hash(w), Hash.hash(x), Hash.hash(y), Hash.hash(z));
	}

	@Override public String toString() {
		return toString(defaultFormat.get());
	}
}