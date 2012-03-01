package tintor.sandbox;

import tintor.geometry.Plane2;
import tintor.geometry.Vector2;

public class ConvexPolygon extends Polygon2d {
	// positive side is outside
	public static Plane2[] edgePlanes(Vector2[] a) {
		assert a.length >= 2 && Polygon2d.isConvexAndCCW(a);

		Plane2[] plane = new Plane2[a.length];
		//		for (int j = a.length - 1, i = 0; i < a.length; j = i++)
		//			plane[i] = Plane2.twoPoints(a[j], a[i]);
		return plane;
	}

	// TESTED
//	static int fclip(Vector2[] a, int len, Plane2 p, Vector2[] r) {
//		if (len == 0) return 0;
//		assert a.length >= len && r.length >= len + 2;
//		int n = 0;
//		double di, dj = p.distance(a[len - 1]);
//
//		for (int j = len - 1, i = 0; i < len; dj = di, j = i++) {
//			di = p.distance(a[i]);
//			if (di * dj < 0) r[n++] = Vector2.linear(a[i], a[j], dj / (dj - di));
//			if (di >= 0) r[n++] = a[i];
//		}
//		return n;
//	}

	// TESTED
	// clip convex poligon with positive side of plane
//	public static Vector2[] clip(Vector2[] a, Plane2 p) {
//		assert Polygon2d.isConvexAndCCW(a);
//		Vector2[] r = new Vector2[a.length + 2];
//		int n = fclip(a, a.length, p, r);
//
//		// return Array.resize(r, n);
//		if (n == r.length) return r;
//		Vector2[] rx = new Vector2[n];
//		System.arraycopy(r, 0, rx, 0, n);
//		return rx;
//	}

	// TESTED
	// ako nema preseka vrati prazan niz
//	public static Vector2[] intersection(Vector2[] a, Vector2[] b) {
//		assert Polygon2d.isConvexAndCCW(a) && Polygon2d.isConvexAndCCW(b);
//
//		if (b.length > a.length) {
//			Vector2[] r = a;
//			a = b;
//			b = r;
//		}
//
//		Vector2[] p = new Vector2[a.length + b.length * 2];
//		Vector2[] q = new Vector2[a.length + b.length * 2];
//
//		int n = a.length;
//		System.arraycopy(a, 0, p, 0, a.length);
//
//		for (int j = b.length - 1, i = 0; n > 0 && i < b.length; j = i++) {
//			n = fclip(p, n, Plane2.twoPointsRAW(b[i], b[j]), q);
//
//			Vector2[] r = p;
//			p = q;
//			q = r;
//		}
//
//		// return Array.resize(p, n);
//		if (n == p.length) return p;
//		Vector2[] px = new Vector2[n];
//		System.arraycopy(p, 0, px, 0, n);
//		return px;
//	}

	// is polygon B completely covered by polygon A
	public static boolean contains(Vector2[] a, Vector2[] b) {
		assert Polygon2d.isConvexAndCCW(a) && Polygon2d.isConvexAndCCW(b);

		for (int j = a.length - 1, i = 0; i < a.length; j = i++)
			for (Vector2 v : b)
				if (v.side(a[j], a[i]) > 0) return false;
		return true;
	}

	// +1 separate
	// 0 contact
	// -1 penetrating
//	public static int classify(Vector2[] a, Vector2[] b) {
//		assert Polygon2d.isConvexAndCCW(a) && Polygon2d.isConvexAndCCW(b);
//
//		// for(int j = a.length - 1, i = 0; i < a.length; j = i++) {
//		// int pos = 0, neg = 0;
//		// for(Vector2d v : b) {
//		// double s = v.side(a[j], a[i]);
//		// if(s > 0)
//		// pos++;
//		// else if(s < 0) neg++;
//		// }
//		// if(pos == b.length) return 1;
//		// if(neg == 0) return 0;
//		// }
//		//
//		// for(int j = b.length - 1, i = 0; i < b.length; j = i++) {
//		// int pos = 0, neg = 0;
//		// for(Vector2d v : a) {
//		// double s = v.side(b[j], b[i]);
//		// if(s > 0)
//		// pos++;
//		// else if(s < 0) neg++;
//		// }
//		// if(pos == a.length) return 1;
//		// if(neg == 0) return 0;
//		// }
//		// return -1;
//
//		Vector2[] v = intersection(a, b);
//		if (v.length == 0) return 1;
//		if (v.length <= 2) return 0;
//		return -1;
//	}

	protected ConvexPolygon() {}
}
