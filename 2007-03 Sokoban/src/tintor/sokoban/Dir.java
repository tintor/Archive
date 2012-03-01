package tintor.sokoban;

public enum Dir {
	North, East, South, West;

	public Dir right() {
		return right;
	}

	public Dir opposite() {
		return opposite;
	}

	public Dir left() {
		return left;
	}

	public int deltaX() {
		return deltaX;
	}
	
	public int deltaY() {
		return deltaY;
	}
	
	private Dir right, opposite, left;
	private int deltaX, deltaY;

	static {
		for (Dir d : values()) {
			d.opposite = values()[(d.ordinal() + 2) % 4];
			d.left = values()[(d.ordinal() + 3) % 4];
		}
		North.deltaY = -1;
		South.deltaY = 1;
		West.deltaX = -1;
		East.deltaX = 1;
	}
}