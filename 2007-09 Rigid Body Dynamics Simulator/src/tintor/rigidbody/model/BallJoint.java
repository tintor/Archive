package tintor.rigidbody.model;

import javax.media.opengl.GL;

import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public class BallJoint extends Joint {
	public Vector3 anchorA, anchorB;

	private Matrix3 invK;
	private Vector3 biasVel;

	public BallJoint(final Body bodyA, final Body bodyB, final Vector3 anchor) {
		super(bodyA, bodyB);
		anchorA = bodyA.transform().iapplyP(anchor);
		anchorB = bodyB.transform().iapplyP(anchor);
	}

	@Override
	public void prepare(final float dt) {
		ra = bodyA.transform().applyV(anchorA);
		rb = bodyB.transform().applyV(anchorB);
		initImpulse();

		final Vector3 pos = bodyA.transform().v.add(ra).sub(bodyB.transform().v).sub(rb);

		// contraint matrix
		invK = Body.imassAt(bodyA, bodyB, ra, rb).inv();

		// bias velocity
		biasVel = pos.mul(-biasFactor / dt);
		//addBiasImpulse(invK.mul(biasVel));
	}

	// TODO use e in process contact to slow down bodies
	@Override
	public void processCollision() {
		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		addImpulse(invK.mul(biasVel.sub(vel)));
		//addImpulse(invK.mul(vel.neg()));
		//		addBiasImpulse(invK.mul(biasVel));

		//final Vector3 bVel = bodyA.bVelAt(ra).sub(bodyB.bVelAt(rb));
		//addBiasImpulse(invK.mul(biasVel.sub(bVel)));
	}

	@Override
	public void render() {
		final Vector3 a = bodyA.transform().applyP(anchorA);
		final Vector3 b = bodyB.transform().applyP(anchorB);

		GLA.gl.glBegin(GL.GL_LINES);
		GLA.color(GLA.green);

		if (bodyA != World.Space) {
			GLA.vertex(bodyA.position());
			GLA.vertex(a);
		}

		if (bodyB != World.Space) {
			GLA.vertex(b);
			GLA.vertex(bodyB.position());
		}

		GLA.color(GLA.red);
		GLA.vertex(a);
		GLA.vertex(b);
		GLA.gl.glEnd();
	}
}