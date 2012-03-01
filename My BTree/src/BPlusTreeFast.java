import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayDeque;
import java.util.Random;

import tintor.Timer;

public final class BPlusTreeFast {
	public class Iterator {
		Node _node;
		int _index;

		Iterator(final Node node, final int index) {
			_node = node;
			_index = index;
		}

		public boolean valid() {
			return _index < _node.keyCount();
		}

		public void next() {
			_index += 1;
			if (_index == _node.keyCount() && _node.pointer(0) >= 0) {
				_node.id(_node.pointer(0));
				_index = 0;
			}
		}

		public int key() {
			return _node.key(_index);
		}

		public int value() {
			return _node.pointer(_index + 1);
		}
	}

	// offsets
	//private final static int Root = 0, Height = 1, NodeCount = 2, Order = 3;
	private final static int HeaderSize = 4;

	//private final String _prefix;
	//private final static int Bits = 26;
	//final static int MaxFileSize = 1 << Bits + 2;
	//private final static int Mask = (1 << Bits) - 1;

	// 26 256M

	//private final int maps = 0;
	//private IntBuffer[] _data = new IntBuffer[2];
	//private FileChannel[] _channel = new FileChannel[_data.length];

	//private final long _map = 0;
	//private ByteBuffer _data;
	FileChannel _channel;

	int _root = 0, _height = 1, _nodeCount = 0;
	final int _order, _nodeSize;

	// Create new B+ Tree
	public BPlusTreeFast(final String fileName, final int order) {
		if (order < 3 || order * 2 < HeaderSize) throw new IllegalArgumentException();
		//_prefix = fileName + "_";
		try {
			_channel = new RandomAccessFile(fileName, "rw").getChannel();
			//_data = _channel.map(FileChannel.MapMode.READ_WRITE, 0, MaxFileSize);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		_order = order;
		_nodeSize = order * 8;
		APointer = order;

		final Node root = allocate();
		root.keyCount(0);
		root.pointer(0, -1);
		root.update();
		freeNode(root);
	}

	//	void write(final long p, final int v) {
	//		//		final int i = p >> Bits;
	//		//		if (i == maps) try {
	//		//			if (i == _channel.length) {
	//		//				_channel = Arrays.copyOf(_channel, _channel.length * 2);
	//		//				_data = Arrays.copyOf(_data, _data.length * 2);
	//		//			}
	//		//			_channel[i] = new RandomAccessFile(_prefix + i, "rw").getChannel();
	//		//			_data[i] = _channel[i].map(FileChannel.MapMode.READ_WRITE, 0, MaxFileSize).asIntBuffer();
	//		//			maps += 1;
	//		//		} catch (final IOException e) {
	//		//			throw new RuntimeException(e);
	//		//		}
	//		//		_data[i].put(p & Mask, v);
	//
	//		//		_data.put(p, v);
	//
	//		load(p >> Bits);
	//		_data.putInt((int) p & Mask, v);
	//	}

	//	void load(final long map) {
	//		if (map != _map) {
	//			_data = null;
	//			System.gc();
	//
	//			try {
	//				_data = _channel.map(FileChannel.MapMode.READ_WRITE, map * MaxFileSize, MaxFileSize);
	//			} catch (final IOException e) {
	//				throw new RuntimeException(e);
	//			}
	//			_map = map;
	//		}
	//	}

	//	int read(final long p) {
	//		//return _data[p >> Bits].get(p & Mask);
	//		//return _data.get(p);
	//
	//		load(p >> Bits);
	//		return _data.get((int) p & Mask);
	//	}

	private Node allocate() {
		final Node node = newNode(null, _nodeCount);
		_nodeCount += 1;
		return node;
	}

	final int APointer;

	private static final int AKeyCount = 0, AKey = 1, DKey = 1, DPointer = 1; // cache friendly

	//	private static final int AKeyCount = 0, APointer = 1, AKey = 2, DKey = 2, DPointer = 2;

	final ArrayDeque<ByteBuffer> _buffers = new ArrayDeque<ByteBuffer>();

	class Node {
		int id;
		long offset;
		Node next;
		ByteBuffer data = ByteBuffer.allocateDirect(_nodeSize);

		Node() {

		}

		Node(final Node n, final int ID) {
			next = n;
			id(ID);
		}

		void update() {
			data.clear();
			try {
				_channel.write(data, offset);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		void id(final int ID) {
			id = ID;
			offset = (long) ID * _nodeSize;

			data.clear();
			try {
				_channel.read(data, offset);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		int find(final int key) {
			int r = keyCount();
			if (Binary) {
				int l = 0;
				while (l < r) {
					final int i = (l + r) / 2;
					if (key < key(i))
						r = i;
					else
						l = i + 1;
				}
				return l;
			}
			for (int i = 0; i < r; i++)
				if (key < key(i)) return i;
			return r;
		}

		boolean insert(final int p, final int key, final int pointer) {
			final int c = keyCount();
			if (c >= _order - 1) return false;
			for (int i = c - 1; i >= p; i--) {
				key(i + 1, key(i));
				pointer(i + 1 + 1, pointer(i + 1));
			}
			pair(p, key, pointer);
			keyCount(c + 1);
			update();
			return true;
		}

		int read(final int p) {
			//return BPlusTreeFast.this.read(offset + p);
			//			System.out.println("data[" + p + "] -> " + data.getInt(p * 4));
			return data.getInt(p * 4);
		}

		void write(final int p, final int v) {
			//BPlusTreeFast.this.write(offset + p, v);
			//			System.out.println(v + " -> data[" + p + "]");
			data.putInt(p * 4, v);
		}

		int keyCount() {
			return read(AKeyCount);
		}

		void keyCount(final int v) {
			write(AKeyCount, v);
		}

		void pair(final int i, final int k, final int p) {
			key(i, k);
			pointer(i + 1, p);
		}

		int key(final int i) {
			return read(AKey + i * DKey);
		}

		void key(final int i, final int v) {
			write(AKey + i * DKey, v);
		}

		int pointer(final int i) {
			return read(APointer + i * DPointer);
		}

		void pointer(final int i, final int v) {
			//			System.out.println(v + " -> pointer[" + i + "] AP=" + APointer);
			write(APointer + i * DPointer, v);
		}
	}

	private boolean nodeInsert(final Node nodeA, final int b, final boolean leaf) {
		// if (there is space in node) do (insert)
		if (nodeA.insert(b, _insertKey, _insertPointer)) {
			freeNodes(nodeA);
			return true;
		}
		// else (node full) do (split node)

		// add and split node
		final Node nodeB = allocate();

		final int sizeB = (nodeA.keyCount() + 1) / 2;
		final int sizeA = nodeA.keyCount() + 1 - sizeB;

		// redistribute
		for (int i = nodeA.keyCount() - 1; i >= Math.min(b, sizeA); i--) {
			final int j = b <= i ? i + 1 : i;
			if (j >= sizeA)
				nodeB.pair(j - sizeA, nodeA.key(i), nodeA.pointer(i + 1));
			else
				nodeA.pair(j, nodeA.key(i), nodeA.pointer(i + 1));
		}

		// add
		if (b >= sizeA)
			nodeB.pair(b - sizeA, _insertKey, _insertPointer);
		else
			nodeA.pair(b, _insertKey, _insertPointer);

		// update sizes
		nodeA.keyCount(sizeA);
		nodeB.keyCount(sizeB);

		if (leaf) { // insert new leaf node in linked list
			nodeB.pointer(0, nodeA.pointer(0));
			nodeA.pointer(0, nodeB.id);
		} else { // extract middle key from internal node
			nodeB.pointer(0, nodeA.pointer(nodeA.keyCount()));
			nodeA.keyCount(nodeA.keyCount() - 1);
		}

		// return split info
		_insertKey = leaf ? nodeB.key(0) : nodeA.key(nodeA.keyCount());
		_insertPointer = nodeB.id;

		nodeA.update();
		freeNodes(nodeA);

		nodeB.update();
		freeNode(nodeB);
		return false;
	}

	private int _insertKey, _insertPointer;

	// === Node cashe
	private Node _nodes;

	private Node newNode(final Node next, final int id) {
		if (_nodes == null) return new Node(next, id);
		final Node a = _nodes;
		_nodes = a.next;
		a.next = next;
		a.id(id);
		return a;
	}

	private void freeNode(final Node a) {
		if (a.next != null) throw new RuntimeException();
		a.next = _nodes;
		_nodes = a;
	}

	private void freeNodes(Node a) {
		while (a != null) {
			final Node b = a.next;
			a.next = _nodes;
			_nodes = a;
			a = b;
		}
	}

	// ===

	public void put(final int key, final int value) {
		_insertKey = key;
		_insertPointer = value;

		// TODO optimize: avoid creating new Nodes
		Node node = newNode(null, _root);

		// PROCESS DOWN INTERNAL NODES
		for (int height = _height - 1; height > 0; height--)
			node = newNode(node, node.pointer(node.find(key)));

		// PROCESS LEAF
		final int p = node.find(key);
		// if (key is in tree) do (update value)
		if (p > 0 && key == node.key(p - 1)) {
			node.pointer(p, value);
			node.update();
			freeNodes(node);
			return;
		}
		if (nodeInsert(node, p, true)) return;

		// PROCESS UP INTERNAL NODES
		for (node = node.next; node != null; node = node.next) {
			if (nodeInsert(node, node.find(_insertKey), false)) return;
			freeNode(node);
		}

		// SPLIT ROOT
		node = allocate();
		node.keyCount(1);
		node.pointer(0, _root);
		node.key(0, _insertKey);
		node.pointer(1, _insertPointer);
		node.update();
		freeNode(node);

		_root = node.id;
		_height += 1;
	}

	public int get(final int key, final int def) {
		final Node node = new Node(null, _root);
		node.id(_root);
		int h = _height;
		while (true) {
			final int p = node.find(key);
			h -= 1;
			if (h == 0) return p == 0 || key != node.key(p - 1) ? def : node.pointer(p);
			node.id(node.pointer(p));
		}
	}

	public Iterator get(final int key) {
		final Node node = new Node(null, _root);
		int h = _height;
		while (true) {
			final int p = node.find(key);
			h -= 1;
			if (h == 0) return p == 0 || key != node.key(p - 1) ? null : new Iterator(node, p - 1);
			node.id(node.pointer(p));
		}
	}

	public Iterator iterator() {
		final Node node = new Node(null, _root);
		for (int h = 1; h < _height; h++)
			node.id(node.pointer(0));
		return new Iterator(node, 0);
	}

	void print(final Node node, final int height) {
		if (height < _height) {
			System.out.print("I" + node.id + " " + node.pointer(0));
			for (int i = 0; i < node.keyCount(); i++)
				System.out.print("\\" + node.key(i) + "/" + node.pointer(i + 1));
			System.out.println();

			for (int i = 0; i <= node.keyCount(); i++) {
				final Node n = new Node(null, node.pointer(i));
				print(n, height + 1);
			}
		} else {
			System.out.print("L" + node.id);
			for (int i = 0; i < node.keyCount(); i++)
				System.out.print(" " + node.key(i) + ":" + node.pointer(i + 1));
			System.out.println(" => " + node.pointer(0));
		}
	}

	public static void main(final String[] args) {
		putLoadTest();
		//		basicTest();
	}

	public static void basicTest() {
		final BPlusTreeFast tree = new BPlusTreeFast("bplus.tree", 3);

		final Random r = new Random();
		for (int f = 1; f <= 3; f++)
			for (int i = f; i <= 10; i += 3)
				tree.put(i, i * 10);

		//tree.print(tree.new Node(null, tree._root), 1);

		for (final BPlusTreeFast.Iterator it = tree.iterator(); it.valid(); it.next())
			System.out.println(it.key() + " " + it.value());
	}

	///////////////////
	//     1e7
	// 20 L16962 B12745
	// 22        B12938
	// 24 L17377 B12471
	// 26 L17007 B11947
	// 32        B10417
	// 48        B13325
	// 60        B12102
	// 64        B10295
	// 68        B12167
	//128        B13129

	public final static boolean Binary = true;

	public static void putLoadTest() {
		final Timer timer = new Timer();
		timer.restart();
		final BPlusTreeFast tree = new BPlusTreeFast("bplus.tree", 20);

		for (int m = 1; m <= 1; m++) {
			System.out.println(m);
			final int p = m * 1000000;
			final int F = 1000;
			for (int f = 1; f <= F; f++)
				for (int i = f; i <= 100000; i += F)
					tree.put(p + i, i);
		}
		timer.stop();

		System.out.println(timer.seconds() + " height=" + tree._height);
	}
}