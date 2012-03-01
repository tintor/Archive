package tintor.sokoban;

class CellSet {
	final int yoff, xoff;
	final int[][] reached;
	int timestamp;

	CellSet(Cell root) {
		// find limits
		int xmin = root.x, ymin = root.y;
		int xmax = root.x, ymax = root.y;
		for (Cell a : Util.cells(root)) {
			xmax = Math.max(xmax, a.x);
			ymax = Math.max(ymax, a.y);
			xmin = Math.min(xmin, a.x);
			ymin = Math.min(ymin, a.y);
		}

		reached = new int[ymax - ymin + 1][xmax - xmin + 1];

		// offsets
		xoff = -xmin;
		yoff = -ymin;
	}

	void clear() {
		// NOTE this can overflow
		timestamp++;
	}

	boolean add(Cell a) {
		if (reached[a.y + yoff][a.x + xoff] != timestamp) {
			reached[a.y + yoff][a.x + xoff] = timestamp;
			return true;
		}
		return false;
	}
}