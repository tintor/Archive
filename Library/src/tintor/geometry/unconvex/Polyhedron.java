package tintor.geometry.unconvex;

import tintor.geometry.Plane3;
import tintor.geometry.Ray3;
import tintor.geometry.Vector3;

public class Polyhedron {
	static class Face {
		Plane3 pa, pb, pc;
		Plane3 plane;

		public boolean zone(final Vector3 p) {
			return pa.distance(p) >= 0 && pb.distance(p) >= 0 && pc.distance(p) >= 0;
		}

		public Vector3 closest(final Vector3 p) {
			return p.sub(plane.distance(p), plane.normal);
		}
	}

	static class Edge {
		Plane3 pa, pb;
		Ray3 ray;
		Plane3 plane;

		public boolean zone(final Vector3 p) {
			return pa.distance(p) >= 0 && pb.distance(p) >= 0;
		}

		public Vector3 closest(final Vector3 p) {
			return ray.point(ray.nearest(p));
		}
	}

	static class Vertex {
		Vector3 a;
		Plane3 plane;
	}

	private Face[] faces;
	private Edge[] edges;
	private Vertex[] vertices;

	public Vector3 closestFromInside(final Vector3 point) {
		float md = Float.POSITIVE_INFINITY;

		Face closestFace = null;
		for (final Face face : faces)
			if (face.zone(point)) {
				final float d = Math.abs(face.plane.distance(point));
				if (d < md) { // TODO remove abs
					md = d;
					closestFace = face;
				}
			}

		float mds = md * md;

		Edge closestEdge = null;
		for (final Edge edge : edges)
			if (edge.zone(point)) {
				final float ds = edge.ray.distanceSquared(point);
				if (ds < mds) {
					mds = ds;
					closestEdge = edge;
				}
			}

		Vertex closestVertex = null;
		for (final Vertex vertex : vertices) {
			final float ds = vertex.a.distanceSquared(point);
			if (ds < mds) {
				mds = ds;
				closestVertex = vertex;
			}
		}

		if (closestVertex != null) return closestVertex.plane.distance(point) > 0 ? null : closestVertex.a;
		if (closestEdge != null) return closestEdge.plane.distance(point) > 0 ? null : closestEdge.closest(point);
		assert closestFace != null;
		return closestFace.plane.distance(point) > 0 ? null : closestFace.closest(point);
	}
}