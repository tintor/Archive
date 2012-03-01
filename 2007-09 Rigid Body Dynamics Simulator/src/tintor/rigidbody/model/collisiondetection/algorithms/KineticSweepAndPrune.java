package tintor.rigidbody.model.collisiondetection.algorithms;

import java.util.List;

import tintor.geometry.GMath;
import tintor.rigidbody.model.Body;

public class KineticSweepAndPrune {
	static class Point {
		Body body;
		boolean min;

		Point left, right;
		double time = GMath.Infinity; // swap time of this and right, time != Inf <=> point is in queue

		double swapTime() {
			if (body == null || right.body == null) return GMath.Infinity;
			// ASSUME right.position >= this.position
			final double dv = 0; // TODO velocity - right.velocity
			final double dx = 0; // TODO right.position - position
			return dv <= 0 ? GMath.Infinity : dx / dv;
		}
	}

	Point min = new Point(), max = new Point();
	double time;

	public KineticSweepAndPrune(final List<Body> bodies) {
		min.right = max;
		max.left = min;
		min.min = true;
		max.min = false;

		// TODO create points, sort them and connect them
		// TODO find all overlaps
	}

	public void motionChanged(final Body body) {
//		Point a = null, b = null; // TODO obtain from body
//		update(a.left);
//		update(a);
//		update(b.left);
//		update(b);
	}

	void update(final Point a) {
		a.time = time + a.swapTime();
		queueUpdate(a);
	}

	void advance(final double dt) {
		while (firstSwapTime <= time + dt) {
			final Point a = queuePop(), b = a.right;

			if (a.min != b.min) if (b.min)
				addOverlap(a.body, b.body);
			else
				addSeparation(a.body, b.body);

			// swap the points
			a.left.right = b;
			b.right.left = a;
			a.right = b.right;
			b.left = a.left;
			a.left = b;
			b.right = a;

			update(b.left);
			update(b);
			update(a);
		}
		// TODO
		// for each overlap
		// 	findContacts(from time to time+dt)
		// merge overlaps and separations
		time += dt;
	}

	void addOverlap(final Body a, final Body b) {
	// TODO
	}

	void addSeparation(final Body a, final Body b) {
	// TODO
	}

	// ----
	double firstSwapTime = GMath.Infinity;

	// to add new point to queue, set its time and update point
	// to remove point, set its time to Inf and update point
	private void queueUpdate(final Point a) {
	// TODO
	}

	private Point queuePop() {
		return null;
	}
}