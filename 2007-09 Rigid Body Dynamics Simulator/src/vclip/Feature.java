//
// Based on the vclip C++ struct Feature
// See the file COPYRIGHT for copyright information.
//

package vclip;

import javax.vecmath.Vector3d;

/**
 * Base class for the features of a polyhedron.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public abstract class Feature {
	/**
	 * Identifier for a vertex.
	 */
	public static final int VERTEX = 1;

	/**
	 * Identifier for an edge.
	 */
	public static final int EDGE = 2;

	/**
	 * Identifier for a face.
	 */
	public static final int FACE = 3;

	/* Instance Variables */
	protected String name;
	protected int type;
	protected int index;

	protected boolean hidden;

	/* Methods */
	final void setName(String name) {
		this.name = new String(name);
	}

	/**
	 * Gets the name of the type of this feature
	 * (either <code>vertex</code>, <code>edge</code>, <code>face</code>).
	 *
	 * @return type name of this feature
	 */
	public String typeName() {
		switch (type) {
		case VERTEX: {
			return "vertex";
		}
		case EDGE: {
			return "edge";
		}
		case FACE: {
			return "face";
		}
		default: {
			return "???";
		}
		}
	}

	public Feature promote(Vector3d nrm, double angtol) {
		return this;
	}

	/**
	 * Gets the specific name of this feature.
	 *
	 * @return name of this feature
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type of this feature.
	 *
	 * @return type of this feature
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets whether or not this feature is hidden. A hidden
	 * feature will not be added to distance reports.
	 *
	 * @param hidden true if the feature is to be hidden.
	 * @see #isHidden
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Returns true if this feature is hidden.
	 *
	 * @return true if the feature is hidden
	 * @see #setHidden
	 */
	public boolean isHidden() {
		return hidden;
	}
}
