//
// Based on the vclip C++ struct Edge.
// See the file COPYRIGHT for copyright information.
//

package vclip;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * An edge of a polyhedron.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class Edge extends Feature {
	/* Instance Variables */
	protected Vertex tail, head;
	protected Face left, right;
	protected double len;
	protected Vector3dX dir;
	protected Plane tplane, hplane, lplane, rplane, bplane;
	protected boolean isCoplanar = false;

	/* Methods */

	Edge() {
		this.setName("edge");
		this.type = EDGE;
		tail = new Vertex();
		head = new Vertex();
		left = new Face();
		right = new Face();
		dir = new Vector3dX();
		len = 0;
		tplane = new Plane();
		hplane = new Plane();
		lplane = new Plane();
		rplane = new Plane();
		bplane = null; // don't set unless needed
		//this.setName(tail.getName() + head.getName());
	}

	/**
	 * Gets the head vertex associated with this edge.
	 *
	 * @return head vertex
	 */
	public Vertex getHead() {
		return head;
	}

	/**
	 * Gets the tail vertex associated with this edge.
	 *
	 * @return tail vertex
	 */
	public Vertex getTail() {
		return tail;
	}

	/**
	 * Gets the left face associated with this edge.
	 *
	 * @return left face
	 */
	public Face leftFace() {
		return left;
	}

	/**
	 * Gets the right face associated with this edge.
	 *
	 * @return right face
	 */
	public Face rightFace() {
		return right;
	}

	/**
	 * Gets the direction vector associated with this edge
	 *
	 * @return direction vector
	 */
	public Vector3d direction() {
		return dir;
	}

	/**
	 * Gets the length of this edge.
	 *
	 * @return length of the edge
	 */
	public double length() {
		return len;
	}

	/**
	 * Produces a string representation of this edge.
	 *
	 * @return string representation
	 */
	public final String toString() {
		return (new String("tail: " + tail.toString() + ", " + "head: " + head.toString()));
	}

	private double crossProductSquared(Vector3d v1, Vector3d v2) {
		double x = v1.y * v2.z - v1.z * v2.y;
		double y = v1.z * v2.x - v1.x * v2.z;
		double z = v1.x * v2.y - v1.y * v2.x;
		return x * x + y * y + z * z;
	}

	public Feature promote(Vector3d nrm, double angtol) {
		if (crossProductSquared(left.plane.normal, nrm) <= angtol * angtol) {
			return left;
		} else if (crossProductSquared(right.plane.normal, nrm) <= angtol * angtol) {
			return right;
		} else {
			return this;
		}
	}

	void projectToPlane(Line2d line, Matrix4d XEP) {
		Point3d p;
		double x, y;

		p = tail.coords;
		// explicit transformation so we can save time and
		// don't have to alloc a Vector3d
		x = XEP.m00 * p.x + XEP.m01 * p.y + XEP.m02 * p.z + XEP.m03;
		y = XEP.m10 * p.x + XEP.m11 * p.y + XEP.m12 * p.z + XEP.m13;
		line.q.set(x, y);

		p = head.coords;
		x = XEP.m00 * p.x + XEP.m01 * p.y + XEP.m02 * p.z + XEP.m03;
		y = XEP.m10 * p.x + XEP.m11 * p.y + XEP.m12 * p.z + XEP.m13;
		line.u.set(x, y);
		line.u.sub(line.q);
	}
}
