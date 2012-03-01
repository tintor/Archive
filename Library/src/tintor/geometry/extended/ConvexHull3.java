package tintor.geometry.extended;

import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;

import tintor.geometry.Plane3;
import tintor.geometry.Ray3;
import tintor.geometry.Vector3;
import tintor.geometry.extended.Polyhedrons.Center;

public final class ConvexHull3 {
	// Constants
	private static final float ComplanarTolerance = 1e-5f;

	// Fields (not private because of synthetic accessor warning)
	Vertex ivertices;
	Face ifaces;

	// Classes
	static class Edge {
		final Vertex vertex;
		Face face;
		Edge next; // next edge on this face

		Edge(final Vertex v, final Face f, final Edge n) {
			vertex = v;
			face = f;
			next = n;
		}
	}

	private static class Vertex {
		Vector3 point;
		Edge edges;
		Vertex prev, next; // linked list of all vertices

		Vertex(final ConvexHull3 hull, final Vector3 point) {
			this.point = point;
			// add to linked list
			next = hull.ivertices;
			if (next != null) next.prev = this;
			hull.ivertices = this;
		}

		Face getFace(final Vertex vertex) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.vertex == vertex) return e.face;
			return null;
		}

		Vertex getVertex(final Face face) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.face == face) return e.vertex;
			return null;
		}

		Edge get(final Face face) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.face == face) return e;
			return null;
		}

		// removes half edge
		void remove(final ConvexHull3 hull, final Vertex v) {
			if (edges.vertex == v) {
				edges = edges.next;
				if (edges == null) {
					// remove from linked list
					if (next != null) next.prev = prev;
					if (prev != null) prev.next = next;
					if (hull.ivertices == this) hull.ivertices = next;
				}
				return;
			}
			for (Edge e = edges; e.next != null; e = e.next)
				if (e.next.vertex == v) {
					e.next = e.next.next;
					break;
				}
		}
	}

	private static class Face {
		Vertex first;
		Face next, prev; // linked list of all faces

		Plane3 plane;
		Vector3 color = new Vector3(0, 0, 1);

		Face(final ConvexHull3 hull, final Vertex... list) {
			plane = new Plane3(list[0].point, list[1].point, list[2].point);
			// attach faces to edges
			q: for (int j = list.length - 1, i = 0; i < list.length; j = i++) {
				final Vertex a = list[j], b = list[i];
				for (Edge e = a.edges; e != null; e = e.next)
					if (e.vertex == b) {
						e.face = this;
						continue q;
					}
				a.edges = new Edge(b, this, a.edges);
			}
			first = list[0];
			// add to linked list
			next = hull.ifaces;
			prev = null;
			if (next != null) next.prev = this;
			hull.ifaces = this;

			// merge complanar faces
			for (int j = list.length - 1, i = 0; i < list.length; j = i++) {
				final Vertex a = list[j], b = list[i];
				final Face f = b.getFace(a);
				if (f == null || f.plane.normal.dot(plane.normal) < 1 - ComplanarTolerance) continue;

				Vertex z = b;
				while (true) {
					final Edge e = z.get(f);
					e.face = this;
					z = e.vertex;
					if (z == b) break;
				}

				a.remove(hull, b);
				b.remove(hull, a);
				// remove face from linked list
				if (f.next != null) f.next.prev = f.prev;
				if (f.prev != null) f.prev.next = f.next;
				if (hull.ifaces == f) hull.ifaces = f.next;
			}
		}

		// ! also removes free edges and free vertices left after face removal
		void remove(final ConvexHull3 hull) {
			Vertex a = first;
			do {
				Vertex b = null;
				assert a.edges != null;
				for (Edge e = a.edges; e != null; e = e.next)
					if (e.face == this) {
						e.face = null;
						b = e.vertex;
						break;
					}

				assert b != null;
				if (b.getFace(a) == null) {
					a.remove(hull, b);
					b.remove(hull, a);
				}
				a = b;
			} while (a != first);
			first = null;
			// remove from linked list
			if (next != null) next.prev = prev;
			if (prev != null) prev.next = next;
			if (hull.ifaces == this) hull.ifaces = next;
		}
	}

	public ConvexHull3(final Vector3... w) {
		this(Arrays.asList(w));
	}

	public ConvexHull3(final List<Vector3> w) {
		if (w.size() < 4) throw new RuntimeException("Too little vertices!");
		try {
			initialTetrahedron(w);
			for (int i = 1; i < w.size(); i++)
				addVertex(w.get(i));
		} catch (final NullPointerException e) {
			for (final Vector3 v : w)
				System.out.println(v);
			throw e;
		}
	}

	private void initialTetrahedron(final List<Vector3> w) {
		final Vertex a = new Vertex(this, w.get(0));
		final Vertex b = new Vertex(this, maxPointDistance(w));
		final Vertex c = new Vertex(this, maxRayDistance(b.point, w));

		// maxPlaneDistance from plane(a, b, c)
		final Plane3 plane = new Plane3(w.get(0), b.point, c.point);
		int max = 1;
		float maxDist = plane.distance(w.get(1));
		for (int i = 2; i < w.size(); i++) {
			final float dist = plane.distance(w.get(i));
			if (Math.abs(dist) > Math.abs(maxDist)) {
				maxDist = dist;
				max = i;
			}
		}
		final Vertex d = new Vertex(this, w.get(max));

		if (maxDist < 0) {
			new Face(this, a, b, c);
			new Face(this, b, a, d);
			new Face(this, c, b, d);
			new Face(this, a, c, d);
		} else {
			new Face(this, c, b, a);
			new Face(this, a, b, d);
			new Face(this, b, c, d);
			new Face(this, c, a, d);
		}
	}

	public Polygon3[] faces() {
		int faceCount = 0;
		for (Face face = ifaces; face != null; face = face.next)
			faceCount++;

		final Polygon3[] faces = new Polygon3[faceCount];
		int f = 0;
		for (Face face = ifaces; face != null; face = face.next) {
			final VList list = new VList();
			Vertex vertex = face.first;
			do {
				list.add(vertex.point);
				vertex = vertex.getVertex(face);
			} while (vertex != face.first);

			faces[f++] = new Polygon3(face.plane, null, list.toArray());
		}
		assert f == faceCount;

		return faces;
	}

	// TODO faster!
	public Vector3 centerOfMass() {
		final Center c = new Center();
		final VList list = new VList();
		for (Face face = ifaces; face != null; face = face.next) {
			list.clear();
			Vertex vertex = face.first;
			do {
				list.add(vertex.point);
				vertex = vertex.getVertex(face);
			} while (vertex != face.first);
			c.add(list.toArray());
		}
		return c.center();
	}

	public Vector3[] vertices() {
		final VList list = new VList();
		for (Vertex v = ivertices; v != null; v = v.next)
			list.add(v.point);
		return list.toArray();
	}

	public void addVertex(final Vector3 w) {
		if (inside(w)) return;
		final Vertex c = new Vertex(this, w);
		for (Face face = ifaces; face != null; face = face.next)
			if (face.plane.distance(w) > -ComplanarTolerance) face.remove(this);
		for (Vertex a = ivertices; a != null; a = a.next)
			if (a != c) for (Edge e = a.edges; e != null; e = e.next)
				if (e.vertex != c && e.face == null) new Face(this, a, e.vertex, c);
	}

	private boolean inside(final Vector3 w) {
		for (Face face = ifaces; face != null; face = face.next)
			if (face.plane.distance(w) > ComplanarTolerance) return false;
		return true;
	}

	// from w[0]
	private static Vector3 maxPointDistance(final List<Vector3> w) {
		int max = 1;
		float maxDist = w.get(1).distanceSquared(w.get(0));
		for (int i = 2; i < w.size(); i++) {
			final float dist = w.get(i).distanceSquared(w.get(0));
			if (dist > maxDist) {
				maxDist = dist;
				max = i;
			}
		}
		return w.get(max);
	}

	// from ray(w[0], b-w[0])
	private static Vector3 maxRayDistance(final Vector3 b, final List<Vector3> w) {
		final Ray3 ray = new Ray3(w.get(0), b.sub(w.get(0)));
		int max = 1;
		float maxDist = ray.distanceSquared(w.get(1));
		for (int i = 2; i < w.size(); i++) {
			final float dist = ray.distanceSquared(w.get(i));
			if (dist > maxDist) {
				maxDist = dist;
				max = i;
			}
		}
		return w.get(max);
	}

	// for debuging only
	public void render(final GL gl) {
		// faces
		for (Face face = ifaces; face != null; face = face.next) {
			gl.glColor3d(face.color.x, face.color.y, face.color.z);
			final Vector3 normal = face.plane.normal;
			gl.glBegin(GL.GL_POLYGON);
			Vertex vertex = face.first;
			do {
				gl.glNormal3d(normal.x, normal.y, normal.z);
				final Vector3 p = vertex.point;
				gl.glVertex3d(p.x, p.y, p.z);
				vertex = vertex.getVertex(face);
			} while (vertex != face.first);
			gl.glEnd();
		}

		// edges
		gl.glLineWidth(4);
		gl.glColor3d(1, 1, 1);
		gl.glBegin(GL.GL_LINES);
		for (Vertex a = ivertices; a != null; a = a.next)
			for (Edge e = a.edges; e != null; e = e.next) {
				gl.glVertex3d(a.point.x, a.point.y, a.point.z);
				final Vector3 p = e.vertex.point;
				gl.glVertex3d(p.x, p.y, p.z);
			}
		gl.glEnd();
		gl.glLineWidth(1);

		// vertices
		gl.glPointSize(8);
		gl.glBegin(GL.GL_POINTS);
		for (Vertex v = ivertices; v != null; v = v.next)
			gl.glVertex3d(v.point.x, v.point.y, v.point.z);
		gl.glEnd();
		gl.glPointSize(1);
	}
}