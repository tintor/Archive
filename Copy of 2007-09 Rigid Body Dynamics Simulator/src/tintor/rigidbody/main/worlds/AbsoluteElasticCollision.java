package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.Body.State;

public class AbsoluteElasticCollision extends World {
	public AbsoluteElasticCollision() {
		final Body a = new Body(new Vector3(-6, 0, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
		a.elasticity = 1;
		a.linVel = new Vector3(-2, 0, 0);
		a.color = GLA.red;
		add(a);

		for (int i = -1; i < 2; i++) {
			final Body x = new Body(new Vector3(i * 4, 0, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
			x.elasticity = 1;
			x.color = GLA.blue;
			add(x);

			final Body b = new Body(new Vector3(i * 4 + 2, 0, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
			b.elasticity = 1;
			b.color = GLA.red;
			add(b);
		}

		final Body c = new Body(new Vector3(20, 0, 0), Quaternion.Identity, Shape.box(4, 4, 4), 1e9f);
		c.state = State.Fixed;
		c.elasticity = 1;
		add(c);

		final Body d = new Body(new Vector3(-20, 0, 0), Quaternion.Identity, Shape.box(4, 4, 4), 1e9f);
		d.state = State.Fixed;
		d.elasticity = 1;
		add(d);
	}
}