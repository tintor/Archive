package tintor.rigidbody.model.solid;

import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.geometry.sandbox.Polygon3;
import tintor.rigidbody.model.solid.atom.Convex;

public class Concave extends Solid {
	public final Polygon3[] faces;

	public Concave(int size) {
		faces = new Polygon3[size];
	}
	
	@Override public Vector3 centerOfMass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Convex convexHull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Matrix3 inertiaTensor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Vector3[] intersection(Plane3 plane) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Interval interval(Vector3 normal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public double mass() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public double maximal(Vector3 center) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public void render() {
	// TODO Auto-generated method stub

	}

	@Override public Side side(Vector3 point) {
		// TODO Auto-generated method stub
		return null;
	}
}