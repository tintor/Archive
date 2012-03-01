package tintor.graph;

import java.util.Iterator;
import java.util.Map;

public abstract class Graph<T, E> implements Iterable<T> {
	public abstract void add(T a);

	public abstract void remove(T a);

	public abstract boolean contains(T a);

	public final void setEdge(T a, T b) {
		setEdge(a, b, null);
	}

	public abstract void setEdge(T a, T b, E e);

	public abstract void remove(T a, T b);

	public abstract E edge(T a, T b);

	public abstract boolean containsEdge(T a, T b);

	public abstract void clear();

	public abstract int size();

	public abstract int edgeCount();

	// TODO convert this to edge iterator
	public abstract Map<T, E> in(T a);

	public abstract Map<T, E> out(T a);

	/** Iterate over all nodes. */
	public abstract Iterator<T> iterator();

	public static class Edge<T, E> {
		public T a, b;
		public E e;
	}

	/** Iterate over all edges. */
	public abstract Iterable<Edge<T,E>> edges();

	// ---

//	static interface EdgePredicate<T, E> {
//		boolean evaluate(T a, T b, E e);
//	}
//
//	public void removeEdges(EdgePredicate<T, E> predicate) {
//		ArrayList<T> list = new ArrayList<T>();
//		for (T a : this) {
//			list.clear();
//			for(Map.Entry<T, E> e : out(a).entrySet())
//				if(predicate.evaluate(a, e.getKey(), e.getValue()));
//			for(T b : list)
//				remove(a, b);
//		}
//	}
}