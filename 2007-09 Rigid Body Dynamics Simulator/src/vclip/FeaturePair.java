//
// Based on the vclip C++ struct FeaturePair.
// See the file COPYRIGHT for copyright information.
//

package vclip;

/**
 * Represents a pair of features. Used with ClosestFeaturesHT.
 * 
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class FeaturePair {
	public Feature first, second;

	public FeaturePair() {
		first = second = null;
	}

	public FeaturePair(Feature f1, Feature f2) {
		set(f1, f2);
	}

	public FeaturePair(FeaturePair fpair) {
		set(fpair);
	}

	public void set(Feature f1, Feature f2) {
		first = f1;
		second = f2;
	}

	public void set(FeaturePair fpair) {
		first = fpair.first;
		second = fpair.second;
	}

	public boolean equals(Feature f1, Feature f2) {
		return f1 == first && f2 == second;
	}

	public String toString() {
		return first.getName() + " " + second.getName();
	}
}
