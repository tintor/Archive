package tintor.sokoban;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tintor.XArrays;

public final class Cell implements Comparable<Cell> {
	public final static class Edge {
		public final Cell cell;
		public final Dir dir;

		public Edge(final Cell cell, final Dir dir) {
			this.cell = cell;
			this.dir = dir;
		}
	}

	public final int x, y;
	boolean dispenser, articulation;
	int goals;

	private final Cell[] map = new Cell[Dir.values().length];
	// DO NOT MODIFY from outside!
	Edge[] edges = new Edge[0];

	// final Map<Cell, Integer> moves = new HashMap<Cell, Integer>();
	final Map<Cell, Integer> pushes = new HashMap<Cell, Integer>();
	int goalPushes = -1;

	Cell(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public Map<Cell, Integer> pushes() {
		return Collections.unmodifiableMap(pushes);
	}

	public Edge[] edges() {
		return edges;
	}

	public boolean isGoal() {
		return goals > 0;
	}

	public boolean isArticulation() {
		return articulation;
	}

	public char code() {
		if (articulation) return Code.Articulation;
		if (dispenser) return Code.Dispenser;
		if (goals > 1) return Code.GoalRoom;
		if (goals == 1) return Code.Goal;
		return Code.Space;
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

	public void invariant() {
		assert edges != null;
		for (final Edge element : edges)
			assert map[element.dir.ordinal()] == element.cell;
		int c = 0;
		for (final Dir dir : Dir.values())
			if (map[dir.ordinal()] != null) c++;
		assert c == edges.length;

		assert goalPushes >= -1;
	}

	@Override public int compareTo(final Cell a) {
		return y != a.y ? y - a.y : x - a.x;
	}

	private void set(final Dir d, final Cell a) {
		for (int i = 0; i < edges.length; i++)
			if (edges[i].dir == d) {
				edges = XArrays.removeAt(edges, i);
				break;
			}
		map[d.ordinal()] = a;
		if (a != null) edges = XArrays.add(edges, new Edge(a, d));
	}

	public Cell get(final Dir d) {
		//invariant();
		assert d != null;
		return map[d.ordinal()];
	}

	// if a == null then detach
	void attach(final Dir d, final Cell a) {
		if (a != null)
			a.set(d.opposite(), this);
		else if (get(d) != null) get(d).set(d.opposite(), null);
		set(d, a);
	}

	void detachAll() {
		for (final Edge e : edges) {
			final Cell b = get(e.dir);
			b.set(e.dir.opposite(), null);
			map[e.dir.ordinal()] = null;
		}
		edges = new Edge[0];
	}

	public boolean dead() {
		return dispenser || goalPushes == Integer.MAX_VALUE;
	}

	@Override public String toString() {
		return String.format("[%s %s]", x, y);
	}

	// Tunnels
	private final int[] tunnel = new int[Dir.values().length];

	public void detachTunnelSegment() {
		assert isTunnel();
		if (edges[0].dir.deltaY() == 0)
			west().attach(Dir.East, east());
		else
			north().attach(Dir.South, south());

		for (final Edge e : edges) {
			e.cell.tunnel[e.dir.opposite().ordinal()] += 1 + tunnel[e.dir.opposite().ordinal()];
			map[e.dir.ordinal()] = null;
		}
		edges = new Edge[0];
	}

	public int tunnel(final Dir dir) {
		return tunnel[dir.ordinal()];
	}

	public boolean isTunnel() {
		return edges.length == 2 && edges[0].dir == edges[1].dir.opposite();
	}
}