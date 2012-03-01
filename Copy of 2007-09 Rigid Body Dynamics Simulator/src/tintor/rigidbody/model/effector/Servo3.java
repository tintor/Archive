package tintor.rigidbody.model.effector;

import tintor.geometry.GMath;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;

public class Servo3 implements Effector {
	public Body body;
	public float maxForce = 1;

	public Vector3 goalPos = Vector3.Zero, goalVel = Vector3.Zero;

	/** turn off servo when goal conditions are met */
	public boolean turnOff = false;
	public float posTol, velTol;

	private boolean active = false;
	private final XServo3 servo = new XServo3();

	public void activate() {
		active = true;
	}

	public void deactivate() {
		active = false;
		servo.reset();
	}

	@Override
	public void apply(final World world) {
		if (!active) return;

		final Vector3 dr = body.position().sub(goalPos);
		final Vector3 dv = body.linVelocity().sub(goalVel);
		if (turnOff && dr.square() <= posTol && dv.square() <= velTol) {
			deactivate();
			// NOTE add some notification
			return;
		}

		final Vector3 acc = servo.eval(dr, dv);
		body.addForce(clamp(acc.mul(body.mass), maxForce));
	}

	static Vector3 clamp(final Vector3 a, final float max) {
		final float q = a.square();
		return q <= max * max ? a : a.mul(max / GMath.sqrt(q));
	}
}

class XServo3 {
	private final XServo servoX = new XServo(), servoY = new XServo(), servoZ = new XServo();

	Vector3 eval(final Vector3 r, final Vector3 v) {
		return new Vector3(servoX.eval(r.x, v.x), servoY.eval(r.y, v.y), servoZ.eval(r.z, v.z));
	}

	void reset() {
		servoX.reset();
		servoY.reset();
		servoZ.reset();
	}
}

class XServo {
	private float ir, iv; // integrals
	private float pr, pv; // prev values

	//	final static float PR = 0.262451, PV = -0.194847;
	//	final static float IR = -0.128559, IV = -0.760191;
	//	final static float DR = -0.687042, DV = -0.017709;

	//	final static float PR = 0.262451, PV = -0.194847;
	//	final static float IR = -0.128559, IV = -0.860191;
	//	final static float DR = -0.687042, DV = -0.017709;

	final static float PR = -0.5f, PV = -0.2f;
	final static float IR = 0, IV = 0;
	final static float DR = -1, DV = 0;

	float eval(final float r, final float v) {
		ir += r;
		iv += v;
		final float a = PR * r + IR * ir + DR * (r - pr) + PV * v + IV * iv + DV * (v - pv);
		pr = r;
		pv = v;
		return a;
	}

	void reset() {
		ir = iv = 0;
		pr = pv = 0;
	}
}