package tintor.sokoban2;

import java.math.BigInteger;
import java.util.*;

import tintor.search.Search;
import tintor.sokoban2.cell.Cell;
import tintor.sokoban2.common.CellSearch;

public class Util {
	public static Set<Cell> cellSet(final Cell start) {
		final Set<Cell> set = new HashSet<Cell>();
		final CellSearch search = new CellSearch(start);
		for (final Cell a : search) {
			set.add(a);
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				search.add(e.cell);
		}
		return set;
	}

	public static List<Cell> cellList(final Cell start) {
		final List<Cell> list = new ArrayList<Cell>();
		final CellSearch search = new CellSearch(start);
		for (final Cell a : search) {
			list.add(a);
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				search.add(e.cell);
		}
		return list;
	}

	public static Cell get(final Cell root, final int x, final int y) {
		final Search<Cell> search = new Search<Cell>(root);
		for (final Cell a : search) {
			if (a.x == x && a.y == y) return a;
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				search.add(e.cell);
		}
		return null;
	}

	public static List<Cell> goals(final Cell root) {
		final List<Cell> list = new ArrayList<Cell>();
		final Search<Cell> search = new Search<Cell>(root);
		for (final Cell a : search) {
			if (a.goal) list.add(a);
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				search.add(e.cell);
		}
		return list;
	}

	public static Deque<Key> expand(Key key) {
		final Deque<Key> result = new ArrayDeque<Key>();
		while (key != null) {
			result.offerFirst(key);
			key = key.prev;
		}
		return result;
	}

	static BigInteger combinations(final int n, final int k) {
		BigInteger c = BigInteger.ONE;
		for (int i = 0; i < k; i++)
			c = c.multiply(BigInteger.valueOf(n - i));
		for (int i = 2; i <= k; i++)
			c = c.divide(BigInteger.valueOf(i));
		return c;
	}
}