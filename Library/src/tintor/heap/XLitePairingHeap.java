package tintor.heap;

import java.util.AbstractQueue;
import java.util.Iterator;

class ZNode<T extends Comparable<T>> extends XLitePairingHeap.Node<ZNode<T>> {
	T element;

	public ZNode(final T e) {
		element = e;
	}

	@Override public int compareTo(final ZNode<T> a) {
		return element.compareTo(a.element);
	}
}

//class ZLitePairingHeap<T extends Comparable<T>> extends AbstractQueue<T> {
//	private final XLitePairingHeap<ZNode<T>> heap;
//
//	private ZLitePairingHeap(final XLitePairingHeap<ZNode<T>> h) {
//		heap = h;
//	}
//
//	public static <T extends Comparable<T>> ZLitePairingHeap<T> newTwopass() {
//		return new ZLitePairingHeap<T>(XLitePairingHeap.newTwopass());
//	}
//
//	public static <T extends Comparable<T>> ZLitePairingHeap<T> newMultipass() {
//		return new XLitePairingHeap<T>(XLitePairingHeap.newMultipass());
//	}
//
//	@Override public boolean offer(final T e) {
//		return heap.offer(new ZNode<T>(e));
//	}
//
//	@Override public T peek() {
//		final ZNode<T> z = heap.peek();
//		return z != null ? z.element : null;
//	}
//
//	@Override public T poll() {
//		final ZNode<T> z = heap.poll();
//		return z != null ? z.element : null;
//	}
//
//	@Override public Iterator<T> iterator() {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override public int size() {
//		return heap.size();
//	}
//
//	@Override public boolean isEmpty() {
//		return heap.isEmpty();
//	}
//}

public class XLitePairingHeap<T extends XLitePairingHeap.Node<T>> extends AbstractQueue<T> {
	@Override public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override public int size() {
		throw new UnsupportedOperationException();
	}

	@Override public boolean isEmpty() {
		return root == null;
	}

	@Override public boolean offer(final T a) {
		assert a != null && a.next == null && a.child == null;
		root = root == null ? a : link(a, root);
		return true;
	}

	@Override public T peek() {
		return root;
	}

	@Override public T poll() {
		if (root == null) return null;
		final T m = root;
		root = pass.run(root.child);
		return m;
	}

	public void merge(final XLitePairingHeap<T> heap) {
		if (heap.root == null) return;
		root = root == null ? heap.root : link(heap.root, root);
		heap.root = null;
	}

	public static abstract class Node<T> implements Comparable<T> {
		T child, next;
	}

	private T root;
	private final Pass pass;

	public XLitePairingHeap(final Pass p) {
		pass = p;
	}

	static <T extends Node<T>> T link(final T a, final T b) {
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

	static <T extends Node<T>> T cons(final T a, final T tail) {
		a.next = tail;
		return a;
	}

	public static enum Pass {
		Twopass {
			@Override @SuppressWarnings("null") public <T extends Node<T>> T run(T listA) {
				if (listA == null) return null;
				T listB = null;

				while (listA != null && listA.next != null) {
					final T a = listA, b = listA.next;
					listA = b.next;
					listB = cons(link(a, b), listB);
				}
				if (listA != null) listB = cons(listA, listB);

				while (listB.next != null) {
					final T a = listB, b = a.next;
					listB = b.next;
					listB = cons(link(a, b), listB);
				}
				return listB;
			}

		},
		Multipass {
			@Override public <T extends Node<T>> T run(T list) {
				if (list == null) return null;
				if (list.next == null) return list;

				T last = list;
				while (last.next != null)
					last = last.next;

				while (true) {
					final T a = list, b = a.next;
					list = b.next;
					if (list == null) return link(a, b);
					last = last.next = link(a, b);
				}
			}

		};

		public abstract <T extends Node<T>> T run(T list);
	}
}