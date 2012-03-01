package tintor.heap;

import java.util.AbstractQueue;
import java.util.Iterator;

public class XAuxTwoPassPairingHeap<T extends XAuxTwoPassPairingHeap.Node<T>> extends AbstractQueue<T> {
	@Override public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override public int size() {
		return size;
	}

	@Override public boolean offer(final T a) {
		if (a == null || a.prev != null || a.next != null || a.child != null) throw new IllegalArgumentException();

		aux = cons(a, aux);
		if (min == null || a.compareTo(min) < 0) min = a;
		size++;
		return true;
	}

	@Override public T peek() {
		return min;
	}

	@Override public T poll() {
		if (size == 0) return null;

		final T m = min;
		size--;
		if (aux != null) root = link(auxMultipass(), root);
		min = root = twopass(root.child);
		return m;
	}

	public void delete(final T a) {
		if (a == null) throw new IllegalArgumentException();

		if (min == a) {
			poll();
			return;
		}

		if (root == a)
			root = twopass(root.child);
		else {
			if (a.prev.child == a)
				a.prev.child = a.next;
			else
				a.prev.next = a.next;
			if (a.next != null) {
				a.next.prev = a.prev;
				a.next = null;
			}
			root = link(root, twopass(a.child));
			a.prev = a.child = null;
		}
		size--;
	}

	public void decrease(final T a) {
		if (min == a || root == a) return;

		if (a.prev.child == a)
			a.prev.child = a.next;
		else
			a.prev.next = a.next;
		if (a.next != null) {
			a.next.prev = a.prev;
			a.next = null;
		}
		a.prev = null;

		aux = cons(a, aux);
		if (min == null || a.compareTo(min) < 0) min = a;
	}

	public void merge(final XAuxTwoPassPairingHeap<T> heap) {
		if (heap.aux != null) heap.root = link(heap.auxMultipass(), heap.root);

		if (heap.root == null) return;

		aux = cons(heap.root, aux);
		if (min == null || heap.root.compareTo(min) < 0) min = heap.root;

		size += heap.size;
		heap.size = 0;
		heap.root = null;
		heap.min = null;
	}

	public static abstract class Node<T> implements Comparable<T> {
		T child, prev, next; // prev can point to parent
	}

	private int size;
	private T min, root, aux;

	private static <T extends Node<T>> T link(final T a, final T b) {
		if (a == null) {
			if (b != null) b.prev = b.next = null;
			return b;
		}
		if (b == null) {
			a.prev = a.next = null;
			return a;
		}

		if (a.compareTo(b) < 0) {
			a.prev = a.next = null;
			b.next = a.child;
			b.prev = a;
			a.child = b;
			return a;
		}

		b.prev = b.next = null;
		a.next = b.child;
		a.prev = b;
		b.child = a;
		return b;
	}

	private static <T extends Node<T>> T cons(final T a, final T tail) {
		a.next = tail;
		return a;
	}

	@SuppressWarnings("null") private static <T extends Node<T>> T twopass(T listA) {
		if (listA == null) return null;
		listA.prev = null;
		T listB = null;

		// left to right
		while (listA != null && listA.next != null) {
			final T a = listA, b = listA.next;
			listA = b.next;
			listB = cons(link(a, b), listB);
		}
		if (listA != null) listB = cons(listA, listB);

		// right to left
		while (listB.next != null) {
			final T a = listB, b = a.next;
			listB = b.next;
			listB = cons(link(a, b), listB);
		}
		return listB;
	}

	private T auxMultipass() {
		assert aux != null;

		if (aux.next == null) {
			final T a = aux;
			aux = null;
			return a;
		}

		T last = aux;
		while (last.next != null)
			last = last.next;

		while (true) {
			final T a = aux, b = a.next;
			aux = b.next;

			final T c = link(a, b);
			if (aux == null) return c;
			last = last.next = c;
		}
	}
}