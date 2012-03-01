package tintor.heap;

import java.util.Arrays;
import java.util.Comparator;

import tintor.Timer;

@SuppressWarnings("unchecked") public class Heap<E> {
	private int size;
	private E[] queue;
	private final Comparator<E> comparator;

	public Timer timer = new Timer();

	public Heap(final Comparator<E> comparator, final int capacity) {
		this.comparator = comparator;
		queue = (E[]) new Object[capacity];
	}

	public Heap(final Comparator<E> comparator, final E[] array) {
		this.comparator = comparator;
		queue = array;
		size = array.length;
		for (int i = (size >>> 1) - 1; i >= 0; i--)
			siftDown(i, queue[i]);
	}

	public void offer(final E e) {
		if (e == null) throw new NullPointerException();
		timer.restart();
		if (size == queue.length) {
			final int capacity = queue.length < 64 ? (queue.length + 1) * 2 : queue.length / 2 * 3;
			queue = Arrays.copyOf(queue, capacity);
		}
		siftUp(size++, e);
		timer.stop();
	}

	public E peek() {
		return size == 0 ? null : queue[0];
	}

	public E poll() {
		if (size == 0) return null;
		timer.restart();
		size -= 1;
		final E result = queue[0];
		final E x = queue[size];
		queue[size] = null;
		if (size != 0) siftDown(0, x);
		timer.stop();
		return result;
	}

	public int size() {
		return size;
	}

	public int capacity() {
		return queue.length;
	}

	public void clear() {
		for (int i = 0; i < size; i++)
			queue[i] = null;
		size = 0;
	}

	private void siftUp(int i, final E key) {
		while (i > 0) {
			final int parent = i - 1 >>> 1;
			if (comparator.compare(key, queue[parent]) >= 0) break;
			queue[i] = queue[parent];
			i = parent;
		}
		queue[i] = key;
	}

	private void siftDown(int i, final E key) {
		final int half = size >>> 1;
		while (i < half) {
			int child = (i << 1) + 1;
			E c = queue[child];
			if (child + 1 < size && comparator.compare(c, queue[child + 1]) > 0) c = queue[++child];
			if (comparator.compare(key, c) <= 0) break;
			queue[i] = c;
			i = child;
		}
		queue[i] = key;
	}
}