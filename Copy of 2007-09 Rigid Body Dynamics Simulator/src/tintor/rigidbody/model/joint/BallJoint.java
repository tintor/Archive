package tintor.rigidbody.model.joint;

import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Constraint;

public class BallJoint implements Constraint {
	// Constants
	private static final double β = 0.2;

	// Fields
	public Body bodyA, bodyB;
	public Vector3 anchorA, anchorB;

	// Private Fields
	private Vector3 ra, rb;
	private Matrix3 K; // constraint matrix
	private Vector3 vbias;

	public void prepare(final double dt) {
		ra = bodyA.transform.applyV(anchorA);
		rb = bodyB.transform.applyV(anchorB);

		// bias velocity
		vbias = bodyB.pos.add(rb).sub(bodyA.pos.add(ra)).mul(β / dt);

		// contraint matrix
		final Matrix3 Ma = bodyA.invI().mul(ra.tildaSqr());
		final Matrix3 Mb = bodyB.invI().mul(rb.tildaSqr());
		K = new Matrix3(bodyA.imass() + bodyB.imass()).sub(Ma).sub(Mb).inv();
	}

	public void resolve() {
		// relative velocity
		final Vector3 va = bodyA.angVel.cross(ra).add(bodyA.linVel);
		final Vector3 vb = bodyB.angVel.cross(rb).add(bodyB.linVel);
		final Vector3 nv = vb.sub(va);

		// total impulse
		final Vector3 j = K.mul(nv.add(vbias));

		// apply impulse
		bodyA.linVel = bodyA.linVel.add(bodyA.imass(), j);
		bodyA.angVel = bodyA.angVel.add(bodyA.invI().mul(ra.cross(j)));
		bodyB.linVel = bodyB.linVel.sub(bodyB.imass(), j);
		bodyB.angVel = bodyB.angVel.sub(bodyB.invI().mul(rb.cross(j)));
	}
}