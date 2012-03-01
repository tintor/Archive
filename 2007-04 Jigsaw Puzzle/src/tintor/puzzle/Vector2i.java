package tintor.puzzle;

class Vector2i {
	final int x, y;

	Vector2i(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	Vector2i(final double x, final double y) {
		this((int) Math.round(x), (int) Math.round(y));
	}

	Vector2i(final Vector2 a) {
		this(a.x, a.y);
	}
}