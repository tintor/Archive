package tintor.rigidbody.model;

import javax.media.opengl.GL;

import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public class BarJoint extends Joint {
	public Vector3 anchorA, anchorB; // TODO optimize for anchorX == 0!
	public float length;

	private Matrix3 invK; // constraint matrix
	private Vector3 biasVel;
	private Vector3 dir;

	public BarJoint(final Body bodyA, final Body bodyB, final Vector3 pa, final Vector3 pb) {
		super(bodyA, bodyB);
		anchorA = pa;
		anchorB = pb;
		length = bodyA.transform().applyP(pa).distance(bodyB.transform().applyP(pb));
	}

	@Override
	public void prepare(final float dt) {
		ra = bodyA.transform().applyV(anchorA);
		rb = bodyB.transform().applyV(anchorB);
		initImpulse();

		final Vector3 pos = bodyA.transform().v.add(ra).sub(bodyB.transform().v).sub(rb);
		dir = pos.div(length);

		// contraint matrix
		invK = Body.imassAt(bodyA, bodyB, ra, rb).inv();

		// bias velocity
		biasVel = dir.mul(-(pos.length() - length) * biasFactor / dt);
		addBiasImpulse(invK.mul(biasVel));
	}

	@Override
	public void processCollision() {
		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		addImpulse(invK.mul(dir.mul(-vel.dot(dir))));

		//		final Vector3 bvel = bodyA.bVelAt(ra).sub(bodyB.bVelAt(rb));
		//		assert bvel.isFinite();
		//		addBiasImpulse(invK.mul(biasVel.sub(bvel.dot(dir), dir)));
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

		GLA.color(GLA.blue);
		GLA.vertex(a);
		GLA.vertex(b);

		GLA.gl.glEnd();
	}
}