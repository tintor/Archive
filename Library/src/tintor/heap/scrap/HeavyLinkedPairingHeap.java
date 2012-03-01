package tintor.heap.scrap;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import tintor.heap.UnderflowException;

@SuppressWarnings("unchecked") public class HeavyLinkedPairingHeap<T extends Comparable<? super T>> extends
		LinkedPairingHeap<T> {
	public static final class Node<T extends Comparable<? super T>> {
		@Override public String toString() {
			final StringBuilder b = new StringBuilder();
			print(b);
			return b.toString();
		}

		// Implementation
		private T element;
		private Node<T> children, prev, next;

		private void print(final StringBuilder b) {
			b.append(element);
			if (children != null) {
				b.append(" [");
				children.print(b);
				b.append(']');
			}
			if (next != null) {
				b.append(", ");
				next.print(b);
			}
		}
	}

	public HeavyLinkedPairingHeap(final boolean useAuxiliary, final boolean twoPass) {
		this.useAuxiliary = useAuxiliary;
		this.twoPass = twoPass;
	}

	@Override public boolean offer(final T e) {
		insert(e);
		return true;
	}

	@Override public Node<T> insert(final T element) {
		final Node<T> node = new Node<T>();
		node.element = element;
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
		root = root.children == null ? null : mergeTrees(root.children);
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
			if (pos.next != null) pos.next.prev = pos.prev;
			if (pos.prev.children == pos)
				pos.prev.children = pos.next;
			else
				pos.prev.next = pos.next;

			pos.next = null;
			root = link(root, pos);
		}
	}

	public void merge(final HeavyLinkedPairingHeap<T> heap) {
		root = root != null ? link(root, heap.root) : heap.root;
		size += heap.size;
		heap.clear();
	}

	@Override public int size() {
		return size;
	}

	@Override public void clear() {
		root = null;
		size = 0;
	}

	@Override public Iterator<T> iterator() {
		try {
			final Queue<T> heap = clone();
			return new Iterator<T>() {
				@Override public boolean hasNext() {
					return heap.size() > 0;
				}

				@Override public T next() {
					return heap.poll();
				}

				@Override public void remove() {
					throw new RuntimeException();
				}
			};
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append('{');
		if (root != null) root.print(b);
		return b.append('}').toString();
	}

	@Override public boolean equals(final Object o) {
		throw new RuntimeException();
	}

	@Override public int hashCode() {
		throw new RuntimeException();
	}

	// Implementation
	private final boolean useAuxiliary, twoPass;
	private Node<T> root, minimal, auxiliary;
	private int size;

	protected static <T extends Comparable<? super T>> Node<T> link(final Node<T> first, final Node<T> second) {
		assert first != null && first.next == null;
		if (second == null) return first;

		if (second.element.compareTo(first.element) < 0) {
			// Attach first as leftmost child of second
			second.prev = first.prev;
			first.prev = second;
			first.next = second.children;
			if (first.next != null) first.next.prev = first;
			second.children = first;
			return second;
		} else {
			// Attach second as leftmost child of first
			second.prev = first;
			first.next = second.next;
			if (first.next != null) first.next.prev = first;
			second.next = first.children;
			if (second.next != null) second.next.prev = second;
			first.children = second;
			return first;
		}
	}

	// two-pass merging
	protected Node<T> mergeTrees(Node<T> node) {
		if (node.next == null) return node;

		final ArrayDeque<Node<T>> list = new ArrayDeque<Node<T>>();

		// Combine subtrees two at a time, going left to right
		while (node != null && node.next != null) {
			final Node<T> a = node, b = node.next;
			node = b.next;
			a.next = null;
			b.next = null;
			list.offerLast(link(a, b));
		}
		if (node == null) node = list.pollLast();

		// Now go right to left, merging last tree with
		// next to last. The result becomes the new last.
		while (list.size() > 0)
			node = link(list.pollLast(), node);

		return node;
	}
}