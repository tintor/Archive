//
// Based on the vclip C++ struct PolyTreePair.
// See the file COPYRIGHT for copyright information.
//

package vclip;

/**
 * Represents a pair of polytrees. Used with ClosestFeaturesHT.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a> 
 */
class PolyTreePair {
	protected PolyTree first, second;

	static boolean equals(PolyTreePair ptree1, PolyTreePair ptree2) {
		return (ptree1.first == ptree2.first && ptree1.second == ptree2.second);
	}

	public PolyTreePair() {
		first = null;
		second = null;
	}

	public PolyTreePair(PolyTree ptree1, PolyTree ptree2) {
		first = ptree1;
		second = ptree2;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PolyTreePair) {
			PolyTreePair pair = (PolyTreePair) obj;
			return (first == pair.first && second == pair.second);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}
}
