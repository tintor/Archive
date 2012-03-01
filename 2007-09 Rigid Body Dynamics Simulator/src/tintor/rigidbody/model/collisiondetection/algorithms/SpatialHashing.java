package tintor.rigidbody.model.collisiondetection.algorithms;

import tintor.geometry.Interval;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.collisiondetection.CollisionDetector;

public abstract class SpatialHashing extends CollisionDetector.BroadPhase {
	protected static final int Treshold = 5;

	protected double icell;
	protected final Entry[] table = new Entry[1511]; // prime number

	static class AABB {
		Interval ix, iy, iz;

		void init(final Body body) {
			ix = body.solid().interval(Vector3.X, body.transform);
			iy = body.solid().interval(Vector3.Y, body.transform);
			iz = body.solid().interval(Vector3.Z, body.transform);
		}
	}

	protected static class Entry {
		Body body;
		Entry next;

		Entry(final Body b, final Entry n) {
			body = b;
			next = n;
		}

		// TODO benchmark!
		//		private static Link trash;
		//
		//		static Link create(Body body, Link next) {
		//			if (trash == null) trash = new Link();
		//			Link a = trash;
		//			trash = a.next;
		//
		//			a.body = body;
		//			a.next = next;
		//			return a;
		//		}
		//
		//		static void destroy(Link a) {
		//			Link b = a;
		//			while(b.next != null)
		//				b = b.next;
		//			b.next = trash;
		//			trash = a;
		//		}
	}

	@Override final protected void init() {
		for (final Body body : detector.bodies)
			add(body);
	}

	@Override final protected void broadPhase() {
		if (detector.bodies.size() <= Treshold) {
			BruteForce.run(detector);
			return;
		}

		// update AABBs
		for (final Body body : detector.bodies)
			if (body.state == Body.State.Dynamic) ((AABB) body.data()).init(body);

		run();
	}

	abstract void run();

	@Override final protected void add(final Body body) {
		final AABB d = new AABB();
		d.init(body);
		body.data(d);
	}

	@Override final protected void remove(final Body body) {
		body.data(null);
	}

	protected final void hashBody(final Body body) {
		final AABB d = (AABB) body.data();

		final int xmin = (int) Math.floor(d.ix.min * icell), xmax = (int) Math.ceil(d.ix.max * icell);
		final int ymin = (int) Math.floor(d.iy.min * icell), ymax = (int) Math.ceil(d.iy.max * icell);
		final int zmin = (int) Math.floor(d.iz.min * icell), zmax = (int) Math.ceil(d.iz.max * icell);

		for (int x = xmin; x < xmax; x++)
			for (int y = ymin; y < ymax; y++)
				for (int z = zmin; z < zmax; z++)
					add(body, index(x, y, z));
	}

	private final int index(final int x, final int y, final int z) {
		return Math.abs((x * 73856093 ^ y * 19349663 ^ z * 83492791) % table.length);
	}

	protected abstract void add(Body body, int i);
}