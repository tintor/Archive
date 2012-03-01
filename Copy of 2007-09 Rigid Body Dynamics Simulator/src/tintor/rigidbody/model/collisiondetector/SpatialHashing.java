package tintor.rigidbody.model.collisiondetector;

import tintor.geometry.Interval;
import tintor.geometry.Transform3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.CollisionDetector;

public class SpatialHashing extends CollisionDetector {
	private static final int Treshold = 5;

	private final double icell;

	private final Entry[] table = new Entry[1511]; // prime number
	private final int[] initFrame = new int[table.length];
	private int frame = 0;

	public SpatialHashing(final double cell) {
		icell = 1 / cell;
	}

	protected static class Entry {
		final Body body;
		final Entry next;

		Entry(final Body b, final Entry n) {
			body = b;
			next = n;
		}
	}

	@Override final protected void broadPhase() {
		if (bodies.size() <= Treshold) {
			bruteForce();
			return;
		}

		frame += 1;
		for (final Body body : bodies) {
			final Transform3 t = body.transform();

			final Interval ix = body.shape.interval(t.m.a);
			final int xmin = (int) Math.floor((ix.min + t.v.x) * icell);
			final int xmax = (int) Math.ceil((ix.max + t.v.x) * icell);

			final Interval iy = body.shape.interval(t.m.b);
			final int ymin = (int) Math.floor((iy.min + t.v.y) * icell);
			final int ymax = (int) Math.ceil((iy.max + t.v.y) * icell);

			final Interval iz = body.shape.interval(t.m.c);
			final int zmin = (int) Math.floor((iz.min + t.v.z) * icell);
			final int zmax = (int) Math.ceil((iz.max + t.v.z) * icell);

			for (int x = xmin; x < xmax; x++)
				for (int y = ymin; y < ymax; y++)
					for (int z = zmin; z < zmax; z++)
						add(body, index(x, y, z));
		}
	}

	private final int index(final int x, final int y, final int z) {
		return Math.abs((x * 73856093 ^ y * 19349663 ^ z * 83492791) % table.length);
	}

	protected void add(final Body body, final int i) {
		if (initFrame[i] != frame) {
			table[i] = null;
			initFrame[i] = frame;
		}

		final Entry a = new Entry(body, table[i]);
		table[i] = a;

		for (Entry b = a.next; b != null; b = b.next)
			narrowPhase(a.body, b.body);
	}
}