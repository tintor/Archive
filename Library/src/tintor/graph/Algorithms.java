package tintor.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Algorithms {
	public static class Path<T> implements Comparable<Path<T>> {
		public Path<T> prev;
		public T node;
		public int distance;

		public Path(Path<T> prev, T node, int distance) {
			this.prev = prev;
			this.node = node;
			this.distance = distance;
		}

		public int compareTo(Path<T> p) {
			return distance - p.distance;
		}
	}

	public <T> Map<T, Path<T>> dijkstra(Graph<T, Integer> graph, T a, int distance) {
		// TODO
		if (true) throw new RuntimeException("incomplete!");

		Path<T> p = new Path<T>(null, a, distance);

		final Map<T, Path<T>> path = new HashMap<T, Path<T>>();
		path.put(a, p);

		final Queue<Path<T>> queue = new PriorityQueue<Path<T>>();
		queue.offer(p);

		while (queue.size() > 0) {
			p = queue.poll();
			for (Map.Entry<T, Integer> e : graph.out(p.node).entrySet()) {
				int d = p.distance + e.getValue();
				if (d < path.get(e.getKey()).distance) {
					path.get(e.getKey()).distance = d;
					path.get(e.getKey()).prev = p;
				}
			}
		}

		return path;
	}
}