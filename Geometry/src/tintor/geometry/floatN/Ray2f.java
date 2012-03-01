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

/** Infinite directed line in space. */
public final class Ray2f {
	// Fields
	public final Vector2 origin;
	public final Vector2 dir;

	// Constructors
	public Ray2f(final Vector2 origin, final Vector2 dir) {
		this.origin = origin;
		this.dir = dir.unit();
	}

	public Ray2f(final Vector2 origin, final float angle) {
		this.origin = origin;
		dir = Vector2.polar(angle);
	}

	// Operations
	public Vector2 point(final float t) {
		return origin.add(t, dir);
	}

	public float nearest(final Vector2 p) {
		return p.sub(origin).dot(dir);
	}

	public float distance(final Vector2 p) {
		final Vector2 v = origin.sub(p);
		return dir.mul(v.dot(dir)).distance(v);
	}

	// Unique
	//	public Side side(Vector2 p, float ε) {
	//		return Side.classify(dir.det(origin.sub(p)));
	//	}

	// from Object
	public boolean equals(final Ray2f o) {
		return this == o ? true : o != null && origin == o.origin && dir == o.dir;
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Ray2f && equals((Ray2f) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Ray3.class.hashCode(), origin.hashCode(), dir.hashCode());
	}
}