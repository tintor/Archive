package tintor.rigidbody.main.worlds;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexPolyhedrons;
import tintor.geometry.sandbox.Polygon3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;

public class Spin extends World {
	public Spin() {
		final Body b = new Body(new Vector3(15, 1, 0), Quaternion.Identity, Shape.box(6, 6, 6), 1);
		b.elasticity = 0.5;
		b.sfriction = 0.5;
		b.dfriction = 0.4;
		b.angVel = new Vector3(0, 1, 0);
		b.color = GLA.blue;
		b.name = "small";
		add(b);

		final Shape s = new Shape(ConvexPolyhedrons.football());
		for (final Polygon3 p : s.faces)
			p.color = p.vertices.length == 5 ? GLA.black : GLA.white;
		final Body c = new Body(new Vector3(-15, 3, 0), Quaternion.Identity, s, 1);
		c.elasticity = 0.5;
		c.sfriction = 0.5;
		c.dfriction = 0.4;
		c.angVel = new Vector3(0, 1, 0);
		c.color = GLA.blue;
		c.name = "small";
		add(c);

		final Body a = new Body(new Vector3(0, 3, 15), Quaternion.axisX(Math.PI / 6), new Shape(ConvexPolyhedrons.prism(
				20, 5, 5, 1)), 1);
		a.sfriction = 0.3;
		a.dfriction = 0.3;
		a.elasticity = 0;
		a.angVel = new Vector3(0, 2, 0);
		add(a);

		surface(-2, 1);
	}
}