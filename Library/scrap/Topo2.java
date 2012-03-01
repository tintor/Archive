package experimental;

import java.util.Iterator;

public class Topo2 {
	private static class Edge {
		Vertex vertex;
		Face face;
		Edge next;

		Edge(Vertex v, Face f, Edge n) {
			vertex = v;
			face = f;
			next = n;
		}
	}

	public class Vertex implements Iterable<Vertex> {
		private Edge edges;
		private Vertex prev, next;

		public Vertex() {
			// add to linked list
			next = ivertices;
			ivertices = this;
		}

		public Face getFace(Vertex vertex) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.vertex == vertex) return e.face;
			return null;
		}

		public Vertex getVertex(Face face) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.face == face) return e.vertex;
			return null;
		}

		private void attachFace(Vertex vertex, Face face) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.vertex == vertex) {
					e.face = face;
					return;
				}
			edges = new Edge(vertex, face, edges);
		}

		private Vertex detachFace(Face face) {
			for (Edge e = edges; e != null; e = e.next)
				if (e.face == face) {
					e.face = null;
					return e.vertex;
				}
			return null;
		}

		private void remove(Vertex v) {
			if (edges.vertex == v) {
				edges = edges.next;
				if (edges == null) {
					// remove from linked list
					if(next != null) next.prev = prev;
					if(prev != null) prev.next = next;
					if(ivertices == this) ivertices = next;
				}
				return;
			}

			for (Edge e = edges; e.next != null; e = e.next)
				if (e.next.vertex == v) {
					e.next = e.next.next;
					break;
				}
		}

		public Iterator<Vertex> iterator() {
			return new Iterator<Vertex>() {
				Edge e = edges;

				public boolean hasNext() {
					return e != null;
				}

				public Vertex next() {
					Vertex v = e.vertex;
					e = e.next;
					return v;
				}

				public void remove() {
					throw new RuntimeException();
				}
			};
		}
	}

	public class Face implements Iterable<Vertex> {
		private Vertex first;
		private Face next, prev;

		public Face(Vertex... list) {
			for (int j = list.length - 1, i = 0; i < list.length; j = i++)
				list[j].attachFace(list[i], this);
			first = list[0];

			// add to linked list
			next = ifaces;
			ifaces = this;
		}

		// ! also removes free edges and free vertices left after face removal
		public void remove() {
			assert first != null;
			Vertex a = first;
			do {
				Vertex b = a.detachFace(this);
				if (b.getFace(a) == null) {
					a.remove(b);
					b.remove(a);
				}
				a = b;
			} while (a != first);
			first = null;

			// remove from linked list
			if(next != null) next.prev = prev;
			if(prev != null) prev.next = next;
			if(ifaces == this) ifaces = next;
		}

		public Iterator<Vertex> iterator() {
			final Face face = this;
			return new Iterator<Vertex>() {
				Vertex current = first;
				boolean more = true;

				public boolean hasNext() {
					return more;
				}

				public Vertex next() {
					if (!more) throw new RuntimeException();
					Vertex a = current;
					current = a.getVertex(face);
					if (current == first) more = false;
					return a;
				}

				public void remove() {
					throw new RuntimeException();
				}
			};
		}
	}

	private Vertex ivertices;
	private Face ifaces;

	public Iterable<Vertex> vertices() {
		return new Iterable<Vertex>() {
			public Iterator<Vertex> iterator() {
				return new Iterator<Vertex>() {
					Vertex current = ivertices;

					public boolean hasNext() {
						return current != null;
					}

					public Vertex next() {
						Vertex a = current;
						current = current.next;
						return a;
					}

					public void remove() {
						throw new RuntimeException();
					}
				};
			}
		};
	}

	public Iterable<Face> faces() {
		return new Iterable<Face>() {
			public Iterator<Face> iterator() {
				return new Iterator<Face>() {
					Face current = ifaces;

					public boolean hasNext() {
						return current != null;
					}

					public Face next() {
						Face a = current;
						current = current.next;
						return a;
					}

					public void remove() {
						throw new RuntimeException();
					}
				};
			}
		};
	}
}