package tintor.geometry.extended;

public class ConcavePolyhedrons {
	// point in polyhedron / with nearest face O(n)
	//	find nearest vertex
	//	calculate distances to planes with that vertex
	//	check side of nearest face

	//	public static boolean contains(final Polygon3[] a, final Vector3 p) {
	//		final Random rand = new Random();
	//		mainloop: while (true) {
	//			final Vector3 dir = new Vector3(rand.nextfloat() - 0.5, rand.nextfloat() - 0.5, rand.nextfloat() - 0.5);
	//			final Ray3 ray = new Ray3(p, dir);
	//			final Plucker plucker = Plucker.ray(p, dir);
	//
	//			for (final Polygon3 f : a) {
	//				final float d = ray.distance(f.plane);
	//				if (float.isNaN(d)) continue mainloop;
	//
	//				boolean positive = false, negative = false;
	//				for (int j = f.vertices.length - 1, i = 0; i < f.vertices.length; j = i++)
	//					switch (Side.classify(plucker.side(f.vertices[j], f.vertices[i]))) {
	//					case Positive:
	//						if (negative) return false;
	//						positive = true;
	//						break;
	//					case Negative:
	//						if (positive) return false;
	//						negative = true;
	//						break;
	//					case Zero:
	//						return false;
	//					}
	//				return true;
	//			}
	//		}
	//	}

	//public static boolean intersects(final Polygon3 a, final Polygon3[] b) {
	//	throw new RuntimeException();
	//}

	//	public static boolean intersects(final Polygon3[] a, final Polygon3[] b) {
	//		for (final Polygon3 p : a)
	//			if (intersects(p, b)) return true;
	//		for (final Polygon3 p : b)
	//			if (intersects(p, a)) return true;
	//		return false;
	//	}
}