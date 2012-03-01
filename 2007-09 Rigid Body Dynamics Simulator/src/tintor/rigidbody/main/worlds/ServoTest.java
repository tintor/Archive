package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.collisiondetector.Off;
import tintor.rigidbody.model.effector.Servo3;
import tintor.rigidbody.model.effector.Thruster;

public class ServoTest extends World {
	public ServoTest() {
		super(new Off());

		servo(new Vector3(0, -5, 0), new Vector3(0, 0, 0), new Vector3(0, 0, 0), 1);
		servo(new Vector3(-5, -5, 0), new Vector3(0, -5, 0), new Vector3(-5, 0, 0), 1);
		servo(new Vector3(5, -5, 0), new Vector3(0, 5, 0), new Vector3(5, 0, 0), 1);
		servo(new Vector3(10, -5, 0), new Vector3(5, 5, 0), new Vector3(10, 0, 0), 1);
		final Body a = servo(new Vector3(-10, -5, 0), new Vector3(0, 0, 0), new Vector3(-10, 0, 0), 1);

		final Thruster thruster = new Thruster();
		thruster.body = a;
		thruster.force = new Vector3(0, -0.95, 0);
		thruster.time = 100;
		effectors.add(thruster);
	}

	private static Shape box = Shape.box(1, 1, 1);

	private Body servo(final Vector3 pos, final Vector3 velocity, final Vector3 goal, final float force) {
		final Body a = new Body(pos, Quaternion.Identity, box, 1);
		a.color = GLA.red;
		a.setLinVelocity(velocity);
		add(a);

		final Body b = new Body(goal, Quaternion.Identity, box, 1);
		b.color = GLA.blue;
		add(b);

		final Servo3 servo = new Servo3();
		servo.maxForce = force;
		servo.goalPos = goal;
		servo.body = a;
		servo.activate();
		effectors.add(servo);
		return a;
	}
}