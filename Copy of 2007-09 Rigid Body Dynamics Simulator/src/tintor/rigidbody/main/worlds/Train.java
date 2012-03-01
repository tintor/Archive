package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.Body.State;

public class Train extends World {
	public Train() {
		final Shape rail = Shape.box(100, 0.5, 0.5);
		final Shape wagon = Shape.box(10, 3, 3); // 90m^2

		Body prev = null;
		for (int i = 0; i < 10; i++) {
			final Body b = new Body(new Vector3(i * 11, 0, 0), Quaternion.Identity, wagon, 1);
			b.dfriction = 0.1;
			b.sfriction = 0.15;
			b.color = i > 0 ? GLA.blue : GLA.red;
			add(b);

			if (i > 0) {
				joints.add(new BallJoint(prev, b, new Vector3(i * 11 - 5.5, -1, 0)));
				joints.add(new BallJoint(prev, b, new Vector3(i * 11 - 5.5, 1, 0)));
			}
			prev = b;

			if (i == 0) effectors.add(new Effector() {
				@Override public void apply(final World world) {
					b.addForce(b.orientation().dirX().neg().mul(600));
				}
			});
		}

		final Body r = new Body(new Vector3(-35, -1.5 + 0.25, -5), Quaternion.axisY(Math.PI / 12), rail, 1e20);
		r.state = State.Fixed;
		r.color = GLA.gray;
		add(r);

		surface(-1.5, 5);
	}
}