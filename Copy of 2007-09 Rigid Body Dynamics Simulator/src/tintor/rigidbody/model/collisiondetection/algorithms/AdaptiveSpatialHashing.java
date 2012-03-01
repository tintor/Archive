package tintor.rigidbody.model.collisiondetection.algorithms;

import tintor.rigidbody.model.Body;
import tintor.util.IntHashSet;
import tintor.util.IntHashSet.IntIterator;

public class AdaptiveSpatialHashing extends SpatialHashing {
	public AdaptiveSpatialHashing(double cell, int subdivision) {
		this.cell = cell;
		this.subdivision = subdivision;
	}

	@Override
	void run() {
		icell = 1 / cell;
		set = new IntHashSet(8);
		for (Body body : detector.bodies)
			hashBody(body);
		recursion();
		set = null;
	}

	void recursion() {
		IntIterator it = set.iterator();
		Entry[] e = new Entry[set.size()];
		int[] s = new int[set.size()];
		for (int i = 0; it.hasNext(); i++) {
			int index = it.next();
			e[i] = table[index];
			s[i] = size[index];
			table[index] = null;
			size[index] = 0;
		}

		for (int i = 0; i < e.length; i++) {
			if (s[i] > Treshold) {
				double ic = icell;
				icell *= subdivision;

				set = new IntHashSet(8);
				for (Entry b = e[i]; b != null; b = b.next)
					add(b.body);
				recursion();

				icell = ic;
			} else {
				for (Entry a = e[i]; a != null; a = a.next)
					for (Entry b = a.next; b != null; b = b.next)
						detector.narrowPhase(a.body, b.body);
			}
		}
	}

	private double cell;
	private int subdivision = 10;

	private IntHashSet set;
	private int[] size = new int[table.length];

	@Override
	protected void add(Body body, int i) {
		table[i] = new Entry(body, table[i]);
		size[i] += 1;
		set.add(i);
	}
}