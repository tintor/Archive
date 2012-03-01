package tintor.rigidbody.model;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexHull3;
import tintor.geometry.extended.ConvexPolyhedrons;
import tintor.geometry.extended.Polygon3;
import tintor.geometry.extended.Polyhedrons;
import tintor.geometry.extended.Polyhedrons.Center;
import tintor.opengl.GLA;
import vclip.ConvexPolyhedron;
import vclip.PolyTree;

enum Hint {
	Sphere, Box, Convex
}

public final class Shape {
	public static boolean useHints = true;

	public static Shape box(final float sizeX, final float sizeY, final float sizeZ) {
		final Shape b = new Shape(Hint.Box, /*ConvexPolyhedron.createBox(sizeX, sizeY, sizeZ),*/ ConvexPolyhedrons.cube(
				sizeX / 2, sizeY / 2, sizeZ / 2));
		b.boxX = new Interval(-sizeX / 2, sizeX / 2);
		b.boxY = new Interval(-sizeY / 2, sizeY / 2);
		b.boxZ = new Interval(-sizeZ / 2, sizeZ / 2);
		return b;
	}

	public static Shape sphere(final float size, final int seg) {
		return new Shape(Hint.Sphere, /*ConvexPolyhedron.createSphere(size / 2, seg),*/ ConvexPolyhedrons.sphere(size / 2,
				seg));
	}

	public static Shape sphere(final float size, final int seg, final Vector3 color1, final Vector3 color2) {
		final Shape s = sphere(size, seg);
		s.bicolor(Vector3.X, color1, color2);
		return s;
	}

	// Fields
	public final Polygon3[] faces; // sorted by size
	final Line3[] edges;
	final Vector3[] vertices;
	final Interval[] intervals;
	public final float radius;
	private final Hint hint;

	//public final PolyTree polyTree;

	private Interval boxX, boxY, boxZ;

	// Constructors
	public Shape(final Vector3... w) {
		this(Hint.Convex, w);
	}

	private Shape(final Hint hint, /*final ConvexPolyhedron convexPolyhedron,*/ final Vector3... w) {
		this.hint = hint;

		final ConvexHull3 hull = new ConvexHull3(w);
		faces = hull.faces();
		vertices = hull.vertices();
		Polyhedrons.moveToCOM(faces, vertices);
		edges = Polyhedrons.edges(faces);
		radius = radius();

		// intervals
		intervals = new Interval[faces.length];
		for (int i = 0; i < faces.length; i++)
			intervals[i] = new Interval().include(faces[i].plane.normal, vertices);

		//		final Map<Vector3, Integer> map = new IdentityHashMap<Vector3, Integer>();
		//		final double[] vlist = new double[3 * vertices.length];
		//		for (int i = 0; i < vertices.length; i++) {
		//			vlist[i] = vertices[i].x;
		//			vlist[i + 1] = vertices[i].y;
		//			vlist[i + 2] = vertices[i].z;
		//			map.put(vertices[i], i);
		//		}
		//		final int[][] flist = new int[faces.length][];
		//		for (int i = 0; i < faces.length; i++) {
		//			flist[i] = new int[faces[i].vertices.length];
		//			for (int j = 0; j < flist[i].length; j++)
		//				flist[i][j] = map.get(faces[i].vertices[j]);
		//		}
		//polyTree = new PolyTree(null, convexPolyhedron);
	}

	private float radius() {
		float r = 0;
		for (final Vector3 v : vertices)
			r = Math.max(r, v.square());
		return GMath.sqrt(r);
	}

	public float volume() {
		switch (hint) {
		case Sphere:
			return GMath.cube(radius) * (float) (Math.PI * 4 / 3);
		case Box:
			return boxX.max * boxY.max * boxZ.max * 8;
		default:
			return Math.abs(Polyhedrons.signedVolume(faces));
		}
	}

	public Matrix3 inertiaTensor() {
		return Polyhedrons.inertiaTensor(faces);
	}

	private int glList = Integer.MIN_VALUE;

	public void render() {
		if (glList == Integer.MIN_VALUE) {
			glList = GLA.gl.glGenLists(1);
			GLA.gl.glNewList(glList, GL.GL_COMPILE);
			Polyhedrons.render(faces);
			GLA.gl.glEndList();
		}
		GLA.gl.glCallList(glList);
	}

	public void bicolor(final Vector3 axis, final Vector3 color1, final Vector3 color2) {
		for (final Polygon3 p : faces)
			p.color = axis.dot(p.plane.normal) > 0 ? color1 : color2;
	}

	/** Axis is in localspace */
	public Interval interval(final Vector3 axis) {
		switch (hint) {
		case Sphere:
			return new Interval(-radius, radius);
		case Box:
			final float m = Math.abs(axis.x) * boxX.max + Math.abs(axis.y) * boxY.max + Math.abs(axis.z) * boxZ.max;
			return new Interval(-m, m);
		default:
			return new Interval().include(axis, vertices);
		}
	}

	/** Project body onto axis WITHOUT offset! */
	private static Interval interval(final Body body, final Vector3 axis) {
		return body.shape.interval(body.transform().iapplyV(axis));
	}

	private static float maxDist;
	private static Vector3 maxAxis;
	private static Vector3 offset;

	private static boolean separating(Vector3 axis, final Interval a, final Interval b) {
		float z = a.min - b.min + a.max - b.max - offset.dot(axis);
		if (z < 0) {
			z = -z;
			axis = axis.neg();
		}
		final float dist = (z + a.min - a.max + b.min - b.max) / 2;
		if (dist > 0) return true;
		if (dist > maxDist) {
			maxDist = dist;
			maxAxis = axis;
		}
		return false;
	}

	/** Algoritm tests candidates (from faces and edges) for separation axis.
	 *  If no separation axis is found, contact is assumed. */
	public static Contact findContact(Body a, Body b) {
		// cheap bounding spheres test
		if (a.position().distanceSquared(b.position()) > GMath.square(a.shape.radius + b.shape.radius)) return null;

		// NOTE sortiraj to broju ivica prvo / malo ubrzava SAT
		if (a.id > b.id) {
			final Body t = a;
			a = b;
			b = t;
		}
		maxDist = Float.NEGATIVE_INFINITY;
		maxAxis = null;
		offset = b.position().sub(a.position()).mul(2); // = (b.pos-a.pos)*2

		// Sphere / Sphere
		if (a.shape.hint == Hint.Sphere && b.shape.hint == Hint.Sphere) return findContactSphereSphere(a, b);

		// Sphere / Box
		if (false && a.shape.hint == Hint.Sphere && b.shape.hint == Hint.Box) return findContactSphereBox(a, b);
		if (false && a.shape.hint == Hint.Box && b.shape.hint == Hint.Sphere) {
			offset = offset.neg();
			return findContactSphereBox(b, a);
		}

		// Box / Box
		if (a.shape.hint == Hint.Box && b.shape.hint == Hint.Box) return findContactBoxBox(a, b);

		// Sphere / Poly 
		if (false && a.shape.hint == Hint.Sphere && b.shape.hint == Hint.Convex) return findContactSpherePoly(a, b);
		if (false && a.shape.hint == Hint.Convex && b.shape.hint == Hint.Sphere) {
			offset = offset.neg();
			return findContactSpherePoly(b, a);
		}

		return findContactPolyPoly(a, b);
	}

	private static Contact findContactPolyPoly(final Body a, final Body b) {
		// Use axis from Arbiter
		final Arbiter arbiter = Arbiter.get(a, b);
		if (arbiter.axis != null && arbiter.impulse == Vector3.Zero)
			if (separating(arbiter.axis, interval(a, arbiter.axis), interval(b, arbiter.axis))) return null;

		// O(a.faces * b.vertices)
		for (int i = 0; i < a.shape.faces.length; i++) {
			final Vector3 axis = a.transform().applyV(a.shape.faces[i].plane.normal);
			if (separating(axis, a.shape.intervals[i], interval(b, axis))) {
				arbiter.impulse = Vector3.Zero;
				arbiter.axis = axis;
				return null;
			}
		}

		// O(a.faces * b.vertices)
		for (int i = 0; i < b.shape.faces.length; i++) {
			final Vector3 axis = b.transform().applyV(b.shape.faces[i].plane.normal);
			if (separating(axis, interval(a, axis), b.shape.intervals[i])) {
				arbiter.impulse = Vector3.Zero;
				arbiter.axis = axis;
				return null;
			}
		}

		// O(a.edges * b.edges * (a.vertices + b.vertices))
		for (final Line3 ea : a.shape.edges) {
			final Vector3 da = a.transform().applyV(ea.direction());
			for (final Line3 eb : b.shape.edges) {
				final Vector3 db = b.transform().applyV(eb.direction());
				final Vector3 axis = da.cross(db).unit();
				if (!axis.isFinite()) continue;
				if (separating(axis, interval(a, axis), interval(b, axis))) {
					arbiter.impulse = Vector3.Zero;
					arbiter.axis = axis;
					return null;
				}
			}
		}

		final Vector3 z = intersectBodyBody(a, b);
		if (z == null) return null;
		return new Contact(a, b, maxAxis, z, -maxDist, Arbiter.get(a, b));
	}

	private static Contact findContactSphereSphere(final Body a, final Body b) {
		final Vector3 d = a.position().sub(b.position());
		final float dist = d.length();

		Vector3 axis = d.div(dist);
		if (!axis.isFinite()) axis = Vector3.X;
		final Vector3 point = Vector3.average(a.position().add(b.shape.radius, axis), b.position().sub(a.shape.radius,
				axis)); // TODO fix this!

		return new Contact(a, b, axis, point, a.shape.radius + b.shape.radius - dist, Arbiter.get(a, b));
	}

	private static Contact findContactSphereBox(final Body a, final Body b) {
		if (true) throw new RuntimeException();

		final Vector3[] bn = new Vector3[3];
		if (separating(bn[0] = b.transform().m.colX(), interval(a, bn[0]), b.shape.boxX)) return null;
		if (separating(bn[1] = b.transform().m.colY(), interval(a, bn[1]), b.shape.boxY)) return null;
		if (separating(bn[2] = b.transform().m.colZ(), interval(a, bn[2]), b.shape.boxZ)) return null;

		final Vector3 axis = null; // TODO from sphere center to nearest feature on Box
		if (separating(axis, interval(a, axis), interval(b, axis))) return null;

		return new Contact(a, b, maxAxis, intersectBodyBody(a, b), -maxDist, Arbiter.get(a, b));
	}

	private static Contact findContactSpherePoly(final Body a, final Body b) {
		if (true) throw new RuntimeException();

		// O(a.faces * b.vertices)
		for (final Polygon3 element : b.shape.faces) {
			final Vector3 axis = b.transform().applyV(element.plane.normal);
			if (separating(axis, interval(a, axis), interval(b, axis))) return null;
		}

		final Vector3 axisS = null; // TODO from sphere center to nearest feature on Polygon 
		if (separating(axisS, new Interval(-a.shape.radius, a.shape.radius), interval(b, axisS))) return null;

		return new Contact(a, b, maxAxis, intersectBodyBody(a, b), -maxDist, Arbiter.get(a, b));
	}

	private static Contact findContactBoxBox(final Body a, final Body b) {
		final Vector3[] an = new Vector3[3];
		if (separating(an[0] = a.transform().m.colX(), a.shape.boxX, interval(b, an[0]))) return null;
		if (separating(an[1] = a.transform().m.colY(), a.shape.boxY, interval(b, an[1]))) return null;
		if (separating(an[2] = a.transform().m.colZ(), a.shape.boxZ, interval(b, an[2]))) return null;

		final Vector3[] bn = new Vector3[3];
		if (separating(bn[0] = b.transform().m.colX(), interval(a, bn[0]), b.shape.boxX)) return null;
		if (separating(bn[1] = b.transform().m.colY(), interval(a, bn[1]), b.shape.boxY)) return null;
		if (separating(bn[2] = b.transform().m.colZ(), interval(a, bn[2]), b.shape.boxZ)) return null;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				final Vector3 axis = an[i].cross(bn[j]).unit();
				if (!axis.isFinite()) continue;
				if (separating(axis, interval(a, axis), interval(b, axis))) return null;
			}

		final Vector3 p = intersectBodyBody(a, b);
		if (p == null) return null;
		return new Contact(a, b, maxAxis, p, -maxDist, Arbiter.get(a, b));
	}

	public static Contact findContact(final Body a, final Plane3 p) {
		// cheap bounding sphere test
		if (p.distance(a.position()) > a.shape.radius) return null;

		if (a.shape.hint == Hint.Sphere)
			return new Contact(a, World.Space, p.normal, a.position().sub(a.shape.radius, p.normal), a.shape.radius
					- p.distance(a.position()), Arbiter.get(a, World.Space));

		final float dist = a.interval(p.normal).min + p.offset;
		if (dist > 0) return null;
		final Vector3 z = intersectBodyPlane(a, p);
		if (z == null) return null;
		return new Contact(a, World.Space, p.normal, z, -dist, Arbiter.get(a, World.Space));
	}

	public static int met = 2;

	public static Vector3 intersectBodyBody(final Body a, final Body b) {
		switch (met) {
		case 1: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Transform3 transform = a.transform().icombine(b.transform());
			for (final Line3 e : a.shape.edges) {
				final Line3 p = Polyhedrons.convexClip(b.shape.faces, transform.apply(e));
				if (p != null) {
					list.add(b.transform().applyP(p.a));
					if (p.a != p.b) list.add(b.transform().applyP(p.b));
				}
			}
			for (final Line3 e : b.shape.edges) {
				final Line3 p = Polyhedrons.convexClip(a.shape.faces, transform.iapply(e));
				if (p != null) {
					list.add(a.transform().applyP(p.a));
					if (p.a != p.b) list.add(a.transform().applyP(p.b));
				}
			}

			return Vector3.average(list);
		}
		case 2: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Transform3 transform = a.transform().icombine(b.transform());
			for (final Line3 e : a.shape.edges) {
				final Line3 p = Polyhedrons.convexClip(b.shape.faces, transform.apply(e));
				if (p != null) {
					if (p.a != e.a) list.add(b.transform().applyP(p.a));
					if (p.b != p.a && p.b != e.b) list.add(b.transform().applyP(p.b));
				}
			}
			for (final Vector3 v : a.shape.vertices)
				if (Polyhedrons.side(b.shape.faces, transform.applyP(v)) != Side.Positive)
					list.add(a.transform().applyP(v));

			for (final Line3 e : b.shape.edges) {
				final Line3 p = Polyhedrons.convexClip(a.shape.faces, transform.iapply(e));
				if (p != null) {
					if (p.a != e.a) list.add(a.transform().applyP(p.a));
					if (p.b != p.a && p.b != e.b) list.add(a.transform().applyP(p.b));
				}
			}
			for (final Vector3 v : b.shape.vertices)
				if (Polyhedrons.side(a.shape.faces, transform.iapplyP(v)) != Side.Positive)
					list.add(b.transform().applyP(v));

			if (list.size() == 0) return null;
			return Vector3.average(list);
		}
		case 3: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Transform3 transform = a.transform().icombine(b.transform());
			for (final Line3 e : a.shape.edges) {
				final Line3 p = Polyhedrons.convexClip(b.shape.faces, transform.apply(e));
				if (p != null) {
					if (p.a != e.a) list.add(b.transform().applyP(p.a));
					if (p.b != p.a && p.b != e.b) list.add(b.transform().applyP(p.b));
				}
			}
			for (final Vector3 v : a.shape.vertices)
				if (Polyhedrons.side(b.shape.faces, transform.applyP(v)) != Side.Positive)
					list.add(a.transform().applyP(v));

			for (final Line3 e : b.shape.edges) {
				final Line3 p = Polyhedrons.convexClip(a.shape.faces, transform.iapply(e));
				if (p != null) {
					if (p.a != e.a) list.add(a.transform().applyP(p.a));
					if (p.b != p.a && p.b != e.b) list.add(a.transform().applyP(p.b));
				}
			}
			for (final Vector3 v : b.shape.vertices)
				if (Polyhedrons.side(a.shape.faces, transform.iapplyP(v)) != Side.Positive)
					list.add(b.transform().applyP(v));

			return new ConvexHull3(list).centerOfMass();
		}
		}
		return null;
	}

	public static Vector3 intersectBodyPlane(final Body a, final Plane3 plane) {
		switch (met) {
		case 1: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Plane3 xplane = a.transform().iapply(plane);
			for (final Line3 e : a.shape.edges) {
				final Line3 p = e.clip(xplane);
				if (p != null) {
					list.add(a.transform().applyP(p.a));
					if (p.b != p.a) list.add(a.transform().applyP(p.b));
				}
			}
			return Vector3.average(list);
		}
		case 2: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Plane3 xplane = a.transform().iapply(plane);
			for (final Line3 e : a.shape.edges) {
				final Line3 p = e.clip(xplane);
				if (p != null) {
					if (p.a != e.a) list.add(a.transform().applyP(p.a));
					if (p.b != p.a) if (p.b != e.b) list.add(a.transform().applyP(p.b));
				}
			}
			for (final Vector3 v : a.shape.vertices)
				if (xplane.distance(v) <= 0) list.add(a.transform().applyP(v));

			if (list.size() == 0) return null;
			return Vector3.average(list);
		}
		case 3: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Plane3 xplane = a.transform().iapply(plane);
			for (final Line3 e : a.shape.edges) {
				final Line3 p = e.clip(xplane);
				if (p != null) {
					if (p.a != e.a) list.add(a.transform().applyP(p.a));
					if (p.b != p.a) if (p.b != e.b) list.add(a.transform().applyP(p.b));
				}
			}
			for (final Vector3 v : a.shape.vertices)
				if (xplane.distance(v) <= 0) list.add(a.transform().applyP(v));

			return new ConvexHull3(list).centerOfMass();
		}
		case 4: {
			final Center c = new Center();
			final Plane3 xplane = a.transform().iapply(plane).flip();
			for (final Polygon3 face : a.shape.faces) {
				final Polygon3 f2 = face.clone();
				f2.clip(xplane);
				if (f2.vertices.length >= 3) c.add(f2.vertices);
			}
			return c.center();
		}
		}
		return null;
	}

	// Box/Box intersection
	private static void clip(final Body s, Vector3 a, Vector3 b) {
		// clipX
		if (a.x > b.x) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		final Interval x = s.shape.boxX;
		if (b.x < x.min || a.x > x.max) return;
		if (a.x < x.min) a = Vector3.linear(a, b, (x.min - a.x) / (b.x - a.x));
		if (b.x > x.max) b = Vector3.linear(a, b, (x.max - a.x) / (b.x - a.x));

		// clipY
		if (a.y > b.y) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		final Interval y = s.shape.boxY;
		if (b.y < y.min || a.y > y.max) return;
		if (a.y < y.min) a = Vector3.linear(a, b, (y.min - a.y) / (b.y - a.y));
		if (b.y > y.max) b = Vector3.linear(a, b, (y.max - a.y) / (b.y - a.y));

		// clipX
		if (a.z > b.z) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		final Interval z = s.shape.boxZ;
		if (b.z < z.min || a.z > z.max) return;
		if (a.z < z.min) a = Vector3.linear(a, b, (z.min - a.z) / (b.z - a.z));
		if (b.z > z.max) b = Vector3.linear(a, b, (z.max - a.z) / (b.z - a.z));

		// finish
		if (!a.equals(b)) setAdd(s.transform().applyP(b));
		setAdd(s.transform().applyP(a));
	}

	private final static int Vertices = 8, Edges = 12;
	private final static int[] Ea = { 0, 2, 0, 1, 4, 6, 4, 5, 0, 2, 1, 3 };
	private final static int[] Eb = { 1, 3, 2, 3, 5, 7, 6, 7, 4, 6, 5, 7 };

	private static Vector3[] temp;
	private static int tempSize;

	private static void setAdd(final Vector3 a) {
		for (int i = tempSize - 1; i >= Vertices; i--)
			if (temp[i].equals(a)) return;
		temp[tempSize++] = a;
	}

	// optimizovano do bola!
	public static Vector3[] intersectBoxBox(final Body a, final Body b) {
		final Transform3 transform = a.transform().icombine(b.transform());

		tempSize = Vertices;
		temp = new Vector3[Vertices + Edges * 4]; // NOTE can this be lowered?

		for (int i = 0; i < Vertices; i++)
			temp[i] = transform.applyP(a.shape.vertices[i]);
		for (int i = 0; i < Edges; i++)
			clip(b, temp[Ea[i]], temp[Eb[i]]);

		for (int i = 0; i < Vertices; i++)
			temp[i] = transform.iapplyP(b.shape.vertices[i]);
		for (int i = 0; i < Edges; i++)
			clip(a, temp[Ea[i]], temp[Eb[i]]);

		final Vector3[] q = new Vector3[tempSize - Vertices];
		assert q.length > 0;
		System.arraycopy(temp, Vertices, q, 0, tempSize - Vertices);
		return q;
	}
}