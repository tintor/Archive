package tintor.heap.scrap;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;

import tintor.heap.UnderflowException;

@SuppressWarnings("unchecked") public final class ArrayTwoPassPairingHeap<T extends Comparable<? super T>> extends
		AbstractQueue<T> {
	public static class Node<T extends Comparable<? super T>> {
		private Node(T element) {
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
		private final ArrayList<Node<T>> children = new ArrayList<Node<T>>();

		private void print(StringBuilder b) {
			b.append(element);
			if (children.size() > 0) {
				b.append(" [");
				for(Node<T> c : children) {
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

	@Override public boolean offer(T e) {
		offer2(e);
		return true;
	}

	public Node<T> offer2(T e) {
		Node<T> node = new Node<T>(e);
		root = (root == null) ? node : link(root, node);
		size++;
		return node;
	}

	@Override public T peek() {
		if (isEmpty()) throw new UnderflowException();
		return root.element;
	}

	@Override public T poll() {
		T x = peek();
		root.element = null; // null it out in case used in decreaseKey
		root = (root.children.size() == 0) ? null : combine(root.children);
		size--;
		return x;
	}

	public void decreaseKey(Node<T> pos, T newValue) {
		if (pos == null) throw new IllegalArgumentException("null Position passed to decreaseKey");
		if (pos.element == null) throw new RuntimeException("IllegalValue: pos already deleted");
		if (pos.element.compareTo(newValue) < 0)
			throw new RuntimeException("IllegalValue: newVal/oldval: " + newValue + " /" + pos.element);

		pos.element = newValue;
		if (pos != root) {
			if (pos.prev.children == pos)
				pos.prev.children = pos.next;
			else
				pos.prev.next = pos.next;

			pos.next = null;
			root = link(root, pos);
		}
	}

	public boolean isEmpty() {
		return root == null;
	}

	public int size() {
		return size;
	}

	public void clear() {
		root = null;
		size = 0;
	}

	public void merge(ArrayTwoPassPairingHeap<T> heap) {
		root = (root != null) ? link(root, heap.root) : heap.root;
		size += heap.size;
		heap.clear();
	}

	@Override public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('{');
		if (root != null) root.print(b);
		return b.append('}').toString();
	}

	// Implementation
	private Node<T> root;
	private int size;

	private static <T extends Comparable<? super T>> Node<T> link(Node<T> first, Node<T> second) {
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
	private Node<T> combine(ArrayList<Node<T>> nodes) {
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