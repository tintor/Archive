package model.solid;

import geometry.VList;
import geometry.base.Interval;
import geometry.base.Line3;
import geometry.base.Matrix3;
import geometry.base.Side;
import geometry.base.Transform3;
import geometry.base.Vector3;

import java.util.List;

import model.Contact;
import view.GLA;

public final class Cylinder extends Atom {
	public final double r, h;

	final Line3 line;
	final Interval interval;

	public Cylinder(double r, double h) {
		this.r = r;
		this.h = h;

		this.line = new Line3(new Vector3(0, -h, 0), new Vector3(0, h, 0));
		this.interval = new Interval(-h, h);
	}

	@Override
	protected List<Contact> findContacts(Transform3 transformA, Solid solidB, Transform3 transformB) {
		if (solidB instanceof Cylinder) cylinder2cylinder(this, transformA, (Cylinder) solidB, transformB);
		return super.findContacts(transformA, solidB, transformB);
	}

	@Override
	public Matrix3 inertiaTensor() {
		double i = (3 * r * r + 4 * h * h) / 12;
		return new Matrix3(i, r * r / 2, i);
	}

	@Override
	public Interval interval(Vector3 normal) {
		double m = Math.abs(normal.y) * h + Math.hypot(normal.x, normal.y) * r;
		return new Interval(-m, m);
	}

	@Override
	public double maximal(Vector3 center) {
		double x = Math.hypot(center.x, center.y);
		return Math.hypot(x > 0 ? r + x : r - x, center.y > 0 ? h + center.y : h - center.y);
	}

	@Override
	public void render() {
		final int as = Sphere.Segments;
		final double ak = 2 * Math.PI / as;

		GLA.beginQuadStrip();
		for (int a = 0; a <= as; a++) {
			double cos = Math.cos(a * ak), sin = Math.sin(a * ak);
			GLA.normal(cos, 0, sin);
			GLA.vertex(r * cos, -h, r * sin);
			GLA.vertex(r * cos, h, r * sin);
		}
		GLA.end();

		GLA.beginPolygon();
		GLA.normal(0, 1, 0);
		for (int a = as; a >= 0; a--)
			GLA.vertex(r * Math.cos(a * ak), h, r * Math.sin(a * ak));
		GLA.end();

		GLA.beginPolygon();
		GLA.normal(0, -1, 0);
		for (int a = 0; a <= as; a++)
			GLA.vertex(r * Math.cos(a * ak), -h, r * Math.sin(a * ak));
		GLA.end();
	}

	@Override
	public Side side(Vector3 point) {
		return Side.classifyConvex(Math.hypot(point.x, point.z) - r, point.y - h, -h - point.y);
	}

	@Override
	public double mass() {
		return r * r * Math.PI * h;
	}

	@Override
	public Polyhedron convexHull() {
		VList list = new VList();

		final int as = Sphere.Segments;
		final double ak = 2 * Math.PI / as;
		for (int a = 0; a <= as; a++) {
			double cos = r * Math.cos(a * ak), sin = r * Math.sin(a * ak);
			list.add(cos, -h, sin);
			list.add(cos, h, sin);
		}

		return new Polyhedron(list);
	}
}