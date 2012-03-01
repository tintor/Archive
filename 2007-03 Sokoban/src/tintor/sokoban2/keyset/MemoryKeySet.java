package tintor.sokoban2.keyset;

import tintor.Timer;
import tintor.sokoban2.Key;

public final class MemoryKeySet implements KeySet {
	private static final float LoadFactor = 0.75f;
	private Key[] table = new Key[1 << 6];
	private int size, threshold = (int) (table.length * LoadFactor);

	public Timer timer = new Timer();

	public int size() {
		return size;
	}

	public int arraysize() {
		return table.length;
	}

	public void clear() {
		for (int i = 0; i < table.length; i++)
			table[i] = null;
		size = 0;
	}

	public boolean contains(final Key key) {
		for (Key e = table[key.hashCode() & table.length - 1]; e != null; e = e.setNext)
			if (e.equals(key)) return true;
		return false;
	}

	public boolean add(final Key key) {
		timer.restart();
		for (Key e = table[key.hashCode() & table.length - 1]; e != null; e = e.setNext)
			if (e.equals(key)) {
				timer.stop();
				return false;
			}

		if (size == threshold) grow();
		size += 1;

		insert(key);
		timer.stop();
		return true;
	}

	public int arraySize() {
		return table.length;
	}

	private void grow() {
		final Key[] old = table;
		table = new Key[old.length * 2];

		for (Key e : old)
			while (e != null) {
				final Key a = e.setNext;
				insert(e);
				e = a;
			}

		threshold = (int) (table.length * LoadFactor);
	}

	private void insert(final Key key) {
		final int i = key.hashCode() & table.length - 1;
		key.setNext = table[i];
		table[i] = key;
	}
}