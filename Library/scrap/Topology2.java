package geometry.exp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.FastHashSet;

public class Topology2 {
	private final Set<Vertex> _allVertices = new FastHashSet<Vertex>();
	public final Set<Vertex> allVertices = Collections.unmodifiableSet(_allVertices);

	private final Set<Face> _faces = new FastHashSet<Face>();
	public final Set<Face> faces = Collections.unmodifiableSet(_faces);

	public class Vertex {
		private final Set<Face> _faces = new FastHashSet<Face>();
		public final Set<Face> faces = Collections.unmodifiableSet(_faces);

		public Vertex() {
			Topology2.this._allVertices.add(this);
		}

		public void delete() {
			Topology2.this._allVertices.remove(this);

//			for (Face f : _faces) {
//			}
			_faces.clear();
		}
	}

	public static class FVertex {
		public final Vertex prev, next;
		private FVertex(Vertex p, Vertex n) {
			prev = p;
			next = n;
		}
	}
	
	public class Face implements Iterable<Vertex> {
		private final Map<Vertex, FVertex> _vertices = new HashMap<Vertex, FVertex>();
		public final Map<Vertex, FVertex> vertices = Collections.unmodifiableMap(_vertices);

		public Face(Vertex... w) {
			Topology2.this._faces.add(this);

			if (w.length > 0) {
				Vertex a = w[w.length > 1 ? w.length - 2 : 0], b = w[w.length - 1];
				for (Vertex c : w) {
					_vertices.put(b, new FVertex(a, c));
					b._faces.add(this);
					a = b; b = c;
				}
			}
		}

		public void delete() {
			Topology2.this._faces.remove(this);

			for (Vertex a : _vertices.keySet())
				a._faces.remove(this);
			_vertices.clear();
		}

		public Iterator<Vertex> iterator() {
			return vertices.keySet().iterator();
		}

		public void remove(Vertex v) {
			FVertex z = _vertices.remove(v);
			assert z != null;
			connect(z.prev, z.next);
			v._faces.remove(this);
		}

		/** get n vertices starting from a */
		public Vertex[] vertices(Vertex a, int n) {
			Vertex[] v = new Vertex[n];
			for (int i = 0; i < n; i++) {
				v[i] = a;
				a = next(a);
			}
			return v;
		}

		public Vertex[] vertices(Vertex a, Vertex b) {
			ArrayList<Vertex> list = new ArrayList<Vertex>();
			while (a != b) {
				list.add(a);
				a = next(a);
			}
			return list.toArray(new Vertex[list.size()]);
		}

		public boolean contains(Vertex v) {
			return next(v) != null;
		}

		public boolean contains(Vertex a, Vertex b) {
			return next(a) == b;
		}

		public boolean containsAny(Vertex... w) {
			for (Vertex v : w)
				if (contains(v)) return true;
			return false;
		}

		public void add(Vertex... w) {
			assert w.length == 0;
			assert !containsAny(w);

			Vertex a = w[w.length - 1];
			for (Vertex b : w) {
				//b._faces.
				//_vertices.add(b);
				connect(a, b);
				a = b;
			}
		}

		public void addBefore(Vertex a, Vertex v) {
			assert contains(a);
			assert !contains(v);

			connect(prev(a), v);
			connect(v, a);
			//_vertices.add(v);
		}

		public void addAfter(Vertex a, Vertex v) {
			assert contains(a);
			assert !contains(v);

			connect(v, next(a));
			connect(a, v);
			//_vertices.add(v);
		}

		public void addAfter(Vertex a, Vertex... w) {
			assert w.length > 0;
			assert contains(a);
			assert !containsAny(w);

			connect(w[w.length - 1], next(a));
			for (Vertex v : w) {
				//_vertices.add(v);
				connect(a, v);
				a = v;
			}
		}

		public void addBefore(Vertex b, Vertex... w) {
			addAfter(prev(b), w);
		}

		private Vertex next(Vertex v) {
			//return v._next.get(this);
			return null;
		}

		private Vertex prev(Vertex v) {
//			return v._prev.get(this);
			return null;
		}

		private void connect(Vertex a, Vertex b) {
//			a._next.put(this, b);
//			b._prev.put(this, a);
		}

		public void merge(Face f) {
			assert this != f;
			
			ArrayList<Vertex> common = new ArrayList<Vertex>();
			//for(Vertex a : _vertices) if(f.contains(a)) common.add(a);

			for(Vertex a : common) {
				Vertex b = a;
				while(f.contains(next(b), b)) b = next(b); // NOTE optimize
				while(f.contains(a, prev(a))) a = next(a);
				if(a == b) continue;
				
				
			}
		}
	}
}