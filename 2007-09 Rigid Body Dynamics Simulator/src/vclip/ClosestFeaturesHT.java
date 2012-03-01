//
// Based on the vclip C++ struct ClosestFeaturesHT.
// See the file COPYRIGHT for copyright information.
//

package vclip;

import java.util.HashMap;

/**
 * Implements a hash table for use by
 * {@link PolyTree#vclip vclip} to store the
 * most recent closest features between pairs of PolyTrees.
 * Use of this information generally improves the performance
 * of subsequent calls to vclip, provided that the spatial
 * relationship between PolyTrees is not changing too rapidly.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class ClosestFeaturesHT {
	/**
	 * Default size of the hash table.
	 */
	static public final int DEFAULT_SIZE = 10007;
	protected HashMap map;

	private class Node {
		Node next;
		FeaturePair feats;
		PolyTreePair polys;

		Node(PolyTreePair polys, FeaturePair feats) {
			this.polys = polys;
			this.feats = feats;
		}
	}

	Node[] table;

	/**
	 * Creates a ClosestFeaturesHT with a default size.
	 * An application will generally create just one of these
	 * objects and use it in all subsequent calls to 
	 * {@link PolyTree#vclip vclip}.
	 *
	 * @see #DEFAULT_SIZE
	 */
	public ClosestFeaturesHT() { // map = new HashMap();
		table = new Node[DEFAULT_SIZE];
	}

	/**
	 * Creates a ClosestFeaturesHT with a user-supplied size.
	 *
	 * @param size size of the hash table
	 */
	public ClosestFeaturesHT(int size) { // map = new HashMap(size); 
		table = new Node[size];
	}

	FeaturePair get(PolyTreePair pair) {
		int idx = (pair.hashCode() % table.length);
		for (Node node = table[idx]; node != null; node = node.next) {
			if (PolyTreePair.equals(node.polys, pair)) {
				return node.feats;
			}
		}
		return null;
	}

	public FeaturePair get(PolyTree ptree1, PolyTree ptree2) {
		return get(new PolyTreePair(ptree1, ptree2));
	}

	public void put(PolyTree ptree1, PolyTree ptree2, FeaturePair feats) {
		put(new PolyTreePair(ptree1, ptree2), feats);
	}

	void put(PolyTreePair pair, FeaturePair feats) {
		int idx = (pair.hashCode() % table.length);
		Node node;
		Node prev = null;
		for (node = table[idx]; node != null; node = node.next) {
			if (PolyTreePair.equals(node.polys, pair)) {
				break;
			}
			prev = node;
		}
		if (node == null) {
			if (prev == null) {
				table[idx] = new Node(pair, feats);
			} else {
				prev.next = new Node(pair, feats);
			}
		} else {
			node.feats = feats;
		}
	}
}
