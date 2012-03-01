package tintor.util;

public final class IntHashSet {
	Entry[] table;
	int size;
	private int threshold;
	private final float loadFactor;

	public IntHashSet() {
		this(16);
	}

	public IntHashSet(final int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public IntHashSet(final int initialCapacity, final float loadFactor) {
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

	int index(int key) {
		key ^= key >>> 20 ^ key >>> 12;
		key = key ^ key >>> 7 ^ key >>> 4;
		return key & table.length - 1;
	}

	public int size() {
		return size;
	}

	public boolean add(final int key) {
		final int i = index(key);
		for (Entry e = table[i]; e != null; e = e.next)
			if (key == e.key) return false;

		table[i] = new Entry(key, table[i]);
		size++;
		if (size > threshold) resize(2 * table.length);
		return true;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int[] toArray() {
		return toArray(new int[size]);
	}

	public int[] toArray(int[] a) {
		if (a.length < size) a = new int[size];
		int i = 0;
		final IntIterator it = iterator();
		while (it.hasNext())
			a[i++] = it.next();
		return a;
	}

	public boolean contains(final int key) {
		for (Entry e = table[index(key)]; e != null; e = e.next)
			if (key == e.key) return true;
		return false;
	}

	void resize(final int newCapacity) {
		final Entry[] oldTable = table;
		table = new Entry[newCapacity];
		for (Entry e : oldTable)
			if (e != null) do {
				final Entry next = e.next;
				final int i = index(e.key);
				e.next = table[i];
				table[i] = e;
				e = next;
			} while (e != null);
		threshold = (int) (newCapacity * loadFactor);
	}

	public boolean remove(final int key) {
		final int i = index(key);
		Entry prev = table[i], e = prev;
		while (e != null) {
			final Entry next = e.next;
			if (key == e.key) {
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

	public void clear() {
		final Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			tab[i] = null;
		size = 0;
	}

	static class Entry {
		final int key;
		Entry next;

		Entry(final int k, final Entry n) {
			key = k;
			next = n;
		}
	}

	public IntIterator iterator() {
		return new IntIterator();
	}

	public class IntIterator {
		Entry current = null, next = null;
		int index = table.length;

		IntIterator() {
			if (size != 0) // advance to first entry
				while (index > 0 && (next = table[--index]) == null) {}
		}

		public boolean hasNext() {
			return next != null;
		}

		public int next() {
			current = next;
			next = current.next;
			while (next == null && index > 0)
				next = table[--index];
			return current.key;
		}

		public void remove() {
			IntHashSet.this.remove(current.key);
			current = null;
		}
	}
}