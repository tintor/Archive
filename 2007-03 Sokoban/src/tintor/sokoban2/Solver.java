package tintor.sokoban2;

import java.util.ArrayDeque;

import tintor.heap.Heap;
import tintor.sokoban2.cell.Cell;
import tintor.sokoban2.common.CellSearch;
import tintor.sokoban2.keyset.MemoryKeySet;
import tintor.util.Block;

// TODO eliminate stupid moves
// TODO expert rules for pushing into goal rooms
// TODO greedy and goal driven search 

public class Solver {
	public static Key astar(final Key level) {
		// trivial tests
		if (Util.goals(level.agent).size() < level.boxes()) {
			System.out.println("not enough goals!");
			System.out.println(level);
			return null;
		}
		//		if (Deadlock.fullTest(level)) {
		//			System.out.println("start position is deadlock!");
		//			System.out.println(level);
		//			return null;
		//		}

		// stats
		int nondead = 0;
		for (final Cell a : Util.cellSet(level.agent))
			if (!a.dead()) nondead++;
		System.out.printf("there are %d nondead cells and %d boxes, maximum of %d keys!\n", nondead, level.boxes(), Util
				.combinations(nondead, level.boxes()));

		// key search
		final MemoryKeySet reached = new MemoryKeySet();
		//final KeyQueue queue = new KeyQueue(); // priority queue
		final Heap<Key> queue = new Heap<Key>(Key.comparator, 1000); // priority queue
		Monitor._queue = queue;
		Monitor._set = reached;

		final Heuristic heuristic = new Heuristic.MatchingGoals(level);

		// cell search
		final CellSearch cells = new CellSearch();

		// go!
		reached.add(level);
		queue.offer(level);
		while (queue.size() > 0) {
			final Key key = queue.poll();

			if (key.isGoal()) return key;

			cells.init(key.agent);
			for (final Cell a : cells) {
				for (Cell.Edge e = a.edges(); e != null; e = e.next)
					if (key.hasBox(e.cell)) {
						final Key newKey = key.pushBox(e.cell, e.dir);
						if (newKey != null) {
							Monitor._branches++;
							if (reached.add(newKey)) {
								final int h = heuristic.estimate(newKey);
								if (h != Integer.MAX_VALUE) {
									newKey.total = (short) (newKey.distance + h);
									queue.offer(newKey);
								} else
									Monitor._hash_deadlocks++;
							}
						}
					} else
						cells.add(e.cell);
				Monitor._cells++;
			}

			Monitor._lastKey = key;
			Monitor._keys++;
		}
		return null;
	}

	public static Key ida(final Key level) {
		final Heuristic heuristic = new Heuristic.MatchingGoals(level);
		final Keys keys = new Keys();

		int limit = heuristic.estimate(level);
		boolean cutoff = false;
		do {
			System.out.println("limit = " + limit);

			final ArrayDeque<Key> deque = new ArrayDeque<Key>();
			deque.add(level);
			while (deque.size() > 0) {
				keys.key = deque.pollFirst();
				if (keys.key.isGoal()) return keys.key;

				for (final Key k : keys) {
					final int h = heuristic.estimate(k);
					if (h == Integer.MAX_VALUE) continue;

					k.total = (short) (keys.key.total + h);
					if (k.total > limit)
						cutoff = true;
					else
						deque.addFirst(k);
				}
			}

			limit += 1;
		} while (cutoff);

		return null;
	}

	static class Keys extends Block<Key> {
		public Key key;
		final CellSearch cells = new CellSearch();

		@Override protected void run() {
			cells.init(key.agent);
			for (final Cell a : cells)
				for (Cell.Edge e = a.edges(); e != null; e = e.next)
					if (key.hasBox(e.cell)) {
						final Key newKey = key.pushBox(e.cell, e.dir);
						if (newKey != null) yield(newKey);
					} else
						cells.add(e.cell);
		}
	}
}