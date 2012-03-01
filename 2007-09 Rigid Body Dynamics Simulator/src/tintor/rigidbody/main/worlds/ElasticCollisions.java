package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class ElasticCollisions extends World {
	public ElasticCollisions() {
		final Shape box = Shape.box(1.5f, 1.5f, 1.5f);
		final int n = 10;
		for (int i = 0; i < n; i++) {
			final Body a = new Body(new Vector3(0 + (i - (n - 1) * 0.5) * 4, 10, 0), Quaternion.Identity, box, 1e3f);
			a.elasticity = (float) (1 - Math.pow(2, -i));
			a.sfriction = 0;
			a.dfriction = 0;
			a.color = GLA.blue;
			add(a);
		}

		surface(-13, 1);
	}
}