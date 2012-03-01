package b_plus_tree;

import java.util.Enumeration;
import java.util.Hashtable;

/// <summary>
/// Summary description for BplusTreeBytes.
/// </summary>
public class BplusTreeBytes implements IByteTree {
	BplusTreeLong tree;
	LinkedFile archive;
	Hashtable FreeChunksOnCommit = new Hashtable();
	Hashtable FreeChunksOnAbort = new Hashtable();
	static int DEFAULTBLOCKSIZE = 1024;
	static int DEFAULTNODESIZE = 32;

	public BplusTreeBytes(final BplusTreeLong tree, final LinkedFile archive) {
		this.tree = tree;
		this.archive = archive;
	}

	public static java.io.RandomAccessFile makeFile(final String path) throws Exception {

		final java.io.File f = new java.io.File(path);
		if (f.exists()) //System.out.println("<br>				DELETING FILE "+path);
			f.delete();
		return new java.io.RandomAccessFile(path, "rw");
	}

	public static BplusTreeBytes Initialize(final String treefileName, final String blockfileName, final int KeyLength,
			final int CultureId, final int nodesize, final int buffersize) throws Exception {
		//java.io.RandomAccessFile treefile = new System.IO.FileStream(treefileName, System.IO.FileMode.CreateNew, 
		//	System.IO.FileAccess.ReadWrite);
		//java.io.RandomAccessFile blockfile = new System.IO.FileStream(blockfileName, System.IO.FileMode.CreateNew, 
		//	System.IO.FileAccess.ReadWrite);
		final java.io.RandomAccessFile treefile = makeFile(treefileName);
		final java.io.RandomAccessFile blockfile = makeFile(blockfileName);
		return Initialize(treefile, blockfile, KeyLength, CultureId, nodesize, buffersize);
	}

	public static BplusTreeBytes Initialize(final String treefileName, final String blockfileName, final int KeyLength,
			final int CultureId) throws Exception {
		final java.io.RandomAccessFile treefile = makeFile(treefileName);
		final java.io.RandomAccessFile blockfile = makeFile(blockfileName);
		return Initialize(treefile, blockfile, KeyLength, CultureId);
	}

	public static BplusTreeBytes Initialize(final String treefileName, final String blockfileName, final int KeyLength)
			throws Exception {
		final java.io.RandomAccessFile treefile = makeFile(treefileName);
		final java.io.RandomAccessFile blockfile = makeFile(blockfileName);
		return Initialize(treefile, blockfile, KeyLength);
	}

	public static BplusTreeBytes Initialize(final java.io.RandomAccessFile treefile,
			final java.io.RandomAccessFile blockfile, final int KeyLength, final int CultureId, final int nodesize,
			final int buffersize) throws Exception {
		final BplusTreeLong tree = BplusTreeLong.InitializeInStream(treefile, KeyLength, nodesize, CultureId);
		final LinkedFile archive = LinkedFile.InitializeLinkedFileInStream(blockfile, buffersize);
		return new BplusTreeBytes(tree, archive);
	}

	public static BplusTreeBytes Initialize(final java.io.RandomAccessFile treefile,
			final java.io.RandomAccessFile blockfile, final int KeyLength, final int CultureId) throws Exception {
		return Initialize(treefile, blockfile, KeyLength, CultureId, DEFAULTNODESIZE, DEFAULTBLOCKSIZE);
	}

	public static BplusTreeBytes Initialize(final java.io.RandomAccessFile treefile,
			final java.io.RandomAccessFile blockfile, final int KeyLength) throws Exception {
		final int CultureId = BplusTreeLong.INVARIANTCULTUREID;
		return Initialize(treefile, blockfile, KeyLength, CultureId, DEFAULTNODESIZE, DEFAULTBLOCKSIZE);
	}

	public static BplusTreeBytes ReOpen(final java.io.RandomAccessFile treefile, final java.io.RandomAccessFile blockfile)
			throws Exception {
		final BplusTreeLong tree = BplusTreeLong.SetupFromExistingStream(treefile);
		final LinkedFile archive = LinkedFile.SetupFromExistingStream(blockfile);
		return new BplusTreeBytes(tree, archive);
	}

	public static BplusTreeBytes ReOpen(final String treefileName, final String blockfileName, final String access)
			throws Exception {
		//java.io.RandomAccessFile treefile = new System.IO.FileStream(treefileName, System.IO.FileMode.Open, 
		//	access);
		//java.io.RandomAccessFile blockfile = new System.IO.FileStream(blockfileName, System.IO.FileMode.Open, 
		//	access);
		final java.io.RandomAccessFile treefile = new java.io.RandomAccessFile(treefileName, access);
		final java.io.RandomAccessFile blockfile = new java.io.RandomAccessFile(blockfileName, access);
		return ReOpen(treefile, blockfile);
	}

	public static BplusTreeBytes ReOpen(final String treefileName, final String blockfileName) throws Exception {
		return ReOpen(treefileName, blockfileName, "rw");
	}

	public static BplusTreeBytes ReadOnly(final String treefileName, final String blockfileName) throws Exception {
		return ReOpen(treefileName, blockfileName, "r");
	}

	/// <summary>
	/// Use non-culture sensitive total order on binary Strings.
	/// </summary>
	public void NoCulture() {
	// not relevant to java implementation currently.
	//this.tree.DontUseCulture = true;
	//this.tree.cultureContext = null;
	}

	public int MaxKeyLength() throws Exception {
		return tree.MaxKeyLength();
	}

	//		#region ITreeIndex Members

	public int Compare(final String left, final String right) throws Exception {
		return tree.Compare(left, right);
	}

	public void Shutdown() throws Exception {
		tree.Shutdown();
		archive.Shutdown();
	}

	public void Recover(final boolean CorrectErrors) throws Exception {
		tree.Recover(CorrectErrors);
		final Hashtable ChunksInUse = new Hashtable();
		String key = tree.FirstKey();
		while (key != null) {
			final Long buffernumber = new Long(tree.get(key));
			if (ChunksInUse.containsKey(buffernumber))
				throw new BplusTreeException("buffer number " + buffernumber
						+ " associated with more than one key '" + key + "' and '"
						+ ChunksInUse.get(buffernumber) + "'");
			//ChunksInUse[buffernumber] = key;
			ChunksInUse.put(buffernumber, key);
			key = tree.NextKey(key);
		}
		// also consider the un-deallocated chunks to be in use
		//foreach (Object thing in this.FreeChunksOnCommit)
		//for (int i=0; i<this.FreeChunksOnCommit.size(); i++)
		for (final Enumeration e = FreeChunksOnCommit.keys(); e.hasMoreElements();) {
			final Long buffernumber = (Long) e.nextElement();
			//ChunksInUse[buffernumber] = "awaiting commit";
			ChunksInUse.put(buffernumber, "awaiting commit");
		}
		archive.Recover(ChunksInUse, CorrectErrors);
	}

	public void RemoveKey(final String key) throws Exception {
		final long map = tree.get(key);
		//this.archive.ReleaseBuffers(map);
		//this.FreeChunksOnCommit.Add(map);
		final Long M = new Long(map);
		if (FreeChunksOnAbort.containsKey(M)) {
			// free it now
			FreeChunksOnAbort.remove(M);
			archive.ReleaseBuffers(map);
		} else
			// free when committed
			FreeChunksOnCommit.put(M, M);
		tree.RemoveKey(key);
	}

	public String FirstKey() throws Exception {
		return tree.FirstKey();
	}

	public String NextKey(final String AfterThisKey) throws Exception {
		return tree.NextKey(AfterThisKey);
	}

	public boolean ContainsKey(final String key) throws Exception {
		return tree.ContainsKey(key);
	}

	public Object Get(final String key, final Object defaultValue) throws Exception {
		long map;
		if (tree.ContainsKey(key)) {
			map = tree.LastValueFound;
			return archive.GetChunk(map);
		}
		return defaultValue;
	}

	public void Set(final String key, final Object map) throws Exception {
		//			if (!(map is byte[]) )
		//			{
		//				throw new BplusTreeBadKeyValue("BplusTreeBytes can only archive byte array as value");
		//			}
		final byte[] thebytes = (byte[]) map;
		//this[key] = thebytes;
		set(key, thebytes);
	}

	public void set(final String key, final byte[] value) throws Exception {
		final long storage = archive.StoreNewChunk(value, 0, value.length);
		//this.FreeChunksOnAbort.add(new Long(storage));
		final Long S = new Long(storage);
		FreeChunksOnAbort.put(S, S);
		long valueFound;
		if (tree.ContainsKey(key)) {
			valueFound = tree.LastValueFound;
			//this.archive.ReleaseBuffers(valueFound);
			final Long F = new Long(valueFound);
			if (FreeChunksOnAbort.containsKey(F)) {
				// free it now
				FreeChunksOnAbort.remove(F);
				archive.ReleaseBuffers(valueFound);
			} else
				FreeChunksOnCommit.put(F, F);
		}
		//this.tree[key] = storage;
		tree.set(key, storage);
	}

	public byte[] get(final String key) throws Exception {
		final long map = tree.get(key);
		return archive.GetChunk(map);
	}

	public void Commit() throws Exception {
		// store all new bufferrs
		archive.Flush();
		// commit the tree
		tree.Commit();
		// at this point the new buffers have been committed, now free the old ones
		//this.FreeChunksOnCommit.Sort();
		//this.OnCommit.Reverse();
		//foreach (Object thing in this.FreeChunksOnCommit) 
		//for (int i=0; i<this.FreeChunksOnCommit.size(); i++)
		for (final Enumeration e = FreeChunksOnCommit.keys(); e.hasMoreElements();) {
			final long chunknumber = ((Long) e.nextElement()).longValue();
			archive.ReleaseBuffers(chunknumber);
		}
		archive.Flush();
		ClearBookKeeping();
	}

	public void Abort() throws Exception {
		//this.FreeChunksOnAbort.Sort();
		//this.FreeChunksOnAbort.Reverse();
		//foreach (Object thing in this.FreeChunksOnAbort) 
		//for (int i=0; i<this.FreeChunksOnAbort.size(); i++)
		for (final Enumeration e = FreeChunksOnAbort.keys(); e.hasMoreElements();) {
			final long chunknumber = ((Long) e.nextElement()).longValue();
			archive.ReleaseBuffers(chunknumber);
		}
		tree.Abort();
		archive.Flush();
		ClearBookKeeping();
	}

	public void SetFootPrintLimit(final int limit) throws Exception {
		tree.SetFootPrintLimit(limit);
	}

	void ClearBookKeeping() throws Exception {
		FreeChunksOnCommit.clear();
		FreeChunksOnAbort.clear();
	}

	//		#endregion

	//		public String toHtml() throws Exception
	//		{
	//			String treehtml = this.tree.toHtml();
	//			System.Text.StringBuilder sb = new System.Text.StringBuilder();
	//			sb.Append(treehtml);
	//			sb.Append("\r\n<br> free on commit "+this.FreeChunksOnCommit.Count+" ::");
	//			foreach (Object thing in this.FreeChunksOnCommit) 
	//			{
	//				sb.Append(" "+thing);
	//			}
	//			sb.Append("\r\n<br> free on abort "+this.FreeChunksOnAbort.Count+" ::");
	//			foreach (Object thing in this.FreeChunksOnAbort) 
	//			{
	//				sb.Append(" "+thing);
	//			}
	//			return sb.ToString(); // archive info not included
	//		}
}
