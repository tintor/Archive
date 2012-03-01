package tintor.graph.sandbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class DirectedGraph<T, E> extends AbstractGraph<T, E> {
	public static class Node<T, E> extends AbstractGraph.Node<T, E> {
		protected final Map<T, E> in;

		public Node(Map<T, E> in, Map<T, E> out) {
			super(out);
			this.in = in;
		}
	}

	public interface NodeFactory<T, E> extends AbstractGraph.NodeFactory<T, E> {};

	public DirectedGraph() {
		factory = new NodeFactory<T, E>() {
			public Node<T, E> createNode(T a) {
				return new Node<T, E>(new HashMap<T, E>(), new HashMap<T, E>());
			}
		};
	}

	// Graph
	private Node<T, E> node(T a) {
		return (Node<T, E>) nodes.get(a);
	}

	@Override public Iterator<T> in(T a) {
		try {
			return node(a).in.keySet().iterator();
		} catch (NullPointerException e) {
			throw new NoSuchElementException();
		}
	}

	@Override public int inDegree(T a) {
		try {
			return node(a).in.size();
		} catch (NullPointerException e) {
			throw new NoSuchElementException();
		}
	}

	@Override public Iterator<Edge<T, E>> edges() {
		return new Iterator<Edge<T, E>>() {
			//Iterator<T> nodeIt = iterator();
			//Iterator<Map.Entry<T, E>> edgeIt = null;

			public boolean hasNext() {
				assert false;
				return false;
			}

			public Edge<T, E> next() {
				assert false;
				return null;
			}

			public void remove() {
				assert false;
			}
		};
	}

	@Override public int edgeCount() {
		int c = 0;
		for (T a : this)
			c += nodes.get(a).out.size();
		return c;
	}

	// MutableGraph
	@Override public MutableGraph<T, E> add(T a, T b, E e) {
		if (autoCreateNodes) {
			add(a);
			add(b);
			nodes.get(a).out.put(b, e);
		} else {
			try {
				if (!nodes.containsKey(b)) throw new NoSuchElementException();
				nodes.get(a).out.put(b, e);
			} catch (NullPointerException ex) {
				throw new NoSuchElementException();
			}
		}
		return this;
	}

	@Override public MutableGraph<T, E> remove(T a, T b) {
		try {
			nodes.get(a).out.remove(b);
			if (autoRemoveNodes) {
				if (outDegree(a) == 0 && inDegree(a) == 0) nodes.remove(a);
				if (outDegree(b) == 0 && inDegree(b) == 0) nodes.remove(b);
			}
		} catch (NullPointerException e) {}
		return this;
	}
}