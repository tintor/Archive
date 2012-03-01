package model.solid;

import geometry.VList;
import geometry.base.Interval;
import geometry.base.Matrix3;
import geometry.base.Side;
import geometry.base.Transform3;
import geometry.base.Vector3;

import java.util.List;

import model.Contact;
import view.GLA;

public final class Cone extends Atom {
	public double r, h;

	public Cone(double r, double h) {
		this.r = r;
		this.h = h;

		radius = maximal(Vector3.Zero);
	}

	@Override
	protected List<Contact> findContacts(Transform3 transformA, Solid solidB, Transform3 transformB) {
		// TODO
		return super.findContacts(transformA, solidB, transformB);
	}

	@Override
	public Side side(Vector3 point) {
		return null;
	}

	@Override
	public Interval interval(Vector3 normal) {
		// TODO
		return null;
	}

	@Override
	public double maximal(Vector3 center) {
		return 0;
	}

	@Override
	public double mass() {
		return r * r * Math.PI * h / 3;
	}

	@Override
	public Matrix3 inertiaTensor() {
		double i = r * r * 3 / 20 + h * h * (3 / 5 + 4 / 9);
		return new Matrix3(i, r * r * 3 / 10, i);
	}

	@Override
	public void render() {
		final int as = Sphere.Segments;
		final double ak = 2 * Math.PI / as;
		final double d = Math.sqrt(r * r + h * h), x = h / d, y = r / d;

		GLA.beginTriangleFan();
		GLA.normal(0, 1, 0);
		GLA.vertex(0, h * 2 / 3, 0);
		for (int a = as; a >=  0; a--) {
			double cos = Math.cos(a * ak), sin = Math.sin(a * ak);
			GLA.normal(x * cos, y, sin);
			GLA.vertex(r * cos, -h / 3, r * sin);
		}
		GLA.end();

		GLA.beginPolygon();
		GLA.normal(0, -1, 0);
		for (int a = 0; a <= as; a++)
			GLA.vertex(r * Math.cos(a * ak), -h / 3, r * Math.sin(a * ak));
		GLA.end();
	}

	@Override
	public Polyhedron convexHull() {
		VList list = new VList();

		final int as = Sphere.Segments;
		final double ak = 2 * Math.PI / as;
		for (int a = 0; a <= as; a++)
			list.add(r * Math.cos(a * ak), -h / 3, r * Math.sin(a * ak));
		list.add(0, h * 2 / 3, 0);

		return new Polyhedron(list);
	}
}