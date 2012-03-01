package vclip;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Describes a pair of closest points and features between two
 * polyhedra or polytrees.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>
 */
public class ClosestPointPair {
	/**
	 * Closest point on the first polyhedron.
	 */
	public Point3d pnt1;

	/**
	 * Closest point on the second polyhedron.
	 */
	public Point3d pnt2;

	/**
	 * Normal (in coordinates of the first polyhedron).
	 */
	public Vector3d nrml;

	/**
	 * Closest feature on the first polyhedron.
	 */
	public Feature feat1;

	/**
	 * Closest feature on the second polyhedron.
	 */
	public Feature feat2;

	/**
	 * Distance between the two points
	 */
	public double dist;

	private final double inf = Double.POSITIVE_INFINITY;

	/**
	 * Creates a ClosestPointPair and initializes it
	 * as being empty.
	 */
	public ClosestPointPair() {
		pnt1 = new Point3d(-inf, -inf, -inf);
		pnt2 = new Point3d(inf, inf, inf);
		nrml = new Vector3d(0, 0, 0);
		feat1 = null;
		feat2 = null;
		dist = 0;
	}

	/**
	 * Creates a ClosestPointPair which is a copy of an existing
	 * one.
	 *
	 * @param pair pair to copy from
	 */
	public ClosestPointPair(final ClosestPointPair pair) {
		this();
		set(pair);
	}

	/**
	 * Sets this pair to the value of another one.
	 *
	 * @param pair pair to copy from
	 */
	public void set(final ClosestPointPair pair) {
		pnt1.set(pair.pnt1);
		pnt2.set(pair.pnt2);
		nrml.set(pair.nrml);
		feat1 = pair.feat1;
		feat2 = pair.feat2;
		dist = pair.dist;
	}

	/**
	 * Clears the values in this pair.
	 */
	public void clear() {
		pnt1.set(-inf, -inf, -inf);
		pnt2.set(inf, inf, inf);
		nrml.set(0, 0, 0);
		feat1 = null;
		feat2 = null;
		dist = 0;
	}

	/**
	 * Returns true if this pair has no set value.
	 *
	 * @return true if this pair has no set value
	 */
	public boolean isClear() {
		return pnt1.x == -inf && pnt2.x == inf;
	}

	//  	/**
	//  	 * Returns the distance between the points of this pair.
	//  	 *
	//  	 * @return distance between points
	//  	 */
	//  	public double distance()
	//  	 {
	//  	   return pnt1.distance (pnt2);
	//  	 }

	//  	/**
	//  	 * Returns the distance squared between the points of this pair.
	//  	 *
	//  	 * @return distance squared between points
	//  	 */
	//  	public double distanceSquared()
	//  	 {
	//  	   return pnt1.distanceSquared (pnt2);
	//  	 }

	/**
	 * Sets the closest features.
	 *
	 * @param f1 First feature
	 * @param f2 Second feature
	 */
	public void setFeatures(final Feature f1, final Feature f2) {
		feat1 = f1;
		feat2 = f2;
	}

	/**
	 * Returns true if this pair has the same features
	 * as the argument pair.
	 *
	 * @param pair pair to check features against
	 * @return true if this pair and the supplied pair have
	 * the same features
	 */
	public boolean featureEquals(final ClosestPointPair pair) {
		return pair.feat1 == feat1 && pair.feat2 == feat2;
	}

	/**
	 * Returns true if the contact points for this pair are within
	 * epsilon of the contact points of the argument pair. The comparison
	 * is performed using an L-infinity metric.
	 *
	 * @param pair pair to check features against
	 * @param eps tolerance for comparing points
	 * @return true if the contact points of this pair and
	 * the supplied pair are within eps of each other */
	public boolean epsilonEquals(final ClosestPointPair pair, final double eps) {
		if (!pnt1.epsilonEquals(pair.pnt1, eps)) return false;
		if (Math.abs(pnt1.x - pair.pnt1.x + (nrml.x - pair.nrml.x) * dist) > eps) return false;
		if (Math.abs(pnt1.y - pair.pnt1.y + (nrml.y - pair.nrml.y) * dist) > eps) return false;
		if (Math.abs(pnt1.z - pair.pnt1.z + (nrml.z - pair.nrml.z) * dist) > eps) return false;
		return true;
	}

}
