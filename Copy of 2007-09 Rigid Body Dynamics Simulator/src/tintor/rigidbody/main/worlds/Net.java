package tintor.rigidbody.main.worlds;

import java.util.Collections;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;

public class Net extends World {
	public Net() {
		impulseIterations = 10;
		forceIterations = 20;

		final double len = 5, dist = 6.5;

		final Shape bar = Shape.box(len, len / 10, len / 10);
		final Shape box = Shape.box(dist, dist, dist);

		final int n = 8, nh = n / 2;
		final Body[][] bx = new Body[n][n + 1], by = new Body[n + 1][n];

		for (int x = 0; x <= n; x++)
			for (int y = 0; y <= n; y++) {
				if (x % 2 == 0 && y % 2 == 0 && x > 0 && y > 0 && x < n && y < n) {
					final Body b = new Body(new Vector3((x - nh) * dist, 20, (y - nh) * dist),
							Quaternion.Identity, box, 1);
					b.color = GLA.white;
					add(b);
				}

				if (x < n) {
					bx[x][y] = new Body(new Vector3((x - nh + 0.5) * dist, 5, (y - nh) * dist),
							Quaternion.Identity, bar, 1);
					add(bx[x][y]);
				}

				if (y < n) {
					by[x][y] = new Body(new Vector3((x - nh) * dist, 5, (y - nh + 0.5) * dist), Quaternion
							.axisY(Math.PI / 2), bar, 1);
					add(by[x][y]);
				}

				if (x > 0 && x < n) joint(bx[x][y], bx[x - 1][y], (x - nh) * dist, (y - nh) * dist);
				if (y > 0 && y < n) joint(by[x][y], by[x][y - 1], (x - nh) * dist, (y - nh) * dist);

				if (x < n && y < n) joint(bx[x][y], by[x][y], (x - nh) * dist, (y - nh) * dist);
				if (x > 0 && y < n) joint(bx[x - 1][y], by[x][y], (x - nh) * dist, (y - nh) * dist);
				if (x > 0 && y > 0) joint(bx[x - 1][y], by[x][y - 1], (x - nh) * dist, (y - nh) * dist);
				if (x < n && y > 0) joint(bx[x][y], by[x][y - 1], (x - nh) * dist, (y - nh) * dist);
			}

		joint(World.Space, bx[0][0], -nh * dist, -nh * dist);
		joint(World.Space, by[0][0], -nh * dist, -nh * dist);

		joint(World.Space, bx[0][8], -nh * dist, nh * dist);
		joint(World.Space, by[0][7], -nh * dist, nh * dist);

		joint(World.Space, bx[7][8], nh * dist, nh * dist);
		joint(World.Space, by[8][7], nh * dist, nh * dist);

		joint(World.Space, bx[7][0], nh * dist, -nh * dist);
		joint(World.Space, by[8][0], nh * dist, -nh * dist);

		Collections.shuffle(joints);
		effectors.add(new Drag());
		surface(-20, 5);
	}

	void joint(final Body a, final Body b, final double x, final double z) {
		joints.add(new BallJoint(a, b, new Vector3(x, 5, z)));
	}
}