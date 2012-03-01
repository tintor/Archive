package tintor.util;

import java.util.Iterator;

public class ArrayBag<T> implements Iterable<T> {
	T[] data;
	private int size;

	public ArrayBag() {
		this(16);
	}

	@SuppressWarnings("unchecked") public ArrayBag(final int capacity) {
		data = (T[]) new Object[capacity];
	}

	public ArrayBag(final T[] array) {
		data = array.clone();
	}

	@SuppressWarnings("unchecked") public void add(final T a) {
		if (size == data.length) {
			final T[] temp = (T[]) new Object[data.length * 2];
			System.arraycopy(data, 0, temp, 0, size);
			data = temp;
		}
		data[size++] = a;
	}

	public void swap(final ArrayBag<T> bag) {
		final T[] d = data;
		final int s = size;

		data = bag.data;
		size = bag.size;

		bag.data = d;
		bag.size = s;
	}

	public void remove(final int i) {
		check(i);
		data[i] = data[--size];
	}

	public T get(final int i) {
		check(i);
		return data[i];
	}

	public void set(final int i, final T a) {
		check(i);
		data[i] = a;
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int index = 0;
			boolean removed = false;

			public boolean hasNext() {
				return index < size();
			}

			public T next() {
				removed = false;
				return data[index++];
			}

			public void remove() {
				if (removed) throw new RuntimeException();
				removed = true;
				ArrayBag.this.remove(index - 1);
			}
		};
	}

	public T[] toArray(final T[] a) {
		assert a.length >= size;
		System.arraycopy(data, 0, a, 0, size);
		return a;
	}

	private void check(final int i) {
		if (i < 0 || i >= size) throw new RuntimeException();
	}
}