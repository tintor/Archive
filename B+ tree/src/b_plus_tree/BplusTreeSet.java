package b_plus_tree;

import java.util.Enumeration;
import java.util.Hashtable;

/// <summary>
/// Summary description for BplusTreeBytes.
/// </summary>
public class BplusTreeSet implements ITreeIndex {
	BplusTreeLong tree;
	Hashtable FreeChunksOnCommit = new Hashtable();
	Hashtable FreeChunksOnAbort = new Hashtable();
	static int DEFAULTBLOCKSIZE = 1024;
	static int DEFAULTNODESIZE = 32;

	@Override public Object Get(final String key, final Object defaultValue) throws Exception {
		throw new UnsupportedOperationException();
	}

	public BplusTreeSet(final BplusTreeLong tree) {
		this.tree = tree;
	}

	public static java.io.RandomAccessFile makeFile(final String path) throws Exception {

		final java.io.File f = new java.io.File(path);
		if (f.exists()) //System.out.println("<br>				DELETING FILE "+path);
			f.delete();
		return new java.io.RandomAccessFile(path, "rw");
	}

	public static BplusTreeSet Initialize(final String treefileName, final int KeyLength, final int CultureId,
			final int nodesize) throws Exception {
		final java.io.RandomAccessFile treefile = makeFile(treefileName);
		return Initialize(treefile, KeyLength, CultureId, nodesize);
	}

	public static BplusTreeSet Initialize(final String treefileName, final int KeyLength, final int CultureId)
			throws Exception {
		return Initialize(makeFile(treefileName), KeyLength, CultureId);
	}

	public static BplusTreeSet Initialize(final String treefileName, final int KeyLength) throws Exception {
		return Initialize(makeFile(treefileName), KeyLength);
	}

	public static BplusTreeSet Initialize(final java.io.RandomAccessFile treefile, final int KeyLength,
			final int CultureId, final int nodesize) throws Exception {
		final BplusTreeLong tree = BplusTreeLong.InitializeInStream(treefile, KeyLength, nodesize, CultureId);
		return new BplusTreeSet(tree);
	}

	public static BplusTreeSet Initialize(final java.io.RandomAccessFile treefile, final int KeyLength, final int CultureId)
			throws Exception {
		return Initialize(treefile, KeyLength, CultureId, DEFAULTNODESIZE);
	}

	public static BplusTreeSet Initialize(final java.io.RandomAccessFile treefile, final int KeyLength) throws Exception {
		final int CultureId = BplusTreeLong.INVARIANTCULTUREID;
		return Initialize(treefile, KeyLength, CultureId, DEFAULTNODESIZE);
	}

	public static BplusTreeSet ReOpen(final java.io.RandomAccessFile treefile) throws Exception {
		final BplusTreeLong tree = BplusTreeLong.SetupFromExistingStream(treefile);
		return new BplusTreeSet(tree);
	}

	public static BplusTreeSet ReOpen(final String treefileName, final String access) throws Exception {
		final java.io.RandomAccessFile treefile = new java.io.RandomAccessFile(treefileName, access);
		return ReOpen(treefile);
	}

	public static BplusTreeSet ReOpen(final String treefileName) throws Exception {
		return ReOpen(treefileName, "rw");
	}

	public static BplusTreeSet ReadOnly(final String treefileName) throws Exception {
		return ReOpen(treefileName, "r");
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
	}

	public void RemoveKey(final String key) throws Exception {
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

	public void Set(final String key, final Object map) throws Exception {
		set(key, null);
	}

	public void set(final String key, final byte[] value) throws Exception {
		tree.set(key, 0);
	}

	public void Commit() throws Exception {
		// commit the tree
		tree.Commit();
		ClearBookKeeping();
	}

	public void Abort() throws Exception {
		tree.Abort();
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
