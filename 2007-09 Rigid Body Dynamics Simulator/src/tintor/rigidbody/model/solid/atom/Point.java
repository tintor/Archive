package tintor.rigidbody.model.solid.atom;

import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.Atom;

public class Point extends Atom {
	public final static Point Instance = new Point();

	@Override
	public Matrix3 inertiaTensor() {
		return Matrix3.Identity;
	}

	@Override
	public Interval interval(Vector3 normal) {
		return new Interval(0, 0);
	}

	@Override
	public double maximal(Vector3 center) {
		return center.length();
	}

	@Override
	public void render() {
		GLA.gl.glPointSize(5);
		GLA.beginPoints();
		GLA.vertex(Vector3.Zero);
		GLA.end();
	}

	@Override
	public Side side(Vector3 point) {
		return point.equals(Vector3.Zero) ? Side.Zero : Side.Positive;
	}

	@Override
	public double mass() {
		return 1;
	}

	@Override
	public Vector3[] intersection(Plane3 plane) {
		return plane.offset <= 0 ? new Vector3[] {Vector3.Zero} : new Vector3[0];
	}

	@Override
	public Convex convexHull() {
		// TOOD
		return null;
	}
}