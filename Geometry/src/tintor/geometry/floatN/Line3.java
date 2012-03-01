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

public final class Line3 {
	// Fields
	public final Vector3 a, b;

	// Constructors
	public Line3(final Vector3 a, final Vector3 b) {
		this.a = a;
		this.b = b;
	}

	// Operations
	public Vector3 point(final float t) {
		return Vector3.linear(a, b, t);
	}

	// With Plane
	/** Distance along the line, line is assumed infinite. */
	public float distance(final Plane3 p) {
		return -p.distance(a) / p.normal.dot(b.sub(a));
	}

	// With Point
	public static float distanceSquared(final Vector3 a, final Vector3 b, final Vector3 p) {
		final Vector3 d = b.sub(a);
		final float n = d.dot(p.sub(a)) / d.square();
		return (n >= 1 ? b : n <= 0 ? a : a.add(n, d)).distanceSquared(p);
	}

	public static float distanceSquaredInf(final Vector3 a, final Vector3 b, final Vector3 p) {
		final Vector3 d = b.sub(a);
		final float n = d.dot(p.sub(a)) / d.square();
		return a.add(n, d).distanceSquared(p);
	}

	public float nearestInf(final Vector3 p) {
		final Vector3 d = b.sub(a);
		return d.dot(p.sub(a)) / d.square();
	}

	public float nearest(final Vector3 p) {
		return clamp(nearestInf(p));
	}

	public float distanceSquaredInf(final Vector3 p) {
		return distanceSquaredInf(a, b, p);
	}

	public float distanceSquared(final Vector3 p) {
		return distanceSquared(a, b, p);
	}

	public float distanceInf(final Vector3 p) {
		return GMath.sqrt(distanceSquaredInf(p));
	}

	public float distance(final Vector3 p) {
		return GMath.sqrt(distanceSquared(p));
	}

	// With Line
	public Vector2 closestPairInfFast(final Line3 q) {
		final Vector3 A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final float aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		final float det = aa * bb - ab * ab;
		return new Vector2((ab * bc - bb * ac) / det, (aa * bc - ab * ac) / det);
	}

	public Vector2 closestPairInf(final Line3 q) {
		final Vector2 k = closestPairInfFast(q);
		return k.isFinite() ? k : new Vector2(nearestInf(Vector3.Zero), q.nearestInf(Vector3.Zero));
	}

	public Vector2 closestPair(final Line3 q) {
		final Vector2 k = closestPairInfFast(q);
		if (k.isFinite()) return new Vector2(clamp(k.x), clamp(k.y));
		final float pa = nearest(q.a), pb = nearest(q.b);
		return point(pa).distanceSquared(q.a) < point(pb).distanceSquared(q.b) ? new Vector2(pa, 0) : new Vector2(pb, 1);
	}

	public float distanceSquaredInf(final Line3 q) {
		final Vector2 k = closestPairInf(q);
		return point(k.x).distanceSquared(q.point(k.y));
	}

	public float distanceSquared(final Line3 q) {
		final Vector2 k = closestPairInfFast(q);
		if (k.isFinite()) return point(clamp(k.x)).distanceSquared(q.point(clamp(k.y)));
		return Math.min(distanceSquared(q.a), distanceSquared(q.b));
	}

	public float distanceInf(final Line3 q) {
		return GMath.sqrt(distanceSquaredInf(q));
	}

	public float distance(final Line3 q) {
		return GMath.sqrt(distanceSquared(q));
	}

	public float closestPointInfFast(final Line3 q) {
		final Vector3 A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final float aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		return (ab * bc - bb * ac) / (aa * bb - ab * ab);
	}

	public float closestPointInf(final Line3 q) {
		final float x = closestPointInfFast(q);
		return GMath.isFinite(x) ? x : 0; // if lines are parallel every point is nearest!
	}

	public float closestPoint(final Line3 q) {
		final float x = closestPointInfFast(q);
		return GMath.isFinite(x) ? clamp(x) : q.distanceSquared(a) < q.distanceSquared(b) ? 1 : 0;
	}

	private static float clamp(final float a) {
		if (a < 0) return 0;
		if (a > 1) return 1;
		return a;
	}

	// Misc
	public Vector3 direction() {
		return b.sub(a);
	}

	/** Returns part of line in negative side of plane */
	public Line3 clip(final Plane3 p) {
		final float da = p.distance(a), db = p.distance(b);
		final Side sb = Side.classify(db);

		switch (Side.classify(da)) {
		case Positive:
			switch (sb) {
			case Positive:
				return null;
			case Zero:
				return new Line3(b, b);
			case Negative:
				return new Line3(Vector3.linear(a, b, da / (da - db)), b);
			}
			break;
		case Zero:
			return sb == Side.Positive ? new Line3(a, a) : this;
		case Negative:
			return sb == Side.Positive ? new Line3(a, Vector3.linear(a, b, da / (da - db))) : this;
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