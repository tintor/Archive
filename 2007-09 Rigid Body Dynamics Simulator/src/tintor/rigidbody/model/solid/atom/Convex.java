package tintor.rigidbody.model.solid.atom;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexHull3;
import tintor.geometry.sandbox.Polygon3;
import tintor.geometry.sandbox.Polyhedrons;
import tintor.rigidbody.model.solid.Atom;
import tintor.rigidbody.model.solid.CollisionPair;
import tintor.util.NonCashingHashSet;

/** Describes structure of convex polyhedron */
public final class Convex extends Atom {
	// Fields
	public final Polygon3[] faces;
	public final Line3[] edges;
	public final Vector3[] vertices;
	public final Interval[] intervals;

	// Constructors
	public Convex(final Vector3... w) {
		// check if equal nodes are identical
		for (final Vector3 a : w)
			for (final Vector3 b : w)
				if (a != b && a.equals(b)) throw new RuntimeException();

		// construct convex hull
		final ConvexHull3 hull = new ConvexHull3(w);

		// vertices and faces
		vertices = hull.vertices;
		faces = hull.faces;
		// TODO sort faces by surface DESC
		// Arrays.sort(faces);

		// init intervals
		intervals = new Interval[faces.length];
		for (int i = 0; i < faces.length; i++)
			intervals[i] = new Interval(faces[i].plane.normal, vertices);

		// edges
		int e = 0;
		for (final Polygon3 f : faces)
			e += f.vertices.length;
		edges = new Line3[e / 2];
		e = 0;
		for (final Polygon3 f : faces)
			for (int j = f.vertices.length - 1, i = 0; i < f.vertices.length; j = i++) {
				final Vector3 a = f.vertices[j], b = f.vertices[i];
				if (a.compareTo(b) < 0) edges[e++] = new Line3(a, b);
			}
		assert e == edges.length;

		// calculate radius
		radius = maximal(Vector3.Zero);
	}

	private Convex(final Convex poly, final Transform3 transform) {
		edges = new Line3[poly.edges.length];
		faces = new Polygon3[poly.faces.length];
		vertices = new Vector3[poly.vertices.length];

		// translate to center of mass
		final Map<Vector3, Vector3> map = new IdentityHashMap<Vector3, Vector3>();
		for (int i = 0; i < poly.vertices.length; i++) {
			final Vector3 p = transform.applyV(poly.vertices[i]);
			map.put(poly.vertices[i], p);
			vertices[i] = p;
		}

		// translate edges
		for (int i = 0; i < poly.edges.length; i++)
			edges[i] = new Line3(map.get(poly.edges[i].a), map.get(poly.edges[i].b));

		// translate faces
		for (int i = 0; i < poly.faces.length; i++) {
			for (int j = 0; j < faces[i].vertices.length; j++)
				faces[i].vertices[j] = map.get(poly.faces[i].vertices[j]);
			faces[i].plane = new Plane3(faces[i].vertices);
		}

		// copy intervals
		intervals = poly.intervals;

		// calculate radius
		radius = maximal(Vector3.Zero);
	}

	@Override public Side side(final Vector3 point) {
		boolean border = false;
		for (final Polygon3 f : faces) {
			final Side s = f.plane.side(point);
			if (s == Side.Positive) return s;
			if (s == Side.Zero) border = true;
		}
		return border ? Side.Zero : Side.Negative;
	}

	@Override public Interval interval(final Vector3 normal) {
		return new Interval(normal, vertices);
	}

	@Override public double maximal(final Vector3 center) {
		double r = 0;
		for (final Vector3 v : vertices)
			r = Math.max(r, v.distanceSquared(center));
		return Math.sqrt(r);
	}

	public double surface() {
		return Polyhedrons.surface(faces);
	}

	@Override public double mass() {
		return Polyhedrons.convexVolume(faces);
	}

	@Override public Matrix3 inertiaTensor() {
		return Polyhedrons.inertiaTensor(faces);
	}

	@Override public void render() {
		Polyhedrons.render(faces);
	}

	@Override public void findContacts(final CollisionPair pair) {
		pair.poly(this);
	}

	/** returns part of line that is inside poly, or null if completely outside */
	public Line3 clip(final Line3 a) {
		return Polyhedrons.convexClip(faces, a);
	}

	public static Vector3[] intersect(final Convex A, final Transform3 transformA, final Convex B,
			final Transform3 transformB) {
		final Set<Vector3> set = new NonCashingHashSet<Vector3>();

		final Transform3 transform = transformA.icombine(transformB);
		for (final Line3 e : A.edges) {
			final Line3 p = B.clip(transform.apply(e));
			if (p != null) {
				set.add(transformB.applyP(p.a));
				if (!p.a.equals(p.b)) set.add(transformB.applyP(p.b));
			}
		}
		for (final Line3 e : B.edges) {
			final Line3 p = A.clip(transform.iapply(e));
			if (p != null) {
				set.add(transformA.applyP(p.a));
				if (p.a != p.b) set.add(transformA.applyP(p.b));
			}
		}

		return set.toArray(new Vector3[set.size()]);
	}

	public double distance(final Vector3 a) {
		return Polyhedrons.signedDistanceSquared(faces, a);
	}

	@Override public Vector3[] intersection(final Plane3 plane) {
		final Set<Vector3> set = new NonCashingHashSet<Vector3>();
		for (final Line3 e : edges) {
			final Line3 p = e.clip(plane);
			if (p != null) {
				set.add(p.a);
				if (!p.a.equals(p.b)) set.add(p.b);
			}
		}
		return set.toArray(new Vector3[set.size()]);
	}

	@Override public Convex convexHull() {
		return this;
	}

	public Convex ptransform(final Transform3 transform) {
		if (transform == Transform3.Identity) return this;
		return new Convex(this, transform);
	}
}