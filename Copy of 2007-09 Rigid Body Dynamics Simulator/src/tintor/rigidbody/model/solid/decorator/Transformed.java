package tintor.rigidbody.model.solid.decorator;

import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.CollisionPair;
import tintor.rigidbody.model.solid.Decorator;
import tintor.rigidbody.model.solid.Solid;
import tintor.rigidbody.model.solid.atom.Convex;

public final class Transformed extends Decorator {
	public final Transform3 transform;

	public Transformed(final Solid shape, final Transform3 transform) {
		super(shape);
		this.transform = transform;
		radius = maximal(Vector3.Zero);
	}

	@Override public Vector3 centerOfMass() {
		return transform.applyP(solid.centerOfMass());
	}

	@Override public void findContacts(final CollisionPair pair) {
		final Transform3 p = pair.transformA;

		pair.transformA = transform.combine(pair.transformA);
		pair.solidA = solid;

		if (pair.sphereTest()) solid.findContacts(pair);

		pair.solidA = this; // TODO is this needed?
		pair.transformA = p;
	}

	@Override public Side side(final Vector3 point) {
		return solid.side(transform.iapplyP(point));
	}

	@Override public double maximal(final Vector3 center) {
		return solid.maximal(transform.iapplyP(center));
	}

	@Override public void render() {
		GLA.pushMatrix();
		GLA.multMatrix(transform.columnMajorArray());
		solid.render();
		GLA.popMatrix();
	}

	@Override public Matrix3 inertiaTensor() {
		return solid.inertiaTensor().similarT(transform.m).add(transform.v.tildaSqr(), -mass());
	}

	@Override public Interval interval(final Vector3 normal) {
		return interval(normal, transform);
	}

	@Override public Vector3[] intersection(final Plane3 plane) {
		final Vector3[] a = solid.intersection(transform.iapply(plane));
		for (int i = 0; i < a.length; i++)
			a[i] = transform.applyP(a[i]);
		return a;
	}

	@Override public Convex convexHull() {
		return solid.convexHull().ptransform(transform);
	}

	@Override public Transformed transform(final Transform3 transform) {
		return new Transformed(solid, this.transform.combine(transform));
	}
}