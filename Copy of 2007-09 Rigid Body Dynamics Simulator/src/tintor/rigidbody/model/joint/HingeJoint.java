package tintor.rigidbody.model.joint;

import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Constraint;

public class HingeJoint implements Constraint, Axis {
	public Body bodyA, bodyB;
	public Vector3 refA, refB;
	public Vector3 anchorA1, anchorB1;
	public Vector3 anchorA2, anchorB2;

	public void prepare(double dt) {
		// TODO
	}

	public void resolve() {
		// TODO
		// TODO randomize order
		// BallJoint.apply(world, bodyA, anchorA1, bodyB, anchorB1);
		// BallJoint.apply(world, bodyA, anchorA2, bodyB, anchorB2);
	}

	/** Returns angle from 0 to 2PI. */
	public double position() {
		Vector3 a = bodyA.transform.applyV(refA), b = bodyB.transform.applyV(refB);
		return bodyA.transform.applyV(anchorA2.sub(anchorA1)).fullAngle(a, b);
	}

	public void apply(double force) {
		// TODO
	}

	public double mass() {
		// TODO
		return 0;
	}

	/** Returns angular velocity */
	public double velocity() {
		Vector3 a = bodyA.transform.applyV(bodyA.linVel.cross(refA));
		Vector3 b = bodyB.transform.applyV(bodyB.linVel.cross(refB));
		return bodyA.transform.applyV(anchorA2.sub(anchorA1)).fullAngle(a, b);
	}
}