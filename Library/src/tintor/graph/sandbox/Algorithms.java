package tintor.graph.sandbox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import tintor.graph.sandbox.Graph;

public class Algorithms {
	@SuppressWarnings("unchecked") static <T> List<T> negativeCostCycle(Graph<T, Integer> graph) {
		final Set<T> ignored = new HashSet<T>();
		for (T v : graph) {
			final Map<T, Integer> dist = new HashMap<T, Integer>();
			final Map<T, T> prev = new HashMap<T, T>();
			final Queue<T> queue = new ArrayDeque<T>();
			final Set<T> queued = new HashSet<T>();

			dist.put(v, 0);
			queue.offer(v);

			while (queue.size() > 0) {
				final T a = queue.poll();
				queued.remove(a);

				for (T b : graph.out(a))
					if (!ignored.contains(b)) {
						int d = dist.get(a) + graph.edge(a, b);
						if (!dist.containsKey(b) || dist.get(b) > d) {
							if (prev.get(b) == a) {
								// found negative cost cycle
								final List<T> cycle = new ArrayList<T>();
								cycle.add(b);
								for (T c = prev.get(b); c != b; c = prev.get(c))
									cycle.add(c);
								Collections.reverse(cycle);
								return cycle;
							}
							dist.put(b, d);
							prev.put(b, a);
							if (queued.add(b)) queue.add(b);
						}
					}
			}

			ignored.add(v);
		}
		return null;
	}
}