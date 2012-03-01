package tintor.rigidbody.main.worlds;

import tintor.geometry.GMath;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Sticks extends World {
	float length = 30, angle = 60;

	public Sticks() {
		final Shape s = Shape.box(length, 1, 1);

		for (int i = 0; i <= 10; i++) {
			final Body b = new Body(new Vector3(0, 0, (i - 3) * 5), Quaternion.axisZ(GMath.deg2rad(angle)), s, 1);
			b.sfriction = i * 0.1f;
			b.dfriction = 0;
			b.elasticity = 0;
			add(b);
		}

		surface(-length / 2, 5);
	}
}