package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;

public class Chain3R extends World {
	public Chain3R() {
		final Shape box = Shape.box(2.5f, 0.5f, 0.5f);

		Body prev = World.Space;
		for (int i = 0; i < 15; i++) {
			final Body b = new Body(new Vector3(i * 3, 10, 0), Quaternion.Identity, box, 1);
			b.dfriction = 0.1f;
			b.sfriction = 0.2f;
			add(b);
			joints.add(new BallJoint(prev, b, b.position().sub(new Vector3(1.5, 0, 0))));
			prev = b;
		}
		effectors.add(new Drag());
		surface(-14, 4);
	}
}