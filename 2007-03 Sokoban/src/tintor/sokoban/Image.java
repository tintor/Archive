package tintor.sokoban;

public final class Image {
	private final char[][] map;
	private final int xoff, yoff;

	public Image(final Cell root) {
		// find limits
		int xmin = root.x, ymin = root.y;
		int xmax = root.x, ymax = root.y;
		for (final Cell a : Util.cells(root)) {
			xmax = Math.max(xmax, a.x);
			ymax = Math.max(ymax, a.y);
			xmin = Math.min(xmin, a.x);
			ymin = Math.min(ymin, a.y);
		}

		// init map
		map = new char[ymax - ymin + 3][xmax - xmin + 3];
		for (int y = 0; y < map.length; y++)
			for (int x = 0; x < map[y].length; x++)
				map[y][x] = '#';

		// offsets
		xoff = 1 - xmin;
		yoff = 1 - ymin;
	}

	// TODO throw exception on multiple sets!
	public void set(final Cell a, final char c) {
		map[a.y + yoff][a.x + xoff] = c;
	}

	public void render(final StringBuilder b) {
		for (final char[] element : map)
			b.append(element).append('\n');
	}

	@Override public String toString() {
		final StringBuilder b = new StringBuilder();
		render(b);
		return b.toString();
	}
}