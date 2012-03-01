package tintor.rigidbody.model.effector;

import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;

public class Thruster implements Effector {
	public Body body;
	public Vector3 force;
	public float time;

	@Override
	public void apply(final World world) {
		if (time <= 0) return;
		body.addForce(time < world.timeStep ? force.mul(time / world.timeStep) : force);
		time -= world.timeStep;
	}
}