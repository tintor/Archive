package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector2;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexPolyhedrons;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class AATrebuchet extends World {
	public AATrebuchet() {
		final Body arm = new Body(new Vector3(-8, 2, 0), Quaternion.Identity, Shape.box(30, 1, 1), 1);
		bodies.add(arm);

		final Vector2[] s = { new Vector2(-4, -5), new Vector2(4, -5), new Vector2(0, 10) };
		final Shape basis = new Shape(ConvexPolyhedrons.prism(s, s, 2));

		final Body b1 = new Body(new Vector3(0, -6, -3), Quaternion.Identity, basis, 1);
		b1.dfriction = 0.5;
		bodies.add(b1);

		final Body b2 = new Body(new Vector3(0, -6, 3), Quaternion.Identity, basis, 1);
		b2.dfriction = 0.5;
		bodies.add(b2);

		link(arm, b1, 0, 2, -4, -2);
		link(arm, b2, 0, 2, 4, 2);

		addSurface(-12, 4);
	}

	void link(final Body a, final Body b, final double x, final double y, final double z1, final double z2) {
		constraints.add(new BallJoint(a, b, new Vector3(x, y, z1)));
		constraints.add(new BallJoint(a, b, new Vector3(x, y, z2)));
	}
}