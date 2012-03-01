package tintor.rigidbody.model.solid;

import tintor.XArrays;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.solid.atom.Convex;

public final class Composite extends Solid {
	public final Solid[] shapes;

	public Composite(final Solid... shapes) {
		this.shapes = shapes.clone();
		radius = maximal(Vector3.Zero);
	}

	@Override public Vector3 centerOfMass() {
		Vector3 p = Vector3.Zero;
		double mass = 0;
		for (final Solid s : shapes) {
			final double m = s.mass();
			p = p.add(m, s.centerOfMass());
			mass += m;
		}
		return p.div(mass);
	}

	@Override public void findContacts(final CollisionPair pair) {
		for (final Solid s : shapes) {
			pair.solidA = s;
			if (pair.sphereTest()) s.findContacts(pair);
		}
		pair.solidA = this; // TODO is this nesesary?
	}

	@Override public Side side(final Vector3 point) {
		boolean border = false;
		for (final Solid s : shapes) {
			final Side a = s.side(point);
			if (a == Side.Negative) return a;
			if (a == Side.Zero) border = true;
		}
		return border ? Side.Zero : Side.Positive;
	}

	@Override public Interval interval(final Vector3 normal) {
		Interval i = Interval.Empty;
		for (final Solid s : shapes)
			i = i.union(s.interval(normal));
		return i;
	}

	@Override public double maximal(final Vector3 center) {
		double r = 0;
		for (final Solid s : shapes)
			r = Math.max(r, s.maximal(center));
		return r;
	}

	//	@Override
	//	public double surface() {
	//		double a = 0;
	//		for(int i = 0; i < shapes.length; i++)
	//			a += shapes[i].surface() * GMath.sqr(transforms[i].s);
	//		return a;
	//	}

	//	@Override
	//	public double volume() {
	//		double a = 0;
	//		for (Solid s : shapes)
	//			a += s.volume();
	//		return a;
	//	}

	@Override public double mass() {
		double a = 0;
		for (final Solid s : shapes)
			a += s.mass();
		return a;
	}

	@Override public Matrix3 inertiaTensor() {
		Matrix3 m = Matrix3.Zero;
		for (final Solid s : shapes)
			m = m.add(s.inertiaTensor());
		return m;
	}

	@Override public void render() {
		for (final Solid s : shapes)
			s.render();
	}

	@Override public Vector3[] intersection(final Plane3 plane) {
		final Vector3[][] a = new Vector3[shapes.length][];
		for (int i = 0; i < shapes.length; i++)
			a[i] = shapes[i].intersection(plane);
		return XArrays.join(a);
	}

	@Override public Convex convexHull() {
		// TOOD
		return null;
	}
}