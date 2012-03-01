package tintor.sokoban2.cell;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import tintor.sokoban2.common.Dir;

public final class Cell implements Comparable<Cell> {
	public final static class Edge {
		public final Cell cell;
		public final Dir dir;
		public final Edge next;

		public Edge(final Cell cell, final Dir dir, final Edge next) {
			this.cell = cell;
			this.dir = dir;
			this.next = next;
		}
	}

	public final static Comparator<Cell> ComparatorXY = new Comparator<Cell>() {
		@Override public int compare(Cell a, Cell b) {
			return a.y != b.y ? a.y - b.y : a.x - b.x;
		}
	};

	static int count, goals;

	public static int count() {
		return count;
	}

	public static int goals() {
		return goals;
	}

	private final Cell[] map = new Cell[Dir.values().length];
	private Edge edges;

	final Map<Cell, Integer> pushes = new HashMap<Cell, Integer>();
	int goalPushes = -1;
	boolean articulation;
	int id;

	public final int x, y;
	public final boolean goal, hole;

	public int flag;

	Cell(final int x, final int y, final boolean goal, final boolean hole) {
		this.x = x;
		this.y = y;
		this.goal = goal;
		this.hole = hole;
		id = count++;
	}

	public int id() {
		return id;
	}

	public boolean articulation() {
		return articulation;
	}

	public Integer pushes(final Cell c) {
		return pushes.get(c);
	}

	public int goalPushes() {
		return goalPushes;
	}

	public Edge edges() {
		return edges;
	}

	public int distance(final Dir dir) {
		final Cell a = get(dir);
		return a == null ? 0 : Math.max(Math.abs(a.x - x), Math.abs(a.y - y));
	}

	/** if box is moved into this cell deadlock is created */
	public boolean dead() {
		return goalPushes == Integer.MAX_VALUE;
	}

	public Cell north() {
		return get(Dir.North);
	}

	public Cell east() {
		return get(Dir.East);
	}

	public Cell south() {
		return get(Dir.South);
	}

	public Cell west() {
		return get(Dir.West);
	}

	public Cell get(final Dir d) {
		return map[d.ordinal()];
	}

	/** If a == null then detach in that dir. */
	void attach(final Dir d, final Cell a) {
		if (a != null) {
			if (a.get(d.opposite()) != null) a.get(d.opposite()).set(d, null);
			a.set(d.opposite(), this);
		}

		if (get(d) != null) get(d).set(d.opposite(), null);
		set(d, a);
	}

	void detachAll() {
		for (Edge e = edges; e != null; e = e.next) {
			e.cell.set(e.dir.opposite(), null);
			map[e.dir.ordinal()] = null;
		}
		edges = null;
	}

	private void set(final Dir d, final Cell a) {
		map[d.ordinal()] = a;
		edges = null;
		for (final Dir dir : Dir.values())
			if (get(dir) != null) edges = new Edge(get(dir), dir, edges);
	}

	public boolean tunnel() {
		return edges != null && edges.next != null && edges.next.next == null && edges.dir == edges.next.dir.opposite();
	}

	public boolean deadend() {
		return edges != null && edges.next == null;
	}

	@Override public String toString() {
		return String.format("[%s %s]", x, y);
	}

	@Override public int compareTo(final Cell a) {
		return id - a.id;
	}
}