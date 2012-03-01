package tintor.rigidbody.model.solid;

import tintor.geometry.Plane3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.solid.atom.Convex;

public abstract class Atom extends Solid {
	@Override
	public final Vector3 centerOfMass() {
		return Vector3.Zero;
	}

//	@Override
//	public double mass() {
//		return volume();
//	}

	@Override
	public Vector3[] intersection(Plane3 plane) {
		return convexHull().intersection(plane);
	}

	public Vector3[] intersection(Atom atomA, Transform3 transformA, Atom atomB, Transform3 transformB) {
		return Convex.intersect(atomA.convexHull(), transformA, atomB.convexHull(), transformB);
	}
}