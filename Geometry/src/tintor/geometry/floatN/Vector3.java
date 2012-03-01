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
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.geometry.GMath;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Vector3 implements Comparable<Vector3>, Serializable {
	// Constants
	public final static Vector3 Zero = new Vector3(0, 0, 0);
	public final static Vector3 X = new Vector3(1, 0, 0);
	public final static Vector3 Y = new Vector3(0, 1, 0);
	public final static Vector3 Z = new Vector3(0, 0, 1);
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%s,%s,%s)");

	// Fields
	public final float x, y, z;

	// Constructors
	public Vector3(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Static Methods
	/** Constructs vector from String using defaultFormat. */
	public static Vector3 valueOf(final String str) {
		return valueOf(str, defaultFormat.get());
	}

	public static Vector3 valueOf(final String str, final String pattern) {
		final String decimal = "([+-]?\\d+(?:\\.\\d+)?)";
		return valueOf(str, Pattern.compile(pattern.replaceAll("[()]", "\\\\$0").replace("%s", decimal)));
	}

	public static Vector3 valueOf(final String str, final Pattern pattern) {
		final Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException(pattern.pattern());
		if (matcher.groupCount() != 3) throw new RuntimeException("invalid pattern");

		final float x = Float.valueOf(matcher.group(1));
		final float y = Float.valueOf(matcher.group(2));
		final float z = Float.valueOf(matcher.group(3));
		return new Vector3(x, y, z);
	}

	/** All vectors are returned with equal probability. */
	public static Vector3 randomDirection(final Random rand) {
		while (true) {
			Vector3 a = new Vector3(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
			final float q = a.square();
			if (q >= 0.25) continue;
			a = a.div(GMath.sqrt(q));
			if (a.isFinite()) return a;
		}
	}

	/** linear interpolation */
	public static Vector3 linear(final Vector3 a, final Vector3 b, final float t) {
		final float s = 1 - t;
		final float x = a.x * s + b.x * t;
		final float y = a.y * s + b.y * t;
		final float z = a.z * s + b.z * t;
		return new Vector3(x, y, z);
	}

	/** cubic interpolation */
	public static Vector3 cubic(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d, final float t) {
		final float s = 1 - t, w = t * s * 3;
		final float A = s * s * s, B = w * s, C = w * t, D = t * t * t;

		final float x = a.x * A + b.x * B + c.x * C + d.x * D;
		final float y = a.y * A + b.y * B + c.y * C + d.y * D;
		final float z = a.z * A + b.z * B + c.z * C + d.z * D;
		return new Vector3(x, y, z);
	}

	/** (a + b) / 2 */
	public static Vector3 average(final Vector3 a, final Vector3 b) {
		return new Vector3((a.x + b.x) / 2, (a.y + b.y) / 2, (a.z + b.z) / 2);
	}

	/** sum(array) / len(array) */
	public static Vector3 average(final Vector3... w) {
		Vector3 v = Zero;
		for (final Vector3 a : w)
			v = v.add(a);
		return v.div(w.length);
	}

	/** sum(array) / len(array) */
	public static Vector3 average(final List<Vector3> w) {
		Vector3 v = Zero;
		for (final Vector3 a : w)
			v = v.add(a);
		return v.div(w.size());
	}

	public static Vector3 sum(final List<Vector3> w) {
		Vector3 v = Zero;
		for (final Vector3 a : w)
			v = v.add(a);
		return v;
	}

	// Addition
	/** this + v */
	public Vector3 add(final Vector3 v) {
		return new Vector3(x + v.x, y + v.y, z + v.z);
	}

	/** this - v */
	public Vector3 sub(final Vector3 v) {
		return new Vector3(x - v.x, y - v.y, z - v.z);
	}

	/** flips direction */
	public Vector3 neg() {
		return new Vector3(-x, -y, -z);
	}

	// Addition with Multiplication
	/** this + a * v */
	public Vector3 add(final float a, final Vector3 v) {
		return new Vector3(x + v.x * a, y + v.y * a, z + v.z * a);
	}

	/** this - a * v */
	public Vector3 sub(final float a, final Vector3 v) {
		return new Vector3(x - v.x * a, y - v.y * a, z - v.z * a);
	}

	/** this + m * v */
	public Vector3 add(final Matrix3 m, final Vector3 v) {
		return new Vector3(x + m.a.dot(v), y + m.b.dot(v), z + m.c.dot(v));
	}

	/** this - m * v */
	public Vector3 sub(final Matrix3 m, final Vector3 v) {
		return new Vector3(x - m.a.dot(v), y - m.b.dot(v), z - m.c.dot(v));
	}

	/** this + transposed(v) * m */
	public Vector3 add(final Vector3 v, final Matrix3 m) {
		return new Vector3(x + m.dotX(v), y + m.dotY(v), z + m.dotZ(v));
	}

	/** this - transposed(v) * m */
	public Vector3 sub(final Vector3 v, final Matrix3 m) {
		return new Vector3(x - m.dotX(v), y - m.dotY(v), z - m.dotZ(v));
	}

	// Multiplication
	/** scale by a */
	public Vector3 mul(final float a) {
		return new Vector3(x * a, y * a, z * a);
	}

	/** = Quaternion(0,this) * a */
	public Quaternion mul(final Quaternion a) {
		final float iw = -x * a.x - y * a.y - z * a.z;
		final float ix = +x * a.w + y * a.z - z * a.y;
		final float iy = -x * a.z + y * a.w + z * a.x;
		final float iz = +x * a.y - y * a.x + z * a.w;
		return new Quaternion(iw, ix, iy, iz);
	}

	/** transposed(this) * a */
	public Matrix3 mul(final Vector3 a) {
		return new Matrix3(a.mul(x), a.mul(y), a.mul(z));
	}

	/** transposed(this) * m */
	public Vector3 mul(final Matrix3 m) {
		final float nx = x * m.a.x + y * m.b.x + z * m.c.x;
		final float ny = x * m.a.y + y * m.b.y + z * m.c.y;
		final float nz = x * m.a.z + y * m.b.z + z * m.c.z;
		return new Vector3(nx, ny, nz);
	}

	// Division
	/** scale by 1/a */
	public Vector3 div(final float a) {
		return mul(1 / a);
	}

	/** scale by 1/ax, 1/ay, 1/az */
	public Vector3 div(final float ax, final float ay, final float az) {
		return new Vector3(x / ax, y / ay, z / az);
	}

	/** unit length direction<br>
	 *  for zero length returns nan vector */
	public Vector3 unit() {
		return div(length());
	}

	/** unit length direction<br>
	 *  for zero vector returns Vector3.Zero */
	public Vector3 unitz() {
		return finite(unit());
	}

	public static Vector3 finite(final Vector3 u) {
		return u.isFinite() ? u : Vector3.Zero;
	}

	/** limit length of this vector to max */
	public Vector3 limit(final float max) {
		final float q = square();
		return q <= max * max ? this : mul(max / GMath.sqrt(q));
	}

	/** direction to a of unit length */
	public Vector3 direction(final Vector3 a) {
		final float dx = a.x - x, dy = a.y - y, dz = a.z - z;
		final float q = 1 / GMath.sqrt(dx * dx + dy * dy + dz * dz);
		return new Vector3(dx * q, dy * q, dz * q);
	}

	// Dot product
	/** dot product */
	public float dot(final Vector3 a) {
		return x * a.x + y * a.y + z * a.z;
	}

	public float dot(final Vector3 a, final Vector3 b) {
		return x * (a.x - b.x) + y * (a.y - b.y) + z * (a.z - b.z);
	}

	/** square of length */
	public float square() {
		return dot(this);
	}

	public float length() {
		return GMath.sqrt(square());
	}

	/** squared distance to a */
	public float distanceSquared(final Vector3 a) {
		return (x - a.x) * (x - a.x) + (y - a.y) * (y - a.y) + (z - a.z) * (z - a.z);
	}

	/** distance to a */
	public float distance(final Vector3 a) {
		return GMath.sqrt(distanceSquared(a));
	}

	// Unique
	/** mixed vector product <pre>this . (a x b)</pre> <pre>det |this / a / b|</pre> */
	public float mixed(final Vector3 a, final Vector3 b) {
		return x * (a.y * b.z - a.z * b.y) + y * (a.z * b.x - a.x * b.z) + z * (a.x * b.y - a.y * b.x);
	}

	/** Angle (0, PI) between this and a. */
	public float angle(final Vector3 a) {
		return GMath.acos(dot(a) / GMath.sqrt(square() + a.square()));
	}

	/** Angle (0, 2*PI) from a to b. */
	public float fullAngle(final Vector3 a, final Vector3 b) {
		return mixed(a, b) >= 0 ? a.angle(b) : (float) (2 * Math.PI) - a.angle(b);
	}

	/** cross product 3x3 matrix */
	public Matrix3 tilda() {
		return new Matrix3(0, -z, y, z, 0, -x, -y, x, 0);
	}

	public Matrix3 tildaSqr() {
		final float xx = -x * x, yy = -y * y, zz = -z * z;
		final float xy = x * y, yz = y * z, xz = x * z;
		return new Matrix3(yy + zz, xy, xz, xy, zz + xx, yz, xz, yz, xx + yy);
	}

	/** returns this x a<br>a x b = 0 <=> a and b are colinear */
	public Vector3 cross(final Vector3 a) {
		// same as tilda().mul(a);
		return new Vector3(y * a.z - z * a.y, z * a.x - x * a.z, x * a.y - y * a.x);
	}

	/** cross product with matrix <pre>tilda(this) * m</pre> */
	public Matrix3 cross(final Matrix3 m) {
		return tilda().mul(m);
	}

	/** project vector into reference system (a, b, c) */
	public Vector3 project(final Vector3 a, final Vector3 b, final Vector3 c) {
		return new Vector3(dot(a), dot(b), dot(c));
	}

	//	/** project vector into reference system (a, b) */
	//	public Vector2 project(final Vector3 a, final Vector3 b) {
	//		return new Vector2(dot(a.project(a, b, a)), dot(b));
	//	}

	/** Generate a vector that is normal to this. */
	public Vector3 normal() {
		return z != 0 ? new Vector3(0, z, -y) : new Vector3(y, -x, 0);
	}

	// Non-Math
	/** if every component is finite number */
	public final boolean isFinite() {
		return GMath.isFinite(x) && GMath.isFinite(y) && GMath.isFinite(z);
	}

	public final int compareTo(final Vector3 a) {
		int c = Float.compare(x, a.x);
		if (c != 0) return c;
		c = Float.compare(y, a.y);
		return c != 0 ? c : Float.compare(z, a.z);
	}

	public final String toString(final String format) {
		return String.format(format, x, y, z);
	}

	public final boolean equals(final Vector3 a, final float ε) {
		return Math.abs(x - a.x) <= ε && Math.abs(y - a.y) <= ε && Math.abs(z - a.z) <= ε;
	}

	// From Object
	@Override public final String toString() {
		return toString(defaultFormat.get());
	}

	@Override public final boolean equals(final Object o) {
		if (o == this) return true;
		try {
			final Vector3 v = (Vector3) o;
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