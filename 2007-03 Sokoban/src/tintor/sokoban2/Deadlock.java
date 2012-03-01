package tintor.sokoban2;

import java.util.HashSet;
import java.util.Set;

import tintor.Timer;
import tintor.properties.Property;
import tintor.sokoban2.cell.Cell;
import tintor.sokoban2.common.CellSearch;

// TODO test for free goals, blocked by frozen boxes on goals
public class Deadlock {
	public final static Property<Boolean> fastBlock = Property.instance(Deadlock.class, "fastBlock", true);
	public final static Property<Boolean> frozenBoxes = Property.instance(Deadlock.class, "frozenBoxes", true);

	public static boolean fullTest(final Key key) {
		if (frozenBoxes(key)) return true;
		return false;
	}

	public final static Timer timer = new Timer();

	public static boolean partialTest(final Key key) {
		timer.restart();

		if (fastBlock(key)) {
			Monitor._fastBlock_deadlocks++;
			timer.stop();
			return true;
		}
		if (frozenBoxes(key)) {
			Monitor._frozenBoxes_deadlocks++;
			timer.stop();
			return true;
		}
		// if (coral(key, key.boxAdded.get(Util.diff(key.boxRemoved, key.boxAdded)))) return true;
		timer.stop();
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
	private static boolean fastBlock(final Key key) {
		if (!fastBlock.get()) return false;

		if (!key.boxAdded.goal) {
			// east
			final Cell e = key.boxAdded.east();
			final boolean fe = frozen(key, e);

			// west
			final Cell w = key.boxAdded.west();
			final boolean fw = frozen(key, w);

			// north
			final Cell n = key.boxAdded.north();
			if (frozen(key, n)) {
				if (fw && frozen(key, n != null ? n.west() : w != null ? w.north() : null)) return true;
				if (fe && frozen(key, n != null ? n.east() : e != null ? e.north() : null)) return true;
			}

			// south
			final Cell s = key.boxAdded.south();
			if (frozen(key, s)) {
				if (fw && frozen(key, s != null ? s.west() : w != null ? w.south() : null)) return true;
				if (fe && frozen(key, s != null ? s.east() : e != null ? e.south() : null)) return true;
			}
		}
		return false;
	}

	// if a is wall or box (not on goal)?
	private static boolean frozen(final Key key, final Cell a) {
		return a == null || !a.goal && key.hasBox(a);
	}

	private static boolean fastBlock2(final Set<Cell> boxes, final Cell movedBox) {
		if (!fastBlock.get()) return false;

		if (!movedBox.goal) {
			// east
			final Cell e = movedBox.east();
			final boolean fe = frozen2(boxes, e);

			// west
			final Cell w = movedBox.west();
			final boolean fw = frozen2(boxes, w);

			// north
			final Cell n = movedBox.north();
			if (frozen2(boxes, n)) {
				if (fw && frozen2(boxes, n != null ? n.west() : w != null ? w.north() : null)) return true;
				if (fe && frozen2(boxes, n != null ? n.east() : e != null ? e.north() : null)) return true;
			}

			// south
			final Cell s = movedBox.south();
			if (frozen2(boxes, s)) {
				if (fw && frozen2(boxes, s != null ? s.west() : w != null ? w.south() : null)) return true;
				if (fe && frozen2(boxes, s != null ? s.east() : e != null ? e.south() : null)) return true;
			}
		}
		return false;
	}

	// if a is wall or box (not on goal)?
	private static boolean frozen2(final Set<Cell> boxes, final Cell a) {
		return a == null || !a.goal && boxes.contains(a);
	}

	// Whole level frozen boxes test
	private static boolean frozenBoxes(final Key key) {
		if (!frozenBoxes.get()) return false;

		// create box set
		final Set<Cell> boxes = new HashSet<Cell>(32);
		for (int i = 0; i < key.boxes(); i++)
			boxes.add(key.box(i));

		// remove alive boxes
		int reachable_goals = 0;
		final CellSearch search = new CellSearch(key.agent);
		for (final Cell a : search) {
			if (a.goal) reachable_goals += 1;

			for (Cell.Edge e = a.edges(); e != null; e = e.next)
				if (boxes.contains(e.cell)) {
					final Cell dest = e.cell.get(e.dir);
					if (dest != null && !dest.dead() && !boxes.contains(dest)) {
						boxes.remove(e.cell);
						if (fastBlock2(boxes, dest))
							boxes.add(e.cell);
						else {
							if (e.cell == key.boxAdded) return false;
							search.add(e.cell);
						}
					}
				} else
					search.add(e.cell);
		}

		// only frozen boxes remain
		for (final Cell b : boxes)
			if (!b.goal) return true;

		// test if all free goals can be reached!
		if (reachable_goals < Cell.goals() - boxes.size()) return true;

		// TODO if there are boxes frozen on goals then recalculate push distances and do goal matching
		return false;
	}
}