package tintor.util;

import java.util.Iterator;

public class SmallList<T extends Comparable<T>> implements Iterable<T> {
	T[] list;
	int size;

	@SuppressWarnings("unchecked") SmallList(int capacity) {
		list = (T[]) new Object[capacity];
	}

	void add(T a) {
		list[size++] = a;
	}

	int size() {
		return size;
	}

	void clear() {
		size = 0;
	}

	T get(int i) {
		return list[i];
	}

	void sort() {
		for (int i = 1; i < size; i++)
			if (list[i - 1].compareTo(list[i]) > 0) {
				int j = i;
				T a = list[j];
				do {
					list[j] = list[j - 1];
					j--;
				} while (j > 0 && list[j - 1].compareTo(list[j]) > 0);
				list[j] = a;
			}
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int pos;

			@Override public boolean hasNext() {
				return pos < size;
			}

			@Override public T next() {
				return list[pos++];
			}

			@Override public void remove() {
				throw new RuntimeException();
			}
		};
	}
}