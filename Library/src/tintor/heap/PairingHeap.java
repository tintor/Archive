package tintor.heap;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;

public final class PairingHeap<T extends PairingHeap.Node<T>> extends AbstractQueue<T> {
	public static void main(final String[] args) {
		final Queue<Adapter<Integer>> heap = new PairingHeap<Adapter<Integer>>();

		for (int i = 5; i >= 1; i--)
			heap.offer(new Adapter<Integer>(i));
		for (int i = 5; i >= 1; i--)
			heap.offer(new Adapter<Integer>(i));

		while (true)
			System.out.println(heap.poll().element);
	}

	@Override public boolean offer(final T a) {
		aux = cons(a, aux);
		return true;
	}

	@Override public T poll() {
		if (aux != null)
			root = root == null ? auxMultipass() : link(auxMultipass(), root);
		else if (root == null) return null;
		final T m = root;
		root = twopass(root.child);
		m.next = m.child = null;
		return m;
	}

	public static abstract class Node<T> implements Comparable<T> {
		T child, next;
	}

	public static class Adapter<T extends Comparable<T>> extends Node<Adapter<T>> {
		public final T element;

		public Adapter(final T e) {
			element = e;
		}

		@Override public int compareTo(final Adapter<T> o) {
			return element.compareTo(o.element);
		}
	}

	private T root, aux;

	private static <T extends Node<T>> T link(final T a, final T b) {
		if (a.compareTo(b) < 0) {
			a.next = null;
			b.next = a.child;
			a.child = b;
			return a;
		}

		b.next = null;
		a.next = b.child;
		b.child = a;
		return b;
	}

	private static <T extends Node<T>> T cons(final T a, final T tail) {
		a.next = tail;
		return a;
	}

	@SuppressWarnings("null") private static <T extends Node<T>> T twopass(T listA) {
		if (listA == null) return null;
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

	@Override public T peek() {
		throw new UnsupportedOperationException();
	}

	@Override public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override public int size() {
		throw new UnsupportedOperationException();
	}
}