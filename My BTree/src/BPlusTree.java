import java.nio.ByteBuffer;

import tintor.Timer;

public final class BPlusTree {
	public class Iterator {
		Node _node;
		int _index;

		Iterator(final Node node, final int index) {
			_node = node;
			_index = index;
		}

		public boolean valid() {
			return _index < _node.keyCount;
		}

		public void next() {
			_index += 1;
			if (_index == _node.keyCount) {
				final int next = _node.branches[0];
				if (next == None) return;

				_node = new Node(null, _order);
				read(_node, next);
				_index = 0;
			}
		}

		public int key() {
			return _node.keys[_index];
		}

		public int value() {
			return _node.branches[_index + 1];
		}
	}

	private static class Node {
		int id;
		Node next;

		int keyCount;
		int[] keys, branches;

		Node(final Node n, final int order) {
			next = n;
			keys = new int[order - 1];
			branches = new int[order];
		}

		void put(final ByteBuffer bb) {
			bb.putShort((short) keyCount);
			for (int i = 0; i < keyCount; i++)
				bb.putInt(keys[i]);
			for (int i = 0; i <= keyCount; i++)
				bb.putInt(branches[i]);
		}

		void get(final ByteBuffer bb) {
			keyCount = bb.getShort();
			for (int i = 0; i < keyCount; i++)
				keys[i] = bb.getInt();
			for (int i = 0; i <= keyCount; i++)
				branches[i] = bb.getInt();
		}

		int find(final int key) {
			for (int i = 0; i < keyCount; i++)
				if (key < keys[i]) return i;
			return keyCount;
		}

		void set(final int b, final int key, final int branch) {
			keys[b] = key;
			branches[b + 1] = branch;
		}
	}

	private static final int None = -1, HeaderSize = 14;

	private final Storage _storage;
	private int _alloc, _height;
	final int _order, _blockSize;
	private int _insertKey, _insertBranch;
	private Node _root;

	public BPlusTree(final String fileName, final boolean reset, final int order) {
		if (!reset && order != 0) throw new IllegalArgumentException();

		_storage = new Storage(fileName, "rw");

		if (reset) {
			_storage.truncate(0);

			_height = 1;
			_alloc = 1;
			_order = order;
			_blockSize = _order * 8;
			_storage.allocBuffer(_blockSize);

			_root = new Node(null, _order);
			_root.id = 0;
			_root.branches[0] = None;
			write(_root);

			writeHeader();
		} else {
			_storage.allocBuffer(HeaderSize);
			_storage.read(0, HeaderSize);
			final int root = _storage.buffer.getInt();
			_height = _storage.buffer.getInt();
			_alloc = _storage.buffer.getInt();
			_order = _storage.buffer.getShort();
			_blockSize = _order * 8;
			_storage.allocBuffer(_blockSize);

			_root = new Node(null, _order);
			read(_root, root);
		}
	}

	private void writeHeader() {
		_storage.buffer.clear();
		_storage.buffer.putInt(_root.id);
		_storage.buffer.putInt(_height);
		_storage.buffer.putInt(_alloc);
		_storage.write(0);
	}

	public void close() {
		_storage.close();
	}

	void read(final Node node, final int id) {
		node.id = id;
		_storage.read(HeaderSize + node.id * _blockSize, _blockSize);
		node.get(_storage.buffer);
	}

	private void write(final Node node) {
		_storage.buffer.clear();
		node.put(_storage.buffer);
		_storage.write(HeaderSize + node.id * _blockSize);
	}

	private Node split(final Node nodeA, final int b) {
		final Node nodeB = new Node(null, _order);
		nodeB.id = _alloc++;
		writeHeader();

		final int sizeB = (nodeA.keyCount + 1) / 2;
		final int sizeA = nodeA.keyCount + 1 - sizeB;

		// redistribute
		for (int i = nodeA.keyCount - 1; i >= Math.min(b, sizeA); i--) {
			final int j = b <= i ? i + 1 : i;
			if (j >= sizeA)
				nodeB.set(j - sizeA, nodeA.keys[i], nodeA.branches[i + 1]);
			else
				nodeA.set(j, nodeA.keys[i], nodeA.branches[i + 1]);
		}

		// add
		if (b >= sizeA)
			nodeB.set(b - sizeA, _insertKey, _insertBranch);
		else
			nodeA.set(b, _insertKey, _insertBranch);

		nodeA.keyCount = sizeA;
		nodeB.keyCount = sizeB;
		return nodeB;
	}

	private boolean nodeInsert(final Node node, final int b, final boolean leaf) {
		// if (no overflow) do (insert key/branch)
		if (node.keyCount < node.keys.length) {
			for (int i = node.keyCount - 1; i >= b; i--)
				node.set(i + 1, node.keys[i], node.branches[i + 1]);
			node.set(b, _insertKey, _insertBranch);
			node.keyCount += 1;
			write(node);
			return false;
		}
		// else (overflow) do (split internal node)

		// add and split node
		final Node nodeB = split(node, b);

		if (leaf) { // insert new leaf node in linked list
			nodeB.branches[0] = node.branches[0];
			node.branches[0] = nodeB.id;
		} else { // extract middle key from internal node
			nodeB.branches[0] = node.branches[node.keyCount];
			node.keyCount -= 1;
		}

		// save nodes
		write(node);
		write(nodeB);

		// return split info
		_insertKey = leaf ? nodeB.keys[0] : node.keys[node.keyCount];
		_insertBranch = nodeB.id;
		return true;
	}

	public void put(final int key, final int value) {
		//System.out.println("put " + key);
		_insertKey = key;
		_insertBranch = value;

		// PROCESS ROOT
		_root.next = null;
		Node node = _root;
		if (_height > 1) {
			int next_id = _root.branches[_root.find(_insertKey)];

			for (int height = 2; height < _height; height++) {
				node = new Node(node, _order);
				read(node, next_id);
				next_id = node.branches[node.find(_insertKey)];
			}

			// PROCESS LEAF
			node = new Node(node, _order);
			read(node, next_id);
		}
		final int b = node.find(_insertKey);
		if (b > 0 && _insertKey == node.keys[b - 1]) {
			// if (leaf node with key) do (update value)
			node.branches[b] = _insertBranch;
			write(node);
			return;
		}
		// else (leaf node with no key)
		if (!nodeInsert(node, b, true)) return;

		// PROCESS UP INTERNALS
		for (node = node.next; node != null; node = node.next)
			if (!nodeInsert(node, node.find(_insertKey), false)) return;

		// SPLIT ROOT
		node = new Node(null, _order);
		node.id = _alloc++;
		node.keyCount = 1;
		node.branches[0] = _root.id;
		node.keys[0] = _insertKey;
		node.branches[1] = _insertBranch;
		write(node);

		_root = node;
		_height += 1;
		writeHeader();
	}

	public int get(final int key, final int def) {
		int b = _root.find(key);
		int id = _root.branches[b];
		if (_height == 1) return b == 0 || key != _root.keys[b - 1] ? def : id;

		final Node node = new Node(null, _order);
		for (int h = 2; h <= _height; h++) {
			read(node, id);
			b = node.find(key);
			id = node.branches[b];
		}
		return b == 0 || key != node.keys[b - 1] ? def : id;
	}

	public Iterator get(final int key) {
		int b = _root.find(key);
		int id = _root.branches[b];
		if (_height == 1) return b == 0 || key != _root.keys[b - 1] ? null : new Iterator(_root, b - 1);

		final Node node = new Node(null, _order);
		for (int h = 2; h <= _height; h++) {
			read(node, id);
			b = node.find(key);
			id = node.branches[b];
		}
		return b == 0 || key != node.keys[b - 1] ? null : new Iterator(node, b - 1);
	}

	public Iterator iterator() {
		if (_height == 1) return new Iterator(_root, 0);

		final Node node = new Node(null, _order);
		read(node, _root.branches[0]);
		for (int h = 3; h <= _height; h++)
			read(node, node.branches[0]);
		return new Iterator(node, 0);
	}

	void print(final Node node, final int height) {
		if (height < _height) {
			System.out.print("I" + node.id + " " + node.branches[0]);
			for (int i = 0; i < node.keyCount; i++)
				System.out.print("\\" + node.keys[i] + "/" + node.branches[i + 1]);
			System.out.println();

			for (int i = 0; i <= node.keyCount; i++) {
				final Node n = new Node(null, _order);
				read(n, node.branches[i]);
				print(n, height + 1);
			}
		} else {
			System.out.print("L" + node.id);
			for (int i = 0; i < node.keyCount; i++)
				System.out.print(" " + node.keys[i] + ":" + node.branches[i + 1]);
			System.out.println(" => " + node.branches[0]);
		}
	}

	// Order=20
	// 1e3 274ms height=3
	// 1e4 872ms height=4
	// 1e5 2508ms height=5
	// 1e6 65s height=6

	// Order=30
	// 1e5 2833ms height=4
	// 1e6 42s height=5

	// Order=40
	// 1e6 33s height=5

	// Order=50
	// 1e6 27s height=5

	// Order=60
	// 1e6 23s height=4

	// Order=70
	// 1e5 2548ms height=4
	// 1e6 21s height=4
	// 1e7 6min height=5

	// Order=80
	// 1e6 22s height=4

	// Order=100
	// 1e7 333s height=4

	// Order=150
	// 1e8 315s height=4

	public static void main(final String[] args) {
		putLoadTest();
	}

	public static void basicTest() {
		final BPlusTree tree = new BPlusTree("bplus.tree", true, 3);

		for (int i = 1; i <= 10; i++)
			tree.put(i, i * 10);

		for (final BPlusTree.Iterator it = tree.iterator(); it.valid(); it.next())
			System.out.println(it.key() + " " + it.value());
	}

	public static void putLoadTest() {
		final Timer timer = new Timer();
		timer.restart();
		final BPlusTree tree = new BPlusTree("bplus.tree", true, 150);

		for (int i = 1; i <= 10000000; i++)
			tree.put(i, i * 10);
		timer.stop();

		System.out.println(timer + " height=" + tree._height);
	}
}