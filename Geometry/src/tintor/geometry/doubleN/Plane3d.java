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

import tintor.geometry.GMath;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Plane3d {
	// Fields
	public final Vector3d normal;
	public final double offset;

	// Constants
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f,%f|%f)");

	// Constructors
	/** Note: All methods assume that |normal| = 1! */
	public Plane3d(final Vector3d normal, final double offset) {
		this.normal = normal;
		this.offset = offset;
	}

	/** Note: All methods assume that |normal| = 1! */
	public Plane3d(final double a, final double b, final double c, final double d) {
		this(new Vector3d(a, b, c), d);
	}

	/** Note: All methods assume that |normal| = 1! */
	public Plane3d(final Vector3d normal, final Vector3d point) {
		this(normal, -normal.dot(point));
	}

	public Plane3d(final Vector3d[] a) {
		this(a[0], a[1], a[2]);
		// TODO find best fitting plane
		//		for (int i = 3; i < a.length; i++)
		//			if (side(a[i]) != Side.Zero) throw new RuntimeException();
	}

	public Plane3d(final Vector3d a, final Vector3d b, final Vector3d c) { // counter-clockwise
		this(b.sub(a).cross(c.sub(a)).unit(), a);
	}

	// Factory Methods
	/** Plane is bisection of line AB<br>
	/*  a is in positive, b is in negative, normal points from b to a */
	public static Plane3d bisection(final Vector3d a, final Vector3d b) {
		return new Plane3d(b.sub(a).unit(), Vector3d.average(a, b));
	}

	// Operations
	public double distance(final Vector3d a) {
		return normal.dot(a) + offset;
	}

	public Sided side(final Vector3d a) {
		return Sided.classify(distance(a));
	}

	public Plane3d move(final double a) {
		return new Plane3d(normal, offset - a);
	}

	public Plane3d flip() {
		return new Plane3d(normal.neg(), -offset);
	}

	// Static Methods
	public static Vector3d intersection(final Plane3d a, final Plane3d b, final Plane3d c) {
		final Vector3d ab = a.normal.cross(b.normal);
		final Vector3d bc = b.normal.cross(c.normal);
		final Vector3d ca = c.normal.cross(a.normal);
		return ab.mul(c.offset).add(a.offset, bc).add(b.offset, ca).div(-a.normal.dot(bc));
	}

	public static Ray3d intersection(final Plane3d a, final Plane3d b) {
		final Vector3d dir = a.normal.cross(b.normal);
		final Vector3d origin = GMath.solveLinearRow(dir, a.normal, b.normal, -a.offset, -b.offset);
		return origin.isFinite() ? new Ray3d(origin, dir) : null;
	}

	// From Object
	@Override public String toString() {
		return String.format("(%f %f %f %f)", normal.x, normal.y, normal.z, offset);
	}

	@Override public int hashCode() {
		return Hash.hash(Plane3d.class, normal.hashCode(), Hash.hash(offset));
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Plane3d && equals((Plane3d) o);
	}

	public boolean equals(final Plane3d p) {
		return p == this || p.offset == offset && p.normal.equals(normal);
	}
}