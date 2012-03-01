package tintor.rigidbody.model.solid;

import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.solid.atom.Box;
import tintor.rigidbody.model.solid.atom.Convex;
import tintor.rigidbody.model.solid.decorator.Colored;
import tintor.rigidbody.model.solid.decorator.Compiled;
import tintor.rigidbody.model.solid.decorator.DensityFactor;
import tintor.rigidbody.model.solid.decorator.Transformed;

@SuppressWarnings("unchecked")
public abstract class Solid {
	public double radius;

	public abstract Side side(Vector3 point);

	public abstract Interval interval(Vector3 normal);

	public abstract double maximal(Vector3 center);

	//	public abstract double volume();

	public abstract double mass();

	public abstract Vector3 centerOfMass();

	public abstract Matrix3 inertiaTensor();

	public abstract void render();

	public abstract Vector3[] intersection(Plane3 plane);

	public abstract Convex convexHull();

	public void findContacts(CollisionPair pair) {
		pair.none();
	}

	public Interval interval(Vector3 normal, Transform3 transform) {
		return interval(transform.iapplyV(normal)).add(transform.v.dot(normal));
	}

	// -----------

	public static Solid cube(double a) {
		return new Box(a, a, a);
	}
	
	// -----------
	
	// TODO merge decorators of same type!
	public Compiled compile() {
		return new Compiled(this);
	}

	public Transformed transform(Transform3 transform) {
		return new Transformed(this, transform);
	}

	public DensityFactor density(double density) {
		return new DensityFactor(this, density);
	}

	public Colored color(Vector3 color) {
		return new Colored(this, color);
	}

	public Colored color(double red, double green, double blue) {
		return new Colored(this, red, green, blue);
	}
}