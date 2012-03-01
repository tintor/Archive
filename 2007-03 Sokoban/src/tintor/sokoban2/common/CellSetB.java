package tintor.sokoban2.common;

import tintor.sokoban2.cell.Cell;

public class CellSetB {
	private final boolean[] reached = new boolean[Cell.count()];

	public boolean add(final Cell a) {
		if (reached[a.id()]) return false;
		reached[a.id()] = true;
		return true;
	}
}