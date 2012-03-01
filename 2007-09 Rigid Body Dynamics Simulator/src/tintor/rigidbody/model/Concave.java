package tintor.rigidbody.model;

import java.util.ArrayList;
import java.util.List;

import tintor.geometry.Line3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.geometry.sandbox.Polygon3;

public class Concave {
	Polygon3[] faces;
	Vector3[] vertices;
	Line3[] edges;

	@SuppressWarnings("null") static List<Contact> contacts(final Concave A, final Concave B) {
		final List<Contact> contacts = new ArrayList<Contact>();

		// vertices of A in B
		for (final Vector3 vertex : A.vertices) {
			Polygon3 nearestFace = null;
			double minDist = Double.MAX_VALUE;
			boolean inside = false;

			for (final Polygon3 face : B.faces) {
				final double dist = face.plane.distance(vertex);
				if (Math.abs(dist) < minDist && face.zone(vertex) != Side.Positive) {
					minDist = Math.abs(dist);
					inside = dist <= 0;
					nearestFace = face;
				}
			}
			if (inside) contacts.add(new Contact(null, null, nearestFace.plane.normal, vertex, minDist, null));
		}

		// edges
		//		for (final Line3 edgeA : A.edges)
		//			for (final Line3 edgeB : B.edges){
		//				double d = edgeA.distance(edgeB);
		//			}

		return contacts;
	}
}