package tintor.rigidbody.model.collisiondetection.algorithms;

import tintor.rigidbody.model.Body;

public class SimpleSpatialHashing extends SpatialHashing {
	public SimpleSpatialHashing(double cell) {
		this.icell = 1 / cell;
	}

	@Override
	void run() {
		frame += 1;
		for (Body body : detector.bodies)
			hashBody(body);
	}

	private final short[] initFrame = new short[table.length];
	private short frame = 0;

	@Override
	protected void add(Body body, int i) {
		if (initFrame[i] != frame) {
			// Link.destroy(table[i]);
			table[i] = null;
			initFrame[i] = frame;
		}

		Entry a = new Entry(body, table[i]);
		table[i] = a;

		for (Entry b = a.next; b != null; b = b.next)
			detector.narrowPhase(a.body, b.body);
	}
}