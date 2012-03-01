package tintor.rigidbody.model.collisiondetector;

import tintor.rigidbody.model.CollisionDetector;

public final class BruteForce extends CollisionDetector {
	@Override public void broadPhase() {
		bruteForce();
	}
}