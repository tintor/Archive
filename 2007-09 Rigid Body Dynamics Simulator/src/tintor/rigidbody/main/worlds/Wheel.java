package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexPolyhedrons;
import tintor.opengl.GLA;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.Body.State;
import tintor.rigidbody.model.effector.SurfaceGravity;

public class Wheel extends World {
	public Wheel() {
		//final Shape box = Shape.box(0.5, 2.5, 5);

		final double radius = 2;
		//		final Body[] b = new Body[15];
		//		for (int i = 0; i < b.length; i++) {
		//			final double a = 2 * Math.PI / b.length * i;
		//			b[i] = new Body(new Vector3(radius * Math.cos(a), radius * Math.sin(a), 0), Quaternion.axisZ(a), box, 1);
		//			b[i].dfriction = 0.5;
		//			bodies.add(b[i]);
		//		}

		final Body w = new Body(new Vector3(20, -2, 0), Quaternion.Identity, new Shape(ConvexPolyhedrons.prism(9,
				radius, radius, 6)), 1);
		//w.angVel = new Vector3(0, 0, 2);
		w.dfriction = 0.8;
		add(w);

		//		for (int j = b.length - 1, i = 0; i < b.length; j = i++) {
		//			final double a = 2 * Math.PI / b.length * (i - 0.5);
		//			link(b[j], b[i], radius * Math.cos(a), radius * Math.sin(a));
		//		}
		//
		//addSurface(-7, 4);
		effectors.add(new SurfaceGravity(4));
		final Body q = new Body(new Vector3(0, -8, 0), Quaternion.axisZ(Math.PI / 24), Shape.box(100, 2, 100), 1e10);
		q.state = State.Fixed;
		q.color = GLA.orange;
		q.dfriction = 10;
		add(q);
	}

	void link(final Body a, final Body b, final double x, final double y) {
		joints.add(new BallJoint(a, b, new Vector3(x, y, 2.5)));
		joints.add(new BallJoint(a, b, new Vector3(x, y, -2.5)));
	}
}