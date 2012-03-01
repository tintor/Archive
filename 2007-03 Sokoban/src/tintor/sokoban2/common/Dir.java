package tintor.sokoban2.common;

public enum Dir {
	North(0, -1), East(1, 0), South(0, 1), West(-1, 0);

	Dir(final int _dx, final int _dy) {
		dx = _dx;
		dy = _dy;
	}

	public final Dir right() {
		return right;
	}

	public final Dir opposite() {
		return opposite;
	}

	public final Dir left() {
		return left;
	}

	private Dir right, opposite, left;
	public final int dx, dy;

	static {
		for (final Dir d : values()) {
			d.opposite = values()[(d.ordinal() + 2) % 4];
			d.left = values()[(d.ordinal() + 3) % 4];
		}
	}
}