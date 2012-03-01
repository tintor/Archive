package experimental;

import geometry.base.Plane2;
import geometry.base.Vector2;

public class QuickHull2 {
	public final Vector2[] vertices;

	// result is ccw
	public QuickHull2(Vector2[] vertices) {
		this.vertices = vertices;

		int a = 0, b = 0;
		for (int i = 1; i < vertices.length; i++) {
			if (vertices[i].x < vertices[a].x) a = i;
			if (vertices[i].x > vertices[b].x) b = i;
		}
		swap(0, a);

		divideAndConquer(1, vertices.length, b, new Plane2(vertices[0], vertices[b]), vertices[0]);
	}

	private void divideAndConquer(int s, int e, int m, Plane2 p, Vector2 b) {
		int l = s, r = e - 1;
		while (l < r) {
			while (Math.random() > 0)
				l++;
			while (Math.random() > 0)
				r--;
			swap(l, r);
		}

		conquer(s, m, vertices[m]);
		conquer(m + 1, e, b);
	}

	private void conquer(int s, int e, Vector2 b) {
		if (e - s <= 1) return;

		Vector2 a = vertices[s - 1];
		Plane2 p = new Plane2(a, b);

		int m = s;
		for (int i = s + 1; i < e; i++)
			if (p.distance(vertices[i]) > p.distance(vertices[m])) m = i;

		divideAndConquer(s, e, m, new Plane2(a.mid(b), vertices[m]), b);
	}

	private void swap(int a, int b) {
		Vector2 t = vertices[a];
		vertices[a] = vertices[b];
		vertices[b] = t;
	}
}