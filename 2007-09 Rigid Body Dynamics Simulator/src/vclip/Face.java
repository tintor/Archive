//
// Based on the vclip C++ struct Face.
// See the file COPYRIGHT for copyright information.
//

package vclip;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 * A face of a polyhedron.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class Face extends Feature {
	/* Instance Variables */

	protected int sides;
	protected Plane plane;
	protected Vector cone; // list of FaceConeNode objects
	protected FaceConeNode coneNode0;

	public class EdgeIterator implements Iterator {
		FaceConeNode startNode;
		FaceConeNode node;

		EdgeIterator(FaceConeNode startNode) {
			this.startNode = startNode;
			this.node = startNode;
		}

		public boolean hasNext() {
			return node != null;
		}

		public Object next() throws NoSuchElementException {
			return nextEdge();
		}

		public Edge nextEdge() throws NoSuchElementException {
			if (node == null) {
				throw new NoSuchElementException();
			} else {
				Edge e = node.nbr;
				node = node.ccw;
				if (node == startNode) {
					node = null;
				}
				return e;
			}
		}

		public void remove() throws UnsupportedOperationException, IllegalStateException {
			throw new UnsupportedOperationException("can't remove edges");
		}
	}

	public class VertexIterator extends EdgeIterator {
		VertexIterator(FaceConeNode startNode) {
			super(startNode);
		}

		public Point3d nextVertex() throws NoSuchElementException {
			Edge e = super.nextEdge();
			return e.left == Face.this ? e.head.coords : e.tail.coords;
		}

		public Object next() {
			return nextVertex();
		}
	}

	/* Methods */

	/** Constructor */
	Face() {
		this.setName("NULL FACE");
		this.type = FACE;
		plane = new Plane();
		cone = new Vector();
		sides = 0;
	}

	/**
	 * Gets the plane associated with this face.
	 *
	 * @return plane of the face
	 */
	public Plane getPlane() {
		return plane;
	}

	/**
	 * Gets an iterator which gives the edges of this
	 * face in counter-clockwise order.
	 *
	 * @return face edge iterator
	 */
	public EdgeIterator getEdges() {
		return new EdgeIterator(coneNode0);
	}

	/**
	 * Gets an iterator which gives the vertices of this
	 * face (each as a Point3d) in counter-clockwise order.
	 *
	 * @return vertex iterator
	 */
	public VertexIterator getVertices() {
		return new VertexIterator(coneNode0);
	}

	void projectToPlane(ConvexPolygon poly, Matrix4d XLP, boolean ccw) {
		FaceConeNode fcn;
		if ((fcn = coneNode0) != null) {
			poly.beginDef();
			do {
				Point3d p;
				if (fcn.nbr.left == this) {
					p = fcn.nbr.head.coords;
				} else {
					p = fcn.nbr.tail.coords;
				}
				// explicit transformation so we can save time and
				// don't have to alloc a Vector3d
				double x, y;
				x = XLP.m00 * p.x + XLP.m01 * p.y + XLP.m02 * p.z + XLP.m03;
				y = XLP.m10 * p.x + XLP.m11 * p.y + XLP.m12 * p.z + XLP.m13;
				poly.addVertex(x, y, ccw);
				fcn = fcn.ccw;
			} while (fcn != coneNode0);
			poly.endDef();
		}
	}

	/**
	 * Produces a string representation of this face.
	 *
	 * @return string representation
	 */
	public final String toString() {
		return (new String(name + ": " + sides + " sides: " + plane.toString()));
	}
}
