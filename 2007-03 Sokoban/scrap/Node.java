package sokoban.solver;
import java.util.ArrayDeque;
import java.util.Deque;

import sokoban.Key;
import tintor.patterns.Immutable;

@Immutable public class Node implements Comparable<Node> {
	public final Key key;
	public final Node prev;
	public final int distance, total;

	public Node(Key key, Node prev, int step, int heuristic) {
		this.key = key;
		this.prev = prev;
		distance = (prev != null) ? prev.distance + step : 0;
		total = (heuristic != Integer.MAX_VALUE) ? distance + heuristic : Integer.MAX_VALUE;
	}
	
	public int compareTo(Node a) {
		return total - a.total;
	}
	
	public static Deque<Key> expand(Node a) {
		final Deque<Key> path = new ArrayDeque<Key>();
		while (a != null) {
			path.addFirst(a.key);
			a = a.prev;
		}
		return path;
	}
}