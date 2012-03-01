package tintor.rigidbody.model;

import tintor.geometry.GMath;
import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

public class Contact implements Constraint {
	private static final float BIAS_FACTOR = 0.2f, BIAS_SLOP = 0.01f;
	private static boolean warmStarting = false;

	// Fields
	public final Body bodyA, bodyB;
	public final float depth; // must be >= 0
	public final Vector3 point;
	public final Vector3 normal; // from B to A
	public final Arbiter arbiter;

	public Contact(final Body bodyA, final Body bodyB, final Vector3 normal, final Vector3 point, final float depth,
			final Arbiter arbiter) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.normal = normal;
		this.point = point;
		this.depth = depth;
		this.arbiter = arbiter;

		assert depth >= 0;
		assert normal != null;
		assert point.isFinite();
	}

	private Vector3 ra, rb;
	private Matrix3 K, invK;
	private Vector3 nK;

	private float elasticity, sfriction, dfriction;
	private float biasVel;

	@Override
	public void prepare(final float dt) {
		ra = point.sub(bodyA.transform().v);
		rb = point.sub(bodyB.transform().v);

		K = Body.imassAt(bodyA, bodyB, ra, rb);
		nK = normal.mul(K);
		invK = K.inv();

		elasticity = elasticity(bodyA.elasticity, bodyB.elasticity);
		sfriction = friction(bodyA.sfriction, bodyB.sfriction);
		dfriction = friction(bodyA.dfriction, bodyB.dfriction);

		if (depth > BIAS_SLOP) {
			biasVel = BIAS_FACTOR / dt * (depth - BIAS_SLOP);
			final Vector3 J = normal.mul(biasVel / nK.dot(normal));
			Body.transferBiasImpulse(J, bodyA, bodyB, ra, rb);
		}

		if (warmStarting) Body.transferImpulse(arbiter.impulse, bodyA, bodyB, ra, rb);
	}

	@Override
	public void correct(final float dt) {}

	@Override
	public void processCollision() {
		processContact(elasticity);
	}

	@Override
	public void processContact(final float e) {
		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		final float nVel = vel.dot(normal);
		if (nVel < 0) {
			// assume static friction
			Vector3 J = invK.mul(normal.mul(-e * nVel).sub(vel));
			float nJ = normal.dot(J);
			if (nJ < 0 || J.sub(nJ, normal).square() > GMath.square(sfriction * nJ)) {
				// not sticking, apply dynamic friction
				final Vector3 tangent = vel.sub(nVel, normal).unitz();
				final Vector3 z = normal.sub(dfriction, tangent);
				nJ = -(1 + e) * nVel / nK.dot(z);
				J = z.mul(nJ);
			}
			if (warmStarting) arbiter.impulse = arbiter.impulse.add(J);
			Body.transferImpulse(J, bodyA, bodyB, ra, rb);
		}
	}

	private void bias(final float e) {
		final Vector3 vel = bodyA.bVelAt(ra).sub(bodyB.bVelAt(rb));
		final float nVel = vel.dot(normal);
		if (nVel >= 0) return;

		final float nJ = (-(1 + e) * nVel + biasVel) / nK.dot(normal);
		final Vector3 J = normal.mul(nJ);
		Body.transferBiasImpulse(J, bodyA, bodyB, ra, rb);
	}

	@Override
	public void render() {}

	private static float elasticity(final float ea, final float eb) {
		return Math.max(ea, eb);
	}

	private static float friction(final float fa, final float fb) {
		return Math.min(fa, fb);
	}
}