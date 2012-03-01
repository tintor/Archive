package experimental;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Mesh2 {
	public static class Vertex {
		private final Set<Vertex> freeEdges = new HashSet<Vertex>();
		private final Map<Vertex, Face> faceEdges = new HashMap<Vertex, Face>();
		private final Map<Face, Vertex> next = new HashMap<Face, Vertex>();

		public Set<Vertex> freeEdges() {
			return Collections.unmodifiableSet(freeEdges);
		}

		public Map<Vertex, Face> faceEdges() {
			return Collections.unmodifiableMap(faceEdges);
		}

		public Map<Face, Vertex> next() {
			return Collections.unmodifiableMap(next);
		}
	}

	public static class Face implements Iterable<Vertex> {
		private Vertex first;

		public Iterator<Vertex> iterator() {
			final Face face = this;
			return new Iterator<Vertex>() {
				Vertex first = face.first, current = face.first;
				boolean more = true;

				public boolean hasNext() {
					return more;
				}

				public Vertex next() {
					Vertex b = current;
					current = b.next.get(face);
					more = current != first;
					return b;
				}

				public void remove() {}
			};
		}
	}

	private final Set<Vertex> ivertices = new HashSet<Vertex>();
	private final Set<Face> ifaces = new HashSet<Face>();
	// vertices with free edges
	private final Set<Vertex> iFreeVertices = new HashSet<Vertex>();

	public final Set<Vertex> vertices = Collections.unmodifiableSet(ivertices);
	public final Set<Face> faces = Collections.unmodifiableSet(ifaces);
	public final Set<Vertex> freeVertices = Collections.unmodifiableSet(iFreeVertices);

	public Vertex addVertex(Vertex v) {
		assert v.faceEdges.isEmpty() && v.freeEdges.isEmpty() && v.next.isEmpty();
		ivertices.add(v);
		return v;
	}

	public void removeVertex(Vertex a) {
		while (!a.faceEdges.isEmpty())
			removeFace(a.next.keySet().iterator().next(), false, false);
		while (!a.freeEdges.isEmpty())
			removeEdge(a.freeEdges.iterator().next(), a, false);
		ivertices.remove(a);
	}

	public void addEdge(Vertex a, Vertex b) {
		assert vertices.contains(a) && vertices.contains(b) && a != b;
		if (!a.freeEdges.contains(b) && !a.faceEdges.containsKey(b)) {
			assert !b.freeEdges.contains(a);
			assert !b.faceEdges.containsKey(a);

			a.freeEdges.add(b);
			iFreeVertices.add(a);
			b.freeEdges.add(a);
			iFreeVertices.add(b);
		}
	}

	public void removeEdge(Vertex a, Vertex b, boolean removeVertices) {
		// remove faces on both sides of edge first
		if (a.faceEdges.containsKey(b)) removeFace(a.faceEdges.get(b), false, false);
		if (b.faceEdges.containsKey(a)) removeFace(b.faceEdges.get(a), false, false);

		a.freeEdges.remove(b);
		if (a.freeEdges.isEmpty()) iFreeVertices.remove(a);
		b.freeEdges.remove(a);
		if (b.freeEdges.isEmpty()) iFreeVertices.remove(b);

		if (removeVertices) {
			if (a.freeEdges.isEmpty() && a.faceEdges.isEmpty()) removeVertex(a);
			if (b.freeEdges.isEmpty() && b.faceEdges.isEmpty()) removeVertex(b);
		}
	}

	private void faceAttachToEdge(Face face, Vertex a, Vertex b) {
		a.freeEdges.remove(b);
		if (a.freeEdges.isEmpty()) iFreeVertices.remove(a);
		a.faceEdges.put(b, face);
		a.next.put(face, b);
	}

	private Vertex faceDetachFromEdge(Face face, Vertex a) {
		Vertex b = a.next.get(face);
		a.freeEdges.add(b);
		iFreeVertices.add(b);
		a.faceEdges.remove(b);
		a.next.remove(face);
		return b;
	}

	public Face addFace(Face face, Vertex... vertices) {
		assert face.first == null;
		// check edges
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++)
			if (faceEdge(vertices[j], vertices[i])) return null; // there is face allready attached to this edge

		// create face
		for (int j = vertices.length - 1, i = 0; i < vertices.length; j = i++)
			faceAttachToEdge(face, vertices[j], vertices[i]);
		face.first = vertices[0];
		ifaces.add(face);
		return face;
	}

	public void removeFace(Face face, boolean removeEdges, boolean removeVertices) {
		Vertex a = face.first;
		while (a.next.containsKey(face)) {
			Vertex b = faceDetachFromEdge(face, a);
			if (removeEdges && b.freeEdges.contains(a)) removeEdge(a, b, removeVertices);
			a = b;
		}
		ifaces.remove(face);
	}

	public boolean faceEdge(Vertex a, Vertex b) {
		return a.faceEdges.containsKey(b);
	}

	//	public void attachVerticesToFace(Vertex[] vertices, Face face, Vertex after) {
	//		assert after.next.containsKey(face) && vertices.length > 0;
	//		// check vertices
	//		for (Vertex v : vertices)
	//			assert !v.next.containsKey(face);
	//		// check edges
	//		Vertex last = vertices[vertices.length - 1];
	//		assert !faceEdge(after, vertices[0]) && !faceEdge(last, after.next.get(face));
	//		for (int j = 0, i = 1; i < vertices.length; j = i++)
	//			assert !faceEdge(vertices[j], vertices[i]);
	//
	//		// attach vertices
	//		faceAttachToEdge(face, last, after.next.get(face));
	//		for (Vertex a : vertices) {
	//			faceAttachToEdge(face, after, a);
	//			after = a;
	//		}
	//	}
	//
	//	public void detachVerticesFromFace(Vertex a, int count, Face face, boolean removeEdges, boolean removeVertex) {
	//		assert a.next.containsKey(face) && count >= 1;
	//
	//		Vertex b = a;
	//		for (int i = 0; i < count; i++) {
	//			assert b.next.containsKey(face);
	//			b = b.next.get(face);
	//		}
	//		Vertex eb = b.next.get(face), ea = eb;
	//		while (ea.next.get(face) != a)
	//			ea = ea.next.get(face);
	//
	//		// attach
	//		faceAttachToEdge(face, ea, eb);
	//		face.first = ea;
	//		// detach
	//		for (int i = 0; i < count; i++)
	//			a = faceDetachFromEdge(face, a);
	//	}

	//	public void splitEdge(Vertex a, Vertex b, Vertex v) {
	//		// TODO
	//		throw new RuntimeException();
	//	}
	//
	//	public void mergeFaces(Face a, Face b) {
	//		// TODO
	//		throw new RuntimeException();
	//	}
	//
	//	public void mergeFaces(Vertex a, boolean removeEdges, boolean removeVertices) {
	//		// TODO
	//		throw new RuntimeException();
	//	}

	// Debug
	//	public void print() {
	//		Vertex[] a = vertices.toArray(new Vertex[] {});
	//		System.out.printf("vertices = %s\n", vertices.size());
	//		for(Vertex v : vertices) {
	//			for(Vertex b : v.freeEdges)
	//				if(find(a,v) < find(a,b))
	//					System.out.printf("edge %s %s\n", find(a,v), find(a,b));
	//			for(Vertex b : v.faceEdges.keySet())
	//				if(find(a,v) < find(a,b))
	//					System.out.printf("edge %s %s\n", find(a,v), find(a,b));
	//		}
	//		for(Face f : faces) {
	//			System.out.print("face");
	//			for(Vertex v : f)
	//				System.out.printf(" %s", find(a,v));
	//			System.out.println();
	//		}
	//	}

	//	private static <T> int find(T[] array, T e) {
	//		for(int i=0; i<array.length; i++)
	//			if(array[i] == e) return i;
	//		return -1;
	//	}
}