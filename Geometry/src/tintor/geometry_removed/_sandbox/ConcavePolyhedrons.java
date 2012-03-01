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
package tintor.geometry.sandbox;

public class ConcavePolyhedrons {
	// point in polyhedron / with nearest face O(n)
	//	find nearest vertex
	//	calculate distances to planes with that vertex
	//	check side of nearest face

	//	public static boolean contains(final Polygon3[] a, final Vector3 p) {
	//		final Random rand = new Random();
	//		mainloop: while (true) {
	//			final Vector3 dir = new Vector3(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
	//			final Ray3 ray = new Ray3(p, dir);
	//			final Plucker plucker = Plucker.ray(p, dir);
	//
	//			for (final Polygon3 f : a) {
	//				final double d = ray.distance(f.plane);
	//				if (Double.isNaN(d)) continue mainloop;
	//
	//				boolean positive = false, negative = false;
	//				for (int j = f.vertices.length - 1, i = 0; i < f.vertices.length; j = i++)
	//					switch (Side.classify(plucker.side(f.vertices[j], f.vertices[i]))) {
	//					case Positive:
	//						if (negative) return false;
	//						positive = true;
	//						break;
	//					case Negative:
	//						if (positive) return false;
	//						negative = true;
	//						break;
	//					case Zero:
	//						return false;
	//					}
	//				return true;
	//			}
	//		}
	//	}

	public static boolean intersects(final Polygon3 a, final Polygon3[] b) {
		throw new RuntimeException();
	}

	public static boolean intersects(final Polygon3[] a, final Polygon3[] b) {
		for (final Polygon3 p : a)
			if (intersects(p, b)) return true;
		for (final Polygon3 p : b)
			if (intersects(p, a)) return true;
		return false;
	}
}