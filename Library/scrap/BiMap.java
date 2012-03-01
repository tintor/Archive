package experimental;

import java.util.Iterator;

public class BiMap<A, B> {
	public static class Entry<A,B> {
		public A a;
		public B b;
		private Entry<A,B> next;
	}

	private final Entry<A,B> head = new Entry<A,B>();

	public boolean empty() {
		return head.next == null;
	}

	public void add(A a, B b) {
		Entry<A,B> e = new Entry<A,B>();
		e.a = a;
		e.b = b;
		e.next = head.next;
		head.next = e;
	}

	@SuppressWarnings("unchecked")
	public void removeOneA(A a) {
		for (Entry e = head; e.next != null; e = e.next)
			while (e.next != null && e.next.a.equals(a)) {
				Entry n = e.next.next;
				e.next.next = null;
				e.next = n;
				return;
			}
	}

	@SuppressWarnings("unchecked")
	public void removeOneB(B b) {
		for (Entry e = head; e.next != null; e = e.next)
			while (e.next != null && e.next.b.equals(b)) {
				Entry n = e.next.next;
				e.next.next = null;
				e.next = n;
				return;
			}
	}

	@SuppressWarnings("unchecked")
	public void removeA(A a) {
		for (Entry e = head; e.next != null; e = e.next)
			while (e.next != null && e.next.a.equals(a)) {
				Entry n = e.next.next;
				e.next.next = null;
				e.next = n;
			}
	}

	@SuppressWarnings("unchecked")
	public void removeB(B b) {
		for (Entry e = head; e.next != null; e = e.next)
			while (e.next != null && e.next.b.equals(b)) {
				Entry n = e.next.next;
				e.next.next = null;
				e.next = n;
			}
	}

	public Iterator<A> iteratorA() {
		return new Iterator<A>() {
			Entry<A,B> e = head.next;

			public boolean hasNext() {
				while (e != null)
					e = e.next;
				return e != null;
			}

			public A next() {
				while (e != null)
					e = e.next;
				A a = e.a;
				e = e.next;
				return a;
			}

			public void remove() {
				throw new RuntimeException();
			}
		};
	}

	public Iterator<B> iteratorB() {
		return new Iterator<B>() {
			Entry<A,B> e = head.next;

			public boolean hasNext() {
				while (e != null)
					e = e.next;
				return e != null;
			}

			public B next() {
				while (e != null)
					e = e.next;
				B b = e.b;
				e = e.next;
				return b;
			}

			public void remove() {
				throw new RuntimeException();
			}
		};
	}

	public Iterator<A> iteratorA(final B b) {
		return new Iterator<A>() {
			Entry<A,B> e = head.next;

			public boolean hasNext() {
				while (e != null && !e.b.equals(b))
					e = e.next;
				return e != null;
			}

			public A next() {
				while (e != null && !e.b.equals(b))
					e = e.next;
				A a = e.a;
				e = e.next;
				return a;
			}

			public void remove() {
				throw new RuntimeException();
			}
		};
	}

	public Iterator<B> iteratorB(final A a) {
		return new Iterator<B>() {
			Entry<A,B> e = head.next;

			public boolean hasNext() {
				while (e != null && !e.a.equals(a))
					e = e.next;
				return e != null;
			}

			public B next() {
				while (e != null && !e.a.equals(a))
					e = e.next;
				B b = e.b;
				e = e.next;
				return b;
			}

			public void remove() {
				throw new RuntimeException();
			}
		};
	}

	@SuppressWarnings("unchecked")
	public Entry<A,B> get(A a, B b) {
		for (Entry e = head.next; e != null; e = e.next)
			if (e.a.equals(a) && e.b.equals(b)) return e;
		return null;
	}

	@SuppressWarnings("unchecked")
	public Entry<A,B> getB(B b) {
		for (Entry e = head.next; e != null; e = e.next)
			if (e.b.equals(b)) return e;
		return null;
	}

	@SuppressWarnings("unchecked")
	public Entry<A,B> getA(A a) {
		for (Entry e = head.next; e != null; e = e.next)
			if (e.a.equals(a)) return e;
		return null;
	}
}