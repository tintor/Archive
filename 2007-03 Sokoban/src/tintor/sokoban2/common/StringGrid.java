package tintor.sokoban2.common;

public class StringGrid implements Grid {
	private final String[] rows;
	private final int width;

	public StringGrid(final String[] rows) {
		this.rows = rows;

		int w = 0;
		for (final String row : rows)
			w = Math.max(w, row.length());
		width = w;
	}

	@Override public int height() {
		return rows.length;
	}

	@Override public int width() {
		return width;
	}

	private char get(final int x, final int y) {
		final String row = rows[y];
		return x < row.length() ? row.charAt(x) : Code.Wall;
	}

	@Override public boolean agent(final int x, final int y) {
		final char c = get(x, y);
		return c == Code.Agent || c == Code.AgentOnGoal;
	}

	@Override public boolean box(final int x, final int y) {
		final char c = get(x, y);
		return c == Code.Box || c == Code.BoxOnGoal;
	}

	@Override public boolean goal(final int x, final int y) {
		final char c = get(x, y);
		return c == Code.Goal || c == Code.AgentOnGoal || c == Code.BoxOnGoal;
	}

	@Override public boolean wall(final int x, final int y) {
		final char c = get(x, y);
		return c == Code.Wall;
	}

	@Override public boolean hole(final int x, final int y) {
		final char c = get(x, y);
		return c == Code.Hole;
	}
}