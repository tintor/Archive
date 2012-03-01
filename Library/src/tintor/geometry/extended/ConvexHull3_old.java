package tintor.geometry.extended;

import javax.media.opengl.GL;

import tintor.geometry.Plane3;
import tintor.geometry.Vector3;

public final class ConvexHull3_old {
	// Constants
	private static final float ComplanarTolerance = 1e-9;
	private static final boolean Profile = false;

	// Fields
	Vertex ivertices;
	Face ifaces;

	public final Polygon3[] faces;
	public final Vector3[] vertices;

	// Classes
	static class Edge {
		Vertex vertex;
		Face face;
		Edge next;
	}

	static class Vertex {
		Vector3 point;
		Edge edges;
		Vertex prev, next;

		Vertex(final ConvexHull3_old hull, final Vector3 point) {
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
		void remove(final ConvexHull3_old hull, final Vertex v) {
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

	static class Face {
		Vertex first;
		Face next, prev;

		Plane3 plane;
		Vector3 color = new Vector3(0, 0, 1);

		Face(final ConvexHull3_old hull, final Vertex... list) {
			plane = new Plane3(list[0].point, list[1].point, list[2].point);
			// attach faces to edges
			q: for (int j = list.length - 1, i = 0; i < list.length; j = i++) {
				final Vertex a = list[j], b = list[i];
				for (Edge e = a.edges; e != null; e = e.next)
					if (e.vertex == b) {
						e.face = this;
						continue q;
					}
				final Edge e = new Edge();
				e.vertex = b;
				e.face = this;
				e.next = a.edges;
				a.edges = e;
			}
			first = list[0];
			// add to linked list
			next = hull.ifaces;
			prev = null;
			if (next != null) next.prev = this;
			hull.ifaces = this;

			// merge complanar faces
			//			for (int j = list.length - 1, i = 0; i < list.length; j = i++) {
			//				Vertex a = list[j], b = list[i];
			//				Face f = b.getFace(a);
			//				if (f == null || f.plane.normal.dot(plane.normal) < 1 - ComplanarTolerance) continue;
			//
			//				Vertex z = b;
			//				while (true) {
			//					Edge e = z.get(f);
			//					e.face = this;
			//					z = e.vertex;
			//					if (z == b) break;
			//				}
			//
			//				a.remove(hull, b);
			//				b.remove(hull, a);
			//				// remove face from linked list
			//				if (f.next != null) f.next.prev = f.prev;
			//				if (f.prev != null) f.prev.next = f.next;
			//				if(hull.ifaces == f) hull.ifaces = f.next;
			//			}
		}

		// ! also removes free edges and free vertices left after face removal
		void remove(final ConvexHull3_old hull) {
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

	// TODO do not change input array
	// TODO make it more robust
	// TODO merge faces
	public ConvexHull3_old(final Vector3... w) {
		if (w.length < 4) throw new RuntimeException("Too little vertices!");

		long time;
		if (Profile) time = System.currentTimeMillis();

		// initial triangle
		{
			// TODO what if points are colinear?
			final Vertex a = new Vertex(this, w[0]);
			final Vertex b = new Vertex(this, w[1]);
			final Vertex c = new Vertex(this, w[2]);
			new Face(this, a, b, c);
			new Face(this, c, b, a);
		}

		// remove degeneric case
		for (int i = 3; i < w.length; i++)
			if (Math.abs(ifaces.plane.distance(w[i])) > ComplanarTolerance) {
				final Vector3 t = w[i];
				w[i] = w[3];
				w[3] = t;
				break;
			}

		// add points and expand hull
		for (int i = 3; i < w.length; i++) {
			boolean inside = true;
			for (Face face = ifaces; inside && face != null; face = face.next)
				inside = face.plane.distance(w[i]) <= ComplanarTolerance;
			if (inside) continue;

			final Vertex c = new Vertex(this, w[i]);
			for (Face face = ifaces; face != null; face = face.next)
				if (face.plane.distance(w[i]) > -ComplanarTolerance) face.remove(this);
			for (Vertex a = ivertices; a != null; a = a.next)
				if (a != c) for (Edge e = a.edges; e != null; e = e.next)
					if (e.vertex != c && e.face == null) new Face(this, a, e.vertex, c);
		}

		// extract faces to array
		int faceCount = 0;
		for (Face face = ifaces; face != null; face = face.next)
			faceCount++;

		faces = new Polygon3[faceCount];
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

		// extract vertices to array
		final VList list = new VList();
		for (Vertex v = ivertices; v != null; v = v.next)
			list.add(v.point);
		vertices = list.toArray();

		// profiling
		if (Profile) {
			time = time - System.currentTimeMillis();
			System.out.printf("convexhull=%sms faces=%s vertices=%s\n", time, faces.length, vertices.length);
		}
	}

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