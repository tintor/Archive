package tintor.netrek.server;

import tintor.util.spatialindex.SpatialIndex;

final class Ship implements SpatialIndex.Vector2 {
	static void afterUpdate() {
		index.update();
	}

	static Iterable<Ship> circle(final float x, final float y, final float radius) {
		return index.circle(x, y, radius);
	}

	Ship(final float x, final float y) {
		this.x = x;
		this.y = y;
		index.add(this);
	}

	void remove() {
		index.remove(this);
	}

	void update() {
		x += dx;
		y += dy;
	}

	@Override public float x() {
		return x;
	}

	@Override public float y() {
		return y;
	}

	public Player player() {
		return player;
	}

	private static final SpatialIndex<Ship> index = new SpatialIndex<Ship>();

	private float x, y;
	private float dx, dy;
	private Player player;
}