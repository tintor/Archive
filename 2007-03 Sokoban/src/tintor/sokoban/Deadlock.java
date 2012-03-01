package tintor.sokoban;

import java.util.HashSet;
import java.util.Set;

import tintor.properties.Property;
import tintor.search.BreadthFirstSearch;
import tintor.search.Search;

// TODO test for free goals, blocked by frozen boxes on goals
public class Deadlock {
	public final static Property<Boolean> fastBlock = Property.instance(Deadlock.class, "fastBlock", true);
	public final static Property<Boolean> frozenBoxes = Property.instance(Deadlock.class, "frozenBoxes", true);

	public static boolean fullTest(final Key key) {
		if (frozenBoxes(key, key.agent, null)) return true;
		return false;
	}

	public static boolean partialTest(final Key key) {
		if (fastBlock(key, key.boxAdded)) {
			if (Monitor.monitor != null) Monitor.monitor.fastBlock_deadlocks++;
			return true;
		}
		if (frozenBoxes(key, key.agent, key.boxAdded)) {
			if (Monitor.monitor != null) Monitor.monitor.frozenBoxes_deadlocks++;
			return true;
		}
		// if (coral(key, key.boxAdded.get(Util.diff(key.boxRemoved, key.boxAdded)))) return true;
		return false;
	}

	@SuppressWarnings("unused") private static boolean coral(final Key key, final Cell coral) {
		if (coral == null || key.hasBox(coral)) return false;

		// TODO implement
		// select only boxes touching the coral
		// can select up to a min(4, boxes.length-1) boxes
		// there must not be any goals inside coral!
		// at least one surrounding box must not be on goal!
		// create test maze with only selected level
		// run full search, but limit number of opened nodes!
		return false;
	}

	// TODO improve!
	// very simple and fast test in 2x2 cells!
	private static boolean fastBlock(final Key key, final Cell box) {
		if (!fastBlock.get()) return false;

		if (!box.isGoal()) {
			// east
			final Cell e = box.east();
			final boolean fe = frozen(key, e);

			// west
			final Cell w = box.west();
			final boolean fw = frozen(key, w);

			// north
			final Cell n = box.north();
			if (frozen(key, n)) {
				if (fw && frozen(key, n != null ? n.west() : w != null ? w.north() : null)) return true;
				if (fe && frozen(key, n != null ? n.east() : e != null ? e.north() : null)) return true;
			}

			// south
			final Cell s = box.south();
			if (frozen(key, s)) {
				if (fw && frozen(key, s != null ? s.west() : w != null ? w.south() : null)) return true;
				if (fe && frozen(key, s != null ? s.east() : e != null ? e.south() : null)) return true;
			}
		}
		return false;
	}

	// if a is wall or box (not on goal)?
	private static boolean frozen(final Key key, final Cell a) {
		return a == null || !a.isGoal() && key.hasBox(a);
	}

	// Whole level frozen boxes test
	private static boolean frozenBoxes(final Key key, final Cell start, final Cell box) {
		if (!frozenBoxes.get()) return false;

		// create box set
		final Set<Cell> boxes = new HashSet<Cell>(32);
		for (final Cell b : key.boxes)
			boxes.add(b);

		// remove alive boxes
		final Search<Cell> search = new BreadthFirstSearch<Cell>();
		search.add(start);
		for (final Cell a : search)
			for (final Cell.Edge e : a.edges)
				if (e.cell.dispenser) {
					while (boxes.remove(e.cell));
					search.add(e.cell);
				} else if (!boxes.contains(e.cell))
					search.add(e.cell);
				else {
					final Cell dest = e.cell.get(e.dir);
					// TODO execute fastBlock test after 'push'
					if (dest != null && !dest.dead() && !dest.dispenser && !boxes.contains(dest)) {
						boxes.remove(e.cell);
						search.add(e.cell);
					}
				}

		// only frozen boxes remain
		for (final Cell b : boxes)
			if (!b.dispenser && !b.isGoal()) return true;

		// TODO test if all free goals can be reached!
		// TODO if there are boxes frozen on goals then recalculate push distances and do goal matching
		return false;
	}
}