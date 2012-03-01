package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;

public class Stack extends World {
	final Shape box = Shape.box(4, 4, 4);
	final float friction = 0.5f;

	public Stack() {
		stack(-30, 10);
		stack(0, 15);
		stack(30, 25);

		effectors.add(new Drag());
		surface(-8, 5);
	}

	void stack(final double x, final int n) {
		double h = 5, y = -20;
		for (int i = 0; i < n; i++) {
			y += 4.1;
			final Body a = new Body(new Vector3(x, y + 10, (Math.random() - 0.5) / 1000000), Quaternion.Identity, box,
					1);
			a.sfriction = friction;
			a.dfriction = friction;
			a.elasticity = 0;
			a.color = i % 2 == 0 ? GLA.mangenta : GLA.blue;
			a.name = "big";
			add(a);
			h -= 0.5;
		}
	}
}