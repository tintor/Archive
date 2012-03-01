package tintor.rigidbody.model.collisiondetection.algorithms;

import tintor.rigidbody.model.collisiondetection.CollisionDetector;

public final class BruteForce extends CollisionDetector.BroadPhase {
	@Override protected void broadPhase() {
		run(detector);
	}

	// also needed for spatial hashing
	public static void run(final CollisionDetector detector) {
		for (int a = 0; a < detector.bodies.size(); a++)
			for (int b = a + 1; b < detector.bodies.size(); b++)
				detector.narrowPhase(detector.bodies.get(a), detector.bodies.get(b));
	}
}