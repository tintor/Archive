package tintor.sokoban2;

import java.util.Comparator;
import java.util.Set;

import tintor.XArrays;
import tintor.search.Search;
import tintor.sokoban2.cell.Cell;
import tintor.sokoban2.common.CellSearch;
import tintor.sokoban2.common.Code;
import tintor.sokoban2.common.Dir;
import tintor.sokoban2.common.Image;

public class Key {
	public static final Comparator<Key> comparator = new Comparator<Key>() {
		@Override public int compare(Key a, Key b) {
			int d;

			d = a.total - b.total;
			if (d != 0) return d;

			d = b.goalBoxes() - a.goalBoxes();
			if (d != 0) return d;

			d = a.carries - b.carries;
			if (d != 0) return d;

			d = b.distance - a.distance;
			if (d != 0) return d;
			return 0;
		}
	};

	int goalBoxes() {
		int c = 0;
		for (final Cell b : _boxes)
			if (b.goal) c += 1;
		return c;
	}

	public final Cell agent;
	private final Cell[] _boxes; // must be sorted
	public final Cell boxRemoved, boxAdded;

	public Key prev;
	public final short distance;
	short total;
	final short carries;

	public Key setNext;

	public Key(final Cell agent, final Cell... boxes) {
		this(agent, boxes, null, 0, null, null);
	}

	Key(final Cell agent, final Cell[] boxes, final Key prev, final int cost, final Cell boxRemoved, final Cell boxAdded) {
		_boxes = boxes;
		this.agent = normalizeAgent(agent);

		this.prev = prev;
		distance = (short) (prev != null ? prev.distance + cost : 0);

		this.boxAdded = boxAdded;
		this.boxRemoved = boxRemoved;
		carries = (short) (prev == null ? 0 : prev.boxAdded == boxRemoved ? prev.carries : prev.carries + 1);
	}

	public Key pushBox(final Cell src, final Dir dir) {
		final Cell dest = src.get(dir);
		if (dest == null || !acceptsBox(dest)) return null;
		if (dest.hole) return new Key(src, XArrays.remove(_boxes, src), this, src.distance(dir), src, null);

		final Key key = new Key(src, pushBox(src, dest), this, src.distance(dir), src, dest);
		if (Deadlock.partialTest(key)) return null;
		return key;
	}

	private Cell normalizeAgent(Cell ag) {
		final CellSearch search = new CellSearch(ag);
		for (final Cell b : _boxes)
			search.reached(b);
		for (final Cell a : search) {
			if (a.compareTo(ag) < 0) ag = a;
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				search.add(e.cell);
		}
		return ag;
	}

	public boolean hasBox(final Cell a) {
		return XArrays.sortedContains(_boxes, a);
	}

	public Cell box(final int i) {
		return _boxes[i];
	}

	public int boxes() {
		return _boxes.length;
	}

	public boolean acceptsBox(final Cell a) {
		return !a.dead() && !hasBox(a);
	}

	private boolean acceptsAgent(final Cell a) {
		return !hasBox(a);
	}

	private Cell[] pushBox(final Cell box, final Cell dest) {
		final Cell[] b = XArrays.copy(_boxes);
		XArrays.sortedReplace(b, box, dest);
		return b;
	}

	public boolean isGoal() {
		for (final Cell box : _boxes)
			if (!box.goal) return false;
		return true;
	}

	public Set<Cell> reachable() {
		final Search<Cell> search = new Search<Cell>(agent);
		for (final Cell a : search)
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				if (acceptsAgent(e.cell)) search.add(e.cell);
		return search.reached;
	}

	public byte[] toBytes() {
		final byte[] b = new byte[1 + _boxes.length];
		b[0] = (byte) agent.id();
		for (int i = 0; i < _boxes.length; i++)
			b[i + 1] = (byte) _boxes[i].id();
		return b;
	}

	@Override public String toString() {
		final Image image = new Image(agent);

		final Set<Cell> reachable = reachable();

		final CellSearch search = new CellSearch(agent);
		for (final Cell a : search) {
			final char c = reachable.contains(a) ? (a.goal ? Code.Goal : a.hole ? Code.Hole : Code.Space)
					: a.goal ? Code.UnreachableGoal : Code.Unreachable;
			image.set(a, c);
			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				search.add(e.cell);
		}

		for (final Cell a : Util.cellList(agent)) {}

		for (final Cell a : _boxes)
			image.set(a, a.goal ? Code.BoxOnGoal : Code.Box);

		// convert to String
		final StringBuilder b = new StringBuilder();
		b.append("total=").append(total);
		b.append(" distance=").append(distance);
		b.append(" carries=").append(carries);
		b.append('\n');
		image.render(b);
		return b.toString();
	}

	public boolean equals(final Key key) {
		return agent == key.agent && XArrays.equals(_boxes, key._boxes);
	}

	@Override public boolean equals(final Object o) {
		try {
			return equals((Key) o);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public int hashCode() {
		int h = agent.id();
		for (final Cell box : _boxes)
			h = h * 953 + box.id();
		h ^= h >>> 20 ^ h >>> 12;
		return h ^ h >>> 7 ^ h >>> 4;
	}
}