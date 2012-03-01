package sokoban.solver;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import sokoban.Level;

public class AStar {
	public static void main(String[] args) {
		Node result = solve(Level.load("levels/microban.soko", "Level 1"));

		if (result == null) System.out.println("no solution");
		for (Key k : Node.expand(result))
			System.out.println(k);
	}

	static Node solve(Level level) {
		return solve(level, new Key(level.agent, level.boxes), Heuristic.PushDistance, Goal.Standard);
	}
	
	static Node solve(Level level, Key s, Heuristic heuristic, Goal goal) {
		final Node start = new Node(s, null, 0, 0);
		final Set<Key> visited = new HashSet<Key>();
		final Queue<Node> queue = new PriorityQueue<Node>();

		visited.add(start.key);
		queue.add(start);

		while (!queue.isEmpty()) {
			final Node n = queue.remove();
			if (goal.test(n.key)) return n;

			for (Key.Pair p : n.key.macroPushes(true))
				if (!visited.contains(p.key)) {
					final Node m = new Node(p.key, n, p.pushes, heuristic.estimate(p.key));
					visited.add(m.key);
					queue.add(m);
				}
		}
		return null;
	}
}