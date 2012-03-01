package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.SurfaceGravity;

public class Pendulum extends World {
	public Pendulum() {
		effectors.add(new SurfaceGravity(5));
		final Shape box = Shape.sphere(2, 12);

		final double s = 2;
		for (int i = 0; i < 5; i++) {
			final Body a = addNail(new Vector3(i * s, 5, 0));
			final Body b = new Body(new Vector3(i * s, -5, 0), Quaternion.Identity, box, 1);
			b.elasticity = 1;
			add(b);

			joints.add(new BallJoint(a, b, new Vector3(i * s, 5, -1)));
			joints.add(new BallJoint(a, b, new Vector3(i * s, 5, 1)));
		}

		final Body a = addNail(new Vector3(-s, 5, 0));
		final Body b = new Body(new Vector3(-10 - s, 5, 0), Quaternion.axisZ((float)(-Math.PI / 2)), box, 1);
		add(b);

		joints.add(new BallJoint(a, b, new Vector3(-s, 5, -1)));
		joints.add(new BallJoint(a, b, new Vector3(-s, 5, 1)));
	}
}