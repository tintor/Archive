package b_plus_tree;

import java.util.Enumeration;
import java.util.Hashtable;

/// <summary>
/// Bplustree mapping fixed length Strings (byte sequences) to longs (seek positions in file indexed).
/// "Next leaf pointer" is not used since it increases the chance of file corruption on failure.
/// All modifications are "shadowed" until a flush of all modifications succeeds.  Modifications are
/// "hardened" when the header record is rewritten with a new root.  This design trades a few "unneeded"
/// buffer writes for lower likelihood of file corruption.
/// </summary>
public class BplusTreeLong implements ITreeIndex {
	public java.io.RandomAccessFile fromfile;
	// should be read only
	public BufferFile buffers;
	// should be read only
	public int buffersize;
	// should be read only
	public int KeyLength;
	public long seekStart = 0;
	public static byte[] HEADERPREFIX = { 98, 112, 78, 98, 112 };
	// header consists of 
	// prefix | version | node size | key size | culture id | buffer number of root | buffer number of free list head
	int headersize = HEADERPREFIX.length + 1 + BufferFile.INTSTORAGE * 3 + BufferFile.LONGSTORAGE * 2;
	public static byte VERSION = 0;
	// for java, only allow the invariant culture.
	public static int INVARIANTCULTUREID = 127;
	// size of allocated key space in each node (should be a read only property)
	public int NodeSize;
	BplusNode root = null;
	long rootSeek;
	long freeHeadSeek;
	public long LastValueFound;
	public Hashtable FreeBuffersOnCommit = new Hashtable();
	public Hashtable FreeBuffersOnAbort = new Hashtable();
	Hashtable IdToTerminalNode = new Hashtable();
	Hashtable TerminalNodeToId = new Hashtable();
	int TerminalNodeCount = 0;
	int LowerTerminalNodeCount = 0;
	int FifoLimit = 100;
	public static int NULLBUFFERNUMBER = -1;
	public static byte NONLEAF = 0, LEAF = 1, FREE = 2;

	public BplusTreeLong(final java.io.RandomAccessFile fromfile, final int NodeSize, final int KeyLength,
			final long StartSeek) throws Exception {
		this.fromfile = fromfile;
		this.NodeSize = NodeSize;
		seekStart = StartSeek;
		// add in key prefix overhead
		this.KeyLength = KeyLength + BufferFile.SHORTSTORAGE;
		rootSeek = NULLBUFFERNUMBER;
		root = null;
		freeHeadSeek = NULLBUFFERNUMBER;
		this.SanityCheck();
	}

	public int MaxKeyLength() {
		return KeyLength - BufferFile.SHORTSTORAGE;
	}

	public void Shutdown() throws Exception {
		fromfile.close();
	}

	public int Compare(final String left, final String right) throws Exception {
		return left.compareTo(right); // only lexicographic compare allowed for java
	}

	public void SanityCheck(final boolean strong) throws Exception {
		this.SanityCheck();
		if (strong) {
			Recover(false);
			// look at all deferred deallocations -- they should not be free
			final byte[] buffer = new byte[1];
			for (final Enumeration e = FreeBuffersOnAbort.keys(); e.hasMoreElements();) {
				final Object thing = e.nextElement();
				final long buffernumber = ((Long) thing).longValue();
				buffers.getBuffer(buffernumber, buffer, 0, 1);
				if (buffer[0] == FREE)
					throw new BplusTreeException("free on abort buffer already marked free " + buffernumber);
			}
			for (final Enumeration e = FreeBuffersOnCommit.keys(); e.hasMoreElements();) {
				final Object thing = e.nextElement();
				final long buffernumber = ((Long) thing).longValue();
				buffers.getBuffer(buffernumber, buffer, 0, 1);
				if (buffer[0] == FREE)
					throw new BplusTreeException("free on commit buffer already marked free " + buffernumber);
			}
		}
	}

	public void Recover(final boolean CorrectErrors) throws Exception {
		final Hashtable visited = new Hashtable();
		if (root != null) // find all reachable nodes
			root.SanityCheck(visited);
		// traverse the free list
		long freebuffernumber = freeHeadSeek;
		while (freebuffernumber != NULLBUFFERNUMBER) {
			if (visited.containsKey(new Long(freebuffernumber)))
				throw new BplusTreeException("free buffer visited twice " + freebuffernumber);
			visited.put(new Long(freebuffernumber), new Byte(FREE));
			freebuffernumber = parseFreeBuffer(freebuffernumber);
		}
		// find out what is missing
		final Hashtable Missing = new Hashtable();
		final long maxbuffer = buffers.nextBufferNumber();
		for (long i = 0; i < maxbuffer; i++)
			if (!visited.containsKey(new Long(i))) //Missing[i] = i;
				Missing.put(new Long(i), new Long(i));
		// remove from missing any free-on-commit blocks
		for (final Enumeration e = FreeBuffersOnCommit.keys(); e.hasMoreElements();)
			Missing.remove(e.nextElement());
		// add the missing values to the free list
		if (CorrectErrors)
			for (final Enumeration e = Missing.keys(); e.hasMoreElements();) {
				final long buffernumber = ((Long) e.nextElement()).longValue();
				deallocateBuffer(buffernumber);
			}
		else if (Missing.size() > 0) throw new BplusTreeException("found " + Missing.size() + " unreachable buffers.");
	}

	public void SerializationCheck() throws Exception {
		if (root == null) throw new BplusTreeException("serialization check requires initialized root, sorry");
		root.SerializationCheck();
	}

	void SanityCheck() throws Exception {
		if (NodeSize < 2) throw new BplusTreeException("node size must be larger than 2");
		if (KeyLength < 5) throw new BplusTreeException("Key length must be larger than 5");
		if (seekStart < 0) throw new BplusTreeException("start seek may not be negative");
		// compute the buffer size
		// indicator | seek position | [ key storage | seek position ]*
		final int keystorage = KeyLength + BufferFile.SHORTSTORAGE;
		buffersize = 1 + BufferFile.LONGSTORAGE + (keystorage + BufferFile.LONGSTORAGE) * NodeSize;
	}

	public BplusTreeLong(final java.io.RandomAccessFile fromfile, final int KeyLength, final int NodeSize,
			final int CultureId) throws Exception {
		// just start seek at 0
		this(fromfile, NodeSize, KeyLength, 0L);
	}

	public static BplusTreeLong SetupFromExistingStream(final java.io.RandomAccessFile fromfile) throws Exception {
		return SetupFromExistingStream(fromfile, 0);
	}

	public static BplusTreeLong SetupFromExistingStream(final java.io.RandomAccessFile fromfile, final long StartSeek)
			throws Exception {
		//int dummyId = System.Globalization.CultureInfo.InvariantCulture.LCID;
		final BplusTreeLong result = new BplusTreeLong(fromfile, 7, 100, StartSeek);
		result.readHeader();
		result.buffers = BufferFile.SetupFromExistingStream(fromfile, StartSeek + result.headersize);
		if (result.buffers.buffersize != result.buffersize)
			throw new BplusTreeException("inner and outer buffer sizes should match");
		if (result.rootSeek != NULLBUFFERNUMBER) {
			result.root = new BplusNode(result, null, -1, true);
			result.root.LoadFromBuffer(result.rootSeek);
		}
		return result;
	}

	public static BplusTreeLong InitializeInStream(final java.io.RandomAccessFile fromfile, final int KeyLength,
			final int NodeSize) throws Exception {
		//int dummyId = System.Globalization.CultureInfo.InvariantCulture.LCID;
		return InitializeInStream(fromfile, KeyLength, NodeSize, INVARIANTCULTUREID);
	}

	public static BplusTreeLong InitializeInStream(final java.io.RandomAccessFile fromfile, final int KeyLength,
			final int NodeSize, final int CultureId) throws Exception {
		return InitializeInStream(fromfile, KeyLength, NodeSize, CultureId, 0);
	}

	public static BplusTreeLong InitializeInStream(final java.io.RandomAccessFile fromfile, final int KeyLength,
			final int NodeSize, final int CultureId, final long StartSeek) throws Exception {
		if (fromfile.length() > StartSeek)
			throw new BplusTreeException("can't initialize bplus tree inside written area of stream");
		final BplusTreeLong result = new BplusTreeLong(fromfile, NodeSize, KeyLength, StartSeek);
		result.setHeader();
		result.buffers = BufferFile.InitializeBufferFileInStream(fromfile, result.buffersize, StartSeek
				+ result.headersize);
		return result;
	}

	public void SetFootPrintLimit(final int limit) throws Exception {
		if (limit < 5) throw new BplusTreeException("foot print limit less than 5 is too small");
		FifoLimit = limit;
	}

	public void RemoveKey(final String key) throws Exception {
		if (root == null) throw new BplusTreeKeyMissing("tree is empty: cannot delete");
		boolean MergeMe;
		final BplusNode theroot = root;
		final BplusNode.Delete deletion = theroot.delete(key);
		MergeMe = deletion.MergeMe;
		// if the root is not a leaf and contains only one child (no key), reroot
		if (MergeMe && !root.isLeaf && root.SizeInUse() == 0) {
			root = root.FirstChild();
			rootSeek = root.makeRoot();
			theroot.Free();
		}
	}

	public long get(final String key) throws Exception {
		boolean test = ContainsKey(key);
		if (!test) throw new BplusTreeKeyMissing("no such key found: " + key);
		return LastValueFound;
	}

	public void set(final String key, final long value) throws Exception {
		if (!BplusNode.KeyOK(key, this)) {
			String data = "null";
			if (key != null) data = "key " + key + " length " + key.length();
			throw new BplusTreeBadKeyValue("null or too large key cannot be inserted into tree: " + data);
		}
		boolean rootinit = false;
		if (root == null) {
			// allocate root
			root = new BplusNode(this, null, -1, true);
			rootinit = true;
		}
		// insert into root...
		root.Insert(key, value);
		final String splitString = root.splitString;
		final BplusNode splitNode = root.splitNode;
		// clear split info
		root.splitString = null;
		root.splitNode = null;
		if (splitNode != null) {
			// split of root: make a new root.
			rootinit = true;
			final BplusNode oldRoot = root;
			root = BplusNode.BinaryRoot(oldRoot, splitString, splitNode, this);
		}
		if (rootinit) rootSeek = root.DumpToFreshBuffer();
		// check size in memory
		ShrinkFootprint();
	}

	public String FirstKey() throws Exception {
		String result = null;
		if (root != null) {
			// empty String is smallest possible tree
			if (ContainsKey(""))
				result = "";
			else
				return root.FindNextKey("");
			ShrinkFootprint();
		}
		return result;
	}

	public String NextKey(final String AfterThisKey) throws Exception {
		if (AfterThisKey == null) throw new BplusTreeBadKeyValue("cannot search for null String");
		final String result = root.FindNextKey(AfterThisKey);
		ShrinkFootprint();
		return result;
	}

	public boolean ContainsKey(final String key) throws Exception {
		if (key == null) throw new BplusTreeBadKeyValue("cannot search for null String");
		boolean result = false;
		//valueFound = (long) 0;
		if (root != null) {
			result = root.FindMatch(key);
			LastValueFound = root.LastValueFound;
		}
		ShrinkFootprint();
		return result;
	}

	public void Set(final String key, final Object map) throws Exception {
		if (map.getClass() != Long.class)
			throw new BplusTreeBadKeyValue("only longs may be used as values in a BplusTreeLong: " + map);
		set(key, ((Long) map).longValue());
	}

	public Object Get(final String key, final Object defaultValue) throws Exception {
		if (ContainsKey(key)) return new Long(LastValueFound);
		return defaultValue;
	}

	/// Store off any changed buffers, clear the fifo, free invalid buffers
	public void Commit() throws Exception {
		// store all modifications
		if (root != null) rootSeek = root.Invalidate(false);
		// commit the new root
		setHeader();
		for (final Enumeration e = FreeBuffersOnCommit.keys(); e.hasMoreElements();) {
			final Long thing = (Long) e.nextElement();
			final long buffernumber = thing.longValue();
			deallocateBuffer(buffernumber);
		}
		// store the free list head
		setHeader();
		//this.fromfile.Flush();
		ResetBookkeeping();
	}

	/// Forget all changes since last commit
	public void Abort() throws Exception {
		// deallocate allocated blocks
		for (final Enumeration e = FreeBuffersOnAbort.keys(); e.hasMoreElements();) {
			final Long thing = (Long) e.nextElement();
			final long buffernumber = thing.longValue();
			deallocateBuffer(buffernumber);
		}
		final long freehead = freeHeadSeek;
		// reread the header (except for freelist head)
		readHeader();
		// restore the root
		if (rootSeek == NULLBUFFERNUMBER)
			root = null; // nothing was committed
		else
			root.LoadFromBuffer(rootSeek);
		ResetBookkeeping();
		freeHeadSeek = freehead;
		setHeader(); // store new freelist head
	}

	void ResetBookkeeping() throws Exception {
		FreeBuffersOnCommit.clear();
		FreeBuffersOnAbort.clear();
		IdToTerminalNode.clear();
		TerminalNodeToId.clear();
	}

	public long allocateBuffer() throws Exception {
		long allocated = -1;
		if (freeHeadSeek == NULLBUFFERNUMBER) {
			// should be written immediately after allocation
			allocated = buffers.nextBufferNumber();
			return allocated;
		}
		// get the free head data
		allocated = freeHeadSeek;
		freeHeadSeek = parseFreeBuffer(allocated);
		return allocated;
	}

	long parseFreeBuffer(final long buffernumber) throws Exception {
		final int freesize = 1 + BufferFile.LONGSTORAGE;
		final byte[] buffer = new byte[freesize];
		buffers.getBuffer(buffernumber, buffer, 0, freesize);
		if (buffer[0] != FREE) throw new BplusTreeException("free buffer not marked free");
		final long result = BufferFile.RetrieveLong(buffer, 1);
		return result;
	}

	public void deallocateBuffer(final long buffernumber) throws Exception {
		//System.Diagnostics.Debug.WriteLine("<br> deallocating "+buffernumber);
		final int freesize = 1 + BufferFile.LONGSTORAGE;
		final byte[] buffer = new byte[freesize];
		// it better not already be marked free
		buffers.getBuffer(buffernumber, buffer, 0, 1);
		if (buffer[0] == FREE) throw new BplusTreeException("attempt to re-free free buffer not allowed");
		buffer[0] = FREE;
		BufferFile.Store(freeHeadSeek, buffer, 1);
		buffers.setBuffer(buffernumber, buffer, 0, freesize);
		freeHeadSeek = buffernumber;
	}

	void setHeader() throws Exception {
		final byte[] header = makeHeader();
		fromfile.seek(seekStart);//, System.IO.SeekOrigin.Begin);
		fromfile.write(header, 0, header.length);
	}

	public void RecordTerminalNode(final BplusNode terminalNode) throws Exception {
		if (terminalNode == root) return; // never record the root node
		if (TerminalNodeToId.containsKey(terminalNode)) return; // don't record it again
		final Integer id = new Integer(TerminalNodeCount);
		TerminalNodeCount++;
		TerminalNodeToId.put(terminalNode, id);
		IdToTerminalNode.put(id, terminalNode);
	}

	public void ForgetTerminalNode(final BplusNode nonterminalNode) throws Exception {
		if (!TerminalNodeToId.containsKey(nonterminalNode)) // silently ignore (?)
			return;
		final Integer id = (Integer) TerminalNodeToId.get(nonterminalNode);
		if (id.intValue() == LowerTerminalNodeCount) LowerTerminalNodeCount++;
		IdToTerminalNode.remove(id);
		TerminalNodeToId.remove(nonterminalNode);
	}

	public void ShrinkFootprint() throws Exception {
		InvalidateTerminalNodes(FifoLimit);
	}

	public void InvalidateTerminalNodes(final int toLimit) throws Exception {
		while (TerminalNodeToId.size() > toLimit) {
			// choose oldest nonterminal and deallocate it
			Integer id = new Integer(LowerTerminalNodeCount);
			while (!IdToTerminalNode.containsKey(id)) {
				LowerTerminalNodeCount++; // since most nodes are terminal this should usually be a short walk
				id = new Integer(LowerTerminalNodeCount);
				if (LowerTerminalNodeCount > TerminalNodeCount)
					throw new BplusTreeException("internal error counting nodes, lower limit went too large");
			}
			final BplusNode victim = (BplusNode) IdToTerminalNode.get(id);
			IdToTerminalNode.remove(id);
			TerminalNodeToId.remove(victim);
			if (victim.myBufferNumber != NULLBUFFERNUMBER) victim.Invalidate(true);
		}
	}

	void readHeader() throws Exception {
		// prefix | version | node size | key size | culture id | buffer number of root | buffer number of free list head
		final byte[] header = new byte[headersize];
		fromfile.seek(seekStart); //, System.IO.SeekOrigin.Begin);
		fromfile.read(header, 0, headersize);
		int index = 0;
		// check prefix
		for (index = 0; index < HEADERPREFIX.length; index++) {
			if (header[index] != HEADERPREFIX[index]) throw new BplusTreeException("invalid header prefix");
			index++;
		}
		index = HEADERPREFIX.length;
		// skip version (for now)
		index++;
		NodeSize = BufferFile.Retrieve(header, index);
		index += BufferFile.INTSTORAGE;
		KeyLength = BufferFile.Retrieve(header, index);
		index += BufferFile.INTSTORAGE;
		final int CultureId = BufferFile.Retrieve(header, index);
		if (CultureId != INVARIANTCULTUREID) throw new BplusTreeException("BplusJ only supports the invariant culture");
		index += BufferFile.INTSTORAGE;
		rootSeek = BufferFile.RetrieveLong(header, index);
		index += BufferFile.LONGSTORAGE;
		freeHeadSeek = BufferFile.RetrieveLong(header, index);
		this.SanityCheck();
	}

	public byte[] makeHeader() throws Exception {
		// prefix | version | node size | key size | culture id | buffer number of root | buffer number of free list head
		final byte[] result = new byte[headersize];
		for (int i = 0; i < HEADERPREFIX.length; i++)
			result[i] = HEADERPREFIX[i];
		result[HEADERPREFIX.length] = VERSION;
		int index = HEADERPREFIX.length + 1;
		BufferFile.Store(NodeSize, result, index);
		index += BufferFile.INTSTORAGE;
		BufferFile.Store(KeyLength, result, index);
		index += BufferFile.INTSTORAGE;
		BufferFile.Store(INVARIANTCULTUREID, result, index);
		index += BufferFile.INTSTORAGE;
		BufferFile.Store(rootSeek, result, index);
		index += BufferFile.LONGSTORAGE;
		BufferFile.Store(freeHeadSeek, result, index);
		return result;
	}

	public static class BplusNode {
		public boolean isLeaf = true;
		// the maximum number of children to each node.
		int Size;
		// false if the node is no longer active and should not be used.
		boolean isValid = true;
		// true if the materialized node needs to be persisted.
		boolean Dirty = true;
		// if non-root reference to the parent node containing this node
		BplusNode parent = null;
		// tree containing this node
		BplusTreeLong owner = null;
		// buffer number of this node
		public long myBufferNumber = BplusTreeLong.NULLBUFFERNUMBER;
		// number of children used by this node
		long[] ChildBufferNumbers;
		String[] ChildKeys;
		BplusNode[] MaterializedChildNodes;
		int indexInParent = -1;
		/// Temporary slots for use in splitting, fetching
		public BplusNode splitNode = null;
		public String splitString = null;
		public long LastValueFound = 0;

		/// Create a new BplusNode and install in parent if parent is not null.
		/// <param name="owner">tree containing the node</param>
		/// <param name="parent">parent node (if provided)</param>
		/// <param name="indexInParent">location in parent if provided</param>
		public BplusNode(final BplusTreeLong owner, final BplusNode parent, final int indexInParent, final boolean isLeaf)
				throws Exception {
			this.isLeaf = isLeaf;
			this.owner = owner;
			this.parent = parent;
			Size = owner.NodeSize;
			isValid = true;
			Dirty = true;
			Clear();
			if (parent != null && indexInParent >= 0) {
				if (indexInParent > Size) throw new BplusTreeException("parent index too large");
				// key info, etc, set elsewhere
				this.parent.MaterializedChildNodes[indexInParent] = this;
				myBufferNumber = this.parent.ChildBufferNumbers[indexInParent];
				this.indexInParent = indexInParent;
			}
		}

		public BplusNode FirstChild() throws Exception {
			final BplusNode result = MaterializeNodeAtIndex(0);
			if (result == null) throw new BplusTreeException("no first child");
			return result;
		}

		public long makeRoot() throws Exception {
			parent = null;
			indexInParent = -1;
			if (myBufferNumber == BplusTreeLong.NULLBUFFERNUMBER)
				throw new BplusTreeException("no root seek allocated to new root");
			return myBufferNumber;
		}

		public void Free() throws Exception {
			if (myBufferNumber != BplusTreeLong.NULLBUFFERNUMBER) {
				final Long L = new Long(myBufferNumber);
				if (owner.FreeBuffersOnAbort.containsKey(L)) {
					// free it now
					owner.FreeBuffersOnAbort.remove(L);
					owner.deallocateBuffer(myBufferNumber);
				} else
					// free on commit
					owner.FreeBuffersOnCommit.put(L, L);
			}
			myBufferNumber = BplusTreeLong.NULLBUFFERNUMBER; // don't do it twice...
		}

		public void SerializationCheck() throws Exception {
			final BplusNode A = new BplusNode(owner, null, -1, false);
			for (int i = 0; i < Size; i++) {
				final long j = i * 0xf0f0f0f0f0f0f01L;
				A.ChildBufferNumbers[i] = j;
				A.ChildKeys[i] = "k" + i;
			}
			A.ChildBufferNumbers[Size] = 7;
			A.TestRebuffer();
			A.isLeaf = true;
			for (int i = 0; i < Size; i++) {
				final long j = -i * 0x3e3e3e3e3e3e666L;
				A.ChildBufferNumbers[i] = j;
				A.ChildKeys[i] = "key" + i;
			}
			A.ChildBufferNumbers[Size] = -9097;
			A.TestRebuffer();
		}

		void TestRebuffer() throws Exception {
			final boolean IL = isLeaf;
			final long[] Ns = ChildBufferNumbers;
			final String[] Ks = ChildKeys;
			final byte[] buffer = new byte[owner.buffersize];
			Dump(buffer);
			Clear();
			Load(buffer);
			for (int i = 0; i < Size; i++) {
				if (ChildBufferNumbers[i] != Ns[i])
					throw new BplusTreeException("didn't get back buffernumber " + i + " got "
							+ ChildBufferNumbers[i] + " not " + Ns[i]);
				if (!ChildKeys[i].equals(Ks[i]))
					throw new BplusTreeException("didn't get back key " + i + " got " + ChildKeys[i] + " not "
							+ Ks[i]);
			}
			if (ChildBufferNumbers[Size] != Ns[Size])
				throw new BplusTreeException("didn't get back buffernumber " + Size + " got "
						+ ChildBufferNumbers[Size] + " not " + Ns[Size]);
			if (isLeaf != IL) throw new BplusTreeException("isLeaf should be " + IL + " got " + isLeaf);
		}

		public String SanityCheck(Hashtable visited) throws Exception {
			String result = null;
			if (visited == null) visited = new Hashtable();
			if (visited.containsKey(this)) throw new BplusTreeException("node visited twice " + myBufferNumber);
			visited.put(this, new Long(myBufferNumber));
			if (myBufferNumber != BplusTreeLong.NULLBUFFERNUMBER) {
				final Long bf = new Long(myBufferNumber);
				if (visited.containsKey(bf))
					throw new BplusTreeException("buffer number seen twice " + myBufferNumber);
				visited.put(bf, this);
			}
			if (parent != null) {
				if (parent.isLeaf) throw new BplusTreeException("parent is leaf");
				parent.MaterializeNodeAtIndex(indexInParent);
				if (parent.MaterializedChildNodes[indexInParent] != this)
					throw new BplusTreeException("incorrect index in parent");
				// since not at root there should be at least size/2 keys
				int limit = Size / 2;
				if (isLeaf) limit--;
				for (int i = 0; i < limit; i++)
					if (ChildKeys[i] == null) throw new BplusTreeException("null child in first half");
			}
			result = ChildKeys[0]; // for leaf
			if (!isLeaf) {
				MaterializeNodeAtIndex(0);
				result = MaterializedChildNodes[0].SanityCheck(visited);
				for (int i = 0; i < Size; i++) {
					if (ChildKeys[i] == null) break;
					MaterializeNodeAtIndex(i + 1);
					final String least = MaterializedChildNodes[i + 1].SanityCheck(visited);
					if (least == null)
						throw new BplusTreeException("null least in child doesn't match node entry "
								+ ChildKeys[i]);
					if (!least.equals(ChildKeys[i]))
						throw new BplusTreeException("least in child " + least + " doesn't match node entry "
								+ ChildKeys[i]);
				}
			}
			// look for duplicate keys
			String lastkey = ChildKeys[0];
			for (int i = 1; i < Size; i++) {
				if (ChildKeys[i] == null) break;
				if (lastkey.equals(ChildKeys[i])) throw new BplusTreeException("duplicate key in node " + lastkey);
				lastkey = ChildKeys[i];
			}
			return result;
		}

		void Destroy() {
			// make sure the structure is useless, it should no longer be used.
			owner = null;
			parent = null;
			Size = -100;
			ChildBufferNumbers = null;
			ChildKeys = null;
			MaterializedChildNodes = null;
			myBufferNumber = BplusTreeLong.NULLBUFFERNUMBER;
			indexInParent = -100;
			Dirty = false;
		}

		public int SizeInUse() {
			int result = 0;
			for (int i = 0; i < Size; i++) {
				if (ChildKeys[i] == null) break;
				result++;
			}
			return result;
		}

		public static BplusNode BinaryRoot(final BplusNode LeftNode, final String key, final BplusNode RightNode,
				final BplusTreeLong owner) throws Exception {
			final BplusNode newRoot = new BplusNode(owner, null, -1, false);
			//newRoot.Clear(); // redundant
			newRoot.ChildKeys[0] = key;
			LeftNode.Reparent(newRoot, 0);
			RightNode.Reparent(newRoot, 1);
			// new root is stored elsewhere
			return newRoot;
		}

		void Reparent(final BplusNode newParent, final int ParentIndex) throws Exception {
			// keys and existing parent structure must be updated elsewhere.
			parent = newParent;
			indexInParent = ParentIndex;
			newParent.ChildBufferNumbers[ParentIndex] = myBufferNumber;
			newParent.MaterializedChildNodes[ParentIndex] = this;
			// parent is no longer terminal
			owner.ForgetTerminalNode(parent);
		}

		void Clear() throws Exception {
			ChildBufferNumbers = new long[Size + 1];
			ChildKeys = new String[Size];
			MaterializedChildNodes = new BplusNode[Size + 1];
			for (int i = 0; i < Size; i++) {
				ChildBufferNumbers[i] = BplusTreeLong.NULLBUFFERNUMBER;
				MaterializedChildNodes[i] = null;
				ChildKeys[i] = null;
			}
			ChildBufferNumbers[Size] = BplusTreeLong.NULLBUFFERNUMBER;
			MaterializedChildNodes[Size] = null;
			// this is now a terminal node
			owner.RecordTerminalNode(this);
		}

		/// <summary>
		/// Find first index in self associated with a key same or greater than CompareKey
		/// </summary>
		/// <param name="CompareKey">CompareKey</param>
		/// <param name="LookPastOnly">if true and this is a leaf then look for a greater value</param>
		/// <returns>lowest index of same or greater key or this.Size if no greater key.</returns>
		int FindAtOrNextPosition(final String CompareKey, boolean LookPastOnly) throws Exception {
			int insertposition = 0;
			//System.Globalization.CultureInfo culture = this.owner.cultureContext;
			//System.Globalization.CompareInfo cmp = culture.CompareInfo;
			if (isLeaf && !LookPastOnly) // look for exact match or greater or null
				while (insertposition < Size && ChildKeys[insertposition] != null &&
				//cmp.Compare(this.ChildKeys[insertposition], CompareKey)<0) 
						owner.Compare(ChildKeys[insertposition], CompareKey) < 0)
					insertposition++;
			else
				// look for greater or null only
				while (insertposition < Size && ChildKeys[insertposition] != null
						&& owner.Compare(ChildKeys[insertposition], CompareKey) <= 0)
					insertposition++;
			return insertposition;
		}

		/// <summary>
		/// Find the first key below atIndex, or if no such node traverse to the next key to the right.
		/// If no such key exists, return nulls.
		/// </summary>
		/// <param name="atIndex">where to look in this node</param>
		/// <param name="FoundInLeaf">leaf where found</param>
		/// <param name="KeyFound">key value found</param>
		TraverseToFollowingKey traverseToFollowingKey(final int atIndex) throws Exception {
			return new TraverseToFollowingKey(atIndex);
		}

		class TraverseToFollowingKey {
			public BplusNode FoundInLeaf = null;
			public String KeyFound = null;

			public TraverseToFollowingKey(final int atIndex) throws Exception {
				FoundInLeaf = null;
				KeyFound = null;
				boolean LookInParent = false;
				if (isLeaf)
					LookInParent = atIndex >= Size || ChildKeys[atIndex] == null;
				else
					LookInParent = atIndex > Size || atIndex > 0 && ChildKeys[atIndex - 1] == null;
				if (LookInParent) // if it's anywhere it's in the next child of parent
					if (parent != null && indexInParent >= 0) {
						final TraverseToFollowingKey t = parent.traverseToFollowingKey(indexInParent + 1);//, out FoundInLeaf, out KeyFound);
						FoundInLeaf = t.FoundInLeaf;
						KeyFound = t.KeyFound;
						return;
					} else
						return; // no such following key
				if (isLeaf) {
					// leaf, we found it.
					FoundInLeaf = BplusNode.this;
					KeyFound = ChildKeys[atIndex];
					return;
				} else // nonleaf, look in child (if there is one)
				if (atIndex == 0 || ChildKeys[atIndex - 1] != null) {
					final BplusNode thechild = MaterializeNodeAtIndex(atIndex);
					final TraverseToFollowingKey t = thechild.traverseToFollowingKey(0); //, out FoundInLeaf, out KeyFound);this.FoundInLeaf = t.FoundInLeaf;
					KeyFound = t.KeyFound;
					FoundInLeaf = t.FoundInLeaf;
				}
			}
		}

		public boolean FindMatch(final String CompareKey) //, out long ValueFound) 
				throws Exception {
			LastValueFound = 0; // dummy value on failure
			BplusNode leaf;
			final FindAtOrNextPositionInLeaf f = new FindAtOrNextPositionInLeaf(CompareKey, false);
			leaf = f.inLeaf;
			final int position = f.atPosition;
			if (position < leaf.Size) {
				final String key = leaf.ChildKeys[position];
				if (key != null && owner.Compare(key, CompareKey) == 0) //(key.equals(CompareKey)
				{
					LastValueFound = leaf.ChildBufferNumbers[position];
					return true;
				}
			}
			return false;
		}

		public String FindNextKey(final String CompareKey) throws Exception {
			String result = null;
			BplusNode leaf;
			final FindAtOrNextPositionInLeaf f = new FindAtOrNextPositionInLeaf(CompareKey, true);
			leaf = f.inLeaf;
			final int position = f.atPosition;
			if (position >= leaf.Size || leaf.ChildKeys[position] == null) {
				// try to traverse to the right.
				BplusNode newleaf;
				final TraverseToFollowingKey t = leaf.traverseToFollowingKey(leaf.Size); //, out newleaf, out result);
				newleaf = t.FoundInLeaf;
				result = t.KeyFound;
			} else
				result = leaf.ChildKeys[position];
			return result;
		}

		/// Find near-index of comparekey in leaf under this node. 
		/// <param name="CompareKey">the key to look for</param>
		/// <param name="inLeaf">the leaf where found</param>
		/// <param name="LookPastOnly">If true then only look for a greater value, not an exact match.</param>
		/// <returns>index of match in leaf</returns>
		FindAtOrNextPositionInLeaf findAtOrNextPositionInLeaf(final String CompareKey, final boolean LookPastOnly)
				throws Exception {
			return new FindAtOrNextPositionInLeaf(CompareKey, LookPastOnly);
		}

		class FindAtOrNextPositionInLeaf {
			public BplusNode inLeaf;
			public int atPosition;

			public FindAtOrNextPositionInLeaf(final String CompareKey, final boolean LookPastOnly) throws Exception {
				final int myposition = FindAtOrNextPosition(CompareKey, LookPastOnly);
				if (isLeaf) {
					inLeaf = BplusNode.this;
					atPosition = myposition;
					return;
				}
				final long childBufferNumber = ChildBufferNumbers[myposition];
				if (childBufferNumber == BplusTreeLong.NULLBUFFERNUMBER)
					throw new BplusTreeException("can't search null subtree");
				final BplusNode child = MaterializeNodeAtIndex(myposition);
				final FindAtOrNextPositionInLeaf f = child.findAtOrNextPositionInLeaf(CompareKey, LookPastOnly);
				inLeaf = f.inLeaf;
				atPosition = f.atPosition;
			}
		}

		BplusNode MaterializeNodeAtIndex(final int myposition) throws Exception {
			if (isLeaf) throw new BplusTreeException("cannot materialize child for leaf");
			final long childBufferNumber = ChildBufferNumbers[myposition];
			if (childBufferNumber == BplusTreeLong.NULLBUFFERNUMBER)
				throw new BplusTreeException("can't search null subtree at position " + myposition + " in "
						+ myBufferNumber);
			// is it already materialized?
			BplusNode result = MaterializedChildNodes[myposition];
			if (result != null) return result;
			// otherwise read it in...
			result = new BplusNode(owner, this, myposition, true); // dummy isLeaf value
			result.LoadFromBuffer(childBufferNumber);
			MaterializedChildNodes[myposition] = result;
			// no longer terminal
			owner.ForgetTerminalNode(this);
			return result;
		}

		public void LoadFromBuffer(final long bufferNumber) throws Exception {
			// freelist bookkeeping done elsewhere
			String parentinfo = "no parent"; // debug
			if (parent != null) parentinfo = "parent=" + parent.myBufferNumber; // debug
			final byte[] rawdata = new byte[owner.buffersize];
			owner.buffers.getBuffer(bufferNumber, rawdata, 0, rawdata.length);
			Load(rawdata);
			Dirty = false;
			myBufferNumber = bufferNumber;
			// it's terminal until a child is materialized
			owner.RecordTerminalNode(this);
		}

		public long DumpToFreshBuffer() throws Exception {
			final long oldbuffernumber = myBufferNumber;
			final long freshBufferNumber = owner.allocateBuffer();
			DumpToBuffer(freshBufferNumber);
			if (oldbuffernumber != BplusTreeLong.NULLBUFFERNUMBER) {
				final Long L = new Long(oldbuffernumber);
				if (owner.FreeBuffersOnAbort.containsKey(L)) {
					// free it now
					owner.FreeBuffersOnAbort.remove(L);
					owner.deallocateBuffer(oldbuffernumber);
				} else
					// free on commit
					owner.FreeBuffersOnCommit.put(L, L);
			}
			final Long F = new Long(freshBufferNumber);
			owner.FreeBuffersOnAbort.put(F, F);
			return freshBufferNumber;
		}

		void DumpToBuffer(final long buffernumber) throws Exception {
			final byte[] rawdata = new byte[owner.buffersize];
			Dump(rawdata);
			owner.buffers.setBuffer(buffernumber, rawdata, 0, rawdata.length);
			Dirty = false;
			myBufferNumber = buffernumber;
			if (parent != null && indexInParent >= 0 && parent.ChildBufferNumbers[indexInParent] != buffernumber) {
				if (parent.MaterializedChildNodes[indexInParent] != this)
					throw new BplusTreeException("invalid parent connection " + parent.myBufferNumber + " at "
							+ indexInParent);
				parent.ChildBufferNumbers[indexInParent] = buffernumber;
				parent.Soil();
			}
		}

		void reParentAllChildren() throws Exception {
			for (int i = 0; i <= Size; i++) {
				final BplusNode thisnode = MaterializedChildNodes[i];
				if (thisnode != null) thisnode.Reparent(this, i);
			}
		}

		/// Delete entry for key
		/// <param name="key">key to delete</param>
		/// <param name="MergeMe">true if the node is less than half full after deletion</param>
		/// <returns>null unless the smallest key under this node has changed in which case it returns the smallest key.</returns>
		Delete delete(final String Key) throws Exception {
			return new Delete(Key);
		}

		public class Delete {
			public String smallestKey;
			public boolean MergeMe;

			public Delete(final String key) throws Exception {
				MergeMe = false; // assumption
				smallestKey = null;
				if (isLeaf) {
					DeleteLeaf(key);
					return;
				}
				final int deleteposition = FindAtOrNextPosition(key, false);
				final long deleteBufferNumber = ChildBufferNumbers[deleteposition];
				if (deleteBufferNumber == BplusTreeLong.NULLBUFFERNUMBER)
					throw new BplusTreeException("key not followed by buffer number in non-leaf (del)");
				// del in subtree
				final BplusNode DeleteChild = MaterializeNodeAtIndex(deleteposition);
				boolean MergeKid;
				final Delete deletion = DeleteChild.delete(key);
				MergeKid = deletion.MergeMe;
				final String delresult = deletion.smallestKey;
				// delete succeeded... now fix up the child node if needed.
				Soil(); // redundant ?
				// bizarre special case for 2-3  or 3-4 trees -- empty leaf
				if (delresult != null && owner.Compare(delresult, key) == 0) // delresult.equals(key)
				{
					if (Size > 3)
						throw new BplusTreeException(
								"assertion error: delete returned delete key for too large node size: "
										+ Size);
					// junk this leaf and shift everything over
					if (deleteposition == 0)
						smallestKey = ChildKeys[deleteposition];
					else if (deleteposition == Size)
						ChildKeys[deleteposition - 1] = null;
					else
						ChildKeys[deleteposition - 1] = ChildKeys[deleteposition];
					if (smallestKey != null && owner.Compare(smallestKey, key) == 0) // result.equals(key)
					{
						// I'm not sure this ever happens
						MaterializeNodeAtIndex(1);
						smallestKey = MaterializedChildNodes[1].LeastKey();
					}
					DeleteChild.Free();
					for (int i = deleteposition; i < Size - 1; i++) {
						ChildKeys[i] = ChildKeys[i + 1];
						MaterializedChildNodes[i] = MaterializedChildNodes[i + 1];
						ChildBufferNumbers[i] = ChildBufferNumbers[i + 1];
					}
					ChildKeys[Size - 1] = null;
					if (deleteposition < Size) {
						MaterializedChildNodes[Size - 1] = MaterializedChildNodes[Size];
						ChildBufferNumbers[Size - 1] = ChildBufferNumbers[Size];
					}
					MaterializedChildNodes[Size] = null;
					ChildBufferNumbers[Size] = BplusTreeLong.NULLBUFFERNUMBER;
					MergeMe = SizeInUse() < Size / 2;
					reParentAllChildren();
					//return result;
					return;
				}
				if (deleteposition == 0)
					// smallest key may have changed.
					smallestKey = delresult;
				else if (delresult != null && deleteposition > 0)
					if (owner.Compare(delresult, key) != 0) ChildKeys[deleteposition - 1] = delresult;
				// if the child needs merging... do it
				if (MergeKid) {
					int leftindex, rightindex;
					BplusNode leftNode;
					BplusNode rightNode;
					String keyBetween;
					if (deleteposition == 0) {
						// merge with next
						leftindex = deleteposition;
						rightindex = deleteposition + 1;
						leftNode = DeleteChild;
						rightNode = MaterializeNodeAtIndex(rightindex);
					} else {
						// merge with previous
						leftindex = deleteposition - 1;
						rightindex = deleteposition;
						leftNode = MaterializeNodeAtIndex(leftindex);
						rightNode = DeleteChild;
					}
					keyBetween = ChildKeys[leftindex];
					String rightLeastKey;
					boolean DeleteRight;
					rightLeastKey = Merge(leftNode, keyBetween, rightNode);
					DeleteRight = !rightNode.isValid;
					// delete the right node if needed.
					if (DeleteRight) {
						for (int i = rightindex; i < Size; i++) {
							ChildKeys[i - 1] = ChildKeys[i];
							ChildBufferNumbers[i] = ChildBufferNumbers[i + 1];
							MaterializedChildNodes[i] = MaterializedChildNodes[i + 1];
						}
						ChildKeys[Size - 1] = null;
						MaterializedChildNodes[Size] = null;
						ChildBufferNumbers[Size] = BplusTreeLong.NULLBUFFERNUMBER;
						reParentAllChildren();
						rightNode.Free();
						// does this node need merging?
						if (SizeInUse() < Size / 2) MergeMe = true;
					} else
						// update the key entry
						ChildKeys[rightindex - 1] = rightLeastKey;
				}
			}

			public String DeleteLeaf(final String key) throws Exception {
				smallestKey = null;
				MergeMe = false;
				boolean found = false;
				int deletelocation = 0;
				for (int i = 0; i < ChildKeys.length; i++) {
					final String thiskey = ChildKeys[i];
					// use comparison, not equals, in case different Strings sometimes compare same
					if (thiskey != null && owner.Compare(thiskey, key) == 0) {
						found = true;
						deletelocation = i;
						break;
					}
				}
				if (!found) throw new BplusTreeKeyMissing("cannot delete missing key: " + key);
				Soil();
				// only keys are important...
				for (int i = deletelocation; i < Size - 1; i++) {
					ChildKeys[i] = ChildKeys[i + 1];
					ChildBufferNumbers[i] = ChildBufferNumbers[i + 1];
				}
				ChildKeys[Size - 1] = null;
				if (SizeInUse() < Size / 2) MergeMe = true;
				if (deletelocation == 0) {
					smallestKey = ChildKeys[0];
					// this is only relevant for the case of 2-3 trees (empty leaf after deletion)
					if (smallestKey == null) smallestKey = key; // deleted value
				}
				return smallestKey;
			}
		}

		String LeastKey() throws Exception {
			String result = null;
			if (isLeaf)
				result = ChildKeys[0];
			else {
				MaterializeNodeAtIndex(0);
				result = MaterializedChildNodes[0].LeastKey();
			}
			if (result == null) throw new BplusTreeException("no key found");
			return result;
		}

		public static String Merge(final BplusNode left, final String KeyBetween, final BplusNode right)
				throws Exception {
			if (left.isLeaf || right.isLeaf) {
				if (!(left.isLeaf && right.isLeaf)) throw new BplusTreeException("can't merge leaf with non-leaf");
				MergeLeaves(left, right);
				final String rightLeastKey = right.ChildKeys[0];
				return rightLeastKey;
			}
			// merge non-leaves
			final String[] allkeys = new String[left.Size * 2 + 1];
			final long[] allseeks = new long[left.Size * 2 + 2];
			final BplusNode[] allMaterialized = new BplusNode[left.Size * 2 + 2];
			if (left.ChildBufferNumbers[0] == BplusTreeLong.NULLBUFFERNUMBER
					|| right.ChildBufferNumbers[0] == BplusTreeLong.NULLBUFFERNUMBER)
				throw new BplusTreeException("cannot merge empty non-leaf with non-leaf");
			int index = 0;
			allseeks[0] = left.ChildBufferNumbers[0];
			allMaterialized[0] = left.MaterializedChildNodes[0];
			for (int i = 0; i < left.Size; i++) {
				if (left.ChildKeys[i] == null) break;
				allkeys[index] = left.ChildKeys[i];
				allseeks[index + 1] = left.ChildBufferNumbers[i + 1];
				allMaterialized[index + 1] = left.MaterializedChildNodes[i + 1];
				index++;
			}
			allkeys[index] = KeyBetween;
			index++;
			allseeks[index] = right.ChildBufferNumbers[0];
			allMaterialized[index] = right.MaterializedChildNodes[0];
			int rightcount = 0;
			for (int i = 0; i < right.Size; i++) {
				if (right.ChildKeys[i] == null) break;
				allkeys[index] = right.ChildKeys[i];
				allseeks[index + 1] = right.ChildBufferNumbers[i + 1];
				allMaterialized[index + 1] = right.MaterializedChildNodes[i + 1];
				index++;
				rightcount++;
			}
			if (index <= left.Size) {
				// it will all fit in one node
				right.isValid = false;
				for (int i = 0; i < index; i++) {
					left.ChildKeys[i] = allkeys[i];
					left.ChildBufferNumbers[i] = allseeks[i];
					left.MaterializedChildNodes[i] = allMaterialized[i];
				}
				left.ChildBufferNumbers[index] = allseeks[index];
				left.MaterializedChildNodes[index] = allMaterialized[index];
				left.reParentAllChildren();
				left.Soil();
				right.Free();
				return null;
			}
			// otherwise split the content between the nodes
			left.Clear();
			right.Clear();
			left.Soil();
			right.Soil();
			final int leftcontent = index / 2;
			final int rightcontent = index - leftcontent - 1;
			String rightLeastKey = allkeys[leftcontent];
			int outputindex = 0;
			for (int i = 0; i < leftcontent; i++) {
				left.ChildKeys[i] = allkeys[outputindex];
				left.ChildBufferNumbers[i] = allseeks[outputindex];
				left.MaterializedChildNodes[i] = allMaterialized[outputindex];
				outputindex++;
			}
			rightLeastKey = allkeys[outputindex];
			left.ChildBufferNumbers[outputindex] = allseeks[outputindex];
			left.MaterializedChildNodes[outputindex] = allMaterialized[outputindex];
			outputindex++;
			rightcount = 0;
			for (int i = 0; i < rightcontent; i++) {
				right.ChildKeys[i] = allkeys[outputindex];
				right.ChildBufferNumbers[i] = allseeks[outputindex];
				right.MaterializedChildNodes[i] = allMaterialized[outputindex];
				outputindex++;
				rightcount++;
			}
			right.ChildBufferNumbers[rightcount] = allseeks[outputindex];
			right.MaterializedChildNodes[rightcount] = allMaterialized[outputindex];
			left.reParentAllChildren();
			right.reParentAllChildren();
			return rightLeastKey;
		}

		public static void MergeLeaves(final BplusNode left, final BplusNode right) throws Exception {
			final String[] allkeys = new String[left.Size * 2];
			final long[] allseeks = new long[left.Size * 2];
			int index = 0;
			for (int i = 0; i < left.Size; i++) {
				if (left.ChildKeys[i] == null) break;
				allkeys[index] = left.ChildKeys[i];
				allseeks[index] = left.ChildBufferNumbers[i];
				index++;
			}
			for (int i = 0; i < right.Size; i++) {
				if (right.ChildKeys[i] == null) break;
				allkeys[index] = right.ChildKeys[i];
				allseeks[index] = right.ChildBufferNumbers[i];
				index++;
			}
			if (index <= left.Size) {
				left.Clear();
				//DeleteRight = true;
				right.isValid = false;
				for (int i = 0; i < index; i++) {
					left.ChildKeys[i] = allkeys[i];
					left.ChildBufferNumbers[i] = allseeks[i];
				}
				right.Free();
				left.Soil();
				return;
			}
			left.Clear();
			right.Clear();
			left.Soil();
			right.Soil();
			final int rightcontent = index / 2;
			final int leftcontent = index - rightcontent;
			int newindex = 0;
			for (int i = 0; i < leftcontent; i++) {
				left.ChildKeys[i] = allkeys[newindex];
				left.ChildBufferNumbers[i] = allseeks[newindex];
				newindex++;
			}
			for (int i = 0; i < rightcontent; i++) {
				right.ChildKeys[i] = allkeys[newindex];
				right.ChildBufferNumbers[i] = allseeks[newindex];
				newindex++;
			}
		}

		/// insert key/position entry in self 
		/// <param name="key">Key to associate with the leaf</param>
		/// <param name="position">position associated with key in external structur</param>
		/// <param name="splitString">if not null then the smallest key in the new split leaf</param>
		/// <param name="splitNode">if not null then the node was split and this is the leaf to the right.</param>
		/// <returns>null unless the smallest key under this node has changed, in which case it returns the smallest key.</returns>
		public String Insert(final String key, final long position) throws Exception {
			splitString = null;
			splitNode = null;
			if (isLeaf) return InsertLeaf(key, position); //, out splitString, out splitNode);
			final int insertposition = FindAtOrNextPosition(key, false);
			final long insertBufferNumber = ChildBufferNumbers[insertposition];
			if (insertBufferNumber == BplusTreeLong.NULLBUFFERNUMBER)
				throw new BplusTreeException("key not followed by buffer number in non-leaf");
			// insert in subtree
			final BplusNode InsertChild = MaterializeNodeAtIndex(insertposition);
			BplusNode childSplit;
			String childSplitString;
			final String childInsert = InsertChild.Insert(key, position); //, out childSplitString, out childSplit);
			childSplit = InsertChild.splitNode;
			childSplitString = InsertChild.splitString;
			InsertChild.splitNode = null;
			InsertChild.splitString = null;
			// if there was a split the node must expand
			if (childSplit != null) {
				// insert the child
				Soil(); // redundant -- a child will have a change so this node will need to be copied
				final int newChildPosition = insertposition + 1;
				boolean dosplit = false;
				// if there is no free space we must do a split
				if (ChildBufferNumbers[Size] != BplusTreeLong.NULLBUFFERNUMBER) {
					dosplit = true;
					PrepareForSplit();
				}
				// bubble over the current values to make space for new child
				for (int i = ChildKeys.length - 2; i >= newChildPosition - 1; i--) {
					final int i1 = i + 1;
					final int i2 = i1 + 1;
					ChildKeys[i1] = ChildKeys[i];
					ChildBufferNumbers[i2] = ChildBufferNumbers[i1];
					final BplusNode childNode = MaterializedChildNodes[i2] = MaterializedChildNodes[i1];
				}
				// record the new child
				ChildKeys[newChildPosition - 1] = childSplitString;
				childSplit.Reparent(this, newChildPosition);
				// split, if needed
				if (dosplit) {
					final int splitpoint = MaterializedChildNodes.length / 2 - 1;
					splitString = ChildKeys[splitpoint];
					splitNode = new BplusNode(owner, parent, -1, isLeaf);
					// make copy of expanded node structure
					final BplusNode[] materialized = MaterializedChildNodes;
					final long[] buffernumbers = ChildBufferNumbers;
					final String[] keys = ChildKeys;
					// repair the expanded node
					ChildKeys = new String[Size];
					MaterializedChildNodes = new BplusNode[Size + 1];
					ChildBufferNumbers = new long[Size + 1];
					Clear();
					for (int i = 0; i < splitpoint + 1; i++) {
						MaterializedChildNodes[i] = materialized[i];
						ChildBufferNumbers[i] = buffernumbers[i];
					}
					//Array.Copy(keys, 0, this.ChildKeys, 0, splitpoint);
					for (int i = 0; i < splitpoint; i++)
						ChildKeys[i] = keys[i];
					// initialize the new node
					splitNode.Clear(); // redundant.
					final int remainingKeys = Size - splitpoint;
					for (int i = 0; i < remainingKeys + 1; i++) {
						splitNode.MaterializedChildNodes[i] = materialized[i + splitpoint + 1];
						splitNode.ChildBufferNumbers[i] = buffernumbers[i + splitpoint + 1];
					}
					for (int i = 0; i < remainingKeys; i++)
						splitNode.ChildKeys[i] = keys[i + splitpoint + 1];
					// fix pointers in materialized children of splitnode
					splitNode.reParentAllChildren();
					// store the new node
					splitNode.DumpToFreshBuffer();
					splitNode.CheckIfTerminal();
					splitNode.Soil();
					CheckIfTerminal();
				}
				// fix pointers in children
				reParentAllChildren();
			}
			if (insertposition == 0) // the smallest key may have changed
				return childInsert;
			return null; // no change in smallest key
		}

		/// <summary>
		/// Check to see if this is a terminal node, if so record it, otherwise forget it
		/// </summary>
		void CheckIfTerminal() throws Exception {
			if (!isLeaf) for (int i = 0; i < Size + 1; i++)
				if (MaterializedChildNodes[i] != null) {
					owner.ForgetTerminalNode(this);
					return;
				}
			owner.RecordTerminalNode(this);
		}

		/// <summary>
		/// insert key/position entry in self (as leaf)
		/// </summary>
		/// <param name="key">Key to associate with the leaf</param>
		/// <param name="position">position associated with key in external structure</param>
		/// <param name="splitString">if not null then the smallest key in the new split leaf</param>
		/// <param name="splitNode">if not null then the node was split and this is the leaf to the right.</param>
		/// <returns>smallest key value in keys, or null if no change</returns>
		public String InsertLeaf(String key, long position) //, out String splitString, out BplusNode splitNode) 
				throws Exception {
			splitString = null;
			splitNode = null;
			boolean dosplit = false;
			if (!isLeaf) throw new BplusTreeException("bad call to InsertLeaf: this is not a leaf");
			Soil();
			int insertposition = FindAtOrNextPosition(key, false);
			if (insertposition >= Size) {
				//throw new BplusTreeException("key too big and leaf is full");
				dosplit = true;
				PrepareForSplit();
			} else // if it's already there then change the value at the current location (duplicate entries not supported).
			if (ChildKeys[insertposition] == null || owner.Compare(ChildKeys[insertposition], key) == 0) {
				ChildBufferNumbers[insertposition] = position;
				ChildKeys[insertposition] = key;
				if (insertposition == 0)
					return key;
				else
					return null;
			}
			// check for a null position
			int nullindex = insertposition;
			while (nullindex < ChildKeys.length && ChildKeys[nullindex] != null)
				nullindex++;
			if (nullindex >= ChildKeys.length) {
				if (dosplit) throw new BplusTreeException("can't split twice!!");
				dosplit = true;
				PrepareForSplit();
			}
			// bubble in the new info XXXX THIS SHOULD BUBBLE BACKWARDS	
			String nextkey = ChildKeys[insertposition];
			long nextposition = ChildBufferNumbers[insertposition];
			ChildKeys[insertposition] = key;
			ChildBufferNumbers[insertposition] = position;
			while (nextkey != null) {
				key = nextkey;
				position = nextposition;
				insertposition++;
				nextkey = ChildKeys[insertposition];
				nextposition = ChildBufferNumbers[insertposition];
				ChildKeys[insertposition] = key;
				ChildBufferNumbers[insertposition] = position;
			}
			// split if needed
			if (dosplit) {
				final int splitpoint = ChildKeys.length / 2;
				final int splitlength = ChildKeys.length - splitpoint;
				splitNode = new BplusNode(owner, parent, -1, isLeaf);
				// copy the split info into the splitNode
				for (int i = 0; i < splitlength; i++) {
					splitNode.ChildBufferNumbers[i] = ChildBufferNumbers[i + splitpoint];
					splitNode.ChildKeys[i] = ChildKeys[i + splitpoint];
					splitNode.MaterializedChildNodes[i] = MaterializedChildNodes[i + splitpoint];
				}
				splitString = splitNode.ChildKeys[0];
				// archive the new node
				splitNode.DumpToFreshBuffer();
				// store the node data temporarily
				final long[] buffernumbers = ChildBufferNumbers;
				final String[] keys = ChildKeys;
				final BplusNode[] nodes = MaterializedChildNodes;
				// repair current node, copy in the other part of the split
				ChildBufferNumbers = new long[Size + 1];
				ChildKeys = new String[Size];
				MaterializedChildNodes = new BplusNode[Size + 1];
				for (int i = 0; i < splitpoint; i++) {
					ChildBufferNumbers[i] = buffernumbers[i];
					ChildKeys[i] = keys[i];
					MaterializedChildNodes[i] = nodes[i];
				}
				for (int i = splitpoint; i < ChildKeys.length; i++) {
					ChildKeys[i] = null;
					ChildBufferNumbers[i] = BplusTreeLong.NULLBUFFERNUMBER;
					MaterializedChildNodes[i] = null;
				}
				// store the new node
				owner.RecordTerminalNode(splitNode);
				splitNode.Soil();
			}
			if (insertposition == 0)
				return key; // smallest key changed.
			else
				return null; // no change in smallest key
		}

		/// Grow to this.size+1 in preparation for insertion and split
		void PrepareForSplit() throws Exception {
			final int supersize = Size + 1;
			final long[] positions = new long[supersize + 1];
			final String[] keys = new String[supersize];
			final BplusNode[] materialized = new BplusNode[supersize + 1];
			for (int i = 0; i < Size; i++) {
				keys[i] = ChildKeys[i];
				positions[i] = ChildBufferNumbers[i];
				materialized[i] = MaterializedChildNodes[i];
			}
			positions[Size] = ChildBufferNumbers[Size];
			positions[Size + 1] = BplusTreeLong.NULLBUFFERNUMBER;
			keys[Size] = null;
			materialized[Size] = MaterializedChildNodes[Size];
			materialized[Size + 1] = null;
			ChildBufferNumbers = positions;
			ChildKeys = keys;
			MaterializedChildNodes = materialized;
		}

		public void Load(final byte[] buffer) throws Exception {
			// load serialized data
			// indicator | seek position | [ key storage | seek position ]*
			Clear();
			if (buffer.length != owner.buffersize)
				throw new BplusTreeException("bad buffer size " + buffer.length + " should be " + owner.buffersize);
			final byte indicator = buffer[0];
			isLeaf = false;
			if (indicator == BplusTreeLong.LEAF)
				isLeaf = true;
			else if (indicator != BplusTreeLong.NONLEAF)
				throw new BplusTreeException("bad indicator, not leaf or nonleaf in tree " + indicator);
			int index = 1;
			// get the first seek position
			//System.Text.Decoder decode = System.Text.Encoding.UTF8.GetDecoder();
			index += BufferFile.LONGSTORAGE;
			final int maxKeyLength = owner.KeyLength;
			final int maxKeyPayload = maxKeyLength - BufferFile.SHORTSTORAGE;
			// get remaining key storages and seek positions
			String lastkey = "";
			for (int KeyIndex = 0; KeyIndex < Size; KeyIndex++) {
				// decode and store a key
				final short keylength = BufferFile.RetrieveShort(buffer, index);
				if (keylength < -1 || keylength > maxKeyPayload)
					throw new BplusTreeException("invalid keylength decoded");
				index += BufferFile.SHORTSTORAGE;
				String key = null;
				if (keylength == 0) key = "";
				if (keylength > 0) key = new String(buffer, index, keylength, "UTF-8");
				ChildKeys[KeyIndex] = key;
				index += maxKeyPayload;
				// decode and store a seek position
				final long seekPosition = BufferFile.RetrieveLong(buffer, index);
				if (!isLeaf)
					if (key == null & seekPosition != BplusTreeLong.NULLBUFFERNUMBER)
						throw new BplusTreeException("key is null but position is not " + KeyIndex);
					else if (lastkey == null && key != null)
						throw new BplusTreeException("null key followed by non-null key " + KeyIndex);
				lastkey = key;
				ChildBufferNumbers[KeyIndex + 1] = seekPosition;
				index += BufferFile.LONGSTORAGE;
			}
		}

		/// check that key is ok for node of this size (put here for locality of relevant code).
		/// <param name="key">key to check</param>
		/// <param name="owner">tree to contain node containing the key</param>
		/// <returns>true if key is ok</returns>
		public static boolean KeyOK(final String key, final BplusTreeLong owner) throws Exception {
			if (key == null) return false;
			final int maxKeyLength = owner.KeyLength;
			final int maxKeyPayload = maxKeyLength - BufferFile.SHORTSTORAGE;
			final byte[] keyBytes = key.getBytes("UTF-8");
			final int charCount = keyBytes.length;
			if (charCount > maxKeyPayload) return false;
			return true;
		}

		public void Dump(final byte[] buffer) throws Exception {
			// indicator | seek position | [ key storage | seek position ]*
			if (buffer.length != owner.buffersize)
				throw new BplusTreeException("bad buffer size " + buffer.length + " should be " + owner.buffersize);
			buffer[0] = BplusTreeLong.NONLEAF;
			if (isLeaf) buffer[0] = BplusTreeLong.LEAF;
			int index = 1;
			// store first seek position
			BufferFile.Store(ChildBufferNumbers[0], buffer, index);
			index += BufferFile.LONGSTORAGE;
			// store remaining keys and seeks
			final int maxKeyLength = owner.KeyLength;
			final int maxKeyPayload = maxKeyLength - BufferFile.SHORTSTORAGE;
			String lastkey = "";
			for (int KeyIndex = 0; KeyIndex < Size; KeyIndex++) {
				// store a key
				final String theKey = ChildKeys[KeyIndex];
				short charCount = -1;
				if (theKey != null) {
					final byte[] keyBytes = theKey.getBytes("UTF-8");
					charCount = (short) keyBytes.length;
					if (charCount > maxKeyPayload)
						throw new BplusTreeException("String bytes to large for use as key " + charCount + ">"
								+ maxKeyPayload);
					BufferFile.Store(charCount, buffer, index);
					index += BufferFile.SHORTSTORAGE;
					for (int i = 0; i < keyBytes.length; i++)
						buffer[index + i] = keyBytes[i];
				} else {
					// null case (no String to read)
					BufferFile.Store(charCount, buffer, index);
					index += BufferFile.SHORTSTORAGE;
				}
				index += maxKeyPayload;
				// store a seek
				final long seekPosition = ChildBufferNumbers[KeyIndex + 1];
				if (theKey == null && seekPosition != BplusTreeLong.NULLBUFFERNUMBER && !isLeaf)
					throw new BplusTreeException("null key paired with non-null location " + KeyIndex);
				if (lastkey == null && theKey != null)
					throw new BplusTreeException("null key followed by non-null key " + KeyIndex);
				lastkey = theKey;
				BufferFile.Store(seekPosition, buffer, index);
				index += BufferFile.LONGSTORAGE;
			}
		}

		// Close the node: invalidate all children, store state if needed, remove materialized self from parent.
		public long Invalidate(final boolean destroyRoot) throws Exception {
			long result = myBufferNumber;
			if (!isLeaf) // need to invalidate kids
				for (int i = 0; i < Size + 1; i++)
					if (MaterializedChildNodes[i] != null) // new buffer numbers are recorded automatically
						ChildBufferNumbers[i] = MaterializedChildNodes[i].Invalidate(true);
			// store if dirty
			if (Dirty) result = DumpToFreshBuffer();
			// remove from owner archives if present
			owner.ForgetTerminalNode(this);
			// remove from parent
			if (parent != null && indexInParent >= 0) {
				parent.MaterializedChildNodes[indexInParent] = null;
				parent.ChildBufferNumbers[indexInParent] = result; // should be redundant
				parent.CheckIfTerminal();
				indexInParent = -1;
			}
			// render all structures useless, just in case...
			if (destroyRoot) Destroy();
			return result;
		}

		// Mark this as dirty and all ancestors too.
		void Soil() throws Exception {
			if (Dirty) return; // don't need to do it again
			Dirty = true;
			if (parent != null) parent.Soil();
		}
	}
}