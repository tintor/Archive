package tintor.util.spatialindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SpatialIndexTest {
	private static final class Point implements SpatialIndex.Vector2 {
		float x, y;

		Point(final double X, final double Y) {
			x = (float) X;
			y = (float) Y;
		}

		public float x() {
			return x;
		}

		public float y() {
			return y;
		}

		float quad(final Point b) {
			final float dx = x - b.x, dy = y - b.y;
			return dx * dx + dy * dy;
		}
	}

	@Test public void test() {
		final SpatialIndex<Point> index = new SpatialIndex<Point>();
		final Point[] points = new Point[1000];

		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(Math.random() * 10, Math.random() * 10);
			index.add(points[i]);
		}

		index.update();

		long total1 = 0, total2 = 0;

		for (int i = 0; i < 1000; i++) {
			final Point b = new Point(Math.random() * 10, Math.random() * 10);

			int lin = 0, log = 0;

			for (final Point a : index.circle(b.x, b.y, 0.5f)) {
				assertTrue(a.quad(b) <= 0.5 * 0.5);
				log += 1;
			}

			final long time1 = System.nanoTime();
			for (final Point a : points)
				if (a.quad(b) <= 0.5 * 0.5) lin += 1;
			total1 += System.nanoTime() - time1;

			assertEquals(lin, log);

			final long time2 = System.nanoTime();
			for (final Point a : index.circle(b.x, b.y, 0.5f))
				log += 1;
			total2 += System.nanoTime() - time2;
		}

		System.out.println((double) total1 / total2);
	}
}