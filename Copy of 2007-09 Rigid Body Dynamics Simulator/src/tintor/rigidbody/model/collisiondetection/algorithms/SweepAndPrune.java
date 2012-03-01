package tintor.rigidbody.model.collisiondetection.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import tintor.geometry.Interval;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.collisiondetection.CollisionDetector;
import tintor.util.NonCashingHashSet;

@SuppressWarnings("unchecked") public class SweepAndPrune extends CollisionDetector.BroadPhase {
	// Constants
	private final static Vector3[] axis = new Vector3[] { Vector3.X, Vector3.Y, Vector3.Z };

	// Fields
	private int bodies = 0, capacity = 16;
	private final ArrayList<Pair>[] overlaps = new ArrayList[axis.length];
	private final int[] cursor = new int[axis.length];
	private final Point[][] points = new Point[axis.length][capacity];

	public SweepAndPrune() {
		for (int a = 0; a < axis.length; a++)
			overlaps[a] = new ArrayList<Pair>();
	}

	@Override protected void init() {
		ensure(detector.bodies.size() * 2);
		for (int a = 0; a < axis.length; a++) {
			int w = 0;
			final Point[] p = points[a];
			for (final Body body : detector.bodies) {
				final Interval s = body.solid().interval(axis[a], body.transform);
				final Point max = new Point(body, s.max, null);
				p[w++] = new Point(body, s.min, max);
				p[w++] = max;
			}
			Arrays.sort(p);
		}
		bodies = detector.bodies.size();
	}

	@Override protected void broadPhase() {
		for (int a = 0; a < axis.length; a++) {
			final Point[] p = points[a];

			// update points
			for (final Point r : p)
				if (r.min() && r.body.state == Body.State.Dynamic) {
					final Interval i = r.body.solid().interval(axis[a], r.body.transform);
					r.position = i.min;
					r.next.position = i.max;
				}

			// sort points
			for (int i, j = 0; j < bodies * 2; j++) {
				final Point m = p[j];
				for (i = j; i > 0 && m.less(p[i - 1]); i--)
					p[i] = p[i - 1];
				p[i] = m;
			}

			// generate axis overlaps
			final Set<Body> set = new NonCashingHashSet<Body>();
			for (int i = 0; i < bodies * 2; i++)
				if (p[i].min()) {
					for (final Body b : set)
						overlaps[a].add(new Pair(b, p[i].body));
					set.add(p[i].body);
				} else
					set.remove(p[i].body);

			// sort overlaps
			Collections.sort(overlaps[a]);

			cursor[a] = 0;
		}

		// merge overlaps along all tree axies
		loop: while (true) {
			for (int a = 0; a < axis.length; a++)
				if (cursor[a] >= overlaps[a].size()) break loop;

			boolean equal = true;
			Pair p = overlaps[0].get(cursor[0]);
			for (int a = 1; equal && a < axis.length; a++)
				if (p != overlaps[a].get(cursor[a])) equal = false;

			if (equal) {
				detector.narrowPhase(p.a, p.b);
				for (int a = 0; a < axis.length; a++)
					cursor[a]++;
			} else {
				for (int a = 1; a < axis.length; a++)
					p = Pair.min(p, overlaps[a].get(cursor[a]));
				for (int a = 0; a < axis.length; a++)
					if (overlaps[a].get(cursor[a]).compareTo(p) == 0) cursor[a]++;
			}
		}

		for (int a = 0; a < axis.length; a++)
			overlaps[a].clear();
	}

	@Override protected void add(final Body b) {
		ensure((bodies + 1) * 2);
		for (int a = 0; a < points.length; a++) {
			final Point[] p = points[a];
			final Interval s = b.solid().interval(axis[a]);
			int i;

			final Point max = new Point(b, s.max, null);
			for (i = 2 * bodies + 1; i > 0 && max.less(p[i - 1]); i--)
				p[i] = p[i - 1];
			p[i] = max;

			final Point min = new Point(b, s.min, max);
			for (i = 2 * bodies; i > 0 && min.less(p[i - 1]); i--)
				p[i] = p[i - 1];
			p[i] = min;

		}
		bodies++;
	}

	@Override protected void remove(final Body b) {
		for (final Point[] p : points) {
			int w = 0;
			for (int i = 0; i < bodies * 2; i++)
				if (p[i].body != b) p[w++] = p[i];
		}
		bodies--;
	}

	private static class Pair implements Comparable<Pair> {
		final Body a, b;
		final int sum;

		Pair(final Body a, final Body b) {
			if (a.id < b.id) {
				this.a = a;
				this.b = b;
			} else {
				this.a = b;
				this.b = a;
			}
			sum = a.id + b.id;
		}

		public int compareTo(final Pair p) {
			if (sum < p.sum && a.id < p.a.id) return -1;
			if (sum > p.sum && a.id > p.a.id) return 1;
			return 0;
		}

		static Pair min(final Pair a, final Pair b) {
			return a.compareTo(b) < 0 ? a : b;
		}
	}

	private void ensure(final int size) {
		if (capacity >= size) return;
		while (capacity < size)
			capacity *= 2;
		for (int i = 0; i < axis.length; i++)
			points[i] = Arrays.copyOf(points[i], capacity);
	}

	private static class Point implements Comparable<Point> {
		Body body;
		double position;
		Point next; // if this is min then next = max point else next = null

		boolean min() {
			return next != null;
		}

		Point(final Body b, final double p, final Point n) {
			body = b;
			position = p;
			next = n;
		}

		boolean less(final Point p) {
			if (position != p.position) return position < p.position;
			if (min() != p.min()) return !min(); // && p.start
			return body.id < p.body.id;
		}

		public int compareTo(final Point p) {
			if (position != p.position) return Double.compare(position, p.position);
			if (min() != p.min()) return min() ? 1 : -1; // && p.start
			return body.id - p.body.id;
		}
	}
}