package tintor.graph.sandbox;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

class SimpleGraphSearch<T> extends Search<T> {
	private static class Info<T> {
		int distance = Integer.MAX_VALUE;
		T prev;
		boolean opened;
	}

	public Graph<T, ?> graph;
	public T goal;
	private final Queue<T> queue;
	private final Map<T, Info<T>> info = new HashMap<T, Info<T>>();

	public SimpleGraphSearch(Queue<T> queue) {
		this.queue = queue;
	}

	@Override protected Iterator<T> getSuccessors(T a) {
		return graph.out(a);
	}

	@Override protected boolean isGoal(T a) {
		return a.equals(goal);
	}

	@Override protected T queueRemove() throws NoSuchElementException {
		T a = queue.remove();
		info.get(a).opened = true;
		return a;
	}

	@Override protected void queueUpdate(T a, T prev) {
		Info<T> i = info.get(a);
		if (i == null) {
			i = new Info<T>();
			info.put(a, i);
			queue.add(a);
		} else if (i.opened) return;

		int d = info.get(prev).distance + 1;
		if (d < i.distance) {
			i.prev = prev;
			i.distance = d;
		}
	}

	public SimpleGraphSearch<T> addStart(T a, int distance) {
		Info<T> i = info.get(a);
		if (i == null) {
			queue.add(a);
			i = new Info<T>();
			info.put(a, i);
		}
		i.distance = distance;
		return this;
	}

	public void reset() {
		queue.clear();
		info.clear();
		goal = null;
	}

	@SuppressWarnings("unchecked") public Deque<T> getPath(T a) {
		if (a == null) throw new NullPointerException();
		Deque<T> deque = new ArrayDeque<T>();
		while (a != null) {
			deque.addFirst(a);
			Info<T> i = info.get(a);
			if (i == null || !i.opened) throw new RuntimeException("No such path");
			a = i.prev;
		}
		return deque;
	}
}

class InformedGraphSearch<T> extends Search<T> {
	private static class Node<T> implements Comparable<Node<T>> {
		final T key;
		final int h;

		int g = Integer.MAX_VALUE;
		int f = Integer.MAX_VALUE;
		Node<T> prev;
		boolean opened;

		public Node(T key, InformedGraphSearch<T> search) {
			this.key = key;
			this.h = search.heuristic(key);
		}

		public int compareTo(Node<T> a) {
			return f - a.f;
		}
	}

	public Graph<T, ?> graph;
	public T goal;
	public Map<T, Integer> heuristic;
	private final java.util.Queue<Node<T>> queue = new PriorityQueue<Node<T>>();
	private final Map<T, Node<T>> info = new HashMap<T, Node<T>>();

	protected int heuristic(T a) {
		return heuristic.get(a);
	}

	protected int edgeCost(T a, T b) {
		return (Integer) graph.edge(a, b);
	}

	@Override protected boolean isGoal(T a) {
		return a.equals(goal);
	}

	@Override protected Iterator<T> getSuccessors(T a) {
		return graph.out(a);
	}

	@Override protected T queueRemove() throws NoSuchElementException {
		Node<T> a = queue.remove();
		a.opened = true;
		return a.key;
	}

	@Override protected void queueUpdate(T a, T p) {
		Node<T> ia = info.get(a), ip = info.get(p);
		int g = ip.g + edgeCost(p, a);

		if (ia == null) {
			ia = new Node<T>(a, this);
			info.put(a, ia);
			queue.add(ia);

			ia.prev = ip;
			ia.g = g;
			ia.f = ia.g + ia.h;
		} else if (!ia.opened && g < ia.g) {
			ia.prev = ip;
			ia.g = g;
			ia.f = ia.g + ia.h;

			// TODO queue decrease
		}
	}

	public void addStart(T a, int g) {
		Node<T> i = info.get(a);
		if (i == null) {
			queue.add(i);
			i = new Node<T>(a, this);
			info.put(a, i);
		}
		i.g = g;
	}

	public void reset() {
		queue.clear();
		info.clear();
		goal = null;
	}

	@SuppressWarnings("unchecked") public Deque<T> getPath(T a) {
		Node<T> i = info.get(a);
		if (i == null) throw new NullPointerException();
		Deque<T> deque = new ArrayDeque<T>();
		while (i != null) {
			if (!i.opened) throw new RuntimeException("No such path");
			deque.addFirst(i.key);
			i = i.prev;
		}
		return deque;
	}
}

public class Sandbox {
	public static void main(String[] args) {
		AbstractGraph<String, Object> graph = new DirectedGraph<String, Object>();
		graph.autoCreateNodes = true;
		graph.add("a", "b").add("b", "e").add("e", "d").add("b", "c");

		final Map<String, Integer> heuristic = new HashMap<String, Integer>();
		heuristic.put("a", 8);
		heuristic.put("b", 7);
		heuristic.put("c", 0);
		heuristic.put("e", 1);
		heuristic.put("d", 1);

		InformedGraphSearch<String> search = new InformedGraphSearch<String>();
		search.graph = graph;
		search.heuristic = heuristic;
		search.addStart("a", 0);
		search.goal = "c";
		search.run();

		for (String a : search.getPath(search.goal))
			System.out.println(a);
	}
}