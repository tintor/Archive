package tintor.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AbstractDirectedGraph<T, E, Z extends AbstractDirectedGraph.Node<T, E>> extends
		AbstractUndirectedGraph<T, E, Z> {
	public static class Node<T, E> extends AbstractUndirectedGraph.Node<T, E> {
		final Map<T, E> _in = new HashMap<T, E>();
		public final Map<T, E> in = Collections.unmodifiableMap(_in);

		@Override Map<T, E> in() {
			return in;
		}
	}

	@Override public void remove(final T a) {
		final Z z = _nodes.remove(a);
		if (z == null) return;
		for (final T b : z._in.keySet())
			_nodes.get(b)._out.remove(a);
		for (final T b : z._out.keySet())
			_nodes.get(b)._in.remove(a);
	}

	@Override public void setEdge(final T a, final T b, final E e) {
		if (!contains(a) || !contains(b)) throw new RuntimeException("Missing nodes!");
		_nodes.get(a)._out.put(b, e);
		_nodes.get(b)._in.put(a, e);
	}

	@Override public void remove(final T a, final T b) {
		try {
			_nodes.get(a)._out.remove(b);
			_nodes.get(b)._in.remove(a);
		} catch (final NullPointerException e) {}
	}
}