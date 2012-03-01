package tintor.geometry.extended;

import java.util.ArrayList;

import javax.media.opengl.GL;

import org.apache.commons.lang.ArrayUtils;

import tintor.XArrays;
import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector2;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;

/** Some methods assume polygon is CONVEX! */
public class Polygon3 {
	public Vector3[] vertices;
	public Plane3 plane;
	public Vector3 color; // data that needs to be preserved when polygon is split

	@Override public Polygon3 clone() {
		return new Polygon3(plane, color, vertices);
	}

	public Polygon3() {}

	public Polygon3(final Vector3... v) {
		this(new Plane3(v), null, v);
	}

	public Polygon3(final Plane3 p, final Vector3 c, final Vector3... v) {
		vertices = v;
		color = c;
		plane = p;
	}

	/** Signed volume projected onto z = 0. */
	public float projectedVolume() {
		float s = 0, z = 0;
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++) {
			final Vector3 a = vertices[j], b = vertices[i];
			z += b.z;
			s += (a.y + b.y) * (a.x - b.x);
		}
		return z * s / (2 * vertices.length);
	}

	public void removeDuplicates() {
		final ArrayList<Vector3> list = new ArrayList<Vector3>();
		loop: for (final Vector3 v : vertices) {
			for (final Vector3 a : list)
				if (a == v) continue loop;
			list.add(v);
		}
		vertices = list.toArray(new Vector3[list.size()]);
	}

	public Interval interval(final Vector3 normal) {
		final Interval i = new Interval();
		i.include(normal, vertices);
		return i;
	}

	public static Vector2[] project(final Vector3[] v, final Plane3 p) {
		final Vector3 ei = p.normal.normal().unit(), ej = p.normal.cross(ei).unit();
		final Vector2[] a = new Vector2[v.length];
		for (int i = 0; i < a.length; i++)
			a[i] = new Vector2(ei.dot(v[i]), ej.dot(v[i]));
		return a;
	}

	public Polygon3 transform(final Transform3 transform) {
		if (transform != Transform3.Identity) {
			for (int i = 0; i < vertices.length; i++)
				vertices[i] = transform.applyP(vertices[i]);
			plane = transform.apply(plane);
		}
		return this;
	}

	/** Returns null if parallel or no common points. */
	public Line3 intersection(final Plane3 p) {
		Vector3 a = null;

		float di, dj = p.distance(vertices[vertices.length - 1]);
		for (int j = vertices.length - 1, i = 0; i < vertices.length; dj = di, j = i++) {
			di = p.distance(vertices[i]);
			if (di * dj < 0) {
				final Vector3 b = Vector3.linear(vertices[j], vertices[i], dj / (dj - di));
				if (a != null) return new Line3(a, b);
				a = b;
			}
			if (di == 0) {
				final Vector3 b = vertices[i];
				if (a != null) return new Line3(a, b);
				a = b;
			}
		}
		return a != null ? new Line3(a, a) : null;
	}

	/** Returns null if parallel or if all points are >= (or <=) plane. */
	public Line3 penetration(final Plane3 p, final float eps) {
		Vector3 a = null;
		boolean z = false;

		float di, dj = p.distance(vertices[vertices.length - 1]);
		for (int j = vertices.length - 1, i = 0; i < vertices.length; dj = di, j = i++) {
			di = p.distance(vertices[i]);
			if (di < -eps && dj > eps || dj < -eps && di > eps) {
				z = true;
				final Vector3 b = Vector3.linear(vertices[j], vertices[i], dj / (dj - di));
				if (a != null) return new Line3(a, b);
				a = b;
			}
			if (Math.abs(di) <= eps) {
				final Vector3 b = vertices[i];
				if (a != null) return z ? new Line3(a, b) : null;
				a = b;
			}
		}
		return null;
	}

	/** Returns false if polygons are parallel or touching. */
	public boolean penetrating(final Polygon3 c, final float eps) {
		// project polygons on common line
		final Line3 p = c.penetration(plane, eps);
		if (p == null) return false;
		final Line3 q = penetration(c.plane, eps);
		if (q == null) return false;

		final Vector3 n = p.b.sub(p.a);

		// and compare intervals
		final Interval pi = new Interval(n.dot(p.a), n.dot(p.b));
		final Interval qi = new Interval(n.dot(q.a), n.dot(q.b));
		return pi.distance(qi) < -eps;
	}

	/** positive then negative */
	public Polygon3[] split(final Plane3 p, final float eps) {
		final Vector3[] v = new Vector3[vertices.length + 4];
		final int[] a = split(p, v, eps);

		final Vector3[] va = (Vector3[]) ArrayUtils.subarray(v, 0, a[0]);
		final Vector3[] vb = (Vector3[]) ArrayUtils.subarray(v, a[1], v.length);
		return new Polygon3[] { new Polygon3(plane, color, va), new Polygon3(plane, color, vb) };
	}

	/** positive then negative */
	public int[] split(final Plane3 p, final Vector3[] v, final float eps) {
		int a = 0, b = v.length;

		float di, dj = p.distance(vertices[vertices.length - 1]);
		for (int j = vertices.length - 1, i = 0; i < vertices.length; dj = di, j = i++) {
			di = p.distance(vertices[i]);
			if (di < -eps && dj > eps || dj < -eps && di > eps) {
				final Vector3 z = Vector3.linear(vertices[j], vertices[i], dj / (dj - di));
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
		XArrays.reverse(v, b, v.length);
		return new int[] { a, b };
	}

	/** Removes negative part. */
	public void clip(final Plane3 p) {
		final Vector3[] v = new Vector3[vertices.length + 2];
		final int[] a = split(p, v, 0);
		vertices = (Vector3[]) ArrayUtils.subarray(v, 0, a[0]);
	}

	/** ASSUME convex! BUGGY */
	public float distanceSquared(final Vector3 a) {
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++) {
			final Vector3 p = vertices[i].add(plane.normal);
			final Vector3 n = vertices[j].sub(p).cross(vertices[i].sub(p));
			if (n.dot(a, p) >= 0) return Line3.distanceSquared(vertices[i], vertices[j], a);
		}
		return GMath.square(plane.distance(a));
	}

	public float distance(final Vector3 a) {
		return distanceSquared(a);
	}

	public Polygon3 flip() {
		ArrayUtils.reverse(vertices);
		plane = plane.flip();
		return this;
	}

	public float surface() {
		float s = 0;
		for (int i = 2; i < vertices.length; i++)
			s += GMath.parallelogramSurface(vertices[i].sub(vertices[0]), vertices[i - 1].sub(vertices[0]));
		return s / 2;
	}

	/** Warning! Ignoring Side.eps! */
	public Side zone(final Vector3 a) {
		boolean border = false;
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++) {
			final Vector3 p = vertices[i].add(plane.normal);
			final Vector3 n = vertices[j].sub(p).cross(vertices[i].sub(p));

			final float d = n.dot(a, p);
			if (d > 0) return Side.Positive;
			if (d == 0) border = true;
		}
		return border ? Side.Zero : Side.Negative;
	}

	@Override public String toString() {
		final StringBuilder s = new StringBuilder().append("plane:").append(plane).append(" vertices:");
		for (final Vector3 v : vertices)
			s.append(v.toString("(%.2f %.2f %.2f)"));
		return s.toString();
	}

	private static boolean renderNormals = false;

	public void render() {
		if (color != null) GLA.color(color);
		GLA.beginPolygon();
		GLA.normal(plane.normal);
		for (final Vector3 v : vertices)
			GLA.vertex(v);
		GLA.gl.glEnd();

		if (renderNormals) {
			GLA.gl.glPushAttrib(GL.GL_CURRENT_BIT);
			GLA.gl.glBegin(GL.GL_LINES);
			GLA.color(GLA.black);
			final Vector3 a = Vector3.average(vertices);
			GLA.vertex(a);
			GLA.color(GLA.red);
			GLA.vertex(a.add(plane.normal));
			GLA.gl.glEnd();
			GLA.gl.glPopAttrib();
		}
	}
}