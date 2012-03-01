package tintor.rigidbody.model;

import tintor.geometry.Vector3;

public abstract class Joint implements Constraint {
	protected static final float biasFactor = 0.3f;

	public final Body bodyA, bodyB;

	protected Vector3 totalJ = Vector3.Zero;
	protected Vector3 ra, rb;

	public Joint(final Body a, final Body b) {
		bodyA = a;
		bodyB = b;
	}

	@Override
	public void correct(final float dt) {}

	@Override
	public void render() {}

	@Override
	public void processContact(final float e) {
		processCollision();
	}

	protected void initImpulse() {
	//Body.transferImpulse(totalJ, bodyA, bodyB, ra, rb);
	}

	protected void addImpulse(final Vector3 j) {
		assert j.isFinite();
		totalJ = totalJ.add(j);
		Body.transferImpulse(j, bodyA, bodyB, ra, rb);
	}

	protected void addBiasImpulse(final Vector3 j) {
		assert j.isFinite();
		Body.transferBiasImpulse(j, bodyA, bodyB, ra, rb);
	}
}