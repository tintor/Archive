package tintor.rigidbody.main.worlds;

import tintor.geometry.Plane3;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Balls extends World {
	public Balls() {
		impulseIterations = 1;
		forceIterations = 1;

		final Shape ball = Shape.sphere(5, 8);
		ball.bicolor(Vector3.X, GLA.yellow, GLA.blue);

		final Body b = new Body(new Vector3(0, 10, 0), Quaternion.Identity, ball, 1);
		b.setAngVelocity(new Vector3(0, 0, Math.PI));
		b.setLinVelocity(new Vector3(0, 0, 0));
		b.elasticity = 0.9f;
		b.dfriction = 0.5f;
		b.sfriction = 0.5f;
		add(b);

		final Body a = new Body(new Vector3(10, 10, 0), Quaternion.Identity, ball, 1);
		//a.angVel = new Vector3(0, 0, Math.PI);
		a.setLinVelocity(new Vector3(0, 0, 0));
		a.elasticity = 0.9f;
		a.dfriction = 0.5f;
		a.sfriction = 0.5f;
		add(a);

		//joints.add(new BarJoint(a, b, new Vector3(0, 2.5, 0), new Vector3(0, 2.5, 0)));

		surface(-10, 1);
		add(new Plane3(new Vector3(1, 0, 0), 30));
		add(new Plane3(new Vector3(-1, 0, 0), 30));
		add(new Plane3(new Vector3(0, 0, 1), 30));
		add(new Plane3(new Vector3(0, 0, -1), 30));
	}
}