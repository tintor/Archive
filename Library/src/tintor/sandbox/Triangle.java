package tintor.sandbox;

public class Triangle extends ConvexPolygon {
//	public static Circle exteriorCircle(Vector2 a, Vector2 b, Vector2 c) {
//		Circle q = new Circle();
//		q.center = Plane2.bisection(a, b).intersect(Plane2.bisection(a, c));
//		q.sradius = q.center.distance(a);
//		return q;
//	}
//
//	public static Circle interiorCircle(Vector2 a, Vector2 b, Vector2 c) {
//		Plane2 ab = new Plane2(a, b);
//		Plane2 bc = new Plane2(b, c);
//		Plane2 ca = new Plane2(c, a);
//		Circle q = new Circle();
//		q.center = Plane2.bisection(ab, bc).intersect(Plane2.bisection(ca, ab));
//		q.sradius = Math.abs(ab.distance(q.center));
//		return q;
//	}

	// // moment oko tačke C koja se nalazi u koordinantnom početku
	// public static double signedMoment(Vector2d a, Vector2d b) {
	// Vector2d q = b.sub(a);
	// // q/3 . [[a + 3q]*a.quad + [a + q/4]*q.quad]
	// return q.dot(a.add(q, 3).mul(a.quad()).add(a.add(q,
	// 0.25).mul(q.quad())))
	// / 3;
	// }

	protected Triangle() {}
}