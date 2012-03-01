package tintor.heap.scrap;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Iterator;

public abstract class LinkedPairingHeap<T extends Comparable<T>> extends AbstractQueue<T> {
	public static class Node<T extends Comparable<T>> {
		private T element;
		private Node<T> next;
	}

	public LinkedPairingHeap(final boolean useAuxiliary, final boolean twoPass) {
		this.useAuxiliary = useAuxiliary;
		this.twoPass = twoPass;
	}

	@Override public boolean offer(final T element) {
		insert(element);
		return true;
	}

	public abstract Node<T> insert(T element);

	@Override public T poll() {
		T element = null;
		do
			element = removeMin();
		while (element == null);
		return element;
	}

	public void remove(final Node<T> node) {
		node.element = null;
	}

	public abstract void update(Node<T> node, T newValue);

	public abstract void merge(LinkedPairingHeap<T> heap);

	public abstract void swap(LightLinkedPairingHeap<T> heap);

	@Override public int size() {
		return size;
	}

	@Override public abstract LinkedPairingHeap<T> clone();

	@SuppressWarnings("unchecked") @Override public Iterator<T> iterator() {
		final LinkedPairingHeap<T> heap = clone();
		return new Iterator<T>() {
			public boolean hasNext() {
				return heap.size() > 0;
			}

			public T next() {
				return heap.poll();
			}

			public void remove() {
				throw new RuntimeException();
			}
		};
	}

	@SuppressWarnings("unchecked") @Override public boolean equals(final Object o) {
		try {
			return equals((LinkedPairingHeap<T>) o);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	public boolean equals(LinkedPairingHeap<T> b) {
		if (size != b.size) return false;
		final LinkedPairingHeap<T> a = clone();
		b = b.clone();

		return true;
	}

	// Implementation
	protected final boolean useAuxiliary, twoPass;
	protected int size;

	protected abstract Node<T> link(Node<T> a, Node<T> b);

	protected abstract T removeMin();

	protected Node<T> twoPassMerge(Node<T> list) {
		if (list == null) return null;

		final ArrayDeque<Node<T>> queue = new ArrayDeque<Node<T>>();
		while (list != null && list.next != null) {
			final Node<T> a = list, b = list.next;
			list = b.next;
			a.next = null;
			b.next = null;
			queue.offerLast(link(a, b));
		}

		if (list == null) list = queue.pollLast();

		while (queue.size() > 0)
			list = link(queue.pollLast(), list);
		return list;
	}

	protected Node<T> multiPassMerge(Node<T> list) {
		if (list == null) return null;

		final ArrayDeque<Node<T>> queue = new ArrayDeque<Node<T>>();
		while (list != null) {
			final Node<T> a = list;
			list = a.next;
			a.next = null;
			queue.offerLast(a);
		}

		while (queue.size() > 1)
			queue.offerLast(link(queue.pollFirst(), queue.pollFirst()));

		return queue.pollFirst();
	}
}