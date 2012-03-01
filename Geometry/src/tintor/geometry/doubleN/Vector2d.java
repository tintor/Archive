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

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.geometry.GMath;
import tintor.util.Hash;

/**
 * Immutable coordinate in 2d space.
 */
public final class Vector2d implements Comparable<Vector2d> {
	// Constants
	/** (0, 0) vector */
	public final static Vector2d Zero = new Vector2d(0, 0);
	/** (1, 0) vector */
	public final static Vector2d X = new Vector2d(1, 0);
	/** (0, 1) vector */
	public final static Vector2d Y = new Vector2d(0, 1);

	// Fields
	public final double x, y;

	// Constructors
	public Vector2d(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	// Factory Methods
	/** Constructs vector from <pre>"decimal,( )?decimal"</pre>.<br/>
	 * @return Vector2 made from String
	 */
	public static Vector2d valueOf(final String str) {
		final Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException();

		final double x = Double.valueOf(matcher.group(1));
		final double y = Double.valueOf(matcher.group(3));
		return new Vector2d(x, y);
	}

	private static final Pattern pattern = Pattern.compile(String.format("%1$s, ?%1$s", "([+-]?\\d+(\\.\\d+)?)"));

	public static Vector2d polar(final double angle) {
		return new Vector2d(Math.cos(angle), Math.sin(angle));
	}

	public static Vector2d polar(final double angle, final double length) {
		return new Vector2d(length * Math.cos(angle), length * Math.sin(angle));
	}

	/** All vectors are returned with equal probability. */
	public static Vector2d randomDirection(final Random rand) {
		while (true) {
			Vector2d a = new Vector2d(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
			final double q = a.square();
			if (q >= 0.5 * 0.5) continue;
			a = a.div(Math.sqrt(q));
			if (a.isFinite()) return a;
		}
	}

	/** linear interpolation */
	public static Vector2d linear(final Vector2d a, final Vector2d b, final double t) {
		return new Vector2d(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
	}

	/** cubic interpolation */
	public static Vector2d cubic(final Vector2d a, final Vector2d b, final Vector2d c, final Vector2d d, final double t) {
		final double s = 1 - t, w = t * s * 3;
		final double A = s * s * s, B = w * s, C = w * t, D = t * t * t;

		final double x = a.x * A + b.x * B + c.x * C + d.x * D;
		final double y = a.y * A + b.y * B + c.y * C + d.y * D;
		return new Vector2d(x, y);
	}

	/** (a + b) / 2 */
	public static Vector2d average(final Vector2d a, final Vector2d b) {
		return new Vector2d((a.x + b.x) / 2, (a.y + b.y) / 2);
	}

	/** sum(array) / size(array) */
	public static Vector2d average(final Vector2d... array) {
		Vector2d v = Zero;
		for (final Vector2d a : array)
			v = v.add(a);
		return v.div(array.length);
	}

	// Addition
	public Vector2d add(final Vector2d a) {
		return new Vector2d(x + a.x, y + a.y);
	}

	public Vector2d sub(final Vector2d a) {
		return new Vector2d(x - a.x, y - a.y);
	}

	public Vector2d neg() {
		return new Vector2d(-x, -y);
	}

	public Vector2d add(final double b, final Vector2d a) {
		return new Vector2d(x + a.x * b, y + a.y * b);
	}

	public Vector2d sub(final double b, final Vector2d a) {
		return new Vector2d(x - a.x * b, y - a.y * b);
	}

	// Multiplication
	public Vector2d mul(final double a) {
		return new Vector2d(x * a, y * a);
	}

	/** transposed(this) * a */
	public Matrix2d mul(final Vector2d a) {
		return new Matrix2d(a.mul(x), a.mul(y));
	}

	/** transposed(this) * m */
	public Vector2d mul(final Matrix2d m) {
		if (m == Matrix2d.Identity) return this;
		return new Vector2d(x * m.a.x + y * m.b.x, x * m.a.y + y * m.b.y);
	}

	public Vector2d div(final double a) {
		return new Vector2d(x / a, y / a);
	}

	// cosinus angle beetwen a-this and b-this
	public double cosa(final Vector2d a, final Vector2d b) {
		final double ax = a.x - x, ay = a.y - y;
		final double bx = b.x - x, by = b.y - y;
		return (ax * bx + ay * by) / Math.sqrt((ax * ax + ay * ay) * (bx * bx + by * by));
	}

	// Magnitude
	public final double dot(final Vector2d a) {
		return x * a.x + y * a.y;
	}

	public double square() {
		return dot(this);
	}

	public double length() {
		return Math.sqrt(square());
	}

	public Vector2d unit() {
		return div(length());
	}

	public Vector2d unitz() {
		final Vector2d u = unit();
		return u.isFinite() ? u : Zero;
	}

	/** squared distance to a */
	public double square(final Vector2d a) {
		return square(x - a.x) + square(y - a.y);
	}

	/** distance to a */
	public double distance(final Vector2d a) {
		return Math.sqrt(square(a));
	}

	/** direction to a of unit length */
	public Vector2d direction(final Vector2d a) {
		final double dx = a.x - x, dy = a.y - y;
		final double q = Math.sqrt(dx * dx + dy * dy);
		return new Vector2d(dx / q, dy / q);
	}

	// Unique
	public Vector2d rotate(final double angle, final Vector2d center) {
		return sub(center).rotate(angle).add(center);
	}

	public Vector2d rotate(final double angle) {
		return rotate(Math.cos(angle), Math.sin(angle));
	}

	public Vector2d rotate(final double cosa, final double sina) {
		return new Vector2d(x * cosa - y * sina, x * sina + y * cosa);
	}

	public double angle() {
		return Math.atan2(y, x);
	}

	public double angle(final Vector2d a) {
		return Math.atan2(a.y - y, a.x - x);
	}

	/** left.angle = this.angle + 90<br>
	 * left is perpendicular to this */
	public Vector2d left() {
		return new Vector2d(-y, x);
	}

	/** right.angle = this.angle - 90<br>
	 * right is perpendicular to this */
	public Vector2d right() {
		return new Vector2d(y, -x);
	}

	/** positive side is (b-a).right<br>
	 * 0 means that points are collinear */
	public double side(final Vector2d a, final Vector2d b) {
		return (y - a.y) * (x - b.x) - (x - a.x) * (y - b.y);
	}

	/** Determinant of 2x2 matrix
	 * @return x*a.y - y*a.x
	 */
	public double det(final Vector2d a) {
		return x * a.y - y * a.x;
	}

	/** Projects vector onto coordinate axes i and j
	 * @return i*r.x + j*r.y
	 * */
	public Vector2d project(final Vector2d i, final Vector2d j) {
		final double q = i.det(j);
		return new Vector2d(i.det(this) / q, j.det(this) / q);
	}

	/** if every component is finite number */
	public final boolean isFinite() {
		return GMath.isFinite(x) && GMath.isFinite(y);
	}

	/** lexicographical comparison: first x, then y */
	public final int compareTo(final Vector2d a) {
		final int c = Double.compare(x, a.x);
		return c != 0 ? c : Double.compare(y, a.y);
	}

	public final String toString(final String format) {
		return String.format(format, x, y);
	}

	public final boolean equals(final Vector2d a, final double e) {
		return square(x - a.x) + square(y - a.y) <= square(e);
	}

	private static double square(final double a) {
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
			final Vector2d v = (Vector2d) o;
			return v.x == x && v.y == y;
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public final int hashCode() {
		return Hash.hash(Vector2d.class, Hash.hash(x), Hash.hash(y));
	}
}