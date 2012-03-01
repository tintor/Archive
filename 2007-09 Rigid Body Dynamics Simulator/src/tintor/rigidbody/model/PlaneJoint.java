package tintor.rigidbody.model;

import tintor.geometry.Plane3;
import tintor.geometry.Vector3;

public class PlaneJoint extends Joint {
	public Vector3 anchorA;
	public Plane3 planeB;

	private Plane3 plane;
	private float inv_nK, biasVel;

	public PlaneJoint(final Body bodyA, final Body bodyB, final Vector3 anchor, final Plane3 plane) {
		super(bodyA, bodyB);
		anchorA = anchor;
		planeB = plane;
	}

	@Override
	public void prepare(final float dt) {
		ra = bodyA.transform().applyV(anchorA);
		final Vector3 p = bodyA.transform().v.add(ra);
		rb = p.sub(bodyB.transform().v);
		plane = bodyB.transform().apply(planeB);
		initImpulse();

		inv_nK = 1 / plane.normal.mul(Body.imassAt(bodyA, bodyB, ra, rb)).dot(plane.normal);

		biasVel = -0.2f * plane.distance(p);
	}

	@Override
	public void processCollision() {
		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		final float nVel = vel.dot(plane.normal);
		addImpulse(plane.normal.mul((-nVel + biasVel) * inv_nK));

		// TODO bias impulse
	}
}