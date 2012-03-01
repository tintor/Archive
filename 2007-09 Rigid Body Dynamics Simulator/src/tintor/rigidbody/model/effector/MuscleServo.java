package tintor.rigidbody.model.effector;

import javax.media.opengl.GL;

import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;

public class MuscleServo implements Effector {
	private final YServo servo = new YServo();
	private final Body bodyA, bodyB;
	private final Vector3 laAnchorA, lbAnchorB;
	
	public float maxForce = 1;
	public boolean active = false;
	
	public float goalPos;
	public float goalVel;

	public MuscleServo(Body bodyA, Body bodyB, Vector3 wAnchorA, Vector3 wAnchorB) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		
		laAnchorA = bodyA.transform().iapplyP(wAnchorA);
		lbAnchorB = bodyB.transform().iapplyP(wAnchorB);
		
		goalPos = wAnchorA.distance(wAnchorB);
	}

	@Override
	public void apply(World world) {
		if (goalPos < 0) goalPos = 0;
		
		if (!active)
			return;

		Vector3 wAnchorA = bodyA.transform().applyP(laAnchorA); 
		Vector3 wAnchorB = bodyB.transform().applyP(lbAnchorB); 

		Vector3 ra = wAnchorA.sub(bodyA.position());
		Vector3 rb = wAnchorB.sub(bodyB.position());
		
		Vector3 wVelAnchorA = bodyA.velAt(ra); 
		Vector3 wVelAnchorB = bodyB.velAt(rb); 

		Vector3 d = wAnchorA.sub(wAnchorB);
		float len = d.length();
		Vector3 dir = d.div(len);
		
		float dr = len - goalPos;
		float dv = dir.dot(wVelAnchorA, wVelAnchorB)- goalVel;
		Vector3 force = dir.mul(servo.eval(dr, dv) * maxForce);
		
		Body.transferForce(force, bodyA, bodyB, ra, rb);
	}
	
	@Override
	public void render() {
		final Vector3 a = bodyA.transform().applyP(laAnchorA);
		final Vector3 b = bodyB.transform().applyP(lbAnchorB);

		GLA.gl.glBegin(GL.GL_LINES);
		GLA.color(GLA.yellow);
		GLA.vertex(a);
		GLA.vertex(b);
		GLA.gl.glEnd();
	}
}
