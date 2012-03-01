package tintor.sokoban;

import java.util.Comparator;
import java.util.Set;

import tintor.XArrays;
import tintor.patterns.Immutable;
import tintor.properties.Property;
import tintor.search.DepthFirstSearch;
import tintor.search.Search;
import tintor.sokoban.Cell.Edge;

// TODO HashTable on hard drive

@Immutable public final class Key {
	public final static Property<Boolean> macroExpansion = Property.instance(Key.class, "macroExpansion", true);

	public static final Comparator<Key> comparator = new Comparator<Key>() {
		@Override public int compare(Key a, Key b) {
			if (a.total != b.total) return a.total - b.total;
			if (a.distance != b.distance) return b.distance - a.distance;
			return a.carries - b.carries;
		}
	};

	public final Cell agent;
	final Cell[] boxes;
	public final Cell boxRemoved, boxAdded;

	public Key prev;
	public final int distance;
	int total;
	final int carries;

	// used by set
	Key setNext;
	final int hash;

	public Key(final Cell agent, final Cell... boxes) {
		this(agent, checkIfSorted(boxes), null, 0, null, null);
	}

	private Key(final Cell agent, final Cell[] boxes, final Key prev, final int cost, final Cell boxRemoved,
			final Cell boxAdded) {
		this.boxes = boxes;
		this.agent = normalize(agent);

		this.prev = prev;
		distance = prev != null ? prev.distance + cost : 0;

		this.boxRemoved = boxRemoved;
		this.boxAdded = boxAdded;
		carries = prev == null ? 0 : prev.boxAdded == boxRemoved ? prev.carries : prev.carries + 1;

		int h = agent.hashCode();
		for (final Cell box : boxes)
			h = h * 953 + box.hashCode();
		h ^= h >>> 20 ^ h >>> 12;
		hash = h ^ h >>> 7 ^ h >>> 4;
	}

	public int boxes() {
		return boxes.length;
	}

	public Cell box(final int i) {
		return boxes[i];
	}

	private static Cell[] checkIfSorted(final Cell[] boxes) {
		for (int i = 1; i < boxes.length; i++)
			if (boxes[i - 1].compareTo(boxes[i]) > 0) throw new IllegalArgumentException("boxes must be sorted!");
		return boxes;
	}

	private Cell normalize(Cell agent) {
		// TODO is it faster to convert boxes to hash table before loop?
		final Search<Cell> search = new DepthFirstSearch<Cell>();
		search.add(agent);
		for (final Cell a : search) {
			if (a.compareTo(agent) < 0) agent = a;
			for (final Cell.Edge e : a.edges)
				if (acceptsAgent(e.cell)) search.add(e.cell);
		}
		return agent;
	}

	public boolean acceptsBox(final Cell a) {
		if (a.dead()) return false;
		if (a.goals > 1) {
			if (countBoxes(a) >= a.goals) return false;
		} else if (hasBox(a)) return false;
		return true;
	}

	boolean acceptsAgent(final Cell a) {
		return a.dispenser || !hasBox(a);
	}

	public Key pushBox(final Cell box, final Dir dir, final boolean macros) {
		final Cell src = box.get(dir.opposite()), dest = box.get(dir);

		Cell pushTo;
		if (box.dispenser && acceptsBox(src))
			pushTo = src;
		else if (dest != null && acceptsBox(dest))
			pushTo = dest;
		else
			return null;
		Key key = new Key(box, pushBox(box, pushTo), this, 1 + box.tunnel(dir), box, pushTo);

		if (macros && macroExpansion.get()) {
			// while box on articulation point and can be pushed on only one way (ignoring other boxes)
			// - box is not on goal, 
			// - box is on goal, behind is a coral with at least one goal without box
			final Set<Cell> reachable = key.reachable();
			loop: while (true) {
				final Cell b = key.boxAdded;
				if (!b.articulation) break;

				Dir d = null;
				for (final Cell.Edge e : b.edges)
					if (reachable.contains(b.get(e.dir.opposite())) && !e.cell.dead()) {
						if (d != null) break loop;
						d = e.dir;
					}

				if (d == null && b.isGoal()) break;
				if (d == null || !key.acceptsBox(b.get(d))) {
					if (Monitor.monitor != null) Monitor.monitor.macro_deadlocks++;
					return null;
				}

				if (b.isGoal()) {
					boolean emptyGoals = false;

					final Search<Cell> search = new DepthFirstSearch<Cell>();
					search.reached.add(b); // ensures that search stays inside
					search.add(b.get(d));
					for (final Cell a : search) {
						if (a.isGoal() && !key.hasBox(a)) {
							emptyGoals = true;
							break;
						}
						for (final Cell.Edge e : a.edges)
							search.add(e.cell);
					}

					if (!emptyGoals) break loop;
				}

				final int preCost = key.distance - (prev != null ? prev.distance : 0);
				key = new Key(b, key.pushBox(b, b.get(d)), prev, preCost + 1 + b.tunnel(d), box, b.get(d));
				reachable.add(b);
			}
		}

		if (Deadlock.partialTest(key)) return null;
		return key;
	}

	public Cell[] addBox(final Cell a) {
		return XArrays.sortedAdd(boxes, a);
	}

	public Cell[] removeBox(final Cell a) {
		return XArrays.remove(boxes, a);
	}

	public Cell[] pushBox(final Cell box, final Cell dest) {
		final Cell[] b = XArrays.copy(boxes);
		XArrays.sortedReplace(b, box, dest);
		return b;
	}

	public int countBoxes(final Cell cell) {
		return XArrays.sortedCount(boxes, cell);
	}

	public boolean hasBox(final Cell cell) {
		return XArrays.sortedContains(boxes, cell);
	}

	public Set<Cell> reachable() {
		final Search<Cell> search = new DepthFirstSearch<Cell>();
		search.add(agent);
		for (final Cell a : search)
			for (final Edge e : a.edges)
				if (acceptsAgent(e.cell)) search.add(e.cell);
		return search.reached;
	}

	public boolean isGoal() {
		for (final Cell box : boxes)
			if (box.goals == 0) return false;
		return true;
	}

	public boolean equals(final Key key) {
		return hash == key.hash && agent == key.agent && XArrays.equals(boxes, key.boxes);
	}

	@Override public boolean equals(final Object o) {
		try {
			return equals((Key) o);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public int hashCode() {
		return hash;
	}

	@Override public String toString() {
		final Image image = new Image(agent);

		// draw unreachable cells
		for (final Cell a : Util.cells(agent))
			image.set(a, a.goals > 0 ? Code.UnreachableGoal : Code.Unreachable);

		// draw reachable cells
		for (final Cell a : reachable())
			image.set(a, a.code());

		// draw boxes
		for (final Cell a : boxes)
			if (!a.dispenser && a.goals <= 1) image.set(a, a.goals == 1 ? Code.BoxOnGoal : Code.Box);

		// convert to String
		final StringBuilder b = new StringBuilder();
		b.append("distance=").append(distance);
		b.append(" total=").append(total);
		b.append(" carries=").append(carries);
		b.append('\n');
		image.render(b);
		return b.toString();
	}

	public String renderMinimalPushes(final Cell cell) {
		final Image image = new Image(cell);
		for (final Cell a : Util.cells(agent))
			image.set(a, cell.pushes.containsKey(a) ? Util.hex(cell.pushes.get(a)) : Code.Unreachable);
		return image.toString();
	}

	public String renderArticulations() {
		final Image image = new Image(agent);
		for (final Cell a : Util.cells(agent))
			image.set(a, a.code());
		return image.toString();
	}
}