package tintor.graph.sandbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import tintor.util.Each;

public class UndirectedGraph<T, E> extends AbstractGraph<T, E> {
	public interface NodeFactory<T, E> extends AbstractGraph.NodeFactory<T, E> {};

	public UndirectedGraph() {
		factory = new NodeFactory<T, E>() {
			public Node<T, E> createNode(T a) {
				return new Node<T, E>(new HashMap<T, E>());
			}
		};
	}

	// Graph
	@Override public Iterator<T> in(T a) {
		return out(a);
	}

	@Override public int inDegree(T a) {
		return outDegree(a);
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
			for (T b : new Each<T>(out(a)))
				if (System.identityHashCode(a) <= System.identityHashCode(b)) c++;
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
				nodes.get(b).out.put(a, e);
			} catch (NullPointerException ex) {
				throw new NoSuchElementException();
			}
		}
		return this;
	}

	@Override public MutableGraph<T, E> remove(T a, T b) {
		try {
			nodes.get(a).out.remove(b);
			nodes.get(b).out.remove(a);
			if (autoRemoveNodes) {
				if (outDegree(a) == 0 && inDegree(a) == 0) nodes.remove(a);
				if (outDegree(b) == 0 && inDegree(b) == 0) nodes.remove(b);
			}
		} catch (NullPointerException e) {}
		return this;
	}
}
