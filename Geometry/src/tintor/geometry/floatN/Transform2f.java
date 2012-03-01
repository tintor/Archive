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

public class Transform2f {
	private float cosa, sina;
	private Vector2 pos;

	public Transform2f() {
		pos = Vector2.Zero;
		cosa = 1;
	}

	public Transform2f(final Vector2 position, final float angle) {
		set(position, angle);
	}

	public void set(final Vector2 position, final float angle) {
		pos = position;
		cosa = GMath.cos(angle);
		sina = GMath.sin(angle);
	}

	public Vector2 translation() {
		return pos;
	}

	public float angle() {
		return GMath.atan2(sina, cosa);
	}

	public static Transform2f comb(final Transform2f a, final Transform2f b) {
		final Transform2f z = new Transform2f();
		z.cosa = a.cosa * b.cosa - a.sina * b.sina;
		z.sina = a.cosa * b.sina + a.sina * b.cosa;
		z.pos = b.rotate(a.pos).add(b.pos);
		return z;
	}

	public Transform2f invert() {
		final Transform2f z = new Transform2f();
		z.cosa = cosa;
		z.sina = -sina;
		// z.pos = new Vector2d(-pos.x * cosa - pos.y * sina, pos.x * sina -
		// pos.y * cosa);
		z.pos = pos.rotate(cosa, -sina).neg();
		return z;
	}

	public Vector2 apply(final Vector2 v) {
		// return new Vector2d(v.x * cosa - v.y * sina + pos.x, v.x * sina +
		// v.y
		// * cosa + pos.y);
		return v.rotate(cosa, sina).add(pos);
	}

	public Plane2f apply(final Plane2f v) {
		final Vector2 nn = v.normal.rotate(cosa, sina);
		return new Plane2f(nn, v.offset - pos.dot(nn));
	}

	public Vector2 iapply(final Vector2 v) {
		return v.sub(pos).rotate(cosa, -sina);
		// return new Vector2d((v.x - pos.x) * cosa + (v.y - pos.y) * sina,
		// (v.x
		// - pos.x) * -sina + (v.y - pos.y)
		// * cosa);
	}

	public Vector2 rotate(final Vector2 v) {
		return v.rotate(cosa, sina);
	}

	public Vector2 irotate(final Vector2 v) {
		return v.rotate(cosa, -sina);
	}

	//	@Override
	//	public boolean equals(Object o) {
	//		if (!(o instanceof Transform2)) return false;
	//		Transform2 a = (Transform2) o;
	//		return cosa == a.cosa && sina == a.sina && pos.equals(a.pos);
	//	}

	@Override public String toString() {
		return "(angle=" + angle() + ", pos=" + pos.toString() + ")";
	}
}