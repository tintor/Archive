package tintor.external;

import java.util.Iterator;

import b_plus_tree.BplusTreeBytes;

public class ExternalMap implements Iterable<String> {
	public static ExternalMap create(final String file, final int maxKeySize) {
		try {
			return new ExternalMap(BplusTreeBytes.Initialize(file + ".tree", file + ".blocks", maxKeySize));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ExternalMap open(final String file) {
		try {
			return new ExternalMap(BplusTreeBytes.ReOpen(file + ".tree", file + ".blocks"));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final BplusTreeBytes _tree;

	private ExternalMap(final BplusTreeBytes tree) {
		_tree = tree;
	}

	@Override public void finalize() {
		close();
	}

	public void close() {
		try {
			_tree.Shutdown();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override public Iterator<String> iterator() {
		return new Iterator<String>() {
			String key = firstKey();

			@Override public boolean hasNext() {
				return key != null;
			}

			@Override public String next() {
				final String a = key;
				if (key != null) key = nextKey(key);
				return a;
			}

			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public String firstKey() {
		try {
			return _tree.FirstKey();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String nextKey(final String key) {
		try {
			return _tree.NextKey(key);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean add(final String key, final byte[] value) {
		if (contains(key)) return false;
		set(key, value);
		return true;
	}

	public void set(final String key, final byte[] value) {
		try {
			_tree.set(key, value);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] get(final String key) {
		return get(key, null);
	}

	public byte[] get(final String key, final byte[] def) {
		try {
			return (byte[]) _tree.Get(key, def);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void remove(final String key) {
		try {
			_tree.RemoveKey(key);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean contains(final String key) {
		try {
			return _tree.ContainsKey(key);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() {
		try {
			_tree.Commit();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}