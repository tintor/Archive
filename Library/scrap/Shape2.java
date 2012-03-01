package experimental;

import geometry.GMath;
import geometry.base.Ray2;
import geometry.base.Vector2;

public class Shape2 {
	public Vector2[] exterior; // counter-clockwise
	public Vector2[][] holes; // clockwise

	public int classify(Vector2 p) {
		for (Vector2[] hole : holes) {
			int r = classify(hole, p);
			if (r <= 0) return -r;
		}
		return classify(exterior, p);
	}

	public static int classify(Vector2[] polygon, Vector2 p) {
		int c;
		do {
			c = shootRay(Ray2.pointAndAngle(p, 2 * Math.PI * Math.random()), polygon);
		} while (c == Integer.MIN_VALUE);
		return c;
	}

	/*
	 * returns -Integer.MIN_VALUE if there is ambiguty returns -1 if ray
	 * starts from interior returns 0 if ray starts from edge returns 1 if ray
	 * starts from exterior
	 */
	private static int shootRay(Ray2 ray, Vector2[] polygon) {
		int c = 0;
		for (int e = 0; e < polygon.length; e++) {
			Vector2 a = polygon[e], an = polygon[(e + 1) % polygon.length];
			Vector2 r = GMath.solveLinearEquation(an.sub(a), ray.dir.neg(), ray.point.sub(a));

			if (r.isInfinite()) continue; // infinite means there are no solutions

			if (r.isNaN()) { // nan means there are infinite solutions
				double ra = ray.div(a), rb = ray.div(an);
				if (ra < 0 && rb < 0) continue;
				if (ra > 0 && rb > 0) return -Integer.MIN_VALUE;
				return 0;
			}

			if (r.y == 0 && 0 <= r.x && r.x <= 1) return 0;
			if (r.y > 0) {
				if (r.x == 0) {
					Vector2 ap = polygon[(e + polygon.length - 1) % polygon.length];
					double s = an.side(ray.point, a) * ap.side(ray.point, a);
					if (s == 0) return -Integer.MIN_VALUE;
					if (s < 0) s++;
				} else if (r.x == 1) {
					Vector2 ann = polygon[(e + 2) % polygon.length];
					double s = ann.side(ray.point, an) * a.side(ray.point, an);
					if (s == 0) return -Integer.MIN_VALUE;
					if (s < 0) s++;
				} else if (0 < r.x && r.x < 1) c++;
			}
		}
		return ((c & 1) << 1) - 1;
	}

	public Shape2[] union(Shape2 a) {
		// neusmereni graf svih tacaka i presecnih tacaka, bez duplikata
		// obrisi sve ivice iz grafa koje ne pripadaju uniji
		return null;
	}

	public Shape2[] intersection(Shape2 a) {
		return null;
	}

	public Shape2[] difference(Shape2 a) {
		return null;
	}
}