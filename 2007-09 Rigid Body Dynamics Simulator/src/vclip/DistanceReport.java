package vclip;

import javax.vecmath.Matrix4d;

/**
 * Used by {@link PolyTree#vclip vclip} to return distance information.
 *
 * <p>This information includes the closest point pair between the two
 * objects (obtained using
 * {@link #getClosestPair getClosestPair}).
 * It also includes, optionally, a list of close point pairs
 * whose distance is within a threshold prescribed by {@link
 * #setMaxPairDistance setMaxPairDistance}. Up to one such close
 * point pair may be generated for each pair of convex polyhedra
 * associated with the two objects.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> */
public class DistanceReport {
	static public final double DEFAULT_PROMOTION_TOL = 0.002;

	ClosestPointPair[] closePairs;
	ClosestPointPair closestPair;
	int numClosePairs = 0;
	double maxDist = 0;
	double closestDist = Double.POSITIVE_INFINITY;
	boolean promoteFeatures = false;
	double promotionTol = DEFAULT_PROMOTION_TOL;

	/**
	 * Creates a DistanceReport with room for reporting a maximum
	 * number of close point pairs.
	 *
	 * @param maxClosePairs maximum number of close point pairs
	 * which can be reported
	 */
	public DistanceReport(int maxClosePairs) {
		closestPair = new ClosestPointPair();
		setMaxClosePairs(maxClosePairs);
	}

	/**
	 * Creates a DistanceReport which does not report
	 * close point pairs.
	 */
	public DistanceReport() {
		this(0);
	}

	/**
	 * Creates a DistanceReport by copying an existing one.
	 *
	 * @param rep distance report to copy
	 */
	public DistanceReport(DistanceReport rep) {
		this(0);
		set(rep);
	}

	/**
	 * Sets this distance report to a copy of the supplied one.
	 *
	 * @param rep distance report to copy
	 */
	public void set(DistanceReport rep) {
		if (getMaxClosePairs() != rep.getMaxClosePairs()) {
			setMaxClosePairs(rep.getMaxClosePairs());
		}
		for (int i = 0; i < rep.numClosePairs; i++) {
			closePairs[i].set(rep.closePairs[i]);
		}
		closestPair.set(rep.closestPair);
		numClosePairs = rep.numClosePairs;
		maxDist = rep.maxDist;
		closestDist = rep.closestDist;
		promoteFeatures = rep.promoteFeatures;
		promotionTol = rep.promotionTol;
	}

	/**
	 * Clears the information in this report.
	 */
	public void clear() {
		closestPair.clear();
		for (int i = 0; i < closePairs.length; i++) {
			closePairs[i].clear();
		}
		numClosePairs = 0;
		closestDist = Double.POSITIVE_INFINITY;
	}

	/**
	 * Returns the number of close point pairs in this
	 * report.
	 *
	 * @return number of close point pairs
	 */
	public int numClosePairs() {
		return numClosePairs;
	}

	/**
	 * Returns the maximum number of close point pairs that
	 * can be reported by this report.
	 *
	 * @return maximum number of close point pairs */
	public int getMaxClosePairs() {
		return closePairs.length;
	}

	/**
	 * Sets the maximum number of close point pairs that
	 * can be reported by this report. This clears
	 * any existing close pair information.
	 *
	 * @param maxClosePairs maximum number of close point pairs
	 */
	public void setMaxClosePairs(int maxClosePairs) {
		closePairs = new ClosestPointPair[maxClosePairs];
		for (int i = 0; i < closePairs.length; i++) {
			closePairs[i] = new ClosestPointPair();
		}
	}

	/**
	 * Add a close point pair to this report.
	 *
	 * @param pair close point pair to add
	 */
	void addClosePair(ClosestPointPair pair) {
		if (numClosePairs < closePairs.length) {
			closePairs[numClosePairs].set(pair);
			numClosePairs++;
		}
	}

	/**
	 * Set the closest pair to a given pair if the
	 * pair's distance is less than the current
	 * closest distance.
	 *
	 * @param pair close point pair to check
	 * @see #getClosestPair
	 * @see #getClosestDistance
	 */
	void setClosestPairIfNecessary(ClosestPointPair pair) {
		if (pair.dist < closestDist) {
			closestDist = pair.dist;
			closestPair.set(pair);
		}
	}

	/**
	 * Adds a close point pair to the list of close pairs
	 * if there is room, and the pair's distance is positive
	 * and within the maximum pair distance.
	 *
	 * @param pair close point pair to check
	 * @see #getMaxPairDistance
	 * @see #getClosePairs
	 */
	void addClosePairIfNecessary(ClosestPointPair pair) {
		if (numClosePairs < closePairs.length && pair.dist > 0 && pair.dist <= maxDist) {
			closePairs[numClosePairs].set(pair);
			numClosePairs++;
		}
	}

	/**
	 * Sets the closest point pair in this report.
	 *
	 * @param pair setting for the closest point pair
	 */
	void setClosestPair(ClosestPointPair pair) {
		closestPair.set(pair);
	}

	/**
	 * Gets the close point pairs in this report. Note that only
	 * the number returned by {@link #numClosePairs numClosePairs}
	 * will be valid.
	 *
	 * @return close point pairs
	 * @see #numClosePairs
	 */
	public ClosestPointPair[] getClosePairs() {
		return closePairs;
	}

	/**
	 * Gets the closest point pairs in this report.
	 *
	 * @return closest point pair
	 */
	public ClosestPointPair getClosestPair() {
		return closestPair;
	}

	/**
	 * Sets the maximum close point pair distance for this report.
	 * When vclip is called, point pairs whose distance is less
	 * than this value will be added to the report, up to
	 * a maximum prescribed by
	 * {@link #getMaxClosePairs getMaxClosePairs}.
	 *
	 * @param dist maximum distance for reporting close
	 * point pairs.
	 * @see #getMaxPairDistance
	 * @see #getMaxClosePairs
	 */
	public void setMaxPairDistance(double dist) {
		maxDist = dist;
	}

	/**
	 * Gets the maximum close point pair distance for this report.
	 *
	 * @return maximum distance for reporting close
	 * point pairs.
	 * @see #setMaxPairDistance
	 */
	public double getMaxPairDistance() {
		return maxDist;
	}

	/**
	 * Gets the closest distance associated with this report.
	 * This is the same value returned by the call to
	 * {@link PolyTree#vclip vclip} that created this report,
	 * and a distance <= 0 indicates a collison.
	 *
	 * @return closest distance
	 */
	public double getClosestDistance() {
		return closestDist;
	}

	/**
	 * Transforms the first point, as well as the normals, of each
	 * point pair in this distance report into a new coordinate
	 * frame (typically that associated with the second points).
	 *
	 * @param T1R transformation into the new reference frame.  */
	public void transformFirstPoints(Matrix4d T1R) {
		if (closestDist != Double.POSITIVE_INFINITY) {
			T1R.transform(closestPair.pnt1);
			T1R.transform(closestPair.nrml);
		}
		for (int i = 0; i < numClosePairs; i++) {
			T1R.transform(closePairs[i].pnt1);
			T1R.transform(closePairs[i].nrml);
		}
	}

	/**
	 * Transforms the second point of each point pair in this
	 * distance report into a new coordinate frame (typically
	 * that associated with the first points).
	 *
	 * @param T2R transformation into the new reference frame.
	 */
	public void transformSecondPoints(Matrix4d T2R) {
		if (closestDist != Double.POSITIVE_INFINITY) {
			T2R.transform(closestPair.pnt2);
		}
		for (int i = 0; i < numClosePairs; i++) {
			T2R.transform(closePairs[i].pnt2);
		}
	}

	/**
	 * Enables or disables feature promotion. If enabled, then
	 * features that lie within the distance specified by {@link
	 * #setMaxPairDistance setMaxPairDistance} are ``promoted'',
	 * where possible, to the feature which has the highest
	 * dimension. Faces have dimension 2, edges have dimenion 1,
	 * and vertices have dimension 0. In the most extreme case,
	 * a vertex-vertex contact could be promoted to a face-face
	 * contact.
	 *
	 * <p>Feature promotion is determined by seeing whether
	 * adjacent features of higher dimension are within
	 * <code>angtol</code> radians of being perpendicular to the
	 * contact normal.
	 *
	 * <p>Feature promotion may cause more contact pairs
	 * to be generated. For example, an edge-face contact
	 * is generally associated with two contact pairs,
	 * associated with the endpoints of the projection of
	 * the edge onto the face. Likewise, a face-face contact
	 * will be associated with several contact pairs, each
	 * associated with a vertex of the polygon formed by
	 * projected one face onto the other.
	 *
	 * <p>The extra contact pairs generated by feature promotion
	 * are simply added to the set of close point pairs
	 * returned by {@link #getClosePairs getClosePairs}.
	 *
	 * @param enable set true to enable feature promotion
	 * @param angtol angular tolerance (in radians). If negative,
	 * then {@link #DEFAULT_PROMOTION_TOL DEFAULT_PROMOTION_TOL}
	 * is used
	 * @see #promotionEnabled
	 * @see #getPromotionTolerance
	 */
	public void setFeaturePromotion(boolean enable, double angtol) {
		promoteFeatures = enable;
		if (angtol >= 0) {
			promotionTol = angtol;
		} else {
			promotionTol = DEFAULT_PROMOTION_TOL;
		}
	}

	/**
	   * Queries whether or not feature promotion is enabled.
	 *
	 * @return true if feature promotion is enabled.
	 * @see #setFeaturePromotion
	 */
	public boolean promotionEnabled() {
		return promoteFeatures;
	}

	/**
	 * Returns the angular tolerance used to promote features.
	 *
	 * @return promotion tolerance (in radians)
	 * @see #setFeaturePromotion
	 */
	public double getPromotionTolerance() {
		return promotionTol;
	}
}
