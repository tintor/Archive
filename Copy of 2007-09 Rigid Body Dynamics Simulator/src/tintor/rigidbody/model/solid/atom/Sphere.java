package tintor.rigidbody.model.solid.atom;

import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.geometry.extended.VList;
import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.Atom;
import tintor.rigidbody.model.solid.CollisionPair;

public class Sphere extends Atom {
	public Sphere(double radius) {
		this.radius = radius;
	}

	@Override
	public void findContacts(CollisionPair pair) {
		pair.sphere();
	}

	@Override
	public Matrix3 inertiaTensor() {
		return new Matrix3(mass() * radius * radius * 2 / 5);
	}

	@Override
	public Interval interval(Vector3 normal) {
		return new Interval(-radius, radius);
	}

	@Override
	public double maximal(Vector3 center) {
		return radius + center.length();
	}

	public static int Segments = 20;

	@Override
	public void render() {
		final int as = Segments / 2, bs = Segments;
		final double ak = Math.PI / as, bk = 2 * Math.PI / bs;

		Vector3 p;
		for (int a = 1; a < as - 1; a++) {
			double cosA = Math.cos(a * ak), cosA1 = Math.cos(a * ak + ak);
			double sinA = Math.sin(a * ak), sinA1 = Math.sin(a * ak + ak);

			GLA.beginQuadStrip();
			for (int b = 0; b <= bs; b++) {
				double cosB = Math.cos(b * bk), sinB = Math.sin(b * bk);

				p = new Vector3(sinA1 * cosB, cosA1, sinA1 * sinB);
				GLA.normal(p);
				GLA.vertex(p.mul(radius));

				p = new Vector3(sinA * cosB, cosA, sinA * sinB);
				GLA.normal(p);
				GLA.vertex(p.mul(radius));
			}
			GLA.end();
		}

		double cosA1 = Math.cos(Math.PI / as), sinA1 = Math.sin(Math.PI / as);
		GLA.beginTriangleFan();
		p = new Vector3(0, 1, 0);
		GLA.normal(p);
		GLA.vertex(p.mul(radius));
		for (int b = bs; b >= 0; b--) {
			double beta = b * 2 * Math.PI / bs;
			p = new Vector3(sinA1 * Math.cos(beta), cosA1, sinA1 * Math.sin(beta));
			GLA.normal(p);
			GLA.vertex(p.mul(radius));
		}
		GLA.end();

		double alpha = (as - 1) * Math.PI / as;
		double cosA = Math.cos(alpha), sinA = Math.sin(alpha);
		GLA.beginTriangleFan();
		p = new Vector3(0, -1, 0);
		GLA.normal(p);
		GLA.vertex(p.mul(radius));
		for (int b = 0; b <= bs; b++) {
			double beta = b * 2 * Math.PI / bs;
			double cosB = Math.cos(beta), sinB = Math.sin(beta);

			p = new Vector3(sinA * cosB, cosA, sinA * sinB);
			GLA.normal(p);
			GLA.vertex(p.mul(radius));

		}
		GLA.end();
	}

	@Override
	public Side side(Vector3 point) {
		return Side.classifySqr(point.square() - radius * radius);
	}

	@Override
	public double mass() {
		return GMath.cube(radius) * Math.PI * 4 / 3;
	}

	@Override
	public Vector3[] intersection(Plane3 plane) {
		switch (Side.classify(plane.offset - radius)) {
		case Positive:
			return new Vector3[0];
		case Zero:
			return new Vector3[] { plane.normal.mul(-radius) };
		case Negative:
			Vector3 x = plane.normal.normal().unit(),
			y = plane.normal.cross(x);
			// FIXME incorrect!
			return new Vector3[] { x.mul(radius), y.mul(radius), x.mul(-radius), y.mul(-radius) };
		}
		return null;
	}

	@Override
	public Convex convexHull() {
		VList list = new VList();

		final int as = Segments / 2, bs = Segments;
		final double ak = Math.PI / as, bk = 2 * Math.PI / bs;
		for (int a = 1; a < as - 1; a++) {
			double cosA = Math.cos(a * ak) * radius, sinA = Math.sin(a * ak) * radius;
			for (int b = 0; b <= bs; b++) {
				double cosB = Math.cos(b * bk), sinB = Math.sin(b * bk);
				list.add(sinA * cosB, cosA, sinA * sinB);
			}
		}
		list.add(0, radius, 0);
		list.add(0, -radius, 0);

		return new Convex(list.toArray());
	}
}