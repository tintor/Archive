package tintor.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AbstractUndirectedGraph<T, E, Z extends AbstractUndirectedGraph.Node<T, E>> extends Graph<T, E> {
	public static class Node<T, E> {
		protected final Map<T, E> _out = new HashMap<T, E>();
		public final Map<T, E> out = Collections.unmodifiableMap(_out);

		Map<T, E> in() {
			return out;
		}

		@Override public String toString() {
			final StringBuilder s = new StringBuilder();
			for (final T a : _out.keySet()) {
				if (s.length() > 0) s.append(", ");
				s.append(a);
				final E e = _out.get(a);
				if (e != null) s.append(":").append(e);
			}
			return s.toString();
		}

		@SuppressWarnings("unchecked") @Override public boolean equals(final Object o) {
			if (this == o) return true;
			try {
				return _out.equals(((Node) o)._out);
			} catch (final ClassCastException e) {
				return false;
			}
		}

		@Override public int hashCode() {
			return _out.hashCode();
		}
	}

	protected final Map<T, Z> _nodes = new HashMap<T, Z>();
	public final Map<T, Z> nodes = Collections.unmodifiableMap(_nodes);

	@Override public void add(final T a) {
		throw new RuntimeException();
	}

	public void add(final T a, final Z z) {
		if (contains(a)) throw new RuntimeException("Node allready exists!");
		_nodes.put(a, z);
	}

	@Override public void remove(final T a) {
		final Z z = _nodes.remove(a);
		if (z == null) return;
		for (final T b : z._out.keySet())
			_nodes.get(b)._out.remove(a);
	}

	@Override public final boolean contains(final T a) {
		return _nodes.containsKey(a);
	}

	@Override public void setEdge(final T a, final T b, final E e) {
		final Z za = _nodes.get(a), zb = _nodes.get(b);
		if (za == null || zb == null) throw new RuntimeException("Missing nodes!");
		za._out.put(b, e);
		zb._out.put(a, e);
	}

	@Override public void remove(final T a, final T b) {
		try {
			_nodes.get(a)._out.remove(b);
			_nodes.get(b)._out.remove(a);
		} catch (final NullPointerException e) {}
	}

	@Override public final E edge(final T a, final T b) {
		try {
			return _nodes.get(a)._out.get(b);
		} catch (final NullPointerException e) {
			return null;
		}
	}

	@Override public final boolean containsEdge(final T a, final T b) {
		final Z za = _nodes.get(a), zb = _nodes.get(b);
		return za != null && zb != null && za._out.containsKey(b);
	}

	@Override public final void clear() {
		_nodes.clear();
	}

	@Override public int size() {
		return _nodes.size();
	}

	@Override public int edgeCount() {
		int e = 0;
		for (final Z z : _nodes.values())
			e += z._out.size();
		return e;
	}

	@Override public final Map<T, E> in(final T a) {
		try {
			return _nodes.get(a).in();
		} catch (final NullPointerException e) {
			return null;
		}
	}

	@Override public final Map<T, E> out(final T a) {
		try {
			return _nodes.get(a)._out;
		} catch (final NullPointerException e) {
			return null;
		}
	}

	@Override public final Iterator<T> iterator() {
		return nodes.keySet().iterator();
	}

	@Override public Iterable<Edge<T, E>> edges() {
		final Iterator<T> nodeIterator = iterator();
		return new Iterable<Edge<T, E>>() {
			public Iterator<Edge<T, E>> iterator() {
				return new Iterator<Edge<T, E>>() {
					Iterator<T> nodeIt = nodeIterator;
					T node;
					Iterator<Map.Entry<T, E>> edgeIt = null;

					public boolean hasNext() {
						if (edgeIt == null) {
							if (!nodeIt.hasNext()) return false;
							node = nodeIt.next();
							edgeIt = _nodes.get(node)._out.entrySet().iterator();
						}

						// TODO Auto-generated method stub
						return false;
					}

					public Edge<T, E> next() {
						// TODO Auto-generated method stub
						return null;
					}

					public void remove() {
						// TODO Auto-generated method stub
						assert false;
					}
				};
			}
		};
	}

	@Override public String toString() {
		final StringBuilder s = new StringBuilder();
		for (final T a : _nodes.keySet())
			s.append(a).append(" -> {").append(_nodes.get(a)).append("}\n");
		return s.toString();
	}

	@SuppressWarnings("unchecked") @Override public boolean equals(final Object o) {
		if (this == o) return true;
		try {
			return _nodes.equals(((AbstractDirectedGraph) o)._nodes);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public int hashCode() {
		return _nodes.hashCode();
	}
}