package tintor.sandbox;

import tintor.geometry.Transform2;
import tintor.geometry.Vector2;

public class Polygon2d {
	public static boolean isConvex(Vector2[] p) {
		assert p.length > 0;
		int pos = 0, neg = 0;
		double s = 0;
		for (int k = p.length - 2, j = p.length - 1, i = 0; i < p.length; k = j, j = i++) {
			s += (p[i].y + p[j].y) * (p[j].x - p[i].x);
			double a = p[i].side(p[k], p[j]);
			if (a >= 0) pos++;
			if (a <= 0) neg++;
		}
		return s == 0 || (s > 0 && neg == p.length) || (s < 0 && pos == p.length);
	}

	public static boolean isConvexAndCCW(Vector2[] p) {
		if (p.length <= 2) return true;
		for (int k = p.length - 2, j = p.length - 1, i = 0; i < p.length; k = j, j = i++)
			if (p[i].side(p[k], p[j]) > 0) return false;
		return true;
	}

	// >0 counte-rclockwise, <0 clockwise
	// public static double signedSurface(double[] p) {
	// if(p.length % 2 != 0 || p.length == 0) throw new
	// IllegalArgumentException();
	// double r = 0;
	// for(int j = p.length - 2, i = 0; i < p.length; j = i, i += 2)
	// r += (p[i + 1] + p[j + 1]) * (p[j] - p[i]);
	// return r / 2;
	// }

	public static double signedSurface(Vector2[] p) {
		assert p.length > 0;
		double s = 0;
		for (int j = p.length - 1, i = 0; i < p.length; j = i++)
			s += (p[i].y + p[j].y) * (p[j].x - p[i].x);
		return s / 2;
	}

	// TESTED
	public static Vector2 centerOfMass(Vector2[] p) {
		switch (p.length) {
		case 0:
			return null;
		case 1:
			return p[0];
		case 2:
			return Vector2.linear(p[0], p[1], 0.5);
		default:
		}

		double s = 0, x = 0, y = 0;
		for (int j = p.length - 1, i = 0; i < p.length; j = i, i++) {
			double a = p[j].det(p[i]);
			s += a;
			x += a * (p[i].x + p[j].x);
			y += a * (p[i].y + p[j].y);
		}
		assert s > 0;
		return new Vector2(x / (s * 3), y / (s * 3));
	}

	public static double signedMoment(Vector2[] p) {
		assert p.length > 1;
		double s = 0;
		for (int j = p.length - 1, i = 0; i < p.length; j = i++) {
			Vector2 a = p[j], b = p[i];
			double d = b.sub(a).square();
			s += (a.dot(b) + d / 3) * Math.sqrt(d) * Math.signum(a.det(b));
			// Vector2d q = p[i].sub(p[j]);
			// s += q.dot(p[j].add(q, 3).mul(p[j].quad()).add(p[j].add(q,
			// 0.25).mul(q.quad())));
		}
		return s / 4;
		// return s / 3;
	}

	// TESTED
	public static Vector2[] move(Vector2[] p, Vector2 t) {
		for (int i = 0; i < p.length; i++)
			p[i] = p[i].add(t);
		return p;
	}

	public static Vector2[] transform(Vector2[] p, Transform2 t) {
		for (int i = 0; i < p.length; i++)
			p[i] = t.apply(p[i]);
		return p;
	}

	// TESTED
	public static Vector2[] make(double[] p) {
		assert p.length % 2 == 0 && p.length > 0;
		final Vector2[] v = new Vector2[p.length / 2];
		for (int i = 0; i < v.length; i++)
			v[i] = new Vector2(p[i * 2], p[i * 2 + 1]);
		return v;
	}

	protected Polygon2d() {}
}