package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;

public class Dominoes extends World {

	public Dominoes() {
		float x = 20, z = -8;
		final float a = 0;
		final int n = 10;
		for (int i = 0; i < n; i++) {
			dominoe(x, z, a);
			x -= 3.5;
		}
		for (int i = 0; i <= 8; i++)
			dominoe((float) (x - 8 * Math.sin(i * Math.PI / 8)), (float) (z + 8 * Math.cos(i * Math.PI / 8) + 8),
					(float) (-i * Math.PI / 8));
		z += 16;
		for (int i = 0; i < n; i++) {
			x += 3.5;
			dominoe(x, z, a);
		}

		final Body p = new Body(new Vector3(24, 1.5, z), Quaternion.Identity, Shape.box(0.5f, 0.5f, 0.5f), 8);
		p.setLinVelocity(new Vector3(-5, 0, 0));
		p.elasticity = 0.5f;
		p.color = GLA.red;
		add(p);

		effectors.add(new Drag());
		surface(-3, 2);
	}
	final Shape box = Shape.box(1, 5, 2.5f);

	void dominoe(final float x, final float z, final float a) {
		final Body b = new Body(new Vector3(x, -0.5, z), Quaternion.axisY(a), box, 1);
		b.elasticity = 0;
		b.sfriction = 0.5f;
		b.dfriction = 0.5f;
		b.color = GLA.blue;
		add(b);
	}
}