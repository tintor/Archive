package tintor.heap.scrap;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import tintor.heap.UnderflowException;

@SuppressWarnings("unchecked") public final class ArrayAuxiliaryTwoPassPairingHeap<T extends Comparable<? super T>> extends
		AbstractQueue<T> {
	public static class Node<T extends Comparable<? super T>> {
		private Node(final T element) {
			this.element = element;
		}

		public T getValue() {
			return element;
		}

		@Override public String toString() {
			final StringBuilder b = new StringBuilder();
			print(b);
			return b.toString();
		}

		// Implementation
		private T element;
		private final ArrayDeque<Node<T>> children = new ArrayDeque<Node<T>>();

		private void print(final StringBuilder b) {
			b.append(element);
			if (children.size() > 0) {
				b.append(" [");
				for (final Node<T> c : children) {
					c.print(b);
					b.append(", ");
				}
				b.append(']');
			}
		}
	}

	@Override public Iterator<T> iterator() {
		return null;
	}

	@Override public boolean offer(final T e) {
		offer2(e);
		return true;
	}

	public Node<T> offer2(final T e) {
		final Node<T> node = new Node<T>(e);
		root = root == null ? node : link(root, node);
		size++;
		return node;
	}

	@Override public T peek() {
		if (isEmpty()) throw new UnderflowException();
		return root.element;
	}

	@Override public T poll() {
		final T x = peek();
		root.element = null; // null it out in case used in decreaseKey
		root = root.children.size() == 0 ? null : twoPass(root.children);
		size--;
		return x;
	}

	public void decreaseKey(final Node<T> pos, final T newValue) {
		if (pos == null) throw new IllegalArgumentException("null Position passed to decreaseKey");
		if (pos.element == null) throw new RuntimeException("IllegalValue: pos already deleted");
		if (pos.element.compareTo(newValue) < 0)
			throw new RuntimeException("IllegalValue: newVal/oldval: " + newValue + " /" + pos.element);

		pos.element = newValue;
		if (pos != root) {
			// cut pos from tree
			if (pos.prev.children == pos)
				pos.prev.children = pos.next;
			else
				pos.prev.next = pos.next;

			pos.next = null;

			root = link(root, pos);
		}
	}

	public void remove(final Node<T> node) {
		if (node == minimal || node == root)
			poll();
		else {
			// Cut node subtree from tree 
			// Remove min from node (and meld its subtrees) 
			// two resulting trees 
			// Leave the resulting tree in auxList if node was initially there.
		}
	}

	@Override public boolean isEmpty() {
		return root == null;
	}

	@Override public int size() {
		return size;
	}

	@Override public void clear() {
		root = null;
		size = 0;
		minimal = null;
		auxiliary.clear();
	}

	public void merge(final ArrayAuxiliaryTwoPassPairingHeap<T> heap) {
		for (final Node<T> a : heap.auxiliary)
			auxiliary.offerLast(a);
		auxiliary.offerLast(heap.root);
		if (minimal == null || heap.minimal != null && heap.minimal.element.compareTo(minimal.element) > 0)
			minimal = heap.minimal;
		size += heap.size;
		heap.clear();
	}

	public String structure() {
		final StringBuilder b = new StringBuilder();
		b.append('{');
		if (root != null) root.print(b);
		b.append(" | ");
		for (final Node<T> a : auxiliary) {
			a.print(b);
			b.append(", ");
		}
		return b.append('}').toString();
	}

	@Override public boolean equals(final Object o) {
		if (o == this) return true;
		throw new RuntimeException();
	}

	@Override public int hashCode() {
		throw new RuntimeException();
	}

	// Implementation
	private Node<T> root, minimal;
	private int size;
	private final ArrayDeque<Node<T>> auxiliary = new ArrayDeque<Node<T>>();

	private Node<T> auxiliaryMultipass() {
		while (auxiliary.size() > 1)
			auxiliary.offerLast(link(auxiliary.pollFirst(), auxiliary.pollFirst()));
		return auxiliary.pollFirst();
	}

	private static <T extends Comparable<? super T>> Node<T> link(final Node<T> first, final Node<T> second) {
		assert first != null;

		if (second == null) return first;

		if (second.element.compareTo(first.element) < 0) {
			// Attach first as leftmost child of second
			second.prev = first.prev;
			first.prev = second;
			first.next = second.leftChild;
			if (first.next != null) first.next.prev = first;
			second.leftChild = first;
			return second;
		} else {
			// Attach second as leftmost child of first
			second.prev = first;
			first.next = second.next;
			if (first.next != null) first.next.prev = first;
			second.next = first.leftChild;
			if (second.next != null) second.next.prev = second;
			first.leftChild = second;
			return first;
		}
	}

	// two-pass merging
	private Node<T> twoPass(final ArrayList<Node<T>> nodes) {
		if (nodes.size() == 1) return nodes.get(0);

		// Combine subtrees two at a time, going left to right
		int i;
		for (i = 0; i + 1 < nodes.size(); i += 2)
			nodes.set(i, link(nodes.get(i), nodes.get(i + 1)));

		int j = i - 2;

		// j has the result of last link().
		// If an odd number of trees, get the last one.
		if (j == nodes.size() - 3) nodes.set(j, link(nodes.get(j), nodes.get(j + 2)));

		// Now go right to left, merging last tree with
		// next to last. The result becomes the new last.
		for (; j >= 2; j -= 2)
			nodes.set(j - 2, link(nodes.get(j - 2), nodes.get(j)));

		return nodes.get(0);
	}
}