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

public final class Ray3d {
	// Fields
	public final Vector3d origin, dir;

	// Constructors
	public Ray3d(final Vector3d origin, final Vector3d dir) {
		this.origin = origin;
		this.dir = dir.unit();
	}

	/** Returns point on the ray. */
	public Vector3d point(final double t) {
		return origin.add(t, dir);
	}

	// Factory Methods
	/** ASSUME |dir| = 1 AND |normal| = 1 */
	public static Ray3d reflect(final Vector3d dir, final Vector3d point, final Vector3d normal) {
		return new Ray3d(point, dir.sub(2 * normal.dot(dir), normal));
	}

	// With Plane
	/** Distance along the ray.<br>
	 *  Returns +-Inf or NaN if ray is parallel to plane. */
	public double distance(final Plane3d p) {
		return -p.distance(origin) / p.normal.dot(dir);
	}

	// With Point
	public double nearest(final Vector3d p) {
		return p.sub(origin).dot(dir);
	}

	public double distanceSquared(final Vector3d p) {
		return point(nearest(p)).distanceSquared(p);
	}

	public double distance(final Vector3d p) {
		return Math.sqrt(distanceSquared(p));
	}

	// With Ray
	/** Returns coordinates of closest points beetwen this and q.<BR>
	 *  Returns non-finite Vector2 if this.dir is colinear with q.dir. */
	public Vector2d closest(final Ray3d q) {
		final Vector3d r = origin.sub(q.origin);
		final double a = dir.dot(q.dir), b = dir.dot(r), c = q.dir.dot(r), d = 1 - a * a;
		return new Vector2d((a * c - b) / d, (a * b - c) / d);
	}

	public Vector2d nearest(final Ray3d q) {
		final Vector2d k = closest(q);
		return k.isFinite() ? k : new Vector2d(-origin.dot(dir), -q.origin.dot(q.dir));
	}

	public double distanceSquared(final Ray3d q) {
		final Vector2d c = nearest(q);
		return point(c.x).distanceSquared(point(c.y));
	}

	public double distance(final Ray3d q) {
		return Math.sqrt(distanceSquared(q));
	}

	public boolean equals(final Ray3d r) {
		return this == r ? true : origin.equals(r.origin) && dir.equals(r.dir);
	}

	// From Object
	@Override public final String toString() {
		return String.format("[%s %s]", origin, dir);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Ray3d && equals((Ray3d) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Ray3d.class.hashCode(), origin.hashCode(), dir.hashCode());
	}
}