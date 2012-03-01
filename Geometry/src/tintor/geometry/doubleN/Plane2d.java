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

import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Plane2d {
	public final Vector2d normal;
	public final double offset;

	// Constants
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f|%f)");

	// Factory Methods
	public static Plane2d bisection(Vector2d a, Vector2d b) {
		return pointAndNormal(Vector2d.linear(a, b, 0.5), a.direction(b));
	}

	/** angular bisection <br>
	  * plane contains intersection point of a and b <br>
	  * negative side of a and positive of b <br>
	  * negative side of b and positive of a <br>
	  * negative side of plane contains intersection of negative sides of a and b */
	public static Plane2d bisection(Plane2d a, Plane2d b) {
		return pointAndNormal(a.intersect(b), (a.normal.add(b.normal)).unit());
	}

	public static Plane2d pointAndDirection(Vector2d a, Vector2d d) {
		return pointAndNormal(a, d.unit());
	}

	public static Plane2d twoPoints(Vector2d a, Vector2d b) {
		Vector2d n = new Vector2d(b.y - a.y, a.x - b.x);
		return new Plane2d(n, -n.dot(a));
	}

	public static Plane2d pointAndNormal(Vector2d a, Vector2d n) {
		assert 0.9999 < n.square() && n.square() < 1.0001;
		return new Plane2d(n, -n.dot(a));
	}

	// Constructors
	public Plane2d(Vector2d a, Vector2d b) {
		this.normal = a.direction(b).right();
		this.offset = -normal.dot(a);
	}

	Plane2d(Vector2d normal, double offset) {
		this.normal = normal;
		this.offset = offset;
	}

	Plane2d(double a, double b, double c) {
		this.normal = new Vector2d(a, b);
		this.offset = c;
	}

	// BASIC OPERATIONS
	public double distance(Vector2d a) {
		return normal.dot(a) + offset;
	}

	public Plane2d move(double a) {
		return new Plane2d(normal, offset + a);
	}

	public Plane2d invert() {
		return new Plane2d(normal.neg(), -offset);
	}

	// ako se poklapaju vektor ce biti NaN
	// ako su paralelne vektor ce biti Infinite
	public Vector2d intersect(Plane2d p) {
		double q = normal.det(p.normal);
		return new Vector2d((offset * p.normal.y - p.offset * normal.y) / q, (offset * p.normal.x - p.offset
				* normal.x)
				/ q);
	}

	// intersect plane with line AB
	// returns T such that instersection point is AT + B(1-T)
	// returns NaN if both points are on plane
	// returns +Infinity if line is parallel above plane
	// returns -Infinity if line is parallel below plane
	public double intersect(Vector2d a, Vector2d b) {
		double bn = b.dot(normal);
		return (bn + offset) / (bn - a.dot(normal));
	}

	@Override
	public int hashCode() {
		return Hash.hash(normal.hashCode(), Hash.hash(offset));
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Plane2d && equals((Plane2d) o);
	}

	public boolean equals(Plane2d p) {
		return p == this || (p.offset == offset && p.normal.equals(normal));
	}

	@Override
	public String toString() {
		return String.format("(%s %s %s)", normal.x, normal.y, offset);
	}
}