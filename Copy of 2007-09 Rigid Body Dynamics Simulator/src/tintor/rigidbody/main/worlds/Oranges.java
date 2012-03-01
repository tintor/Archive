package tintor.rigidbody.main.worlds;

import tintor.geometry.Plane3;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Sensor;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Oranges extends World implements Sensor {
	public Oranges() {
		impulseIterations = 5;
		forceIterations = 5;

		sensors.add(this);
		surface(-4, 4);
		add(new Plane3(new Vector3(1, 0, 0), 8));
		add(new Plane3(new Vector3(-1, 0, 0), 8));
		add(new Plane3(new Vector3(0, 0, 1), 8));
		add(new Plane3(new Vector3(0, 0, -1), 8));
	}

	int frame = 0;

	@Override public void update() {
		frame++;
		if (frame % 250 == 0) place((Math.random() - 0.5) * 5, 40, (Math.random() - 0.5) * 5);
	}

	final Shape orange = Shape.sphere(4, 6, GLA.orange, GLA.yellow);

	void place(final double x, final double y, final double z) {
		final Body b = new Body(new Vector3(x, y, z), Quaternion.Identity, orange, 1);
		b.sfriction = 0.5;
		b.dfriction = 0.5;
		b.elasticity = 0;
		add(b);
	}
}