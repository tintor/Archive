package tintor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class XArrays {
	public static <T> List<T> immutable(final T... array) {
		return Collections.unmodifiableList(Arrays.asList(array));
	}

	public static <T extends Comparable<T>> boolean isSorted(final T[] array) {
		for (int i = 1; i < array.length; i++)
			if (array[i - 1].compareTo(array[i]) > 0) return false;
		return true;
	}

	public static <T extends Comparable<T>> T[] sort(final T[] source) {
		return sort(source, 0, source.length);
	}

	@SuppressWarnings("unchecked") public static <T extends Comparable<T>> T[] sort(final T[] source, final int start,
			final int end) {
		final T[] dest = (T[]) Array.newInstance(source.getClass().getComponentType(), end - start);
		mergeSort(source, dest, start, end, 0);
		return dest;
	}

	// Side effect!
	/** source and dest must NOT overlap! */
	public static <T extends Comparable<T>> void mergeSort(final T[] source, final T[] dest, int low, int high,
			final int off) {
		final int length = high - low;

		// Insertion sort on smallest arrays
		if (length < 7) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low && dest[j - 1].compareTo(dest[j]) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}

		// Recursively sort halves of dest into src
		final int destLow = low;
		final int destHigh = high;
		low += off;
		high += off;
		final int mid = low + high >>> 1;
		mergeSort(dest, source, low, mid, -off);
		mergeSort(dest, source, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (source[mid - 1].compareTo(source[mid]) <= 0) {
			System.arraycopy(source, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++)
			dest[i] = source[q >= high || p < mid && source[p].compareTo(source[q]) <= 0 ? p++ : q++];
	}

	public static <T> T[] copy(final T[] a) {
		return Arrays.copyOf(a, a.length);
	}

	// Side effect!
	public static <T extends Comparable<T>> void insertionSort(final T[] a, int i) {
		// TODO optimize writes!
		for (; i < a.length; i++)
			for (int j = i; j > 0 && a[j - 1].compareTo(a[j]) > 0; j--)
				swap(a, j, j - 1);
	}

	// Side effect!
	public static <T extends Comparable<T>> void insertionSort(final T[] a) {
		insertionSort(a, 1);
	}

	public static <T> T[] remove(final T[] a, final T b) {
		for (int i = 0; i < a.length; i++)
			if (a[i] == b) return removeAt(a, i);
		return a;
	}

	// TODO optimize
	public static <T> T[] removeAt(final T[] a, final int i) {
		final T[] aa = Arrays.copyOf(a, a.length - 1);
		System.arraycopy(a, i + 1, aa, i, a.length - i - 1);
		return aa;
	}

	// Side effect!
	public static <T> void replace(final T[] a, final T b, final T c) {
		for (int i = 0; i < a.length; i++)
			if (a[i] == b) {
				a[i] = c;
				break;
			}
	}

	// Side effect!
	public static <T extends Comparable<T>> boolean sortedReplace(final T[] a, final T b, final T c) {
		int s = 0, e = a.length;
		while (s < e) {
			final int m = (s + e) / 2;
			if (b == a[m]) {
				a[m] = c;
				// TODO optimization: only a[m] needs to move
				insertionSort(a, m);
				return true;
			}

			if (b.compareTo(a[m]) < 0)
				e = m;
			else
				s = m + 1;
		}
		return false;
	}

	public static <T> boolean equals(final T[] a, final T[] b) {
		if (a == b) return true;
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++)
			if (!a[i].equals(b[i])) return false;
		return true;
	}

	public static <T> int find(final T[] a, final T b) {
		for (int i = 0; i < a.length; i++)
			if (a[i].equals(b)) return i;
		return -1;
	}

	// TODO compare performance with: int find(T[], T)
	public static <T extends Comparable<T>> int sortedFind(final T[] a, final T b) {
		int s = 0, e = a.length;
		while (s < e) {
			final int m = (s + e) / 2, cmp = b.compareTo(a[m]);
			if (cmp == 0) return m;
			if (cmp < 0)
				e = m;
			else
				s = m + 1;
		}
		return -1;
	}

	@SuppressWarnings("unchecked") public static <T> T[] fromIterator(final Iterator<T> it) {
		final List<T> list = new ArrayList<T>();
		while (it.hasNext())
			list.add(it.next());
		return list.toArray((T[]) new Object[list.size()]);
	}

	// Side effect!
	// s inclusive, e exclusive
	public static <T> void reverse(final T[] a, int s, int e) {
		for (e--; s < e; s++, e--)
			swap(a, s, e);
	}

	// Side effect!
	public static <T> void reverse(final T[] a) {
		reverse(a, 0, a.length);
	}

	public static <T> boolean contains(final T[] a, final T e) {
		for (final T i : a)
			if (i.equals(e)) return true;
		return false;
	}

	public static <T extends Comparable<T>> boolean sortedContains(final T[] a, final T b) {
		return sortedContains(a, 0, a.length, b);
	}

	public static <T extends Comparable<T>> boolean sortedContains(final T[] a, int start, int end, final T b) {
		while (start < end) {
			final int m = (start + end) / 2, cmp = b.compareTo(a[m]);

			if (cmp == 0)
				return true;
			else if (cmp < 0)
				end = m;
			else
				start = m + 1;
		}
		return false;
	}

	public static <T extends Comparable<T>> int count(final T[] a, final T b) {
		int c = 0;
		for (final T e : a)
			if (e.equals(b)) c++;
		return c;
	}

	// TODO optimize
	public static <T extends Comparable<T>> int sortedCount(final T[] a, final T b) {
		return sortedEnd(a, b) - sortedStart(a, b);
	}

	// TODO optimize
	public static <T extends Comparable<T>> int sortedStart(final T[] a, final T b) {
		int s = 1, e = a.length;
		while (s < e) {
			final int m = (s + e) / 2, cmp = b.compareTo(a[m]);

			if (cmp == 0 && a[m - 1].compareTo(b) < 0)
				return m;
			else if (cmp <= 0)
				e = m;
			else
				s = m + 1;
		}
		return a.length > 0 && b.compareTo(a[0]) == 0 ? 0 : -1;
	}

	// returns index of last element + 1!
	// TODO optimize
	public static <T extends Comparable<T>> int sortedEnd(final T[] a, final T b) {
		System.out.println("a");
		int s = 0, e = a.length - 1;
		while (s < e) {
			final int m = (s + e) / 2, cmp = b.compareTo(a[m]);
			System.out.println("m=" + m + " cmp=" + cmp);

			if (cmp == 0 && b.compareTo(a[m + 1]) < 0)
				return m + 1;
			else if (cmp >= 0)
				s = m + 1;
			else
				e = m;
		}
		return a.length > 0 && b.compareTo(a[a.length - 1]) == 0 ? a.length : -1;
	}

	@SuppressWarnings("unchecked") public static <T> T[] sub(final T[] a, final int s, final int e) {
		final Object[] x = new Object[e - s];
		System.arraycopy(a, s, x, 0, e - s);
		return (T[]) x;
	}

	@SuppressWarnings("unchecked") public static <T> T[] join(final T[] a, final T[] b) {
		if (b.length == 0) return a;
		if (a.length == 0) return b;
		final Object[] x = new Object[a.length + b.length];
		System.arraycopy(a, 0, x, 0, a.length);
		System.arraycopy(b, 0, x, a.length, b.length);
		return (T[]) x;
	}

	@SuppressWarnings("unchecked") public static <T> T[] join(final T[]... a) {
		int len = 0;
		for (final T[] i : a)
			len = i.length;

		final Object[] x = new Object[len];

		int pos = 0;
		for (final T[] i : a) {
			System.arraycopy(i, 0, x, pos, i.length);
			pos += i.length;
		}
		return (T[]) x;
	}

	public static <T> T[] add(final T[] a, final T e) {
		final T[] x = Arrays.copyOf(a, a.length + 1);
		x[a.length] = e;
		return x;
	}

	public static <T extends Comparable<T>> T[] sortedAdd(T[] a, final T e) {
		a = add(a, e);
		insertionSort(a, a.length - 1);
		return a;
	}

	@SuppressWarnings("unchecked") public static <T> T[] filter(final T[] a) {
		int c = 0;
		for (final T e : a)
			if (e != null) c++;

		if (c == a.length) return a;
		if (c == 0) return (T[]) new Object[0];

		final Object[] x = new Object[c];
		c = 0;
		for (final T e : a)
			if (e != null) x[c++] = e;
		return (T[]) x;
	}

	public static <T> T[] resize(final T[] a, final int size) {
		return resize(a, size, null);
	}

	@SuppressWarnings("unchecked") public static <T> T[] resize(final T[] a, final int size, final T def) {
		if (size == a.length) return a;
		final T[] b = (T[]) new Object[size];
		System.arraycopy(a, 0, b, 0, Math.min(size, a.length));
		for (int i = a.length; i < size; i++)
			b[i] = def;
		return b;
	}

	// Side effect!
	public static <T> void swap(final T[] a, final int i, final int j) {
		final T t = a[i];
		a[i] = a[j];
		a[j] = t;
	}
}