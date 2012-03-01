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

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import tintor.geometry.doubleN.Intervald;
import tintor.geometry.doubleN.Line3;
import tintor.geometry.doubleN.Plane3d;
import tintor.geometry.doubleN.Sided;
import tintor.geometry.doubleN.Transform3d;
import tintor.geometry.doubleN.Vector2d;
import tintor.geometry.doubleN.Vector3d;

/** Some methods assume polygon is CONVEX! */
public class Polygon3 {
	// TODO protect vertices
	public Vector3d[] vertices;
	public Plane3d plane;
	// TODO make color external
	public Vector3d color; // data that needs to be preserved when polygon is split

	@Override public Polygon3 clone() {
		return new Polygon3(plane, color, vertices);
	}

	public Polygon3() {}

	public Polygon3(final Vector3d... v) {
		this(new Plane3d(v), null, v);
	}

	public Polygon3(final Plane3d p, final Vector3d c, final Vector3d... v) {
		vertices = v;
		color = c;
		plane = p;
	}

	/** Signed volume projected onto z = 0. */
	public double projectedVolume() {
		double s = 0, z = 0;
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++) {
			final Vector3d a = vertices[j], b = vertices[i];
			z += b.z;
			s += (a.y + b.y) * (a.x - b.x);
		}
		return z * s / (2 * vertices.length);
	}

	public void removeDuplicates() {
		final ArrayList<Vector3d> list = new ArrayList<Vector3d>();
		loop: for (final Vector3d v : vertices) {
			for (final Vector3d a : list)
				if (a == v) continue loop;
			list.add(v);
		}
		vertices = list.toArray(new Vector3d[list.size()]);
	}

	public Intervald interval(final Vector3d normal) {
		return new Intervald(normal, vertices);
	}

	public static Vector2d[] project(final Vector3d[] v, final Plane3d p) {
		final Vector3d ei = p.normal.normal().unit(), ej = p.normal.cross(ei).unit();
		final Vector2d[] a = new Vector2d[v.length];
		for (int i = 0; i < a.length; i++)
			a[i] = new Vector2d(ei.dot(v[i]), ej.dot(v[i]));
		return a;
	}

	public Polygon3 transform(final Transform3d transform) {
		if (transform != Transform3d.Identity) {
			for (int i = 0; i < vertices.length; i++)
				vertices[i] = transform.applyP(vertices[i]);
			plane = transform.apply(plane);
		}
		return this;
	}

	/** Returns null if parallel or no common points. */
	public Line3 intersection(final Plane3d p) {
		Vector3d a = null;

		double di, dj = p.distance(vertices[vertices.length - 1]);
		for (int j = vertices.length - 1, i = 0; i < vertices.length; dj = di, j = i++) {
			di = p.distance(vertices[i]);
			if (di * dj < 0) {
				final Vector3d b = Vector3d.linear(vertices[j], vertices[i], dj / (dj - di));
				if (a != null) return new Line3(a, b);
				a = b;
			}
			if (di == 0) {
				final Vector3d b = vertices[i];
				if (a != null) return new Line3(a, b);
				a = b;
			}
		}
		return a != null ? new Line3(a, a) : null;
	}

	/** Returns null if parallel or if all points are >= (or <=) plane. */
	public Line3 penetration(final Plane3d p, final double eps) {
		Vector3d a = null;
		boolean z = false;

		double di, dj = p.distance(vertices[vertices.length - 1]);
		for (int j = vertices.length - 1, i = 0; i < vertices.length; dj = di, j = i++) {
			di = p.distance(vertices[i]);
			if (di < -eps && dj > eps || dj < -eps && di > eps) {
				z = true;
				final Vector3d b = Vector3d.linear(vertices[j], vertices[i], dj / (dj - di));
				if (a != null) return new Line3(a, b);
				a = b;
			}
			if (Math.abs(di) <= eps) {
				final Vector3d b = vertices[i];
				if (a != null) return z ? new Line3(a, b) : null;
				a = b;
			}
		}
		return null;
	}

	/** Returns false if polygons are parallel or touching. */
	public boolean penetrating(final Polygon3 c, final double eps) {
		// project polygons on common line
		final Line3 p = c.penetration(plane, eps);
		if (p == null) return false;
		final Line3 q = penetration(c.plane, eps);
		if (q == null) return false;

		final Vector3d n = p.b.sub(p.a);

		// and compare intervals
		final Intervald pi = new Intervald(n.dot(p.a), n.dot(p.b));
		final Intervald qi = new Intervald(n.dot(q.a), n.dot(q.b));
		return pi.distance(qi) < -eps;
	}

	/** positive then negative */
	public Polygon3[] split(final Plane3d p, final double eps) {
		final Vector3d[] v = new Vector3d[vertices.length + 4];
		final int[] a = split(p, v, eps);

		final Vector3d[] va = (Vector3d[]) ArrayUtils.subarray(v, 0, a[0]);
		final Vector3d[] vb = (Vector3d[]) ArrayUtils.subarray(v, a[1], v.length);
		return new Polygon3[] { new Polygon3(plane, color, va), new Polygon3(plane, color, vb) };
	}

	/** positive then negative */
	public int[] split(final Plane3d p, final Vector3d[] v, final double eps) {
		int a = 0, b = v.length;

		double di, dj = p.distance(vertices[vertices.length - 1]);
		for (int j = vertices.length - 1, i = 0; i < vertices.length; dj = di, j = i++) {
			di = p.distance(vertices[i]);
			if (di < -eps && dj > eps || dj < -eps && di > eps) {
				final Vector3d z = Vector3d.linear(vertices[j], vertices[i], dj / (dj - di));
				assert Math.abs(p.distance(z)) <= eps;
				assert Math.abs(plane.distance(z)) <= eps;
				v[a++] = z;
				v[--b] = z;
				assert a <= b;
			}
			if (di >= -eps) v[a++] = vertices[i];
			if (di <= eps) v[--b] = vertices[i];
			assert a <= b;
		}

		// flip negative
		arrayReverse(v, b, v.length);
		return new int[] { a, b };
	}

	// s inclusive, e exclusive
	public static <T> void arrayReverse(final T[] a, int s, int e) {
		for (e--; s < e; s++, e--) {
			final T x = a[s];
			a[s] = a[e];
			a[e] = x;
		}
	}

	/** Removes negative part. */
	public void clip(final Plane3d p) {
		final Vector3d[] v = new Vector3d[vertices.length + 2];
		final int[] a = split(p, v, 0);
		vertices = (Vector3d[]) ArrayUtils.subarray(v, 0, a[0]);
	}

	/** ASSUME convex! BUGGY */
	public double distanceSquared(final Vector3d a) {
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++) {
			final Vector3d p = vertices[i].add(plane.normal);
			final Vector3d n = vertices[j].sub(p).cross(vertices[i].sub(p));
			if (n.dot(a, p) >= 0) return Line3.distanceSquared(vertices[i], vertices[j], a);
		}
		final double d = plane.distance(a);
		return d * d;
	}

	public double distance(final Vector3d a) {
		return distanceSquared(a);
	}

	public Polygon3 flip() {
		ArrayUtils.reverse(vertices);
		plane = plane.flip();
		return this;
	}

	public double surface() {
		double s = 0;
		for (int i = 2; i < vertices.length; i++)
			s += parallelogramSurface(vertices[i].sub(vertices[0]), vertices[i - 1].sub(vertices[0]));
		return s / 2;
	}

	private static double parallelogramSurface(final Vector3d a, final Vector3d b) {
		final double ab = a.dot(b);
		return Math.sqrt(a.square() * b.square() - ab * ab);
	}

	/** Warning! Ignoring Side.eps! */
	public Sided zone(final Vector3d a) {
		boolean border = false;
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++) {
			final Vector3d p = vertices[i].add(plane.normal);
			final Vector3d n = vertices[j].sub(p).cross(vertices[i].sub(p));

			final double d = n.dot(a, p);
			if (d > 0) return Sided.Positive;
			if (d == 0) border = true;
		}
		return border ? Sided.Zero : Sided.Negative;
	}

	@Override public String toString() {
		final StringBuilder s = new StringBuilder().append("plane:").append(plane).append(" vertices:");
		for (final Vector3d v : vertices)
			s.append(v.toString("(%.2f %.2f %.2f)"));
		return s.toString();
	}

	//	private static boolean renderNormals = false;
	//	public void render() {
	//		if (color != null) GLA.color(color);
	//		GLA.beginPolygon();
	//		GLA.normal(plane.normal);
	//		for (final Vector3 v : vertices)
	//			GLA.vertex(v);
	//		GLA.gl.glEnd();
	//
	//		if (renderNormals) {
	//			GLA.gl.glPushAttrib(GL.GL_CURRENT_BIT);
	//			GLA.gl.glBegin(GL.GL_LINES);
	//			GLA.color(GLA.black);
	//			final Vector3 a = Vector3.average(vertices);
	//			GLA.vertex(a);
	//			GLA.color(GLA.red);
	//			GLA.vertex(a.add(plane.normal));
	//			GLA.gl.glEnd();
	//			GLA.gl.glPopAttrib();
	//		}
	//	}
}