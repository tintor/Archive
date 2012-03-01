package tintor.geometry;

import tintor.geometry.extended.ConvexHull3;
import tintor.geometry.extended.Polyhedrons;

public final class GMath {
	/** golden ratio */
	public static final float fi = (float) ((1 + Math.sqrt(5)) / 2);

	// Statistics
	public static float average(final float[] array) {
		float m = 0;
		for (final float a : array)
			m += a;
		return m / array.length;
	}

	public static float deviation(final float[] array) {
		final float m = average(array);
		float d = 0;
		for (final float a : array)
			d += square(m - a);
		return (float) Math.sqrt(d / array.length);
	}

	// Scalar functions
	public static boolean isFinite(final float a) {
		return !Float.isNaN(a) && !Float.isInfinite(a);
	}

	public static float rad2deg(final float a) {
		return a * (float) (180 / Math.PI);
	}

	public static float deg2rad(final float a) {
		return a * (float) (Math.PI / 180);
	}

	public static float sqrt(final float a) {
		return (float) Math.sqrt(a);
	}

	public static float sin(final float a) {
		return (float) Math.sin(a);
	}

	public static float cos(final float a) {
		return (float) Math.cos(a);
	}

	public static float square(final float a) {
		return a * a;
	}

	public static float cube(final float a) {
		return a * a * a;
	}

	public static float clamp(final float a, final float min, final float max) {
		return a < min ? min : a > max ? max : a;
	}

	public static float clamp(final float a, final float mag) {
		return clamp(a, -mag, mag);
	}

	public static float max(final float a, final float b, final float c) {
		return Math.max(Math.max(a, b), c);
	}

	public static float min(final float a, final float b, final float c) {
		return Math.min(Math.min(a, b), c);
	}

	/** 3x3 determinant, row major order */
	public static float det(final float ax, final float ay, final float az, final float bx, final float by,
			final float bz, final float cx, final float cy, final float cz) {
		return ax * (by * cz - bz * cy) + ay * (bz * cx - bx * cz) + az * (bx * cy - by * cx);
	}

	// Linear Equation Solver
	// A*Rx + B*Ry = C
	public static Vector2 solveLinear(final Vector2 a, final Vector2 b, final Vector2 c) {
		final float q = a.det(b);
		return new Vector2(a.det(c) / q, b.det(c) / q);
	}

	/** A*Rx + B*Ry + C*Rz = D */
	public static Vector3 solveLinearCol(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		final float x = det(b.x, c.x, d.x, b.y, c.y, d.y, b.z, c.z, d.z);
		final float y = det(a.x, c.x, d.y, a.y, c.y, d.y, a.z, c.z, d.z);
		final float z = det(a.x, b.x, d.z, a.y, b.y, d.y, a.z, b.z, d.z);
		final float q = 1 / det(a.x, b.x, c.x, a.y, b.y, c.y, a.z, b.z, c.z);
		return new Vector3(x * q, y * q, z * q);
	}

	/** (A*R, B*R, C*R) = D */
	public static Vector3 solveLinearRow(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		final float x = det(a.y, a.z, d.x, b.y, b.z, d.y, c.y, c.z, d.z);
		final float y = det(a.x, a.z, d.x, b.x, b.z, d.y, c.x, c.z, d.z);
		final float z = det(a.x, a.y, d.x, b.x, b.y, d.y, c.x, c.y, d.z);
		final float q = 1 / a.mixed(b, c);
		return new Vector3(x * q, y * q, z * q);
	}

	// Surface
	public static float parallelogramSurface(final Vector3 a, final Vector3 b) {
		// vertices are: 0, a, b, a+b
		return (float) Math.sqrt(a.square() * b.square() - GMath.square(a.dot(b)));
	}

	public static float triangleSurface(final Vector3 a, final Vector3 b, final Vector3 c) {
		return parallelogramSurface(a.sub(c), b.sub(c)) / 2;
	}

	// Mass properties
	// moment oko tacke C koja se nalazi u koordinantnom pocetku
	public static float triangleSignedMoment(final Vector2 a, final Vector2 b) {
		final Vector2 q = b.sub(a);
		// q/3 . [[a + 3q]*a.quad + [a + q/4]*q.quad]
		return q.dot(a.add(3, q).mul(a.square()).add(a.add(0.25f, q).mul(q.square()))) / 3;
	}

	public static Vector3 box_inertal_moment(final float a, final float b, final float c) {
		// ASSUME mass = 1
		return new Vector3(b * b + c * c, a * a + c * c, a * a + b * b).div(12);
	}

	// pointing along y axis
	public static Vector3 cylinder_inertal_moment(final float h, final float r) {
		// ASSUME mass = 1
		final float i = r * r / 4 + h * h / 12;
		return new Vector3(i, r * r / 2, i);
	}

	public static Vector3 sphere_inertal_moment(final float r) {
		// ASSUME mass = 1
		final float i = r * r * 2 / 5;
		return new Vector3(i, i, i);
	}

	// pointing along y axis
	public static Vector3 cone_inertal_moment(final float h, final float r) {
		// ASSUME mass = 1
		final float i = r * r * 3 / 20 + h * h * (3 / 5 + 4 / 9);
		return new Vector3(i, r * r * 3 / 10, i);
	}

	// Center of mass
	public static Vector3 centerOfMass(final Vector3... points) {
		// TODO handle case when points are colinear! (return mid of extremal points)
		switch (points.length) {
		case 0:
			throw new RuntimeException();
		case 1:
			return points[0];
		case 2:
			return Vector3.average(points[0], points[1]);
		default:
			return Polyhedrons.centerOfMass(new ConvexHull3(points).faces());
		}
	}

	// Volume
	public static float tetrahedronVolume(final Vector3 a, final Vector3 b, final Vector3 c) {
		// vertices are: 0, a, b, c
		return Math.abs(a.mixed(b, c)) / 6;
	}

	public static float tetrahedronVolume(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		return tetrahedronVolume(a.sub(d), b.sub(d), c.sub(d));
	}
}