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

import tintor.geometry.GMath;
import tintor.util.Hash;
import tintor.util.SimpleThreadLocal;

public final class Plane3 {
	// Fields
	public final Vector3 normal;
	public final float offset;

	// Constants
	public static final ThreadLocal<String> defaultFormat = new SimpleThreadLocal<String>("(%f,%f,%f|%f)");

	// Constructors
	/** Note: All methods assume that |normal| = 1! */
	public Plane3(final Vector3 normal, final float offset) {
		this.normal = normal;
		this.offset = offset;
	}

	/** Note: All methods assume that |normal| = 1! */
	public Plane3(final float a, final float b, final float c, final float d) {
		this(new Vector3(a, b, c), d);
	}

	/** Note: All methods assume that |normal| = 1! */
	public Plane3(final Vector3 normal, final Vector3 point) {
		this(normal, -normal.dot(point));
	}

	public Plane3(final Vector3[] a) {
		this(a[0], a[1], a[2]);
		// TODO find best fitting plane
		//		for (int i = 3; i < a.length; i++)
		//			if (side(a[i]) != Side.Zero) throw new RuntimeException();
	}

	public Plane3(final Vector3 a, final Vector3 b, final Vector3 c) { // counter-clockwise
		this(b.sub(a).cross(c.sub(a)).unit(), a);
	}

	// Factory Methods
	/** Plane is bisection of line AB<br>
	/*  a is in positive, b is in negative, normal points from b to a */
	public static Plane3 bisection(final Vector3 a, final Vector3 b) {
		return new Plane3(b.sub(a).unit(), Vector3.average(a, b));
	}

	// Operations
	public float distance(final Vector3 a) {
		return normal.dot(a) + offset;
	}

	public Side side(final Vector3 a) {
		return Side.classify(distance(a));
	}

	public Plane3 move(final float a) {
		return new Plane3(normal, offset - a);
	}

	public Plane3 flip() {
		return new Plane3(normal.neg(), -offset);
	}

	// Static Methods
	public static Vector3 intersection(final Plane3 a, final Plane3 b, final Plane3 c) {
		final Vector3 ab = a.normal.cross(b.normal);
		final Vector3 bc = b.normal.cross(c.normal);
		final Vector3 ca = c.normal.cross(a.normal);
		return ab.mul(c.offset).add(a.offset, bc).add(b.offset, ca).div(-a.normal.dot(bc));
	}

	public static Ray3 intersection(final Plane3 a, final Plane3 b) {
		final Vector3 dir = a.normal.cross(b.normal);
		final Vector3 origin = GMath.solveLinearRow(dir, a.normal, b.normal, -a.offset, -b.offset);
		return origin.isFinite() ? new Ray3(origin, dir) : null;
	}

	// From Object
	@Override public String toString() {
		return String.format("(%f %f %f %f)", normal.x, normal.y, normal.z, offset);
	}

	@Override public int hashCode() {
		return Hash.hash(Plane3.class, normal.hashCode(), Hash.hash(offset));
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Plane3 && equals((Plane3) o);
	}

	public boolean equals(final Plane3 p) {
		return p == this || p.offset == offset && p.normal.equals(normal);
	}
}