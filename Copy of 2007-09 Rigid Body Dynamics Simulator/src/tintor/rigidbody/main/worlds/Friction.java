package tintor.rigidbody.main.worlds;

import tintor.geometry.GMath;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Friction extends World {
	public Friction() {
		//      A    B
		// new
		// 10 - 0.24 (0.18)
		// 20 - 0.48 (0.36)
		// 30 - 0.74 (0.58)
		// old
		// 40 - 0.87 (0.84)
		// 50 - 1.35 (1.19)
		// 60 - 1.87 (1.73)
		// 70 - 3.01 (2.73)
		// 80 - 5.75 (5.67)

		// A measured static friction
		// B calculated static friction

		final double angle = GMath.deg2rad(30);
		final double df = 0.6, sf = 0.6;

		final Quaternion q = Quaternion.make(Vector3.Z, -angle);

		final Body a = new Body(q.rotate(new Vector3(-10, 2, 0)), q, Shape.box(12, 2, 4), 1);
		a.elasticity = 0;
		a.sfriction = sf;
		a.dfriction = df;
		//a.linVel = new Vector3(-v * Math.cos(angle), v * Math.sin(angle), 0);
		a.color = GLA.blue;
		add(a);

		final Body b = new Body(Vector3.Zero, q, Shape.box(400, 2, 10), 1e10);
		b.elasticity = 0;
		b.sfriction = sf;
		b.dfriction = sf;
		b.state = Body.State.Fixed;
		b.color = GLA.orange;
		add(b);

		surface(-15, 1);
	}
}
