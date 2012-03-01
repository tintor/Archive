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

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.geometry.GMath;
import tintor.util.Hash;

/**
 * Immutable coordinate in 2d space.
 */
public final class Vector2 implements Comparable<Vector2> {
	// Constants
	/** (0, 0) vector */
	public final static Vector2 Zero = new Vector2(0, 0);
	/** (1, 0) vector */
	public final static Vector2 X = new Vector2(1, 0);
	/** (0, 1) vector */
	public final static Vector2 Y = new Vector2(0, 1);

	// Fields
	public final float x, y;

	// Constructors
	public Vector2(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	// Factory Methods
	/** Constructs vector from <pre>"decimal,( )?decimal"</pre>.<br/>
	 * @return Vector2 made from String
	 */
	public static Vector2 valueOf(final String str) {
		final Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException();

		final float x = Float.valueOf(matcher.group(1));
		final float y = Float.valueOf(matcher.group(3));
		return new Vector2(x, y);
	}

	private static final Pattern pattern = Pattern.compile(String.format("%1$s, ?%1$s", "([+-]?\\d+(\\.\\d+)?)"));

	public static Vector2 polar(final float angle) {
		return new Vector2(GMath.cos(angle), GMath.sin(angle));
	}

	public static Vector2 polar(final float angle, final float length) {
		return new Vector2(length * GMath.cos(angle), length * GMath.sin(angle));
	}

	/** All vectors are returned with equal probability. */
	public static Vector2 randomDirection(final Random rand) {
		while (true) {
			Vector2 a = new Vector2(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
			final float q = a.square();
			if (q >= 0.5 * 0.5) continue;
			a = a.div(GMath.sqrt(q));
			if (a.isFinite()) return a;
		}
	}

	/** linear interpolation */
	public static Vector2 linear(final Vector2 a, final Vector2 b, final float t) {
		return new Vector2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
	}

	/** cubic interpolation */
	public static Vector2 cubic(final Vector2 a, final Vector2 b, final Vector2 c, final Vector2 d, final float t) {
		final float s = 1 - t, w = t * s * 3;
		final float A = s * s * s, B = w * s, C = w * t, D = t * t * t;

		final float x = a.x * A + b.x * B + c.x * C + d.x * D;
		final float y = a.y * A + b.y * B + c.y * C + d.y * D;
		return new Vector2(x, y);
	}

	/** (a + b) / 2 */
	public static Vector2 average(final Vector2 a, final Vector2 b) {
		return new Vector2((a.x + b.x) / 2, (a.y + b.y) / 2);
	}

	/** sum(array) / size(array) */
	public static Vector2 average(final Vector2... array) {
		Vector2 v = Zero;
		for (final Vector2 a : array)
			v = v.add(a);
		return v.div(array.length);
	}

	// Addition
	public Vector2 add(final Vector2 a) {
		return new Vector2(x + a.x, y + a.y);
	}

	public Vector2 sub(final Vector2 a) {
		return new Vector2(x - a.x, y - a.y);
	}

	public Vector2 neg() {
		return new Vector2(-x, -y);
	}

	public Vector2 add(final float b, final Vector2 a) {
		return new Vector2(x + a.x * b, y + a.y * b);
	}

	public Vector2 sub(final float b, final Vector2 a) {
		return new Vector2(x - a.x * b, y - a.y * b);
	}

	// Multiplication
	public Vector2 mul(final float a) {
		return new Vector2(x * a, y * a);
	}

	/** transposed(this) * a */
	public Matrix2f mul(final Vector2 a) {
		return new Matrix2f(a.mul(x), a.mul(y));
	}

	/** transposed(this) * m */
	public Vector2 mul(final Matrix2f m) {
		if (m == Matrix2f.Identity) return this;
		return new Vector2(x * m.a.x + y * m.b.x, x * m.a.y + y * m.b.y);
	}

	public Vector2 div(final float a) {
		return new Vector2(x / a, y / a);
	}

	// cosinus angle beetwen a-this and b-this
	public float cosa(final Vector2 a, final Vector2 b) {
		final float ax = a.x - x, ay = a.y - y;
		final float bx = b.x - x, by = b.y - y;
		return (ax * bx + ay * by) / GMath.sqrt((ax * ax + ay * ay) * (bx * bx + by * by));
	}

	// Magnitude
	public final float dot(final Vector2 a) {
		return x * a.x + y * a.y;
	}

	public float square() {
		return dot(this);
	}

	public float length() {
		return GMath.sqrt(square());
	}

	public Vector2 unit() {
		return div(length());
	}

	public Vector2 unitz() {
		final Vector2 u = unit();
		return u.isFinite() ? u : Zero;
	}

	/** squared distance to a */
	public float square(final Vector2 a) {
		return square(x - a.x) + square(y - a.y);
	}

	/** distance to a */
	public float distance(final Vector2 a) {
		return GMath.sqrt(square(a));
	}

	/** direction to a of unit length */
	public Vector2 direction(final Vector2 a) {
		final float dx = a.x - x, dy = a.y - y;
		final float q = GMath.sqrt(dx * dx + dy * dy);
		return new Vector2(dx / q, dy / q);
	}

	// Unique
	public Vector2 rotate(final float angle, final Vector2 center) {
		return sub(center).rotate(angle).add(center);
	}

	public Vector2 rotate(final float angle) {
		return rotate(GMath.cos(angle), GMath.sin(angle));
	}

	public Vector2 rotate(final float cosa, final float sina) {
		return new Vector2(x * cosa - y * sina, x * sina + y * cosa);
	}

	public float angle() {
		return GMath.atan2(y, x);
	}

	public float angle(final Vector2 a) {
		return GMath.atan2(a.y - y, a.x - x);
	}

	/** left.angle = this.angle + 90<br>
	 * left is perpendicular to this */
	public Vector2 left() {
		return new Vector2(-y, x);
	}

	/** right.angle = this.angle - 90<br>
	 * right is perpendicular to this */
	public Vector2 right() {
		return new Vector2(y, -x);
	}

	/** positive side is (b-a).right<br>
	 * 0 means that points are collinear */
	public float side(final Vector2 a, final Vector2 b) {
		return (y - a.y) * (x - b.x) - (x - a.x) * (y - b.y);
	}

	/** Determinant of 2x2 matrix
	 * @return x*a.y - y*a.x
	 */
	public float det(final Vector2 a) {
		return x * a.y - y * a.x;
	}

	/** Projects vector onto coordinate axes i and j
	 * @return i*r.x + j*r.y
	 * */
	public Vector2 project(final Vector2 i, final Vector2 j) {
		final float q = i.det(j);
		return new Vector2(i.det(this) / q, j.det(this) / q);
	}

	/** if every component is finite number */
	public final boolean isFinite() {
		return GMath.isFinite(x) && GMath.isFinite(y);
	}

	/** lexicographical comparison: first x, then y */
	public final int compareTo(final Vector2 a) {
		final int c = Float.compare(x, a.x);
		return c != 0 ? c : Float.compare(y, a.y);
	}

	public final String toString(final String format) {
		return String.format(format, x, y);
	}

	public final boolean equals(final Vector2 a, final float e) {
		return square(x - a.x) + square(y - a.y) <= square(e);
	}

	private static float square(final float a) {
		return a * a;
	}

	public static final ThreadLocal<String> defaultFormat = new ThreadLocal<String>() {
		@Override protected String initialValue() {
			return "(%s, %s)";
		}
	};

	// From Object
	@Override public final String toString() {
		return toString(defaultFormat.get());
	}

	@Override public final boolean equals(final Object o) {
		if (o == this) return true;
		try {
			final Vector2 v = (Vector2) o;
			return v.x == x && v.y == y;
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public final int hashCode() {
		return Hash.hash(Vector2.class, Hash.hash(x), Hash.hash(y));
	}
}