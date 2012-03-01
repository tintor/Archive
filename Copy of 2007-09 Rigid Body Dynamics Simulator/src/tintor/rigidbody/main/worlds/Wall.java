package tintor.rigidbody.main.worlds;

import java.util.Random;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Wall extends World {
	public Wall() {
		final Shape brick = Shape.box(25, 6.5f, 12);

		for (int y = 0; y < 10; y++)
			for (int x = 0; x < 5; x++) {
				final Body b = new Body(new Vector3((x - 2) * 25 + (y % 2 == 0 ? 12 : 0), (y + 0.5) * 6.5 - 30, 0),
						Quaternion.Identity, brick, 1);
				final Random r = new Random();
				b.elasticity = 0;
				b.color = new Vector3(1, (x % 2 == 0 ? 0.4 : 0.60) + r.nextGaussian() * 0.1, 0);
				b.dfriction = 0.4f;
				add(b);
			}

		final Body a = new Body(new Vector3(0, 0, 100), Quaternion.Identity, Shape.sphere(15, 8), 5);
		a.elasticity = 0;
		a.dfriction = 0.4f;
		a.setLinVelocity(new Vector3(0, 0, -60));
		a.setAngVelocity(new Vector3(0, 0, Math.PI * 0.2));
		add(a);

		surface(-30, 5);
	}
}