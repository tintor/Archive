//
// Based on the vclip C++ struct FaceConeNode.
// See the file COPYRIGHT for copyright information.
//

package vclip;


/**
 * Represents a face cone element associated with a specific
 * edge of a face.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
class FaceConeNode {
	/* Instance Variables */

	FaceConeNode next;

	public Plane plane;
	public Edge nbr; // neighbouring edge when plane violated
	public FaceConeNode ccw, cw;
	// ranges from 0 to n-1,
	// where n = number of edges on face
	public int idx;

	int code = 0;
	double lam = 0;

	/** toString
	 *
	 * returns a string representation of this object
	 */
	public final String toString() {
		return new String("plane: " + plane.toString() + ", nbr = [" + nbr.toString() + "]");
	}
}
