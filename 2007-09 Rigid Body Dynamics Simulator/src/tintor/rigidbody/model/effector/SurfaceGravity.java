package tintor.rigidbody.model.effector;

import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.Body.State;

public class SurfaceGravity implements Effector {
	public Vector3 gravity;
	public double zero; // height with zero potential energy

	public SurfaceGravity(double g) {
		this(new Vector3(0, -g, 0));
	}

	public SurfaceGravity(final Vector3 gravity) {
		this.gravity = gravity;
	}

	@Override public void apply(final World world) {
		for (final Body b : world.bodies)
			if (b.state != State.Fixed) b.addLinAcc(gravity);
	}
	
	@Override
	public void render() {
	}
}