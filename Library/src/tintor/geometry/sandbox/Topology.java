package tintor.geometry.sandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tintor.util.NonCashingHashSet;

public class Topology {
	final Set<Vertex> _vertices = new NonCashingHashSet<Vertex>();
	public final Set<Vertex> vertices = Collections.unmodifiableSet(_vertices);

	final Set<Face> _faces = new NonCashingHashSet<Face>();
	public final Set<Face> faces = Collections.unmodifiableSet(_faces);

	public class Vertex {
		final Map<Face, Vertex> _next = new HashMap<Face, Vertex>();
		public final Map<Face, Vertex> next = Collections.unmodifiableMap(_next);

		final Map<Face, Vertex> _prev = new HashMap<Face, Vertex>();
		public final Map<Face, Vertex> prev = Collections.unmodifiableMap(_prev);

		public Vertex() {
			_vertices.add(this);
		}

		public void delete() {
			_vertices.remove(this);

			for (final Face f : _next.keySet()) {
				f.connect(prev(f), next(f));
				f._vertices.remove(this);
			}

			_next.clear();
			_prev.clear();
		}

		public Vertex prev(final Face face) {
			return _prev.get(face);
		}

		public Vertex next(final Face face) {
			return _next.get(face);
		}
	}

	public class Face implements Iterable<Vertex> {
		final Set<Vertex> _vertices = new NonCashingHashSet<Vertex>();
		public final Set<Vertex> vertices = Collections.unmodifiableSet(_vertices);

		public Face(final Vertex... w) {
			_faces.add(this);

			if (w.length > 0) {
				Vertex a = w[w.length - 1];
				for (final Vertex b : w) {
					_vertices.add(b);
					connect(a, b);
					a = b;
				}
			}
		}

		public void delete() {
			_faces.remove(this);

			for (final Vertex a : _vertices) {
				a._next.remove(this);
				a._prev.remove(this);
			}
			_vertices.clear();
		}

		public void remove(final Vertex a) {
			final boolean c = _vertices.remove(a);
			assert c;

			connect(a._prev.remove(this), a._next.remove(this));
		}

		/** Remove from a (inclusive) to b (exclusive). */
		public void remove(Vertex a, final Vertex b) {
			if (a == b) return;
			connect(prev(a), b);
			do {
				_vertices.remove(a);
				a._prev.remove(this);
				a = a._next.remove(this);
			} while (a != b);
		}

		public Iterator<Vertex> iterator() {
			return vertices.iterator();
		}

		/** get n vertices starting from a */
		public Vertex[] vertices(Vertex a, final int n) {
			assert contains(a);

			final Vertex[] v = new Vertex[n];
			for (int i = 0; i < n; i++) {
				v[i] = a;
				a = next(a);
			}
			return v;
		}

		public Vertex[] vertices(Vertex a, final Vertex b) {
			assert contains(a);

			final ArrayList<Vertex> list = new ArrayList<Vertex>();
			final Vertex s = a;
			while (a != b) {
				list.add(a);
				a = next(a);
				if (a == s) break;
			}
			return list.toArray(new Vertex[list.size()]);
		}

		public boolean contains(final Vertex a) {
			return next(a) != null;
		}

		public boolean contains(final Vertex a, final Vertex b) {
			return next(a) == b;
		}

		public boolean containsAny(final Vertex... w) {
			for (final Vertex v : w)
				if (contains(v)) return true;
			return false;
		}

		public void add(final Vertex... w) {
			assert w.length == 0;
			assert !containsAny(w);

			Vertex a = w[w.length - 1];
			for (final Vertex b : w) {
				_vertices.add(b);
				connect(a, b);
				a = b;
			}
		}

		public void addBefore(final Vertex a, final Vertex v) {
			assert contains(a);
			assert !contains(v);

			connect(prev(a), v);
			connect(v, a);
			_vertices.add(v);
		}

		public void addAfter(final Vertex a, final Vertex v) {
			assert contains(a);
			assert !contains(v);

			connect(v, next(a));
			connect(a, v);
			_vertices.add(v);
		}

		public void addAfter(Vertex a, final Vertex... w) {
			assert w.length > 0;
			assert contains(a);
			assert !containsAny(w);

			connect(w[w.length - 1], next(a));
			for (final Vertex v : w) {
				_vertices.add(v);
				connect(a, v);
				a = v;
			}
		}

		public void addBefore(final Vertex b, final Vertex... w) {
			addAfter(prev(b), w);
		}

		public void expand(final Face f, Vertex a, final Vertex b) {
			assert a != b;

			remove(next(a), b);
			do {
				final Vertex i = f.next(a);
				assert !contains(i);
				connect(a, i);
				a = i;
			} while (a != b);
		}

		public Vertex next(final Vertex v) {
			return v.next(this);
		}

		public Vertex prev(final Vertex v) {
			return v.prev(this);
		}

		void connect(final Vertex a, final Vertex b) {
			a._next.put(this, b);
			b._prev.put(this, a);
		}
	}
}