package tintor.puzzle;

public final class Vector2 {
	public final double x;
	public final double y;

	public Vector2(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 add(final Vector2 a) {
		return new Vector2(x + a.x, y + a.y);
	}

	public Vector2 sub(final Vector2 a) {
		return new Vector2(x - a.x, y - a.y);
	}

	public Vector2 rotate(final double angle) {
		return rotate(Math.cos(angle), Math.sin(angle));
	}

	public Vector2 rotate(final double cosa, final double sina) {
		return new Vector2(x * cosa - y * sina, x * sina + y * cosa);
	}
}