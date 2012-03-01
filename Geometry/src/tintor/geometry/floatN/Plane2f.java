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

import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Plane2f {
	public final Vector2 normal;
	public final float offset;

	// Constants
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f|%f)");

	// Factory Methods
	public static Plane2f bisection(final Vector2 a, final Vector2 b) {
		return pointAndNormal(Vector2.linear(a, b, 0.5f), a.direction(b));
	}

	/** angular bisection <br>
	  * plane contains intersection point of a and b <br>
	  * negative side of a and positive of b <br>
	  * negative side of b and positive of a <br>
	  * negative side of plane contains intersection of negative sides of a and b */
	public static Plane2f bisection(final Plane2f a, final Plane2f b) {
		return pointAndNormal(a.intersect(b), a.normal.add(b.normal).unit());
	}

	public static Plane2f pointAndDirection(final Vector2 a, final Vector2 d) {
		return pointAndNormal(a, d.unit());
	}

	public static Plane2f twoPoints(final Vector2 a, final Vector2 b) {
		final Vector2 n = new Vector2(b.y - a.y, a.x - b.x);
		return new Plane2f(n, -n.dot(a));
	}

	public static Plane2f pointAndNormal(final Vector2 a, final Vector2 n) {
		assert 0.9999 < n.square() && n.square() < 1.0001;
		return new Plane2f(n, -n.dot(a));
	}

	// Constructors
	public Plane2f(final Vector2 a, final Vector2 b) {
		normal = a.direction(b).right();
		offset = -normal.dot(a);
	}

	Plane2f(final Vector2 normal, final float offset) {
		this.normal = normal;
		this.offset = offset;
	}

	Plane2f(final float a, final float b, final float c) {
		normal = new Vector2(a, b);
		offset = c;
	}

	// BASIC OPERATIONS
	public float distance(final Vector2 a) {
		return normal.dot(a) + offset;
	}

	public Plane2f move(final float a) {
		return new Plane2f(normal, offset + a);
	}

	public Plane2f invert() {
		return new Plane2f(normal.neg(), -offset);
	}

	// ako se poklapaju vektor ce biti NaN
	// ako su paralelne vektor ce biti Infinite
	public Vector2 intersect(final Plane2f p) {
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
		return o instanceof Plane2f && equals((Plane2f) o);
	}

	public boolean equals(final Plane2f p) {
		return p == this || p.offset == offset && p.normal.equals(normal);
	}

	@Override public String toString() {
		return String.format("(%s %s %s)", normal.x, normal.y, offset);
	}
}