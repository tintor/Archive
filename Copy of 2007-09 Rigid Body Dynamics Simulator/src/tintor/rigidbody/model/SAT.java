package tintor.rigidbody.model;

import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Plane3;
import tintor.geometry.Vector2;
import tintor.geometry.Vector3;
import tintor.geometry.extended.Polygon3;

public class SAT {
	public static boolean contact(final Body a, final Plane3 p) {
		axis = p.normal;
		dist = a.interval(axis).min;

		if (dist > 0) return false;

		float m = Float.POSITIVE_INFINITY;
		//		Polygon3 bestF = null;
		//		for (final Polygon3 f : a.shape.faces) {
		//			final Vector3 n = a.transform().applyV(f.plane.normal);
		//			final float d = n.dot(axis);
		//			if (d < m) {
		//				m = d;
		//				bestF = f;
		//			}
		//		}
		//		if (m < -0.9999) {
		//			//point = a.transform().applyP(center(bestF.vertices));
		//			point = a.transform().applyP(Vector3.average(bestF.vertices));
		//			return true;
		//		}

		m = Float.POSITIVE_INFINITY;
		for (final Vector3 v : a.shape.vertices) {
			final Vector3 w = a.transform().applyP(v);
			final float d = w.dot(axis);
			if (d < m) {
				m = d;
				point = w;
			}
		}

		return true;
	}

	private static Vector3 center(final Vector3[] w) {
		final Vector3 p = Vector3.Zero;
		float V = 0;
		for (int j = 1, i = 2; i < w.length; j = i++) {
			final Vector3 a = w[j], b = w[i];
			final float v = a.sub(w[0]).cross(b.sub(w[0])).length();
			p.add(v, Vector3.average(w[0], a, b));
			V += v;
		}
		return p.div(V);
	}

	public static boolean contact(final Body a, final Body b) {
		dist = Float.NEGATIVE_INFINITY;
		axis = null;

		// O(a.faces * b.vertices)
		for (final Polygon3 element : a.shape.faces) {
			final Vector3 axis = a.transform().applyV(element.plane.normal);
			switch (separating(axis, a.interval(axis), b.interval(axis))) {
			case Separating:
				return false;
			case Best:
				float m = Float.NEGATIVE_INFINITY;
				for (final Vector3 v : b.shape.vertices) {
					final Vector3 w = b.transform().applyP(v);
					final float d = w.dot(SAT.axis);
					if (d > m) {
						m = d;
						point = w;
					}
				}
				break;
			case Bad:
				// ignore
			}
		}

		// O(a.faces * b.vertices)
		for (final Polygon3 element : b.shape.faces) {
			final Vector3 axis = b.transform().applyV(element.plane.normal);
			switch (separating(axis, a.interval(axis), b.interval(axis))) {
			case Separating:
				return false;
			case Best:
				float m = Float.POSITIVE_INFINITY;
				for (final Vector3 v : a.shape.vertices) {
					final Vector3 w = a.transform().applyP(v);
					final float d = w.dot(SAT.axis);
					if (d < m) {
						m = d;
						point = w;
					}
				}
				break;
			case Bad:
				// ignore
			}
		}

		// O(a.edges * b.edges * (a.vertices + b.vertices))
		for (final Line3 ea : a.shape.edges) {
			final Vector3 da = a.transform().applyV(ea.direction());
			for (final Line3 eb : b.shape.edges) {
				final Vector3 db = b.transform().applyV(eb.direction());
				final Vector3 axis = da.cross(db).unit();
				if (!axis.isFinite()) continue;

				switch (separating(axis, a.interval(axis), b.interval(axis))) {
				case Separating:
					return false;
				case Best:
					final Line3 eea = a.transform().apply(ea);
					final Line3 eeb = b.transform().apply(eb);
					final Vector2 k = eea.closestInf(eeb);
					point = eea.point(k.x).add(eeb.point(k.y)).mul(0.5f);
					break;
				case Bad:
					// ignore
				}
			}
		}

		return true;
	}

	static enum AxisStatus {
		Separating, Best, Bad
	};

	static AxisStatus separating(Vector3 axis, final Interval ia, final Interval ib) {
		float z = ia.min - ib.min + ia.max - ib.max;
		if (z < 0) {
			z = -z;
			axis = axis.neg();
		}
		final float dist = (z + ia.min - ia.max + ib.min - ib.max) / 2;
		if (dist > 0) {
			SAT.axis = axis;
			return AxisStatus.Separating;
		}
		if (dist > SAT.dist) {
			SAT.dist = dist;
			SAT.axis = axis;
			return AxisStatus.Best;
		}
		return AxisStatus.Bad;
	}

	static Vector3 axis;
	static float dist;
	static Vector3 point;

	//	private static Vector2 center(final Vector2[] p) {
	//		if (p.length == 1) return p[0];
	//		if (p.length == 2) return p[0].add(p[1]).mul(0.5);
	//
	//		float x = 0, y = 0;
	//		float V = 0;
	//		for (int j = p.length - 1, i = 0; i < p.length; j = i++) {
	//			final Vector2 a = p[j], b = p[i];
	//			final float v = a.det(b);
	//			x += (a.x + b.x) * v;
	//			y += (a.y + b.y) * v;
	//			V += v;
	//		}
	//		V = 1 / (V * 3);
	//		return new Vector2(x * V, y * V);
	//	}
}