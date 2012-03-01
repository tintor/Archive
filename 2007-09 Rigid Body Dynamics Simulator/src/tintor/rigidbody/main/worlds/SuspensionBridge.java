package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;

public class SuspensionBridge extends World {
	public SuspensionBridge() {
		bridge();

		final Body b = new Body(new Vector3(-15, 40, 0), Quaternion.Identity, Shape.sphere(2, 6, GLA.blue, GLA.red), 1);
		b.elasticity = 0.9f;
		b.dfriction = 0.1f;
		b.sfriction = 0.1f;
		add(b);

		effectors.add(new Drag());
		surface(-14, 4);
	}

	void bridge() {
		final Shape box = Shape.box(2.5f, 0.5f, 5);
		final Body[] b = new Body[15];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Body(new Vector3((i - b.length * 0.5) * 3 + 1.5, 0, 0), Quaternion.Identity, box, 1);
			b[i].color = GLA.orange;
			b[i].elasticity = 1;
			add(b[i]);
		}

		for (int i = 1; i < b.length; i++)
			link(b[i - 1], b[i], b[i].position().x - 1.5, 0);

		link(World.Space, b[0], b[0].position().x - 1.5, 0);
		link(b[b.length - 1], World.Space, b[b.length - 1].position().x + 1.5, 0);
	}

	void link(final Body a, final Body b, final double x, final double y) {
		joints.add(new BallJoint(a, b, new Vector3(x, y, 2.5)));
		joints.add(new BallJoint(a, b, new Vector3(x, y, -2.5)));
	}
}