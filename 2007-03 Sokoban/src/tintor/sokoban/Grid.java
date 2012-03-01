package tintor.sokoban;

public interface Grid {
	int height();

	int width();
	
	boolean wall(int x, int y);

	boolean goal(int x, int y);

	boolean box(int x, int y);

	boolean agent(int x, int y);
}

//class CharArrayGrid implements Grid {
//	private final char[][] map;
//
//	public CharArrayGrid(char[][] map) {
//		this.map = map;
//	}
//
//	@Override public int height() {
//		return map.length;
//	}
//
//	@Override public int width(int y) {
//		return map[y].length;
//	}
//
//	@Override public char get(int x, int y) {
//		return map[y][x];
//	}
//	
//	public void set(int x, int y, char c) {
//		map[y][x] = c;
//	}
//}
//

class StringGrid implements Grid {
	private final String[] rows;
	private final int width;

	public StringGrid(String[] rows) {
		this.rows = rows;
		
		int w = 0;
		for(String row : rows)
			w = Math.max(w, row.length());
		width = w;
	}

	@Override public int height() {
		return rows.length;
	}

	@Override public int width() {
		return width;
	}

	private char get(int x, int y) {
		String row = rows[y];
		return x < row.length() ? row.charAt(x) : Code.Wall;
	}

	@Override public boolean agent(int x, int y) {
		char c = get(x, y);
		return c == Code.Agent || c == Code.AgentOnGoal;
	}

	@Override public boolean box(int x, int y) {
		char c = get(x, y);
		return c == Code.Box || c == Code.BoxOnGoal;
	}

	@Override public boolean goal(int x, int y) {
		char c = get(x, y);
		return c == Code.Goal || c == Code.AgentOnGoal || c == Code.BoxOnGoal;
	}

	@Override public boolean wall(int x, int y) {
		char c = get(x, y);
		return c == Code.Wall;
	}
}
