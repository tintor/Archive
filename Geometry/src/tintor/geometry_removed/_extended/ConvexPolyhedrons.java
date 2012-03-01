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
package tintor.geometry.extended;

import static tintor.geometry.GMath.fi;

import java.util.Random;

import tintor.geometry.Vector2;
import tintor.geometry.Vector3;
import tintor.geometry.sandbox.Polygon3;

public class ConvexPolyhedrons {
	public static Polygon3[] make(final Vector3... v) {
		return new ConvexHull3(v).faces();
	}

	public static Vector3[] sphere(final double radius, final int segments) {
		final VList v = new VList().add(0, radius, 0).add(0, -radius, 0);

		final double slice = Math.PI / segments;
		for (int a = 1; a < segments; a++) {
			final double cosA = Math.cos(a * slice) * radius, sinA = Math.sin(a * slice) * radius;
			for (int b = 0; b <= segments * 2; b++) {
				final double cosB = Math.cos(b * slice), sinB = Math.sin(b * slice);
				v.add(sinA * cosB, cosA, sinA * sinB);
			}
		}
		return v.toArray();
	}

	public static Vector3[] randomSphere(final double radius, final int vertices) {
		final VList v = new VList();
		final Random rand = new Random();
		for (int i = 0; i < vertices; i++)
			v.add(Vector3.randomDirection(rand).mul(radius));
		return v.toArray();
	}

	public static Vector3[] truncatedCube() {
		return new VList().addc3(Math.sqrt(2) - 1, 1, 1).toArray();
	}

	public static Vector3[] football() {
		return new VList().addc2(0, 1, 3 * fi).addc3(2, 1 + 2 * fi, fi).addc3(1, 2 + fi, 2 * fi).toArray();
	}

	public static Vector3[] tetrahedron() {
		return new VList().add(1, 1, 1).addc(-1, -1, 1).toArray();
	}

	/** Half edges */
	public static Vector3[] cube(final double x, final double y, final double z) {
		return new VList().add3(x, y, z).toArray();
	}

	public static Vector3[] octahedron(final double r) {
		return new VList().addc(r, 0, 0).addc(-r, 0, 0).toArray();
	}

	public static Vector3[] dodecahedron() {
		return new VList().add3(1, 1, 1).addc2(0, 1 / fi, fi).toArray();
	}

	public static Vector3[] icosahedron() {
		return new VList().addc2(0, 1, fi).toArray();
	}

	//	private static Vector2[] triangle(double a, double b, double c) {
	//		double x = (a * a + b * b - c * c) / (2 * a), y = Math.sqrt(b * b - x * x);
	//		double cx = (x + a) / 3, cy = y / 3;
	//		return new Vector2[] { new Vector2(-cx, -cy), new Vector2(a - cx, -cy), new Vector2(x - cx, y - cy) };
	//	}

	private static Vector2[] polygon(final int k, final double semiradius) {
		final Vector2[] v = new Vector2[k];
		final double a = Math.PI * 2 / k;
		for (int i = 0; i < k; i++)
			v[i] = Vector2.polar(a * i, semiradius);
		return v;
	}

	public static Vector3[] prism(final int k, final double r1, final double r2, final double height) {
		return prism(polygon(k, r1), polygon(k, r2), height);
	}

	public static Vector3[] prism(final Vector2[] poly1, final Vector2[] poly2, final double height) {
		final VList v = new VList();
		for (final Vector2 a : poly1)
			v.add(a.x, a.y, height / 2);
		for (final Vector2 a : poly2)
			v.add(a.x, a.y, -height / 2);
		return v.toArray();
	}

	public static Vector3[] pyramid(final int k, final double r, final double height) {
		return pyramid(polygon(k, r), height);
	}

	public static Vector3[] pyramid(final Vector2[] poly, final double height) {
		final VList v = new VList().add(0, 0, height * 2.0 / 3);
		for (final Vector2 a : poly)
			v.add(a.x, a.y, -height / 3);
		return v.toArray();
	}
}