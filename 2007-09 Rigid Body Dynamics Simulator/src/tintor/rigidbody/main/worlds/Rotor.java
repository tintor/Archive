package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Rotor extends World {
	public Rotor() {
		final Body b = new Body(Vector3.Zero, Quaternion.Identity, Shape.box(30, 4, 30), 1e3f);
		b.setAngVelocity(new Vector3(0, 0.5, 0));
		b.elasticity = 0;
		add(b);

		final Body a = new Body(new Vector3(-5, 3, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
		a.color = GLA.red;
		a.elasticity = 0;
		add(a);

		final Body c = new Body(new Vector3(0, -4, 0), Quaternion.Identity, Shape.box(100, 4, 100), 1e3f);
		c.elasticity = 0;
		c.dfriction = 0;
		c.sfriction = 0;
		c.color = GLA.orange;
		add(c);

		surface(-6, 4);
	}
}