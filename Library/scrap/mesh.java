package experimental;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Mesh<V extends Mesh.Vertex, F extends Mesh.Face> implements Iterable<F> {
	public static class Vertex {
		private final Set<Vertex> freeEdges = new HashSet<Vertex>();
		private final Map<Vertex, Face> faceEdges = new HashMap<Vertex, Face>();
		private final Map<Face, Vertex> next = new HashMap<Face, Vertex>();

		public Vertex next(Face face) {
			return next.get(face);
		}
	}

	public static class Face {
		private Vertex first;

		private void attachToEdge(Vertex a, Vertex b) {
			a.freeEdges.remove(b);
			a.faceEdges.put(b, this);
			a.next.put(this, b);
		}

		private Vertex detachFromEdge(Vertex a) {
			Vertex b = a.next.get(this);
			a.freeEdges.add(b);
			a.faceEdges.remove(b);
			a.next.remove(this);
			return b;
		}

		public Vertex first() {
			return first;
		}
	}

	private final Set<V> vertices = new HashSet<V>();
	private final Set<F> faces = new HashSet<F>();

	private final F[] e;

	public Mesh(F[] e) {
		this.e = e;
		assert e.length == 0;
	}

	public F[] faces() {
		return faces.toArray(e);
	}

	public Iterator<F> iterator() {
		// TODO: change to read-only iterator
		return faces.iterator();
	}

	public V addVertex(V v) {
		assert v.faceEdges.isEmpty() && v.freeEdges.isEmpty() && v.next.isEmpty();
		vertices.add(v);
		return v;
	}

	public void removeVertex(Vertex a) {
		while (!a.faceEdges.isEmpty())
			removeFace(a.next.keySet().iterator().next(), false, false);
		while (!a.freeEdges.isEmpty())
			removeEdge(a.freeEdges.iterator().next(), a, false);
		vertices.remove(a);
	}

	public void addEdge(Vertex a, Vertex b) {
		if (!a.freeEdges.contains(b) && !a.faceEdges.containsKey(b)) {
			assert !b.freeEdges.contains(a);
			assert !b.faceEdges.containsKey(a);

			a.freeEdges.add(b);
			b.freeEdges.add(a);
		}
	}

	public void removeEdge(Vertex a, Vertex b, boolean removeVertices) {
		// remove faces on both sides of edge first
		if (a.faceEdges.containsKey(b)) removeFace(a.faceEdges.get(b), false, false);
		if (b.faceEdges.containsKey(a)) removeFace(b.faceEdges.get(a), false, false);

		a.freeEdges.remove(b);
		b.freeEdges.remove(a);

		if (removeVertices) {
			if (a.freeEdges.isEmpty() && a.faceEdges.isEmpty()) removeVertex(a);
			if (b.freeEdges.isEmpty() && b.faceEdges.isEmpty()) removeVertex(a);
		}
	}

	public F addFace(F face, Vertex... vertices) {
		assert face.first == null;
		// check edges
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++)
			if (faceEdge(vertices[j], vertices[i])) return null; // there is face allready attached to this edge

		// create face
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++)
			face.attachToEdge(vertices[j], vertices[i]);
		face.first = vertices[0];
		faces.add(face);
		return face;
	}

	public void removeFace(Face face, boolean removeEdges, boolean removeVertices) {
		Vertex a = face.first;
		while (a.next.containsKey(face)) {
			Vertex b = face.detachFromEdge(a);
			if (removeEdges && b.freeEdges.contains(a)) removeEdge(a, b, removeVertices);
			a = b;
		}
		faces.remove(face);
	}

	public boolean faceEdge(Vertex a, Vertex b) {
		return a.faceEdges.containsKey(b);
	}

	public void attachVerticesToFace(Vertex[] vertices, Face face, Vertex after) {
		assert after.next.containsKey(face) && vertices.length > 0;
		// check vertices
		for (Vertex v : vertices)
			assert !v.next.containsKey(face);
		// check edges
		Vertex last = vertices[vertices.length - 1];
		assert !faceEdge(after, vertices[0]) && !faceEdge(last, after.next.get(face));
		for (int j = 0, i = 1; i < vertices.length; j = i++)
			assert !faceEdge(vertices[j], vertices[i]);

		// attach vertices
		face.attachToEdge(last, after.next.get(face));
		for (Vertex a : vertices) {
			face.attachToEdge(after, a);
			after = a;
		}
	}

	public void detachVerticesFromFace(Vertex a, int count, Face face, boolean removeEdges, boolean removeVertex) {
		assert a.next.containsKey(face) && count >= 1;

		Vertex b = a;
		for (int i = 0; i < count; i++) {
			assert b.next.containsKey(face);
			b = b.next.get(face);
		}
		Vertex eb = b.next.get(face), ea = eb;
		while (ea.next.get(face) != a)
			ea = ea.next.get(face);

		// attach
		face.attachToEdge(ea, eb);
		face.first = ea;
		// detach
		for (int i = 0; i < count; i++)
			a = face.detachFromEdge(a);
	}
}