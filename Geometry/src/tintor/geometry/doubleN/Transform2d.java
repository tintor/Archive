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

public class Transform2d {
	private double cosa, sina;
	private Vector2d pos;

	public Transform2d() {
		pos = Vector2d.Zero;
		cosa = 1;
	}

	public Transform2d(Vector2d position, double angle) {
		set(position, angle);
	}

	public void set(Vector2d position, double angle) {
		this.pos = position;
		cosa = Math.cos(angle);
		sina = Math.sin(angle);
	}

	public Vector2d translation() {
		return pos;
	}

	public double angle() {
		return Math.atan2(sina, cosa);
	}

	public static Transform2d comb(Transform2d a, Transform2d b) {
		Transform2d z = new Transform2d();
		z.cosa = a.cosa * b.cosa - a.sina * b.sina;
		z.sina = a.cosa * b.sina + a.sina * b.cosa;
		z.pos = b.rotate(a.pos).add(b.pos);
		return z;
	}

	public Transform2d invert() {
		Transform2d z = new Transform2d();
		z.cosa = cosa;
		z.sina = -sina;
		// z.pos = new Vector2d(-pos.x * cosa - pos.y * sina, pos.x * sina -
		// pos.y * cosa);
		z.pos = pos.rotate(cosa, -sina).neg();
		return z;
	}

	public Vector2d apply(Vector2d v) {
		// return new Vector2d(v.x * cosa - v.y * sina + pos.x, v.x * sina +
		// v.y
		// * cosa + pos.y);
		return v.rotate(cosa, sina).add(pos);
	}

	public Plane2d apply(Plane2d v) {
		Vector2d nn = v.normal.rotate(cosa, sina);
		return new Plane2d(nn, v.offset - pos.dot(nn));
	}

	public Vector2d iapply(Vector2d v) {
		return v.sub(pos).rotate(cosa, -sina);
		// return new Vector2d((v.x - pos.x) * cosa + (v.y - pos.y) * sina,
		// (v.x
		// - pos.x) * -sina + (v.y - pos.y)
		// * cosa);
	}

	public Vector2d rotate(Vector2d v) {
		return v.rotate(cosa, sina);
	}

	public Vector2d irotate(Vector2d v) {
		return v.rotate(cosa, -sina);
	}

//	@Override
//	public boolean equals(Object o) {
//		if (!(o instanceof Transform2)) return false;
//		Transform2 a = (Transform2) o;
//		return cosa == a.cosa && sina == a.sina && pos.equals(a.pos);
//	}

	@Override
	public String toString() {
		return "(angle=" + angle() + ", pos=" + pos.toString() + ")";
	}
}