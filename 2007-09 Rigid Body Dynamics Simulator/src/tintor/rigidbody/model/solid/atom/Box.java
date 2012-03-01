package tintor.rigidbody.model.solid.atom;

import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.Atom;
import tintor.rigidbody.model.solid.CollisionPair;

public final class Box extends Atom {
	public final Interval x, y, z;
	private final Vector3[] vertices;

	public Box(double a, double b, double c) {
		if (a <= 0 || b <= 0 || c <= 0) throw new RuntimeException();

		a /= 2;
		b /= 2;
		c /= 2;

		x = new Interval(-a, a);
		y = new Interval(-b, b);
		z = new Interval(-c, c);

		vertices = new Vector3[] { new Vector3(a, b, c), new Vector3(a, b, -c), new Vector3(a, -b, c),
				new Vector3(a, -b, -c), new Vector3(-a, b, c), new Vector3(-a, b, -c), new Vector3(-a, -b, c),
				new Vector3(-a, -b, -c) };

		radius = maximal(Vector3.Zero);
	}

	@Override
	public void findContacts(CollisionPair pair) {
		pair.box(this);
	}

	@Override
	public double mass() {
		return x.max * y.max * z.max * 8;
	}

	@Override
	public Side side(Vector3 point) {
		double dx = Math.abs(point.x) - x.max;
		double dy = Math.abs(point.y) - y.max;
		double dz = Math.abs(point.z) - z.max;
		return Side.classify(GMath.min(dx, dy, dz));
	}

	@Override
	public Interval interval(Vector3 n) {
		double m = Math.abs(n.x) * x.max + Math.abs(n.y) * y.max + Math.abs(n.z) * z.max;
		return new Interval(-m, m);
	}

	@Override
	public double maximal(Vector3 center) {
		double r = 0; // NOTE optimize this!
		for (int i = 0; i < Vertices; i++)
			r = Math.max(r, vertices[i].distanceSquared(center));
		return r;
	}

	@Override
	public Matrix3 inertiaTensor() {
		double k = mass() / 3;
		double a = y.max * y.max + z.max * z.max;
		double b = x.max * x.max + z.max * z.max;
		double c = x.max * x.max + y.max * y.max;
		return new Matrix3(a * k, b * k, c * k);
	}

	@Override
	public void render() {
		GLA.beginQuads();

		// front
		GLA.normal(0, 0, 1);
		GLA.vertex(x.min, y.min, z.max);
		GLA.vertex(x.max, y.min, z.max);
		GLA.vertex(x.max, y.max, z.max);
		GLA.vertex(x.min, y.max, z.max);

		// back
		GLA.normal(0, 0, -1);
		GLA.vertex(x.min, y.max, z.min);
		GLA.vertex(x.max, y.max, z.min);
		GLA.vertex(x.max, y.min, z.min);
		GLA.vertex(x.min, y.min, z.min);

		// left
		GLA.normal(-1, 0, 0);
		GLA.vertex(x.min, y.min, z.max);
		GLA.vertex(x.min, y.max, z.max);
		GLA.vertex(x.min, y.max, z.min);
		GLA.vertex(x.min, y.min, z.min);

		// right
		GLA.normal(1, 0, 0);
		GLA.vertex(x.max, y.min, z.min);
		GLA.vertex(x.max, y.max, z.min);
		GLA.vertex(x.max, y.max, z.max);
		GLA.vertex(x.max, y.min, z.max);

		// top
		GLA.normal(0, 1, 0);
		GLA.vertex(x.min, y.max, z.max);
		GLA.vertex(x.max, y.max, z.max);
		GLA.vertex(x.max, y.max, z.min);
		GLA.vertex(x.min, y.max, z.min);

		// bottom
		GLA.normal(0, -1, 0);
		GLA.vertex(x.min, y.min, z.min);
		GLA.vertex(x.max, y.min, z.min);
		GLA.vertex(x.max, y.min, z.max);
		GLA.vertex(x.min, y.min, z.max);

		GLA.end();
	}

	private void clip(Vector3 a, Vector3 b, Transform3 transform) {
		// clipX
		if (a.x > b.x) {
			Vector3 t = a;
			a = b;
			b = t;
		}

		if (b.x < x.min - Side.eps || a.x > x.max + Side.eps) return;
		if (a.x < x.min - Side.eps) a = Vector3.linear(a, b, (x.min - a.x) / (b.x - a.x));
		if (b.x > x.max + Side.eps) b = Vector3.linear(a, b, (x.max - a.x) / (b.x - a.x));

		// clipY
		if (a.y > b.y) {
			Vector3 t = a;
			a = b;
			b = t;
		}

		if (b.y < y.min - Side.eps || a.y > y.max + Side.eps) return;
		if (a.y < y.min - Side.eps) a = Vector3.linear(a, b, (y.min - a.y) / (b.y - a.y));
		if (b.y > y.max + Side.eps) b = Vector3.linear(a, b, (y.max - a.y) / (b.y - a.y));

		// clipX
		if (a.z > b.z) {
			Vector3 t = a;
			a = b;
			b = t;
		}

		if (b.z < z.min - Side.eps || a.z > z.max + Side.eps) return;
		if (a.z < z.min - Side.eps) a = Vector3.linear(a, b, (z.min - a.z) / (b.z - a.z));
		if (b.z > z.max + Side.eps) b = Vector3.linear(a, b, (z.max - a.z) / (b.z - a.z));

		// finish
		if (!a.equals(b)) setAdd(transform.applyP(b));
		setAdd(transform.applyP(a));
	}

	private final static int Vertices = 8, Edges = 12;
	private final static int[] Ea = { 0, 2, 0, 1, 4, 6, 4, 5, 0, 2, 1, 3 };
	private final static int[] Eb = { 1, 3, 2, 3, 5, 7, 6, 7, 4, 6, 5, 7 };

	private static Vector3[] temp;
	private static int tempSize;

	private static void setAdd(Vector3 a) {
		for (int i = tempSize - 1; i >= Vertices; i--)
			if (temp[i].equals(a)) return;
		temp[tempSize++] = a;
	}

	// optimizovano do bola!
	public static Vector3[] intersect(Box boxA, Transform3 transformA, Box boxB, Transform3 transformB) {
		Transform3 transform = transformA.icombine(transformB);

		tempSize = Vertices;
		temp = new Vector3[Vertices + Edges * 4]; // NOTE can this be lowered?

		for (int i = 0; i < Vertices; i++)
			temp[i] = transform.applyP(boxA.vertices[i]);
		for (int i = 0; i < Edges; i++)
			boxB.clip(temp[Ea[i]], temp[Eb[i]], transformB);

		for (int i = 0; i < Vertices; i++)
			temp[i] = transform.iapplyP(boxB.vertices[i]);
		for (int i = 0; i < Edges; i++)
			boxA.clip(temp[Ea[i]], temp[Eb[i]], transformA);

		Vector3[] a = new Vector3[tempSize - Vertices];
		System.arraycopy(temp, Vertices, a, 0, tempSize - Vertices);
		return a;
	}

	@Override
	public Convex convexHull() {
		return new Convex(vertices);
	}

	@Override
	public Vector3[] intersection(Atom atomA, Transform3 transformA, Atom atomB, Transform3 transformB) {
		if (atomB instanceof Box) return intersect(this, transformA, (Box) atomB, transformB);
		return super.intersection(atomA, transformA, atomB, transformB);
	}
}