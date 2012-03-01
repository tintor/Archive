package tintor.rigidbody.model.effector;

import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;

public class Servo1R implements Effector {
	private final YServo servo = new YServo();
	private final Body bodyA, bodyB;
	private final Vector3 laAnchor, lbAnchor;
	private final Vector3 laRefA, lbRefB;
	
	public float maxTorque = 1;
	public boolean active = false;
	
	public float goalPos = (float) Math.PI / 2;
	public float goalVel = 0;

	public Servo1R(Body bodyA, Body bodyB, Vector3 wAnchor, Vector3 wRefA,
			Vector3 wRefB) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		
		laAnchor = bodyA.transform().iapplyP(wAnchor);
		lbAnchor = bodyB.transform().iapplyP(wAnchor);

		laRefA = reference(laAnchor, bodyA.transform().iapplyP(wRefA));
		lbRefB = reference(lbAnchor, bodyB.transform().iapplyP(wRefB));
	}
	
	private static Vector3 reference(Vector3 a, Vector3 b) {
		return a.add(b.sub(a).unit());
	}

	@Override
	public void apply(World world) {
		if (!active)
			return;

		Vector3 wRefA = bodyA.transform().applyP(laRefA); 
		Vector3 wRefB = bodyB.transform().applyP(lbRefB); 
		Vector3 wAnchor = bodyA.transform().applyP(laAnchor).add(bodyB.transform().applyP(lbAnchor)).mul(0.5f);
		
		float dr = (float) Math.acos(wRefA.sub(wAnchor).dot(wRefB.sub(wAnchor)));
		float dv = 0;

		float torque = servo.eval(dr, dv) * maxTorque;
		
		// TODO compute torques in world space 
		bodyA.addTorque(null);
		bodyB.addTorque(null);
	}
}
