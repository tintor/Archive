package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;

public class Chain1R extends World {
	public Chain1R() {
		final Shape box = Shape.box(2.5, 0.5, 2);

		Body prev = World.Space;
		for (int i = 0; i < 15; i++) {
			final Body b = new Body(new Vector3(i * 3, 10, 0), Quaternion.Identity, box, 10);
			b.dfriction = 0.1;
			b.sfriction = 0.2;
			add(b);
			joints.add(new BallJoint(prev, b, b.position().sub(new Vector3(1.5, 0, 1))));
			joints.add(new BallJoint(prev, b, b.position().sub(new Vector3(1.5, 0, -1))));
			prev = b;
		}
		effectors.add(new Drag());
		surface(-14, 4);
	}
}