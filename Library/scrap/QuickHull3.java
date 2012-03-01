package experimental;

import geometry.base.Plane3;
import geometry.base.Vector2;
import geometry.base.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.Arrays;

public class QuickHull3 {
	public final Vector3[] vertices;
	public final List<Vector3[]> faces = new ArrayList<Vector3[]>();

	@SuppressWarnings("serial")
	private static class VList extends ArrayList<Vector3> {
		public VList(Collection<Vector3> a) {
			super(a);
		}

		public VList(Vector3... a) {
			for (Vector3 v : a)
				add(v);
		}

		public Vector3[] toArray() {
			return toArray(new Vector3[] {});
		}

		public VList shuffle() {
			Collections.shuffle(this);
			return this;
		}

		public VList reverse() {
			Collections.reverse(this);
			return this;
		}
	}

	// result is ccw, normal is in positive half-plane
	public static Vector2[] quickHull2(Vector2[] s) {
		int vmin = 0, vmax = 0;
		for (int i = 1; i < s.length; i++) {
			if (s[i].x < s[vmin].x) vmin = i;
			if (s[i].x > s[vmax].x) vmax = i;
		}

		// partition points
		int front = 1, back = s.length;
		//		for (Vector2 v : s)
		//			if (v != vmin && v != vmax) if (v.side(vmin, vmax) < 0)
		//				tmp[front++] = v;
		//			else
		//				tmp[--back] = v;
		//
		//		quick2(s, 0, 0, vmin, vmax);
		//		quick2(s, 0, 0, vmax, vmin);
		return s;
	}

	private static Vector2[] quick2(Vector2[] s, Vector2 a, Vector2 b) {
		if (s.length == 0) return new Vector2[] { a };
		if (s.length == 1) return new Vector2[] { a, s[0] };

		//		Plane2 p = Plane2.twoPoints(a, b);
		// select most distant point
		Vector2 m = null;
		//		for (Vector2 v : s)
		//			if (p.distance(v) > p.distance(m)) m = v;

		// partition points
		Vector2[] tmp = new Vector2[s.length];
		int left = 0, right = s.length;
		Vector2 c = a.mid(b);
		for (Vector2 v : s)
			if (v != m) if (v.side(a, b) > 0)
				tmp[--right] = v;
			else
				tmp[left++] = v;

		Vector2[] f1 = quick2(Arrays.sub(tmp, 0, left), a, m);
		Vector2[] f2 = quick2(Arrays.sub(tmp, right, tmp.length), m, b);
		return Arrays.join(f1, f2);
	}

	public QuickHull3(Vector3[] s) {
		// select 3 random external points
		Plane3 p = new Plane3(external(s));

		// partition points
		VList front = new VList(), on = new VList(), back = new VList();
		for (Vector3 v : s)
			switch (p.classify(v, 1e-8)) {
			case Front:
				front.add(v);
				break;
			case On:
				on.add(v);
				break;
			case Back:
				back.add(v);
				break;
			}

		//		on = quickHull2(p.normal, on);

		quick(front, p, on);
		quick(back, p.flip(), on.reverse());

		// extract vertices
		Set<Vector3> v = new HashSet<Vector3>();
		for (Vector3[] i : faces)
			for (Vector3 j : i)
				v.add(j);
		vertices = v.toArray(new Vector3[] {});
	}

	// all points in 's', are in Front of 'p'
	@SuppressWarnings("unchecked")
	private void quick(List<Vector3> S, Plane3 P, List<Vector3> A) {
		if (S.size() == 0) {
			faces.add(A.toArray(new Vector3[] {}));
			return;
		}

		final int N = A.size();

		// find external point
		Vector3 m = S.get(0);
		for (Vector3 i : S)
			if (P.distance(i) > P.distance(m)) m = i;

		// partition points
		List[] s = new List[N];
		for (int i = 0; i < N; i++)
			s[i] = new ArrayList<Vector3>();

		Plane3[] p = new Plane3[N];
		for (int j = A.size() - 1, i = 0; i < N; j = i++)
			p[i] = new Plane3(A.get(j), A.get(i), m);

		//List[]

		for (Vector3 v : S)
			if (v != m) for (int i = 0; i < A.size(); i++)
				if (p[i].distance(v) > 0) s[i].add(v);

		//		for (int i = 0; i < N; i++)
		//			quick(s[i], p[i], A, b, m);
	}

	private static Vector3[] external(Vector3[] s) {
		// find min and max points
		double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
		double zmin = Double.POSITIVE_INFINITY, zmax = Double.NEGATIVE_INFINITY;

		for (Vector3 v : s) {
			if (v.x < xmin) xmin = v.x;
			if (v.x > xmax) xmax = v.x;

			if (v.y < ymin) ymin = v.y;
			if (v.y > ymax) ymax = v.y;

			if (v.z < zmin) zmin = v.z;
			if (v.z > zmax) zmax = v.z;
		}

		Set<Vector3> external = new HashSet<Vector3>();
		for (Vector3 v : s)
			if (v.x == xmin || v.x == xmax || v.y == ymin || v.y == ymax || v.z == zmin || v.z == zmax) {
				external.add(v);
				if (external.size() == 3) break;
			}

		assert external.size() == 3;
		return external.toArray(new Vector3[3]);
	}
}