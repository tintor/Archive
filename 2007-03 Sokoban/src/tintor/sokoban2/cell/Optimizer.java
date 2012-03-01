package tintor.sokoban2.cell;

import java.util.Collections;
import java.util.List;

import tintor.search.Articulations;
import tintor.search.Search;
import tintor.sokoban2.Key;
import tintor.sokoban2.Util;
import tintor.sokoban2.common.CellSearch;
import tintor.sokoban2.common.Dir;

public class Optimizer {
	public static boolean compressTunnels = true;
	public static boolean removeDeadends = true;

	public static Key optimize(Key level) {
		if (level == null) return null;

		calculateMinimalPushes(level);
		if (removeDeadends) level = removeDeadends(level);
		markArticulations(level.agent);
		if (compressTunnels) level = compressTunnels(level);
		return level;
	}

	// TODO agent could be moved outside of deadend
	private static Key removeDeadends(final Key level) {
		final Search<Cell> search = new Search<Cell>();
		search.addFirst(level.agent);
		for (final Cell a : search) {
			final boolean deadend = a.deadend() && !a.goal && a != level.agent && !level.hasBox(a);
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				if (deadend)
					search.addFirstForced(e.cell);
				else
					search.addFirst(e.cell);
			if (deadend) a.detachAll();
		}
		fixIDs(level);
		return level;
	}

	// TODO optimize!
	private static Key compressTunnels(final Key level) {
		final CellSearch search = new CellSearch(level.agent);
		for (final Cell c : search) {
			if (c != level.agent && !c.goal && c.tunnel() && !level.hasBox(c)) {
				final Cell a = c.edges().cell, b = c.edges().next.cell;

				if (a.dead() && b.dead() || a.tunnel() || a.deadend() || b.tunnel() || b.deadend() || a.articulation
						&& b.articulation && c.articulation) {
					if (a.y == b.y)
						a.attach(a.x < b.x ? Dir.East : Dir.West, b);
					else
						a.attach(a.y < b.y ? Dir.South : Dir.North, b);

					search.force(a);
					search.force(b);
				}
			}

			for (Cell.Edge e = c.edges(); e != null; e = e.next)
				search.add(e.cell);
		}

		fixIDs(level);
		return level;
	}

	// FIXME bugy!
	// TODO optimize very slow! O(cells^2) => O(goals*cells)
	public static void calculateMinimalPushes(final Key level) {
		final List<Cell> cells = Util.cellList(level.agent);
		for (final Cell a : cells)
			a.goalPushes = -1;

		final Search<Cell> cellSearch = new Search<Cell>();
		final Search<Key> keySearch = new Search<Key>();
		for (final Cell start : cells) {
			start.pushes.clear();
			keySearch.addLast(new Key(start, start));

			for (final Key keyA : keySearch) {
				if (!start.pushes.containsKey(keyA.box(0))) start.pushes.put(keyA.box(0), (int) keyA.distance);

				cellSearch.addFirst(keyA.agent);
				for (final Cell cell : cellSearch)
					for (Cell.Edge e = cell.edges(); e != null; e = e.next)
						if (keyA.hasBox(e.cell)) {
							final Key keyB = keyA.pushBox(e.cell, e.dir);
							if (keyB != null) keySearch.addLast(keyB);
						} else
							cellSearch.addFirst(e.cell);
				cellSearch.clear();
			}
			keySearch.clear();
		}

		for (final Cell a : cells)
			a.goalPushes = Integer.MAX_VALUE;

		for (final Cell g : cells)
			if (g.goal || g.hole) for (final Cell a : cells) {
				final Integer p = a.pushes.get(g);
				if (p != null && p < a.goalPushes) a.goalPushes = p;
			}
	}

	private static void markArticulations(final Cell root) {
		new CellArticulations(root) {
			@Override protected void articulation(final Cell a) {
				a.articulation = true;
			}
		};
	}

	private static void fixIDs(final Key level) {
		final List<Cell> list = Util.cellList(level.agent);
		Cell.count = Cell.goals = 0;
		Collections.sort(list, Cell.ComparatorXY);
		for (final Cell c : list) {
			c.id = Cell.count;
			Cell.count += 1;
			if (c.goal) Cell.goals += 1;
		}
	}

	//	public static Set<Cell> getArticulations(final Cell root) {
	//		final Set<Cell> result = new HashSet<Cell>();
	//		new CellArticulations(root) {
	//			@Override protected void articulation(final Cell a) {
	//				result.add(a);
	//			}
	//		};
	//		return result;
	//	}

	public static abstract class CellArticulations extends Articulations<Cell> {
		public CellArticulations(final Cell root) {
			super(root);
		}

		@Override protected final void eachEdge(final Cell from, final Cell a) {
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				edge(from, a, e.cell);
		}
	}
}