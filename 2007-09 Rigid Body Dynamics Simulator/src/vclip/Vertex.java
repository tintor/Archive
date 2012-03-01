//
// Based on the vclip C++ struct Vertex
// See the file COPYRIGHT for copyright information.
//

package vclip;

import java.util.Vector;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A vertex of a polyhedron.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class Vertex extends Feature {

	/* Instance Variables */

	protected Point3dX coords;
	protected Vector cone; // list of VertexConeNode objects
	VertexConeNode coneNode0;

	/* Methods */

	/** Default Constructor */
	Vertex() {
		this("");
		type = VERTEX;
	}

	/** Constructor */
	Vertex(final String name) {
		setName(name);
		type = VERTEX;
		coords = new Point3dX();
		cone = new Vector();
	}

	/**
	 * Constructs a Vertex with a given name and coordinates.
	 *
	 * @param name vertex name
	 * @param p coordinate values
	 */
	public Vertex(final String name, final Point3d p) {
		setName(name);
		type = VERTEX;
		coords = new Point3dX(p);
		cone = new Vector();
	}

	/**
	 * Constructs a Vertex with a given name and coordinate
	 * values.
	 *
	 * @param name vertex name
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public Vertex(final String name, final double x, final double y, final double z) {
		setName(name);
		type = VERTEX;
		coords = new Point3dX(x, y, z);
		cone = new Vector();
	}

	/**
	 * Sets the coordinates of this vertex.
	 *
	 * @param coords coordinates of this vertex
	 */
	public void setCoords(final Point3d coords) {
		this.coords = new Point3dX(coords);
	}

	/**
	 * Gets the coordinates of this vertex.
	 *
	 * @return coordinates of this vertex
	 */
	public Point3d getCoords() {
		return coords;
	}

	/**
	 * Produces a string representation of this vertex.
	 *
	 * @return string representation
	 */
	@Override
	public final String toString() {
		return "vertex: " + name + ": " + coords.toString();
	}

	@Override
	public Feature promote(final Vector3d nrm, final double angtol) {
		final Edge e1 = null; /* 1st edge which is nearly parallel to plane */
		final Edge e2 = null; /* 2nd edge which is nearly parallel to plane */

		double minAbsDot = Double.POSITIVE_INFINITY;
		Edge minEdge = null;

		for (VertexConeNode vcn = coneNode0; vcn != null; vcn = vcn.next) {
			final double absDot = Math.abs(vcn.nbr.dir.dot(nrm));
			if (absDot < minAbsDot) {
				minEdge = vcn.nbr;
				minAbsDot = absDot;
			}
		}
		// minAbsDot is the absolute value of the cosine of the angle
		// between the edge and the normal. If the feature is to be
		// promoted, then this angle should be close to +/- PI/2, and we can
		// use the approximation
		//
		// cos (PI/2 + e) = -e for small e
		//
		if (minAbsDot <= angtol) return minEdge.promote(nrm, angtol);
		return this;
	}
}
