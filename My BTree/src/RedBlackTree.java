import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

import tintor.Timer;

public final class RedBlackTree implements Iterable<Integer> {
	private static final int None = -1, HeaderSize = 8, RootOffset = 0;

	public static int reads, writes, small_reads, small_writes;

	private final Storage _storage;

	private int _root = None;
	private int _size;
	private int _valueSize;

	static class Entry {
		int id = RedBlackTree.None;

		// 0
		boolean black = true;
		// 1
		int key;
		// 5
		int left = RedBlackTree.None;
		// 9
		int right = RedBlackTree.None;
		// 13
		int parent = RedBlackTree.None;

		private static final int Color = 0, Left = 5, Right = 9, Parent = 13, SIZE = 17;
		@SuppressWarnings("hiding") static final Entry None = new Entry();
	}

	public RedBlackTree(final String fileName, final boolean reset, final int valueSize) {
		if (!reset && valueSize != -1) throw new IllegalArgumentException();

		_storage = new Storage(fileName, "rw");
		_storage.allocBuffer(HeaderSize);

		if (reset) {
			_valueSize = valueSize;

			_storage.truncate(0);
			_storage.buffer.clear();
			_storage.buffer.putInt(_root);
			_storage.buffer.putInt(_valueSize);
			_storage.write(0);
		} else {
			_storage.read(0, HeaderSize);
			_root = _storage.buffer.getInt();
			_valueSize = _storage.buffer.getInt();

			final long a = _storage.size() - HeaderSize;
			_size = (int) (a / (Entry.SIZE + _valueSize));
			if (_size * (long) (Entry.SIZE + _valueSize) != a) throw new RuntimeException("corruption");
		}

		_storage.allocBuffer(Math.max(HeaderSize, Entry.SIZE + _valueSize));
	}

	public void close() {
		_storage.close();
	}

	public void sync() {
		_storage.sync();
	}

	private void free(final Entry e) {
		if (e == Entry.None) return;

		assert e.parent == None;
		assert e.left == None;
		assert e.right == None;

		// if not last
		if (e.id != _size - 1) {
			// replace with entry with last id and truncate
			final Entry last = read(_size - 1);

			if (last.parent != None) {
				final int x_left = readInt(last.parent, Entry.Left);
				writeInt(last.parent, x_left == last.id ? Entry.Left : Entry.Right, e.id);
			}

			if (last.left != None) writeInt(last.left, Entry.Parent, e.id);
			if (last.right != None) writeInt(last.right, Entry.Parent, e.id);

			last.id = e.id;
			write(last, readValue(_size - 1));

			if (_root == _size - 1) setRoot(e.id);
		}

		_size -= 1;
		_storage.truncate(position(_size));
	}

	private long position(final int id) {
		return HeaderSize + (long) (Entry.SIZE + _valueSize) * id;
	}

	private Entry read(final int id) {
		return id == None ? Entry.None : read(id, new Entry());
	}

	private Entry read(final int id, final Entry e) {
		_storage.read(position(id), Entry.SIZE);
		reads += 1;

		e.id = id;
		e.black = _storage.buffer.get() != 0;
		e.key = _storage.buffer.getInt();
		e.left = _storage.buffer.getInt();
		e.right = _storage.buffer.getInt();
		e.parent = _storage.buffer.getInt();
		return e;
	}

	private void write(final Entry e, final byte[] value) {
		_storage.buffer.clear();
		_storage.buffer.put(e.black ? (byte) 1 : (byte) 0);
		_storage.buffer.putInt(e.key);
		_storage.buffer.putInt(e.left);
		_storage.buffer.putInt(e.right);
		_storage.buffer.putInt(e.parent);
		if (value != null) _storage.buffer.put(value);

		_storage.write(position(e.id));
		writes += 1;
	}

	private void writeColor(final int id, final boolean black) {
		if (id == None) return;
		_storage.buffer.clear();
		_storage.buffer.put(black ? (byte) 1 : (byte) 0);
		_storage.write(position(id) + Entry.Color);
		small_writes += 1;
	}

	private boolean readColor(final int id) {
		if (id == None) return Entry.None.black;
		_storage.read(position(id) + Entry.Color, 1);
		small_reads += 1;
		return _storage.buffer.get() != 0;
	}

	private int readInt(final int id, final int offset) {
		if (id == None) return None;
		_storage.read(position(id) + offset, 4);
		small_reads += 1;
		return _storage.buffer.getInt();
	}

	private void writeInt(final int id, final int offset, final int value) {
		if (id == None) throw new RuntimeException();
		_storage.buffer.clear();
		_storage.buffer.putInt(value);
		_storage.write(position(id) + offset);
		small_writes += 1;
	}

	private byte[] readValue(final int id) {
		if (id == None) throw new RuntimeException();
		_storage.read(position(id) + Entry.SIZE, _valueSize);
		small_reads += 1;

		final byte[] value = new byte[_valueSize];
		_storage.buffer.get(value);
		return value;
	}

	private void writeValue(final int id, final byte[] a) {
		if (id == None) return;
		_storage.buffer.clear();
		_storage.buffer.put(a);
		_storage.write(position(id) + Entry.SIZE);
		small_writes += 1;
	}

	private void setRoot(final int root) {
		_root = root;
		_storage.buffer.clear();
		_storage.buffer.putInt(_root);
		_storage.write(RootOffset);
		small_writes += 1;
	}

	public int size() {
		return _size;
	}

	public int valueSize() {
		return _valueSize;
	}

	public boolean put(final int key) {
		return put(key, (byte[]) null);
	}

	public boolean put(final int key, final String value) {
		return put(key, str2bts(value, _valueSize));
	}

	public boolean put(final int key, final byte[] value) {
		if (value != null && value.length != _valueSize) throw new IllegalArgumentException();

		if (_root == None) {
			final Entry e1 = new Entry();
			e1.id = _size;
			_size += 1;
			final Entry x = e1;
			x.key = key;
			write(x, value);
			setRoot(x.id);
			return true;
		}

		final Entry e = new Entry();
		read(_root, e);

		while (true)
			if (key < e.key) {
				if (e.left == None) {
					writeInt(e.id, Entry.Left, _size);
					break;
				}
				read(e.left, e);
			} else if (key > e.key) {
				if (e.right == None) {
					writeInt(e.id, Entry.Right, _size);
					break;
				}
				read(e.right, e);
			} else {
				if (value != null) writeValue(e.id, value);
				return false;
			}

		if (_size == Integer.MAX_VALUE) throw new RuntimeException("pointer overflow");

		e.key = key;
		e.black = false;
		e.left = e.right = None;
		e.parent = e.id;
		e.id = _size;
		_size += 1;
		write(e, value);

		fixAfterInsertion(e);
		return true;
	}

	public boolean contains(final int key) {
		return find(key) != Entry.None;
	}

	public byte[] get(final int key) {
		final Entry e = find(key);
		return e != Entry.None ? readValue(e.id) : null;
	}

	public String getString(final int key) {
		final byte[] v = get(key);
		return v != null ? bts2str(v) : null;
	}

	public ByteBuffer getBuffer(final int key) {
		final Entry e = find(key);
		if (e == Entry.None) return null;
		_storage.read(position(e.id) + Entry.SIZE, _valueSize);
		small_reads += 1;
		return _storage.buffer;
	}

	public boolean remove(final int key) {
		Entry e = find(key);
		if (e == Entry.None) return false;

		// If strictly internal, copy successor's element to p and then make p point to successor.
		if (e.left != None && e.right != None) {
			final Entry s = successor(e, new Entry());
			e.key = s.key;
			write(e, s == Entry.None ? null : readValue(s.id));
			e = s;
		} // p has 2 children

		// Start fixup at replacement node, if it exists.
		final Entry replacement = read(e.left != None ? e.left : e.right);

		if (replacement != Entry.None) {
			// Link replacement to parent
			replacement.parent = e.parent;
			write(replacement, null);
			if (e.parent == None)
				setRoot(replacement.id);
			else {
				final int x_left = readInt(e.parent, Entry.Left);
				writeInt(e.parent, e.id == x_left ? Entry.Left : Entry.Right, replacement.id);
			}

			// Null out links so they are OK to use by fixAfterDeletion.
			e.left = e.right = e.parent = None;
			write(e, null);

			// Fix replacement
			if (e.black) fixAfterDeletion(replacement);
		} else if (e.parent == None)
			setRoot(None);
		else { //  No children. Use self as phantom replacement and unlink.
			if (e.black) fixAfterDeletion(e);

			if (e.parent != None) {
				final Entry x = read(e.parent);
				if (e.id == x.left)
					writeInt(x.id, Entry.Left, None);
				else if (e.id == x.right) writeInt(x.id, Entry.Right, None);

				e.parent = None;
				writeInt(e.id, Entry.Parent, None);
			}
		}

		free(e);
		return true;
	}

	public int first() {
		final Entry e = minimal(_root, new Entry());
		if (e == Entry.None) throw new RuntimeException("empty");
		return e.key;
	}

	public int last() {
		final Entry e = maximal(_root, new Entry());
		if (e == Entry.None) throw new RuntimeException("empty");
		return e.key;
	}

	public int next(final int key) {
		return next(key, Integer.MAX_VALUE);
	}

	public int next(final int key, final int pos_inf) {
		if (_root == None) return pos_inf;

		final Entry e = new Entry();
		read(_root, e);
		while (true)
			if (key < e.key) {
				if (e.left == None) return e.key;
				read(e.left, e);
			} else {
				if (e.right == None) {
					final Entry x = successor(e, e);
					return x != Entry.None ? x.key : pos_inf;
				}
				read(e.right, e);
			}
	}

	public int prev(final int key) {
		return prev(key, Integer.MIN_VALUE);
	}

	public int prev(final int key, final int neg_inf) {
		if (_root == None) return neg_inf;

		final Entry e = new Entry();
		read(_root, e);
		while (true)
			if (key <= e.key) {
				if (e.left == None) {
					final Entry x = predecessor(e, e);
					return x != Entry.None ? x.key : neg_inf;
				}
				read(e.left, e);
			} else {
				if (e.right == None) return e.key;
				read(e.right, e);
			}
	}

	private Entry minimal(final int id, final Entry e) {
		if (id == None) return Entry.None;
		read(id, e);
		while (e.left != None)
			read(e.left, e);
		return e;
	}

	private Entry maximal(final int id, final Entry e) {
		if (id == None) return Entry.None;
		read(id, e);
		while (e.right != None)
			read(e.right, e);
		return e;
	}

	@Override public Iterator<Integer> iterator() {
		final Entry first = minimal(_root, new Entry());
		return new Iterator<Integer>() {
			Entry e = first;

			@Override public boolean hasNext() {
				return e != Entry.None;
			}

			@Override public Integer next() {
				final int k = e.key;
				successor(e, e);
				return k;
			}

			@Override public void remove() {
				throw new RuntimeException();
			}
		};
	}

	private Entry find(final int key) {
		if (_root == None) return Entry.None;

		final Entry e = new Entry();
		read(_root, e);
		while (true)
			if (key < e.key) {
				if (e.left == None) return Entry.None;
				read(e.left, e);
			} else if (key > e.key) {
				if (e.right == None) return Entry.None;
				read(e.right, e);
			} else
				return e;
	}

	private void setBlack(final Entry p) {
		if (p != Entry.None) {
			p.black = true;
			writeColor(p.id, true);
		}
	}

	private void setRed(final Entry p) {
		if (p != Entry.None) {
			p.black = false;
			writeColor(p.id, false);
		}
	}

	private void rotateLeft(final Entry p) {
		if (p != Entry.None) {
			final Entry r = read(p.right);
			p.right = r.left;
			if (r.left != None) writeInt(r.left, Entry.Parent, p.id);
			r.parent = p.parent;
			if (p.parent == None)
				setRoot(r.id);
			else {
				final int x_left = readInt(p.parent, Entry.Left);
				writeInt(p.parent, x_left == p.id ? Entry.Left : Entry.Right, r.id);
			}
			r.left = p.id;
			p.parent = r.id;
			write(r, null);
			write(p, null);
		}
	}

	private void rotateRight(final Entry p) {
		if (p != Entry.None) {
			final Entry l = read(p.left);
			p.left = l.right;
			if (l.right != None) writeInt(l.right, Entry.Parent, p.id);
			l.parent = p.parent;
			if (p.parent == None)
				setRoot(l.id);
			else {
				final int x_right = readInt(p.parent, Entry.Right);
				writeInt(p.parent, x_right == p.id ? Entry.Right : Entry.Left, l.id);
			}
			l.right = p.id;
			p.parent = l.id;
			write(l, null);
			write(p, null);
		}
	}

	private void fixAfterInsertion(Entry x) {
		while (x != Entry.None && x.id != _root) {
			final Entry x_parent = read(x.parent);
			if (x_parent.black) break;

			final Entry x_parent_parent = read(x_parent.parent);
			//if(x_parent_parent == Entry.None) break;

			if (x.parent == x_parent_parent.left) {
				final boolean y_black = readColor(x_parent_parent.right);
				if (!y_black) {
					setBlack(x_parent);
					if (x_parent_parent.right != None) writeColor(x_parent_parent.right, true);
					setRed(x_parent_parent);
					x = x_parent_parent;
				} else {
					if (x.id == x_parent.right) {
						x = x_parent;
						rotateLeft(x);
					}
					writeColor(x.parent, true);

					final Entry e = read(readInt(x.parent, Entry.Parent));
					if (e != Entry.None) e.black = false;
					rotateRight(e);
				}
			} else {
				final Boolean y_black = readColor(x_parent_parent.left);
				if (!y_black) {
					setBlack(x_parent);
					if (x_parent_parent.left != None) writeColor(x_parent_parent.left, true);
					setRed(x_parent_parent);
					x = x_parent_parent;
				} else {
					if (x.id == x_parent.left) {
						x = x_parent;
						rotateRight(x);
					}
					writeColor(x.parent, true);

					final Entry e = read(readInt(x.parent, Entry.Parent));
					if (e != Entry.None) e.black = false;
					rotateLeft(e);
				}
			}
		}
		writeColor(_root, true);
	}

	Entry successor(final Entry a, final Entry e) {
		if (a == Entry.None) return Entry.None;
		if (a.right != None) return minimal(a.right, e);
		if (a.parent == None) return Entry.None;

		final Entry p = read(a.parent, e);
		if (p.parent == None) return Entry.None;
		int ch_id = a.id;

		while (ch_id == p.right) {
			ch_id = p.id;
			if (p.parent == None) return Entry.None;
			read(p.parent, p);
		}
		return p;
	}

	Entry predecessor(final Entry a, final Entry e) {
		if (a == Entry.None) return Entry.None;
		if (a.left != None) return maximal(a.left, e);
		if (a.parent == None) return Entry.None;

		final Entry p = read(a.parent, e);
		if (p.parent == None) return Entry.None;
		int ch_id = a.id;

		while (ch_id == p.left) {
			ch_id = p.id;
			if (p.parent == None) return Entry.None;
			read(p.parent, p);
		}
		return p;
	}

	private void fixAfterDeletion(Entry x) {
		while (x.id != _root && x.black) {
			final Entry x_parent = read(x.parent);
			if (x.id == x_parent.left) {
				Entry sib = read(x_parent.right);

				if (!sib.black) {
					setBlack(sib);
					if (x_parent != Entry.None) x_parent.black = false;
					rotateLeft(x_parent);
				}

				final Entry sib_left = read(sib.left);
				final Entry sib_right = read(sib.right);
				if (sib_left.black && sib_right.black) {
					setRed(sib);
					x = x_parent;
				} else {
					if (sib_right.black) {
						setBlack(sib_left);
						if (sib != Entry.None) sib.black = false;
						rotateRight(sib);
						sib = read(x_parent.right);
					}
					if (x_parent.black)
						setBlack(sib);
					else
						setRed(sib);

					setBlack(x_parent);

					setBlack(read(sib.right));

					rotateLeft(x_parent);
					x = read(_root);
				}
			} else { // symmetric
				Entry sib = read(x_parent.left);

				if (!sib.black) {
					setBlack(sib);
					if (x_parent != Entry.None) x_parent.black = false;
					rotateRight(x_parent);
					sib = read(x_parent.left);
				}

				if (read(sib.right).black && read(sib.left).black) {
					setRed(sib);
					x = read(x.parent);
				} else {
					if (read(sib.left).black) {
						writeColor(sib.right, true); // setBlack(read(sib.right));
						setRed(sib);
						rotateLeft(sib);
						sib = read(x_parent.left);
					}
					if (x_parent.black)
						setBlack(sib);
					else
						setRed(sib);

					setBlack(x_parent);
					writeColor(sib.left, true); // setBlack(read(sib.left));
					rotateRight(x_parent);
					x = read(_root);
				}
			}
		}
		setBlack(x);
	}

	public static byte[] str2bts(final String a, final int size) {
		if (size > Byte.MAX_VALUE) throw new IllegalArgumentException();
		final byte[] b = a.getBytes(Charset.forName("utf8"));
		if (b.length > size - 1) throw new RuntimeException("overflow");
		final byte[] c = Arrays.copyOf(b, size);
		c[size - 1] = (byte) b.length;
		return c;
	}

	public static String bts2str(final byte[] a) {
		return new String(a, 0, a[a.length - 1], Charset.forName("utf8"));
	}

	@Override public String toString() {
		final StringBuilder b = new StringBuilder();
		print(b, _root);
		return b.toString();
	}

	private void print(final StringBuilder b, final int id) {
		if (id != None) {
			b.append('(');
			final Entry e = read(id);
			b.append(e.black ? 'B' : 'R');
			print(b, e.left);
			b.append(' ');
			b.append(e.key);
			b.append(' ');
			print(b, e.right);
			b.append(')');
		}
	}

	public int depth() {
		return depth(_root, new Entry());
	}

	private int depth(final int id, final Entry e) {
		if (id == None) return 0;
		read(id, e);
		final int right = e.right;
		return 1 + Math.max(depth(e.left, e), depth(right, e));
	}

	public static void main(final String[] args) {
		//		final RedBlackTree tree = new RedBlackTree("redblack.tree", true, 4);
		//
		//		tree.put(20, "marko");
		//		System.out.println(tree.getString(20) + "|");

		putLoadTest();
	}

	public static void removeTest() {
		final RedBlackTree tree = new RedBlackTree("redblack.tree", true, 0);

		for (int i = 1; i <= 100; i++)
			tree.put(i);

		for (int i = 1; i <= 100; i++) {
			System.out.println(i);
			tree.remove(i);
			System.out.println(tree);
		}
	}

	// N 100, reads 1872, writes 608, tinyWrites 548, time 68ms, 0.68
	// N 1e3, reads 26263, writes 6434, swrites 5915, time 352ms, 0.352
	// N 1e4, reads 331082, writes 64906, swrites 59887, time 1343ms, 0.1343
	// N 1e5, r 3972003, w 649878, sw 599859, time 11399ms, 0.11399
	//        r 3822034, w 299938, sw 749799, time 9722ms
	//        r 3822034, w 299938, sw 749799, sr 0, rw 100044, rr 100088, time 11158ms
	//        r 3822034, w 299938, sw 749799, sr 0, rw 100044, rr 100088, time 11504ms
	//        r 3572131, w 299938, sw 749799, sr 249903, time 10090ms
	//        r 3472177, w 299938, sw 749799, sr 349857, time 8697ms

	// N 1e6, reads 46378180, writes 6499854, tinyWrites 5999832, time 118s, 0.1185
	//        reads 44878217, writes 2999926, tinyWrites 7499760, time 122s
	//        r 41378388, w 2999926, sw 7499760, sr 3499829, time 96s

	public static void putLoadTest() {
		final Timer timer = new Timer();
		timer.restart();

		final RedBlackTree tree = new RedBlackTree("redblack.tree", true, 4);
		final byte[] value = new byte[4];

		for (int i = 1; i <= 1000000; i++)
			tree.put(i, value);
		timer.stop();
		System.out.println("r " + reads + ", w " + writes + ", sw " + small_writes + ", sr " + small_reads);
		System.out.println(timer);
	}
}