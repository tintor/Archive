package tintor.sokoban;

public final class KeySet {
	private static final float LoadFactor = 0.85f;
	private Key[] table = new Key[1 << 6];
	private int size, threshold = (int) (table.length * LoadFactor);

	public int size() {
		return size;
	}

	public void clear() {
		for (int i = 0; i < table.length; i++)
			table[i] = null;
		size = 0;
	}

	public boolean contains(Key key) {
		for (Key e = table[key.hash & (table.length - 1)]; e != null; e = e.setNext)
			if (e.equals(key)) return true;
		return false;
	}

	public boolean add(Key key) {
		for (Key e = table[key.hash & (table.length - 1)]; e != null; e = e.setNext)
			if (e.equals(key)) return false;

		if (size == threshold) grow();
		size += 1;

		insert(key);
		return true;
	}

	public int arraySize() {
		return table.length;
	}

	private void grow() {
		Key[] old = table;
		table = new Key[old.length * 2];

		for (Key e : old)
			while (e != null) {
				final Key a = e.setNext;
				insert(e);
				e = a;
			}

		threshold = (int) (table.length * LoadFactor);
	}

	private void insert(Key key) {
		int i = key.hash & (table.length - 1);
		key.setNext = table[i];
		table[i] = key;
	}
}