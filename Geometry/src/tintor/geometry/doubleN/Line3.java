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

public final class Line3 {
	// Fields
	public final Vector3d a, b;

	// Constructors
	public Line3(final Vector3d a, final Vector3d b) {
		this.a = a;
		this.b = b;
	}

	// Operations
	public Vector3d point(final double t) {
		return Vector3d.linear(a, b, t);
	}

	// With Plane
	/** Distance along the line, line is assumed infinite. */
	public double distance(final Plane3d p) {
		return -p.distance(a) / p.normal.dot(b.sub(a));
	}

	// With Point
	public static double distanceSquared(final Vector3d a, final Vector3d b, final Vector3d p) {
		final Vector3d d = b.sub(a);
		final double n = d.dot(p.sub(a)) / d.square();
		return (n >= 1 ? b : n <= 0 ? a : a.add(n, d)).distanceSquared(p);
	}

	public static double distanceSquaredInf(final Vector3d a, final Vector3d b, final Vector3d p) {
		final Vector3d d = b.sub(a);
		final double n = d.dot(p.sub(a)) / d.square();
		return a.add(n, d).distanceSquared(p);
	}

	public double nearestInf(final Vector3d p) {
		final Vector3d d = b.sub(a);
		return d.dot(p.sub(a)) / d.square();
	}

	public double nearest(final Vector3d p) {
		return clamp(nearestInf(p));
	}

	public double distanceSquaredInf(final Vector3d p) {
		return distanceSquaredInf(a, b, p);
	}

	public double distanceSquared(final Vector3d p) {
		return distanceSquared(a, b, p);
	}

	public double distanceInf(final Vector3d p) {
		return Math.sqrt(distanceSquaredInf(p));
	}

	public double distance(final Vector3d p) {
		return Math.sqrt(distanceSquared(p));
	}

	// With Line
	public Vector2d closestPairInfFast(final Line3 q) {
		final Vector3d A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final double aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		final double det = aa * bb - ab * ab;
		return new Vector2d((ab * bc - bb * ac) / det, (aa * bc - ab * ac) / det);
	}

	public Vector2d closestPairInf(final Line3 q) {
		final Vector2d k = closestPairInfFast(q);
		return k.isFinite() ? k : new Vector2d(nearestInf(Vector3d.Zero), q.nearestInf(Vector3d.Zero));
	}

	public Vector2d closestPair(final Line3 q) {
		final Vector2d k = closestPairInfFast(q);
		if (k.isFinite()) return new Vector2d(clamp(k.x), clamp(k.y));
		final double pa = nearest(q.a), pb = nearest(q.b);
		return point(pa).distanceSquared(q.a) < point(pb).distanceSquared(q.b) ? new Vector2d(pa, 0) : new Vector2d(pb, 1);
	}

	public double distanceSquaredInf(final Line3 q) {
		final Vector2d k = closestPairInf(q);
		return point(k.x).distanceSquared(q.point(k.y));
	}

	public double distanceSquared(final Line3 q) {
		final Vector2d k = closestPairInfFast(q);
		if (k.isFinite()) return point(clamp(k.x)).distanceSquared(q.point(clamp(k.y)));
		return Math.min(distanceSquared(q.a), distanceSquared(q.b));
	}

	public double distanceInf(final Line3 q) {
		return Math.sqrt(distanceSquaredInf(q));
	}

	public double distance(final Line3 q) {
		return Math.sqrt(distanceSquared(q));
	}

	public double closestPointInfFast(final Line3 q) {
		final Vector3d A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final double aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		return (ab * bc - bb * ac) / (aa * bb - ab * ab);
	}

	public double closestPointInf(final Line3 q) {
		final double x = closestPointInfFast(q);
		return GMath.isFinite(x) ? x : 0; // if lines are parallel every point is nearest!
	}

	public double closestPoint(final Line3 q) {
		final double x = closestPointInfFast(q);
		return GMath.isFinite(x) ? clamp(x) : q.distanceSquared(a) < q.distanceSquared(b) ? 1 : 0;
	}

	private static double clamp(final double a) {
		if (a < 0) return 0;
		if (a > 1) return 1;
		return a;
	}

	// Misc
	public Vector3d direction() {
		return b.sub(a);
	}

	/** Returns part of line in negative side of plane */
	public Line3 clip(final Plane3d p) {
		final double da = p.distance(a), db = p.distance(b);
		final Sided sb = Sided.classify(db);

		switch (Sided.classify(da)) {
		case Positive:
			switch (sb) {
			case Positive:
				return null;
			case Zero:
				return new Line3(b, b);
			case Negative:
				return new Line3(Vector3d.linear(a, b, da / (da - db)), b);
			}
			break;
		case Zero:
			return sb == Sided.Positive ? new Line3(a, a) : this;
		case Negative:
			return sb == Sided.Positive ? new Line3(a, Vector3d.linear(a, b, da / (da - db))) : this;
		}
		throw new RuntimeException();
	}

	public boolean equals(final Line3 o) {
		return this == o ? true : a.equals(o.a) && b.equals(o.b);
	}

	// From Object
	@Override public final String toString() {
		return String.format("[%s %s]", a, b);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Line3 && equals((Line3) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Line3.class.hashCode(), a.hashCode(), b.hashCode());
	}
}