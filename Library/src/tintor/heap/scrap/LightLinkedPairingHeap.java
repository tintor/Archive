package tintor.heap.scrap;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LightLinkedPairingHeap<T extends Comparable<T>> extends LinkedPairingHeap<T> {
	public static class Node<T extends Comparable<T>> extends LinkedPairingHeap.Node<T> {
		private Node<T> child;
	}

	public LightLinkedPairingHeap(final boolean useAuxiliary, final boolean twoPass) {
		this.useAuxiliary = useAuxiliary;
		this.twoPass = twoPass;
	}

	@Override public boolean offer(final T element) {
		insert(element);
		return true;
	}

	@Override public Node<T> insert(final T element) {
		assert element != null;

		final Node<T> node = new Node<T>();
		node.element = element;
		size++;

		if (useAuxiliary) {
			if (minimal == null || element.compareTo(minimal.element) < 0) minimal = node;
			// add 'node' to 'auxiliary'
			node.next = auxiliary;
			auxiliary = node;
		} else
			root = link(root, node);
		return node;
	}

	@Override public T peek() {
		if (useAuxiliary) {
			while (minimal != null && minimal.element == null)
				removeMin();

			if (minimal == null) throw new NoSuchElementException();
			return minimal.element;
		} else {
			while (root != null && root.element == null)
				removeMin();

			if (root == null) throw new NoSuchElementException();
			return root.element;
		}
	}

	@Override public T poll() {
		T element = null;
		do
			element = removeMin();
		while (element == null);
		return element;
	}

	public void remove(final Node<T> node) {
		if (false)
			node.element = null;
		else {

		}
	}

	public void update(final Node<T> node, final T newValue) {
		final int cmp = newValue.compareTo(node.element);
		node.element = newValue;
		if (cmp < 0) {
			if (useAuxiliary) {
				if (newValue.compareTo(minimal.element) < 0) minimal = node;
				// TODO cut 'node'

				// add 'node' to 'auxiliary'
				node.next = auxiliary;
				auxiliary = node;
			} else if (node != root) // TODO cut 'node'
				root = link(root, node);
		} else if (cmp > 0) // TODO cut 'node'
			if (useAuxiliary) {
				// FIXME what about minimal?
				// TODO
			} else
				root = link(root, node);
	}

	public void merge(final LightLinkedPairingHeap<T> heap) {
		if (useAuxiliary != heap.useAuxiliary) throw new RuntimeException("incompatible");

		if (useAuxiliary) {
			if (minimal == null || heap.minimal.element.compareTo(minimal.element) < 0) minimal = heap.minimal;

			// move 'heap.auxiliary' to 'auxiliary'
			while (heap.auxiliary != null) {
				final Node<T> a = heap.auxiliary;
				heap.auxiliary = a.next;
				a.next = auxiliary;
				auxiliary = a;
			}

			// move 'heap.root' to 'auxiliary'
			if (heap.root != null) {
				heap.root.next = auxiliary;
				auxiliary = heap.root;
				heap.root = null;
			}
		} else {
			root = link(root, heap.root);
			heap.root = null;
		}
		size += heap.size;
		heap.size = 0;
	}

	@Override public void swap(final LightLinkedPairingHeap<T> heap) {
		if (useAuxiliary != heap.useAuxiliary) throw new RuntimeException("incompatible");

		final int s = size;
		size = heap.size;
		heap.size = s;

		final Node<T> r = root;
		root = heap.root;
		heap.root = r;

		if (useAuxiliary) {
			final Node<T> m = minimal;
			minimal = heap.minimal;
			heap.minimal = m;

			final Node<T> a = auxiliary;
			auxiliary = heap.auxiliary;
			heap.auxiliary = a;
		}
	}

	@Override public int size() {
		return size;
	}

	@Override public void clear() {
		root = null;
		minimal = null;
		auxiliary = null;
		size = 0;
	}

	@Override public LightLinkedPairingHeap<T> clone() {
		throw new RuntimeException();
	}

	@SuppressWarnings("unchecked") @Override public Iterator<T> iterator() {
		final LightLinkedPairingHeap<T> heap = clone();
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

	// Implementation
	private Node<T> root, minimal, auxiliary;

	@Override protected <T extends Comparable<T>> Node<T> link(final Node<T> a, final Node<T> b) {
		if (a == null) return b;
		if (b == null) return a;

		assert a.next == null && b.next == null;

		if (a.element.compareTo(b.element) <= 0) {
			b.next = a.child;
			a.child = b;
			return a;
		} else {
			a.next = b.child;
			b.child = a;
			return b;
		}
	}

	@Override protected T removeMin() {
		if (useAuxiliary) {
			root = link(root, multiPassMerge(auxiliary));
			auxiliary = null;
		}

		if (root == null) throw new RuntimeException("underflow");
		final T element = root.element;
		root = twoPass ? twoPassMerge(root.child) : multiPassMerge(root.child);
		return element;
	}

	private static <T extends Comparable<T>> Node<T> twoPassMerge(Node<T> list) {
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

	private static <T extends Comparable<T>> Node<T> multiPassMerge(Node<T> list) {
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