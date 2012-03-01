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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import tintor.geometry.doubleN.Intervald;
import tintor.geometry.doubleN.Line3;
import tintor.geometry.doubleN.Matrix3;
import tintor.geometry.doubleN.Plane3d;
import tintor.geometry.doubleN.Sided;
import tintor.geometry.doubleN.Transform3d;
import tintor.geometry.doubleN.Vector3d;

public class Polyhedrons {
	/** outside - Positive
	 *  border - Zero
	 *  inside - Negative */
	public static Sided side(final Polygon3[] poly, final Vector3d a) {
		boolean border = false;
		for (final Polygon3 p : poly) {
			final Sided s = p.plane.side(a);
			if (s == Sided.Positive) return Sided.Positive;
			if (s == Sided.Zero) border = true;
		}
		return border ? Sided.Zero : Sided.Negative;
	}

	/** ASSUMES that polygon has no holes in surface! */
	public static Line3[] edges(final Polygon3[] poly) {
		int e = 0;
		for (final Polygon3 f : poly)
			e += f.vertices.length;
		final Line3[] edges = new Line3[e / 2];
		e = 0;
		for (final Polygon3 f : poly)
			for (int j = f.vertices.length - 1, i = 0; i < f.vertices.length; j = i++) {
				final Vector3d a = f.vertices[j], b = f.vertices[i];
				if (a.compareTo(b) < 0) edges[e++] = new Line3(a, b);
			}
		assert e == edges.length;
		return edges;
	}

	public static Vector3d[] vertices(final Polygon3[] poly) {
		final Map<Vector3d, Object> map = new IdentityHashMap<Vector3d, Object>();
		for (final Polygon3 f : poly)
			for (final Vector3d v : f.vertices)
				map.put(v, null);
		return map.entrySet().toArray(new Vector3d[map.size()]);
	}

	public static Polygon3[] clone(final Polygon3[] poly) {
		final Polygon3[] p = new Polygon3[poly.length];
		for (int i = 0; i < poly.length; i++)
			p[i] = poly[i].clone();
		return p;
	}

	public static Polygon3[] transform(final Polygon3[] poly, final Transform3d transform) {
		for (final Polygon3 p : poly)
			p.transform(transform);
		return poly;
	}

	public static boolean convex(final Polygon3[] poly) {
		for (final Polygon3 p : poly) {
			final Vector3d a = Vector3d.average(p.vertices);
			for (final Polygon3 q : poly)
				if (Sided.classify(q.plane.distance(a)) == Sided.Positive) return false;
		}
		return true;
	}

	public final static class CenterOfMass {
		private Vector3d P = Vector3d.Zero;
		private double V = 0;

		public void add(final Vector3d[] vertices) {
			final Vector3d a = vertices[0];
			for (int i = 2; i < vertices.length; i++) {
				final Vector3d b = vertices[i - 1], c = vertices[i];
				final double v = a.mixed(b, c);
				P = P.add(v, a.add(b).add(c));
				V += v;
			}
		}

		public Vector3d get() {
			return P.div(V * 4);
		}
	}

	public static Vector3d centerOfMass(final Polygon3[] poly) {
		final CenterOfMass c = new CenterOfMass();
		for (final Polygon3 face : poly)
			c.add(face.vertices);
		return c.get();
	}

	public static void moveToCOM(final Polygon3[] faces, final Vector3d[] vertices) {
		final Vector3d centerOfMass = Polyhedrons.centerOfMass(faces);

		final Map<Vector3d, Vector3d> map = new IdentityHashMap<Vector3d, Vector3d>();
		for (int i = 0; i < vertices.length; i++) {
			final Vector3d p = vertices[i].sub(centerOfMass);
			map.put(vertices[i], p);
			vertices[i] = p;
		}

		// translate faces
		for (final Polygon3 f : faces) {
			for (int j = 0; j < f.vertices.length; j++)
				f.vertices[j] = map.get(f.vertices[j]);
			f.plane = new Plane3d(f.vertices);
		}
	}

	/** Can modify argument. */
	public static Polygon3[] mergeFaces(final Polygon3[] poly) {
		// TODO
		return poly;
	}

	/** Can modify argument. */
	public static Polygon3[] removeDumbVertices(final Polygon3[] poly) {
		// TODO
		// pronadji dve povrsine sa istim ravnima, temenima ABD / DBC i B = lin(A,C)
		// pomeri B prema brizem od A/C ako povrsine ostaju 
		return poly;
	}

	/** Can modify argument. */
	public static Polygon3[] mergeCloseVertices(final Polygon3[] poly, double eps) {
		final Map<Polygon3, Object> updated = new IdentityHashMap<Polygon3, Object>();
		final Map<Vector3d, Integer> weight = new IdentityHashMap<Vector3d, Integer>();

		eps *= eps;
		//		boolean change = true;
		//		while (change) {
		//			change = false;
		//			int c = 0;

		for (int pi = 0; pi < poly.length; pi++)
			for (int pj = pi; pj < poly.length; pj++) {
				final Vector3d[] a = poly[pi].vertices, b = poly[pj].vertices;

				for (int i = 0; i < a.length; i++)
					for (int j = 0; j < b.length; j++)
						if (a[i] != b[j] && a[i].distanceSquared(b[j]) <= eps) {
							final int wa = weight.containsKey(a[i]) ? weight.get(a[i]) : 1;
							final int wb = weight.containsKey(b[j]) ? weight.get(b[j]) : 1;

							final Vector3d v = a[i] = b[j] = Vector3d.linear(a[i], b[j], wa / (wa + wb));
							weight.put(v, wa + wb);

							updated.put(poly[pi], null);
							updated.put(poly[pj], null);
							//								change = true;
							//								c++;
						}
			}

		// TODO remove empty polygons
		for (final Polygon3 c : updated.keySet()) {
			c.removeDuplicates();
			c.plane = new Plane3d(c.vertices);
		}

		return poly;
	}

	public static Polygon3[] difference(final Polygon3[] a, final Polygon3[] b) {
		return operation(a, b, Outside, InsideFlip);
	}

	public static Polygon3[] intersection(final Polygon3[] a, final Polygon3[] b) {
		return operation(a, b, Inside, Inside);
	}

	public static Polygon3[] union(final Polygon3[] a, final Polygon3[] b) {
		return operation(a, b, Outside, Outside);
	}

	enum Policy {
		Accept, Flip, Reject;
	}

	// NOTE depends on order of Side.* constants
	private static final Policy[] Outside = { Policy.Accept, Policy.Accept, Policy.Reject };
	private static final Policy[] Inside = { Policy.Reject, Policy.Accept, Policy.Accept };
	private static final Policy[] OutsideFlip = { Policy.Flip, Policy.Flip, Policy.Reject };
	private static final Policy[] InsideFlip = { Policy.Reject, Policy.Flip, Policy.Flip };
	private static final Policy[] AcceptAll = { Policy.Accept, Policy.Accept, Policy.Accept };

	public static Polygon3[] operation(final Polygon3[] a, final Polygon3[] b, final Policy[] policyA,
			final Policy[] policyB) {
		final List<Polygon3> q = new ArrayList<Polygon3>();
		if (policyA != null) half(a, b, policyA, q);
		if (policyB != null) half(b, a, policyB, q);
		return q.toArray(new Polygon3[q.size()]);
	}

	static void half(final Polygon3[] a, final Polygon3[] b, final Policy[] policy, final List<Polygon3> q) {
		List<Polygon3> r = new ArrayList<Polygon3>(), w = new ArrayList<Polygon3>();
		final double eps = 1e-8;

		for (final Polygon3 pa : a)
			r.add(pa);

		// cut every polygon from A with every polygon from B
		for (final Polygon3 pb : b) {
			w.clear();
			for (final Polygon3 pa : r)
				if (pa.penetrating(pb, eps)) {
					final Polygon3[] v = pa.split(pb.plane, eps);
					assert v[0].vertices.length >= 3 && v[1].vertices.length >= 3;
					w.add(v[0]);
					w.add(v[1]);
				} else
					w.add(pa);

			final List<Polygon3> t = r;
			r = w;
			w = t;
		}

		// select pieces of A based on policy
		for (Polygon3 face : r) {
			final Sided s = Sided.classify(signedDistanceSquared(b, Vector3d.average(face.vertices)), eps);
			final Policy p = policy[s.ordinal()];
			if (p != Policy.Reject) {
				face = face.clone();
				if (p == Policy.Flip) face = face.flip();
				q.add(face);
			}
		}
	}

	public static double signedVolume(final Polygon3[] poly) {
		double v = 0;
		for (final Polygon3 p : poly)
			v += p.projectedVolume();
		return v;
	}

	// NOTE does this work only on Convex?
	public static double convexVolume(final Polygon3[] poly) {
		double v = 0;
		for (final Polygon3 p : poly)
			for (int i = 2; i < p.vertices.length; i++)
				v += p.vertices[0].mixed(p.vertices[i - 1], p.vertices[i]);
		return v / 4;
	}

	public static double surface(final Polygon3[] poly) {
		double s = 0;
		for (final Polygon3 f : poly)
			s += f.surface();
		return s;
	}

	/** ASSUME density = 1 and center of mass = 0 */
	public static Matrix3 inertiaTensor(final Polygon3[] poly) {
		final double a = 1 / 60., b = 1 / 120.;
		final Matrix3 canonical = new Matrix3(a, b, b, b, a, b, b, b, a);
		Matrix3 C = Matrix3.Zero; // covariance
		for (final Polygon3 p : poly)
			for (int i = 2; i < p.vertices.length; i++) { // for each surface triangle
				final Matrix3 A = new Matrix3(p.vertices[0], p.vertices[i - 1], p.vertices[i]);
				C = C.add(A.transpose().mul(canonical).mul(A), A.det());
			}
		return new Matrix3(C.trace()).sub(C); // C -> I
	}

	public static Intervald interval(final Polygon3[] poly, final Vector3d normal) {
		Intervald i = Intervald.Empty;
		for (final Polygon3 p : poly)
			i = i.union(p.interval(normal));
		return i;
	}

	public static Polygon3 planePolygon(final Polygon3[] poly, final Plane3d plane) {
		final Vector3d ex = plane.normal.normal().unit(), ey = plane.normal.cross(ex);
		assert ex.isFinite() && ey.isFinite();
		final Intervald ix = interval(poly, ex), iy = interval(poly, ey);
		final Vector3d center = ex.mul(ix.center()).add(iy.center(), ey).sub(plane.offset, plane.normal);

		final Vector3d a = center.add(ix.width(), ex).add(iy.width(), ey);
		final Vector3d b = center.sub(ix.width(), ex).add(iy.width(), ey);
		final Vector3d c = center.sub(ix.width(), ex).sub(iy.width(), ey);
		final Vector3d d = center.add(ix.width(), ex).sub(iy.width(), ey);
		return new Polygon3(plane, a, b, c, d);
	}

	static Polygon3[] intersect(final Polygon3[] poly, final Plane3d plane, final boolean halfspace) {
		final Polygon3[] p = { planePolygon(poly, plane) };
		return operation(poly, p, halfspace ? Inside : null, Inside);
	}

	public static Polygon3[] optimize(Polygon3[] poly, final double eps) {
		poly = clone(poly);
		poly = mergeCloseVertices(poly, eps);
		poly = removeDumbVertices(poly);
		return mergeFaces(poly);
	}

	public static void splitIntoConvex(Polygon3[] poly, final List<Polygon3[]> list) {
		poly = optimize(poly, 1e-8);
		if (convex(poly)) {
			list.add(poly);
			return;
		}

		final double v = signedVolume(poly);
		double dmin = Double.POSITIVE_INFINITY;
		Polygon3[] amin = null, bmin = null;

		for (final Polygon3 p : poly) {
			final Polygon3[] c = { planePolygon(poly, p.plane) };

			final Polygon3[] a = intersection(poly, c);
			final double va = signedVolume(a);

			c[0].flip();

			final Polygon3[] b = intersection(poly, c);
			final double vb = signedVolume(b);

			// System.out.println("log: va:" + va + " vb:" + vb + " va+vb-v:" + (va + vb - v));
			assert Math.abs(va + vb - v) < 1e-5 : va + " " + vb + " " + v;

			final double d = Math.abs(va - vb);
			if (d < dmin) {
				amin = a;
				bmin = b;
				dmin = d;
			}
		}

		splitIntoConvex(amin, list);
		splitIntoConvex(bmin, list);
	}

	//	public static void render(final Polygon3[] poly) {
	//		for (final Polygon3 p : poly)
	//			p.render();
	//	}

	public static Line3 convexClip(final Polygon3[] poly, Line3 a) {
		for (final Polygon3 p : poly) {
			a = a.clip(p.plane);
			if (a == null) return null;
		}
		return a;
	}

	@SuppressWarnings("null") public static double signedDistanceSquared(final Polygon3[] poly, final Vector3d a) {
		double dmin = Double.POSITIVE_INFINITY;
		Polygon3 pmin = null;

		for (final Polygon3 p : poly) {
			final double d = p.distanceSquared(a);
			assert !Double.isNaN(d);
			if (d < dmin) {
				pmin = p;
				dmin = d;
			}
		}

		return pmin.plane.distance(a) >= 0 ? dmin : -dmin;
	}
}