package tintor.geometry_removed;

import tintor.geometry.double2.Vector2;
import tintor.geometry.double3.Vector3;

public class GMath {
	/** golden ratio */
	public static final double FI = (1 + Math.sqrt(5)) / 2;

	public static double average(final double[] array) {
		double m = 0;
		for (final double a : array)
			m += a;
		return m / array.length;
	}

	public static double deviation(final double[] array) {
		final double m = average(array);
		double d = 0;
		for (final double a : array)
			d += (a - m) * (a - m);
		return Math.sqrt(d / array.length);
	}

	public static double clamp(final double a, final double min, final double max) {
		return a < min ? min : a > max ? max : a;
	}

	public static double clamp(final double a, final double mag) {
		return clamp(a, -mag, mag);
	}

	public static double max(final double a, final double b, final double c) {
		return Math.max(Math.max(a, b), c);
	}

	public static double min(final double a, final double b, final double c) {
		return Math.min(Math.min(a, b), c);
	}

	public static double square(final double a) {
		return a * a;
	}

	public static double cube(final double a) {
		return a * a * a;
	}

	public static double rad2deg(final double a) {
		return a * 180 / Math.PI;
	}

	public static double deg2rad(final double a) {
		return a * Math.PI / 180;
	}

	/** 3x3 determinant, row major order */
	public static double det(final double ax, final double ay, final double az, final double bx, final double by,
			final double bz, final double cx, final double cy, final double cz) {
		return ax * (by * cz - bz * cy) + ay * (bz * cx - bx * cz) + az * (bx * cy - by * cx);
	}

	// Linear Equation Solver
	// A*r.x + B*r.y = C
	//	public static Vector2 solveLinear(Vector2 a, Vector2 b, Vector2 c) {
	//		double q = a.det(b);
	//		return new Vector2(a.det(c) / q, b.det(c) / q);
	//	}

	/** A*Rx + B*Ry + C*Rz = D */
	public static Vector3 solveLinearCol(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		final double x = det(b.x, c.x, d.x, b.y, c.y, d.y, b.z, c.z, d.z);
		final double y = det(a.x, c.x, d.y, a.y, c.y, d.y, a.z, c.z, d.z);
		final double z = det(a.x, b.x, d.z, a.y, b.y, d.y, a.z, b.z, d.z);
		final double q = 1 / det(a.x, b.x, c.x, a.y, b.y, c.y, a.z, b.z, c.z);
		return new Vector3(x * q, y * q, z * q);
	}

	// Surface

	/** vertices are: 0, a, b, a+b */
	public static double parallelogramSurface(final Vector3 a, final Vector3 b) {
		final double ab = a.dot(b);
		return Math.sqrt(a.square() * b.square() - ab * ab);
	}

	public static double triangleSurface(final Vector3 a, final Vector3 b, final Vector3 c) {
		return parallelogramSurface(a.sub(c), b.sub(c)) / 2;
	}

	// Mass properties
	// moment oko tačke C koja se nalazi u koordinantnom početku
	public static double triangleSignedMoment(final Vector2 a, final Vector2 b) {
		final Vector2 q = b.sub(a);
		// q/3 . [[a + 3q]*a.quad + [a + q/4]*q.quad]
		return q.dot(a.add(3, q).mul(a.square()).add(a.add(0.25, q).mul(q.square()))) / 3;
	}

	public static Vector3 box_inertal_moment(final double a, final double b, final double c) {
		// ASSUME mass = 1
		return new Vector3(b * b + c * c, a * a + c * c, a * a + b * b).div(12);
	}

	// pointing along y axis
	public static Vector3 cylinder_inertal_moment(final double h, final double r) {
		// ASSUME mass = 1
		final double i = r * r / 4 + h * h / 12;
		return new Vector3(i, r * r / 2, i);
	}

	public static Vector3 sphere_inertal_moment(final double r) {
		// ASSUME mass = 1
		final double i = r * r * 2 / 5;
		return new Vector3(i, i, i);
	}

	// pointing along y axis
	public static Vector3 cone_inertal_moment(final double h, final double r) {
		// ASSUME mass = 1
		final double i = r * r * 3 / 20 + h * h * (3 / 5 + 4 / 9);
		return new Vector3(i, r * r * 3 / 10, i);
	}

	// Center of mass
	//	public static Vector3 centerOfMass(final Vector3... points) {
	//		// TODO handle case when points are colinear! (return mid of extremal points)
	//		switch (points.length) {
	//		case 0:
	//			throw new RuntimeException();
	//		case 1:
	//			return points[0];
	//		case 2:
	//			return Vector3.average(points[0], points[1]);
	//		default:
	//			return Polyhedrons.centerOfMass(new ConvexHull3(points).faces());
	//		}
	//	}

	// Volume
	public static double tetrahedronVolume(final Vector3 a, final Vector3 b, final Vector3 c) {
		// vertices are: 0, a, b, c
		return Math.abs(a.mixed(b, c)) / 6;
	}

	public static double tetrahedronVolume(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		return tetrahedronVolume(a.sub(d), b.sub(d), c.sub(d));
	}
}