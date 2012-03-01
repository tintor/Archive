package tintor.sokoban;

import java.util.Arrays;

public final class KeyQueue {
	private static final int CounterRange = 1;

	private int size, minimal;
	private int[] counter = new int[2];
	private Key[] bucket = new Key[counter.length << CounterRange];

	public int ups;

	// TODO move to library
	private static int floorPowerOf2(final int a) {
		int p = 1;
		while (p <= a)
			p <<= 1;
		return p;
	}

	public void add(final Key e, final int order) {
		if (order < 0 || order > 2000000) throw new IllegalStateException("order=" + order);

		if (order + 1 > bucket.length) {
			final int len = floorPowerOf2(order + 1);
			bucket = Arrays.copyOf(bucket, len);
			counter = Arrays.copyOf(counter, len >> CounterRange);
		}

		counter[order >> CounterRange]++;
		size++;

		if (order < minimal) minimal = order;
		e.queueNext = bucket[order];
		bucket[order] = e;
	}

	public Key remove() {
		if (size == 0) return null;
		// TODO to speed up: create array of counts but 128 times compressed

		//				int c = minimal >> CounterRange;
		//				if (counter[c] == 0) {
		//					final int m = c;
		//					do
		//						c++;
		//					while (counter[c] == 0);
		//					ups += c - m;
		//					minimal = c << CounterRange;
		//				}

		final int m = minimal;
		while (bucket[minimal] == null)
			minimal++;
		ups += minimal - m;

		counter[minimal >> CounterRange]++;
		size--;

		final Key e = bucket[minimal];
		bucket[minimal] = e.queueNext;
		e.queueNext = null;
		return e;
	}

	public int size() {
		return size;
	}

	public int arraySize() {
		return bucket.length;
	}
}