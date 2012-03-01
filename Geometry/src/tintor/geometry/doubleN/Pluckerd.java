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

/** @see vcg.isti.cnr.it/~ponchio/computergraphics/exercises/plucker.pdf */
public class Pluckerd {
	public final Vector3d u, v;

	public Pluckerd(final Vector3d u, final Vector3d v) {
		this.u = u;
		this.v = v;
	}

	public static Pluckerd line(final Vector3d a, final Vector3d b) {
		return new Pluckerd(b.sub(a), b.cross(a));
	}

	public static Pluckerd ray(final Vector3d origin, final Vector3d dir) {
		return new Pluckerd(dir, dir.cross(origin));
	}

	public Pluckerd(final Line3 p) {
		u = p.b.sub(p.a);
		v = p.b.cross(p.a);
	}

	public Pluckerd(final Ray3d p) {
		u = p.dir;
		v = p.dir.cross(p.origin);
	}

	/** <0 Clockwise (if you look in direction of one line, other will go CW around it) 
	 *  =0 Intersect or Parallel
	 *  >0 Counterclockwise*/
	public double side(final Pluckerd p) {
		return u.dot(p.v) + v.dot(p.u);
	}

	public double side(final Vector3d a, final Vector3d b) {
		return u.mixed(a, b) + v.dot(a, b);
	}

	public double side(final Line3 p) {
		return u.mixed(p.a, p.b) + v.dot(p.a, p.b);
	}

	public double side(final Ray3d p) {
		return u.mixed(p.dir, p.origin) + v.dot(p.dir);
	}

	//	public boolean inside(final Vector3... convex) {
	//		boolean p = false, n = false;
	//		for (int j = convex.length - 1, i = 0; i < convex.length; j = i++)
	//			switch (Side.classify(side(convex[j], convex[i]))) {
	//			case Positive:
	//				if (n) return false;
	//				p = true;
	//				break;
	//			case Negative:
	//				if (p) return false;
	//				n = true;
	//				break;
	//			case Zero:
	//				return false;
	//			}
	//		return true;
	//	}
}