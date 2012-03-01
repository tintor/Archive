package tintor.rigidbody.model.solid.atom;

import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.Atom;
import tintor.rigidbody.model.solid.CollisionPair;

public class Plane extends Atom {
	public final Plane3 plane;

	public Plane(Plane3 plane) {
		this.plane = plane;
		radius = GMath.Infinity;
	}

	@Override
	public void findContacts(CollisionPair pair) {
		if(pair.solidB instanceof Atom)
			pair.plane(plane);
		else
			pair.none();
	}

	@Override
	public Matrix3 inertiaTensor() {
		return new Matrix3(GMath.Infinity, GMath.Infinity, GMath.Infinity);
	}

	@Override
	public Interval interval(Vector3 normal) {
		throw new RuntimeException();
	}

	@Override
	public double maximal(Vector3 center) {
		return GMath.Infinity;
	}

	@Override
	public void render() {
		Vector3 c = plane.normal.mul(-plane.offset);
		Vector3 i = c.normal().unit(), j = i.cross(c).unit();

		GLA.beginQuads();
		GLA.normal(plane.normal);
		GLA.vertex(c.add(1e10, i));
		GLA.vertex(c.add(1e10, j));
		GLA.vertex(c.sub(1e10, i));
		GLA.vertex(c.sub(1e10, j));
		GLA.end();
	}

	@Override
	public Side side(Vector3 point) {
		return plane.side(point);
	}

	@Override
	public double mass() {
		return GMath.Infinity;
	}

	@Override
	public Vector3[] intersection(Plane3 plane) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Convex convexHull() {
		throw new UnsupportedOperationException();
	}
}