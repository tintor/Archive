package tintor.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Performance note: T.hashCode IS NOT cached, but rehashing is only done when capacity is doubled.  */
@SuppressWarnings("unchecked") public final class IdentityHashSet<T> extends AbstractSet<T> {
	Entry<T>[] table;
	int size;
	private int threshold;
	private final float loadFactor;

	public IdentityHashSet() {
		this(16);
	}

	public IdentityHashSet(final int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public IdentityHashSet(final int initialCapacity, final float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		if (loadFactor <= 0 || loadFactor >= 1 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

		int capacity = 1;
		while (capacity < initialCapacity)
			capacity <<= 1;

		this.loadFactor = loadFactor;
		threshold = (int) (capacity * loadFactor);
		table = new Entry[capacity];
	}

	static int index(final Object o, final int length) {
		return System.identityHashCode(o) & length - 1;
	}

	@Override public int size() {
		return size;
	}

	@Override public boolean add(final T key) {
		final int i = index(key, table.length);
		for (Entry<T> e = table[i]; e != null; e = e.next)
			if (key.equals(e.key)) return false;

		table[i] = new Entry<T>(key, table[i]);
		size++;
		if (size > threshold) resize(2 * table.length);
		return true;
	}

	@Override public boolean addAll(final Collection<? extends T> c) {
		for (final T a : c)
			put(a);
		return true;
	}

	@Override public boolean containsAll(final Collection<?> c) {
		for (final Object a : c)
			if (!contains(a)) return false;
		return true;
	}

	@Override public boolean isEmpty() {
		return size == 0;
	}

	@Override public boolean removeAll(final Collection<?> c) {
		boolean b = false;
		for (final Object a : c)
			if (remove(a)) b = true;
		return b;
	}

	@Override public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override public Object[] toArray() {
		return toArray(new Object[size]);
	}

	@Override public <X> X[] toArray(X[] a) {
		if (a.length < size) a = (X[]) new Object[size];
		int i = 0;
		final Iterator it = iterator();
		while (it.hasNext())
			a[i++] = (X) it.next();
		return a;
	}

	public T get(final Object key) {
		final int i = index(key, table.length);
		for (Entry<T> e = table[i]; e != null; e = e.next)
			if (key.equals(e.key)) return e.key;
		return null;
	}

	@Override public boolean contains(final Object o) {
		return get(o) != null;
	}

	public T put(final T key) {
		final int i = index(key, table.length);
		for (Entry<T> e = table[i]; e != null; e = e.next)
			if (key.equals(e.key)) return e.key;

		table[i] = new Entry<T>(key, table[i]);
		size++;
		if (size > threshold) resize(2 * table.length);
		return key;
	}

	void resize(final int newCapacity) {
		final Entry<T>[] newTable = new Entry[newCapacity];
		for (int j = 0; j < table.length; j++) {
			Entry<T> e = table[j];
			if (e != null) {
				table[j] = null;
				do {
					final Entry<T> next = e.next;
					final int i = index(e.key, newCapacity);
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				} while (e != null);
			}
		}
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	@Override public boolean remove(final Object o) {
		final T key = (T) o;
		final int i = index(key, table.length);

		Entry<T> prev = table[i], e = prev;
		while (e != null) {
			final Entry<T> next = e.next;
			if (key.equals(e.key)) {
				size--;
				if (prev == e)
					table[i] = next;
				else
					prev.next = next;
				return true;
			}
			prev = e;
			e = next;
		}
		return false;
	}

	@Override public void clear() {
		final Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			tab[i] = null;
		size = 0;
	}

	static class Entry<T> {
		final T key;
		Entry<T> next;

		Entry(final T k, final Entry n) {
			key = k;
			next = n;
		}
	}

	@Override public Iterator<T> iterator() {
		return new HashIterator();
	}

	private class HashIterator implements Iterator<T> {
		Entry<T> current, next;
		int index;

		HashIterator() {
			final Entry<T>[] t = table;
			int i = t.length;
			Entry n = null;
			if (size != 0) while (i > 0 && (n = t[--i]) == null) {}
			next = n;
			index = i;
		}

		public boolean hasNext() {
			return next != null;
		}

		public T next() {
			final Entry<T> e = next;
			if (e == null) throw new NoSuchElementException();

			Entry<T> n = e.next;
			final Entry<T>[] t = table;
			int i = index;
			while (n == null && i > 0)
				n = t[--i];
			index = i;
			next = n;
			current = e;
			return e.key;
		}

		public void remove() {
			if (current == null) throw new IllegalStateException();
			final T k = current.key;
			current = null;
			IdentityHashSet.this.remove(k);
		}
	}
}