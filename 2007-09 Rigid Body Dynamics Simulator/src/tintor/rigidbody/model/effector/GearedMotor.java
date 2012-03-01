package tintor.rigidbody.model.effector;

import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.joint.Axis;

public class GearedMotor implements Effector {
	public Axis axis;
	public double maxForce;
	public double maxVelociy;

	@Override public void apply(final World world) {

	}
}