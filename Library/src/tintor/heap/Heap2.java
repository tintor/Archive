package tintor.heap;

import java.util.Arrays;

interface Orderable {
	int order();
}

@SuppressWarnings("unchecked") public class Heap2<E extends Orderable> {
	private int size;
	private E[] queue;

	public Heap2(int capacity) {
		queue = (E[]) new Object[capacity];
	}

	public Heap2(E[] array, int size) {
		queue = array;
		this.size = size;
		for (int i = (size >>> 1) - 1; i >= 0; i--)
			moveDown(i, queue[i]);
	}

	public void offer(E e) {
		if (e == null) throw new NullPointerException();
		if (size == queue.length) {
			int capacity = (queue.length < 64) ? (queue.length + 1) * 2 : (queue.length / 2) * 3;
			queue = Arrays.copyOf(queue, capacity);
		}
		moveUp(size++, e);
	}

	public E peek() {
		return size == 0 ? null : queue[0];
	}

	public E poll() {
		if (size == 0) return null;
		size -= 1;
		E result = queue[0];
		E x = queue[size];
		queue[size] = null;
		if (size != 0) moveDown(0, x);
		return result;
	}

	public int size() {
		return size;
	}

	public void clear() {
		for (int i = 0; i < size; i++)
			queue[i] = null;
		size = 0;
	}

	private void moveUp(int i, E key) {
		int keyOrder = key.order();
		while (i > 0) {
			int parent = (i - 1) >>> 1;
			if (keyOrder >= queue[parent].order()) break;
			queue[i] = queue[parent];
			i = parent;
		}
		queue[i] = key;
	}

	private void moveDown(int i, E key) {
		int keyOrder = key.order();
		int half = size >>> 1;
		while (i < half) {
			int child = (i << 1) + 1;
			E c = queue[child];
			if (child + 1 < size && queue[child + 1].order() < c.order()) c = queue[child += 1];
			if (keyOrder <= c.order()) break;
			queue[i] = c;
			i = child;
		}
		queue[i] = key;
	}
}