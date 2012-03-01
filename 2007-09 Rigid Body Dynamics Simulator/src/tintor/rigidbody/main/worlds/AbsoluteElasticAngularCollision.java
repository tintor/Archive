package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

// TODO add joints to hold bodies in place
public class AbsoluteElasticAngularCollision extends World {
	public AbsoluteElasticAngularCollision() {
		final Body a = new Body(new Vector3(-2.2, 0, 0), Quaternion.Identity, Shape.box(1, 5, 1), 1);
		a.setAngVelocity(new Vector3(0, 0, 0.1));
		a.elasticity = 1;
		a.mass = 1e6f;
		a.imass = 1 / a.mass;
		add(a);

		final Body b = new Body(a.position().neg(), Quaternion.Identity, a.shape, 1);
		b.setAngVelocity(a.angVelocity());
		b.elasticity = a.elasticity;
		b.mass = 1e6f;
		b.imass = 1 / b.mass;
		add(b);
	}
}