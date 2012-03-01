package tintor.rigidbody.model.solid;

import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.solid.atom.Convex;

public class Decorator extends Solid {
	public final Solid solid;

	public Decorator(Solid shape) {
		this.solid = shape;
		this.radius = shape.radius;
	}

	@Override
	public Vector3 centerOfMass() {
		return solid.centerOfMass();
	}

	@Override
	public void findContacts(CollisionPair pair) {
		pair.solidA = solid;
		solid.findContacts(pair);
		pair.solidA = this; // TODO is this needed?
	}

	@Override
	public Matrix3 inertiaTensor() {
		return solid.inertiaTensor();
	}

	@Override
	public Interval interval(Vector3 normal) {
		return solid.interval(normal);
	}

	@Override
	public double mass() {
		return solid.mass();
	}

	@Override
	public double maximal(Vector3 center) {
		return solid.maximal(center);
	}

	@Override
	public void render() {
		solid.render();
	}

	@Override
	public Side side(Vector3 point) {
		return solid.side(point);
	}

//	@Override
//	public double volume() {
//		return solid.volume();
//	}

	@Override
	public Vector3[] intersection(Plane3 plane) {
		return solid.intersection(plane);
	}

	@Override
	public Convex convexHull() {
		return solid.convexHull();
	}
}