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
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.geometry.GMath;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Vector3d implements Comparable<Vector3d>, Serializable {
	// Constants
	public final static Vector3d Zero = new Vector3d(0, 0, 0);
	public final static Vector3d X = new Vector3d(1, 0, 0);
	public final static Vector3d Y = new Vector3d(0, 1, 0);
	public final static Vector3d Z = new Vector3d(0, 0, 1);
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%s,%s,%s)");

	// Fields
	public final double x, y, z;

	// Constructors
	public Vector3d(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Static Methods
	/** Constructs vector from String using defaultFormat. */
	public static Vector3d valueOf(final String str) {
		return valueOf(str, defaultFormat.get());
	}

	public static Vector3d valueOf(final String str, final String pattern) {
		final String decimal = "([+-]?\\d+(?:\\.\\d+)?)";
		return valueOf(str, Pattern.compile(pattern.replaceAll("[()]", "\\\\$0").replace("%s", decimal)));
	}

	public static Vector3d valueOf(final String str, final Pattern pattern) {
		final Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException(pattern.pattern());
		if (matcher.groupCount() != 3) throw new RuntimeException("invalid pattern");

		final double x = Double.valueOf(matcher.group(1));
		final double y = Double.valueOf(matcher.group(2));
		final double z = Double.valueOf(matcher.group(3));
		return new Vector3d(x, y, z);
	}

	/** All vectors are returned with equal probability. */
	public static Vector3d randomDirection(final Random rand) {
		while (true) {
			Vector3d a = new Vector3d(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
			final double q = a.square();
			if (q >= 0.25) continue;
			a = a.div(Math.sqrt(q));
			if (a.isFinite()) return a;
		}
	}

	/** linear interpolation */
	public static Vector3d linear(final Vector3d a, final Vector3d b, final double t) {
		final double s = 1 - t;
		final double x = a.x * s + b.x * t;
		final double y = a.y * s + b.y * t;
		final double z = a.z * s + b.z * t;
		return new Vector3d(x, y, z);
	}

	/** cubic interpolation */
	public static Vector3d cubic(final Vector3d a, final Vector3d b, final Vector3d c, final Vector3d d, final double t) {
		final double s = 1 - t, w = t * s * 3;
		final double A = s * s * s, B = w * s, C = w * t, D = t * t * t;

		final double x = a.x * A + b.x * B + c.x * C + d.x * D;
		final double y = a.y * A + b.y * B + c.y * C + d.y * D;
		final double z = a.z * A + b.z * B + c.z * C + d.z * D;
		return new Vector3d(x, y, z);
	}

	/** (a + b) / 2 */
	public static Vector3d average(final Vector3d a, final Vector3d b) {
		return new Vector3d((a.x + b.x) / 2, (a.y + b.y) / 2, (a.z + b.z) / 2);
	}

	/** sum(array) / len(array) */
	public static Vector3d average(final Vector3d... w) {
		Vector3d v = Zero;
		for (final Vector3d a : w)
			v = v.add(a);
		return v.div(w.length);
	}

	/** sum(array) / len(array) */
	public static Vector3d average(final List<Vector3d> w) {
		Vector3d v = Zero;
		for (final Vector3d a : w)
			v = v.add(a);
		return v.div(w.size());
	}

	public static Vector3d sum(final List<Vector3d> w) {
		Vector3d v = Zero;
		for (final Vector3d a : w)
			v = v.add(a);
		return v;
	}

	// Addition
	/** this + v */
	public Vector3d add(final Vector3d v) {
		return new Vector3d(x + v.x, y + v.y, z + v.z);
	}

	/** this - v */
	public Vector3d sub(final Vector3d v) {
		return new Vector3d(x - v.x, y - v.y, z - v.z);
	}

	/** flips direction */
	public Vector3d neg() {
		return new Vector3d(-x, -y, -z);
	}

	// Addition with Multiplication
	/** this + a * v */
	public Vector3d add(final double a, final Vector3d v) {
		return new Vector3d(x + v.x * a, y + v.y * a, z + v.z * a);
	}

	/** this - a * v */
	public Vector3d sub(final double a, final Vector3d v) {
		return new Vector3d(x - v.x * a, y - v.y * a, z - v.z * a);
	}

	/** this + m * v */
	public Vector3d add(final Matrix3 m, final Vector3d v) {
		return new Vector3d(x + m.a.dot(v), y + m.b.dot(v), z + m.c.dot(v));
	}

	/** this - m * v */
	public Vector3d sub(final Matrix3 m, final Vector3d v) {
		return new Vector3d(x - m.a.dot(v), y - m.b.dot(v), z - m.c.dot(v));
	}

	/** this + transposed(v) * m */
	public Vector3d add(final Vector3d v, final Matrix3 m) {
		return new Vector3d(x + m.dotX(v), y + m.dotY(v), z + m.dotZ(v));
	}

	/** this - transposed(v) * m */
	public Vector3d sub(final Vector3d v, final Matrix3 m) {
		return new Vector3d(x - m.dotX(v), y - m.dotY(v), z - m.dotZ(v));
	}

	// Multiplication
	/** scale by a */
	public Vector3d mul(final double a) {
		return new Vector3d(x * a, y * a, z * a);
	}

	/** = Quaternion(0,this) * a */
	public Quaterniond mul(final Quaterniond a) {
		final double iw = -x * a.x - y * a.y - z * a.z;
		final double ix = +x * a.w + y * a.z - z * a.y;
		final double iy = -x * a.z + y * a.w + z * a.x;
		final double iz = +x * a.y - y * a.x + z * a.w;
		return new Quaterniond(iw, ix, iy, iz);
	}

	/** transposed(this) * a */
	public Matrix3 mul(final Vector3d a) {
		return new Matrix3(a.mul(x), a.mul(y), a.mul(z));
	}

	/** transposed(this) * m */
	public Vector3d mul(final Matrix3 m) {
		final double nx = x * m.a.x + y * m.b.x + z * m.c.x;
		final double ny = x * m.a.y + y * m.b.y + z * m.c.y;
		final double nz = x * m.a.z + y * m.b.z + z * m.c.z;
		return new Vector3d(nx, ny, nz);
	}

	// Division
	/** scale by 1/a */
	public Vector3d div(final double a) {
		return mul(1 / a);
	}

	/** scale by 1/ax, 1/ay, 1/az */
	public Vector3d div(final double ax, final double ay, final double az) {
		return new Vector3d(x / ax, y / ay, z / az);
	}

	/** unit length direction<br>
	 *  for zero length returns nan vector */
	public Vector3d unit() {
		return div(length());
	}

	/** unit length direction<br>
	 *  for zero vector returns Vector3.Zero */
	public Vector3d unitz() {
		return finite(unit());
	}

	public static Vector3d finite(final Vector3d u) {
		return u.isFinite() ? u : Vector3d.Zero;
	}

	/** limit length of this vector to max */
	public Vector3d limit(final double max) {
		final double q = square();
		return q <= max * max ? this : mul(max / Math.sqrt(q));
	}

	/** direction to a of unit length */
	public Vector3d direction(final Vector3d a) {
		final double dx = a.x - x, dy = a.y - y, dz = a.z - z;
		final double q = 1 / Math.sqrt(dx * dx + dy * dy + dz * dz);
		return new Vector3d(dx * q, dy * q, dz * q);
	}

	// Dot product
	/** dot product */
	public double dot(final Vector3d a) {
		return x * a.x + y * a.y + z * a.z;
	}

	public double dot(final Vector3d a, final Vector3d b) {
		return x * (a.x - b.x) + y * (a.y - b.y) + z * (a.z - b.z);
	}

	/** square of length */
	public double square() {
		return dot(this);
	}

	public double length() {
		return Math.sqrt(square());
	}

	/** squared distance to a */
	public double distanceSquared(final Vector3d a) {
		return (x - a.x) * (x - a.x) + (y - a.y) * (y - a.y) + (z - a.z) * (z - a.z);
	}

	/** distance to a */
	public double distance(final Vector3d a) {
		return Math.sqrt(distanceSquared(a));
	}

	// Unique
	/** mixed vector product <pre>this . (a x b)</pre> <pre>det |this / a / b|</pre> */
	public double mixed(final Vector3d a, final Vector3d b) {
		return x * (a.y * b.z - a.z * b.y) + y * (a.z * b.x - a.x * b.z) + z * (a.x * b.y - a.y * b.x);
	}

	/** Angle (0, PI) between this and a. */
	public double angle(final Vector3d a) {
		return Math.acos(dot(a) / Math.sqrt(square() + a.square()));
	}

	/** Angle (0, 2*PI) from a to b. */
	public double fullAngle(final Vector3d a, final Vector3d b) {
		return mixed(a, b) >= 0 ? a.angle(b) : 2 * Math.PI - a.angle(b);
	}

	/** cross product 3x3 matrix */
	public Matrix3 tilda() {
		return new Matrix3(0, -z, y, z, 0, -x, -y, x, 0);
	}

	public Matrix3 tildaSqr() {
		final double xx = -x * x, yy = -y * y, zz = -z * z;
		final double xy = x * y, yz = y * z, xz = x * z;
		return new Matrix3(yy + zz, xy, xz, xy, zz + xx, yz, xz, yz, xx + yy);
	}

	/** returns this x a<br>a x b = 0 <=> a and b are colinear */
	public Vector3d cross(final Vector3d a) {
		// same as tilda().mul(a);
		return new Vector3d(y * a.z - z * a.y, z * a.x - x * a.z, x * a.y - y * a.x);
	}

	/** cross product with matrix <pre>tilda(this) * m</pre> */
	public Matrix3 cross(final Matrix3 m) {
		return tilda().mul(m);
	}

	/** project vector into reference system (a, b, c) */
	public Vector3d project(final Vector3d a, final Vector3d b, final Vector3d c) {
		return new Vector3d(dot(a), dot(b), dot(c));
	}

	/** project vector into reference system (a, b) */
	public Vector2d project(final Vector3d a, final Vector3d b) {
		return new Vector2d(dot(a.project(a, b, a)), dot(b));
	}

	/** Generate a vector that is normal to this. */
	public Vector3d normal() {
		return z != 0 ? new Vector3d(0, z, -y) : new Vector3d(y, -x, 0);
	}

	// Non-Math
	/** if every component is finite number */
	public final boolean isFinite() {
		return GMath.isFinite(x) && GMath.isFinite(y) && GMath.isFinite(z);
	}

	public final int compareTo(final Vector3d a) {
		int c = Double.compare(x, a.x);
		if (c != 0) return c;
		c = Double.compare(y, a.y);
		return c != 0 ? c : Double.compare(z, a.z);
	}

	public final String toString(final String format) {
		return String.format(format, x, y, z);
	}

	public final boolean equals(final Vector3d a, final double ε) {
		return Math.abs(x - a.x) <= ε && Math.abs(y - a.y) <= ε && Math.abs(z - a.z) <= ε;
	}

	// From Object
	@Override public final String toString() {
		return toString(defaultFormat.get());
	}

	@Override public final boolean equals(final Object o) {
		if (o == this) return true;
		try {
			final Vector3d v = (Vector3d) o;
			return v.x == x && v.y == y && v.z == z;
		} catch (final ClassCastException e) {
			return false;
		}
	}

	// 953 683 983 607 941 607 839 769 967 859 919 863 911 601 881 733 1009 751 701 701 719 557 929 661 761 641 647 719 613 653
	@Override public final int hashCode() {
		int a = 929;
		final int b = 599;
		a = a * b + Hash.hash(x);
		a = a * b + Hash.hash(y);
		a = a * b + Hash.hash(z);
		return a;
	}
}