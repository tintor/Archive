package tintor.rigidbody.main.worlds;

import java.util.Random;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.SurfaceGravity;

public class Tower extends World {
	//final Body plate;

	public Tower() {
		impulseIterations = 40;
		forceIterations = 80;

		final int n = 6, h = 30;
		final double r = 30;

		final Random rand = new Random();
		for (int y = 0; y < h; y++) {
			final double s = 1, off = 15;

			final double ir = r * 10 / (y * s + off);
			final Shape brick = Shape.box(25 * 10 / (y * s + off), 6.5, 12 * 10 / (y * s + off));

			for (int x = 0; x < n; x++) {
				final double p = x * 2 * Math.PI / n + (y % 2 == 0 ? 0 : Math.PI / n);
				final double q = p + Math.PI / 2;
				final Body a = new Body(new Vector3(Math.cos(p) * ir, y * 6.5, -Math.sin(p) * ir), Quaternion
						.axisY(q), brick, 1);
				a.sfriction = 0.4;
				a.dfriction = 0.4;
				a.elasticity = 0.1;
				a.color = new Vector3(1, (rand.nextInt(2) == 0 ? 0.4 : 0.60) + rand.nextGaussian() * 0.1, 0);
				add(a);
			}
		}

		final Body plate = new Body(new Vector3(0, -5 - 6.5 / 2, 0), Quaternion.Identity, Shape.box(100, 10, 100), 1e10);
		plate.color = GLA.brown;
		add(plate);

		//		link(-100, -100);
		//		link(100, -100);
		//		link(100, 100);
		//		link(-100, 100);

		//		final Body a = new Body(new Vector3(0, 50, 100), Quaternion.Identity, Shape.sphere(15, 8), 5);
		//		a.elasticity = 0;
		//		a.dfriction = 0.4;
		//		a.linVel = new Vector3(0, 0, -60);
		//		a.angVel = new Vector3(0, 0, Math.PI * 0.2);
		//		bodies.add(a);

		effectors.add(new SurfaceGravity(5));
		//addSurface(-6.5, 5);

	}

	double len = 400;
	final Shape bar = Shape.box(1, 20, 1);

	//	void link(final double x, final double z) {
	//		final Body b = new Body(new Vector3(x, len / 2 + 5, z), Quaternion.Identity, bar, 1e10);
	//		b.color = GLA.gray;
	//		bodies.add(b);
	//
	//		constraints.add(new BallJoint(World.Space, b, new Vector3(x, len + 7.5, z)));
	//		constraints.add(new BallJoint(plate, b, new Vector3(x, 2.5, z)));
	//	}
}