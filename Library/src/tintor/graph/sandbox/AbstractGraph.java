package tintor.graph.sandbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class AbstractGraph<T, E> implements MutableGraph<T, E> {
	public static class Node<T, E> {
		protected final Map<T, E> out;

		public Node(Map<T, E> out) {
			this.out = out;
		}
	}

	public static interface NodeFactory<T, E> {
		Node<T, E> createNode(T a);
	}

	protected final Map<T, Node<T, E>> nodes = new HashMap<T, Node<T, E>>();
	public NodeFactory<T, E> factory;

	public boolean autoCreateNodes = false;
	public boolean autoRemoveNodes = false;
	public boolean autoRemoveEdges = true;

	// Graph
	@Override public Iterator<T> iterator() {
		return nodes.keySet().iterator();
	}

	@Override public int size() {
		return nodes.size();
	}

	@Override public boolean contains(T a) {
		return nodes.containsKey(a);
	}

	@Override public Iterator<T> out(T a) {
		try {
			return nodes.get(a).out.keySet().iterator();
		} catch (NullPointerException e) {
			throw new NoSuchElementException();
		}
	}

	@Override public int outDegree(T a) {
		try {
			return nodes.get(a).out.size();
		} catch (NullPointerException e) {
			throw new NoSuchElementException();
		}
	}

	@Override public boolean contains(T a, T b) {
		try {
			return nodes.get(a).out.containsKey(b);
		} catch (NullPointerException e) {
			return false;
		}
	}

	@Override public E edge(T a, T b) {
		try {
			Map<T, E> map = nodes.get(a).out;
			if (!map.containsKey(b)) throw new NoSuchElementException();
			return map.get(b);
		} catch (NullPointerException e) {
			throw new NoSuchElementException();
		}
	}

	// MutableGraph
	@Override public MutableGraph<T, E> add(T a) {
		if (!nodes.containsKey(a)) nodes.put(a, factory.createNode(a));
		return this;
	}

	@Override public MutableGraph<T, E> remove(T a) {
		if (nodes.containsKey(a)) {
			// remove edges
			Iterator<T> i = in(a);
			if (i.hasNext() && !autoRemoveEdges) throw new RuntimeException();
			while (i.hasNext()) {
				i.next();
				i.remove();
			}
			// remove node
			nodes.remove(a);
		}
		return this;
	}

	@Override public MutableGraph<T, E> add(T a, T b) {
		add(a, b, null);
		return this;
	}

	@Override public MutableGraph<T, E> clear() {
		nodes.clear();
		return this;
	}

	// Object
	@Override public String toString() {
		StringBuilder s = new StringBuilder();
		for (T a : nodes.keySet()) {
			s.append(a).append(" -> {");
			Node<T, E> node = nodes.get(a);

			boolean first = true;
			for (T b : node.out.keySet()) {
				if (!first) s.append(", ");
				s.append(b);
				E e = node.out.get(b);
				if (e != null) s.append(":").append(e);
				first = false;
			}
			s.append("}\n");
		}
		return s.toString();
	}
}