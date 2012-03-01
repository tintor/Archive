package model.solid;

import geometry.GMath;
import geometry.base.Interval;
import geometry.base.Line3;
import geometry.base.Matrix3;
import geometry.base.Side;
import geometry.base.Transform3;
import geometry.base.Tuple2;
import geometry.base.Vector3;

import java.util.List;

import model.Contact;

public class Capsule extends Atom {
	public final double r;
	/** Distance between centers of hemispheres. */
	public final double h;

	public Capsule(double r, double h) {
		this.r = r;
		this.h = h;

		radius = maximal(Vector3.Zero);
	}

	@Override
	protected List<Contact> findContacts(Transform3 transformA, Solid solidB, Transform3 transformB) {
		if (solidB instanceof Capsule) return findContacts(transformA, (Capsule) solidB, transformB);
		return super.findContacts(transformA, solidB, transformB);
	}

	List<Contact> findContacts(Transform3 transformA, Capsule capsule, Transform3 transformB) {
		Line3 lineA = transformA.apply(new Line3(new Vector3(0, -h / 2, 0), new Vector3(0, h / 2, 0)));
		Line3 lineB = transformB
				.apply(new Line3(new Vector3(0, -capsule.h / 2, 0), new Vector3(0, capsule.h / 2, 0)));

		Tuple2 k = lineA.nearest(lineB);
		return Sphere.sphere2sphere(lineA.point(k.x), r, lineB.point(k.y), capsule.r);
	}

	@Override
	public Matrix3 inertiaTensor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Interval interval(Vector3 normal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double maximal(Vector3 center) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void render() {
	// TODO Auto-generated method stub
	}

	@Override
	public Side side(Vector3 point) {
		boolean border = false;
		double d = point.x * point.x + point.z * point.z - r * r;

		switch (Side.classifySqr(d)) {
		case Outside:
			return Side.Outside;
		case Border:
			border = true;
		case Inside:
		}

		if (point.y >= h / 2)
			switch (Side.classifySqr(d + GMath.sqr(point.y - h / 2))) {
			case Outside:
				return Side.Outside;
			case Border:
				border = true;
			case Inside:
			}
		else if (point.y <= -h / 2) switch (Side.classifySqr(d + GMath.sqr(point.y + h / 2))) {
		case Outside:
			return Side.Outside;
		case Border:
			border = true;
		case Inside:
		}

		return border ? Side.Border : Side.Inside;
	}

	@Override
	public double mass() {
		return r * r * Math.PI * (h + r * 4 / 3);
	}

	@Override
	public Polyhedron convexHull() {
		// TOOD
		return null;
	}
}