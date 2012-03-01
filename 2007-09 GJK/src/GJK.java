import java.util.ArrayList;
import java.util.List;

import tintor.geometry.Vector3;

// from Collisions Detection in Interactive 3d Environments, Gino Van Bengen
public class GJK {
	public static Vector3 nearest(final Shape a, final Shape b) {
		final Vector3 v = a.anyPoint().sub(b.anyPoint());
		final List<Vector3> W = new ArrayList<Vector3>();
		final List<Vector3> Y = new ArrayList<Vector3>();
		final double e_tol = 1e-5, e_rel_sqr = 1e-5 * 1e-5;

		while (true) {
			final Vector3 w = support(a, b, v.neg());
			if (Y.contains(w) || v.square() - v.dot(w) <= e_rel_sqr * v.square()) return v;

			Y.clear();
			Y.addAll(W);
			Y.add(w);

			if (W.size() == 4 || v.square() <= e_tol * maxSquare(W)) break;
		}
		return null;
	}

	private static double maxSquare(final List<Vector3> a) {
		double m = 0;
		for (final Vector3 v : a)
			m = Math.max(m, v.square());
		return m;
	}

	//	public static boolean intersect(final Shape a, final Shape b) {
	//		final List<Vector3> list = new ArrayList<Vector3>();
	//		Vector3 dir = Vector3.X;
	//		while (dir != null) {
	//			final Vector3 s = support(a, b, dir);
	//			if (s.dot(dir) < 0) return false;
	//			list.add(s);
	//			dir = doSimplex(list);
	//		}
	//		return true;
	//	}
	//
	//	public Vector3 dir;
	//	public List<Vector3> simplex = new ArrayList<Vector3>();
	//
	//	/** @return intersecting */
	//	public boolean intersect(final Shape s) {
	//
	//	}
	//
	//	/** s.support(dir) is point nearest to origin 
	//	 * @return !intersecting */
	//	public boolean nearest(final Shape s) {
	//
	//	}
	//
	//	private static Vector3 doSimplex(final List<Vector3> list) {
	//		switch (list.size()) {
	//		case 1: {
	//			final Vector3 a = list.get(0);
	//			return a.neg();
	//		}
	//		case 2: {
	//			final Vector3 a = list.get(1), b = list.get(0);
	//			final Vector3 ao = a.neg(), ab = b.sub(a);
	//
	//			if (sameDir(ao, ab)) return ab.cross(ao).cross(ab);
	//			list.clear();
	//			list.add(a);
	//			return ao;
	//		}
	//		case 3: {
	//			final Vector3 a = list.get(2), b = list.get(1), c = list.get(0);
	//
	//			final Vector3 ab = b.sub(a), ac = c.sub(a), abc = ab.cross(ac);
	//			final Vector3 ao = a.neg();
	//
	//			//if (abc.cross(ac)) break;
	//			break;
	//		}
	//		case 4: {
	//			final Vector3 a = list.get(3), b = list.get(2), c = list.get(1), d = list.get(0);
	//			break;
	//		}
	//		default:
	//			throw new IllegalStateException();
	//		}
	//	}

	private static boolean sameDir(final Vector3 a, final Vector3 b) {
		return a.dot(b) > 0;
	}

	private static Vector3 support(final Shape a, final Shape b, final Vector3 dir) {
		return a.support(dir).sub(b.support(dir.neg()));
	}
}