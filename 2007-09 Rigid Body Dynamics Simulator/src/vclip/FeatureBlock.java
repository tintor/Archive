package vclip;

/**
 * Scratch space for storing transformed features components
 * associated with a specific polyhedron.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>, Winter 2003
 */
class FeatureBlock {
	Matrix4dX T = new Matrix4dX();

	Feature feat;
	Point3dX xcoords = new Point3dX();
	Point3dX xtail = new Point3dX();
	Point3dX xhead = new Point3dX();
	Vector3dX xseg = new Vector3dX();

	Point3dX pnt = new Point3dX();
	Vector3dX nrm = new Vector3dX();

	Point3dX xpnt = new Point3dX();
	Vector3dX xvec = new Vector3dX();

	Edge edge;
	Vertex vert;
	Face face;

	ConvexPolyhedron.DoublePtr dist = new ConvexPolyhedron.DoublePtr(0);

	/* Constructor */
	FeatureBlock(Feature f) {
		if (f != null) {
			setFeature(f);
		}
	}

	void xformVertex() {
		T.transform(vert.coords, xcoords);
	}

	void xformEdge() {
		T.transform(edge.tail.coords, xtail);
		T.transform(edge.head.coords, xhead);
		xseg.sub(xhead, xtail);
	}

	void setFeature(Feature f) {
		if (f == null) {
			feat = null;
			edge = null;
			vert = null;
			face = null;
		} else if (f.type == Feature.VERTEX) {
			setVert((Vertex) f);
		} else if (f.type == Feature.EDGE) {
			setEdge((Edge) f);
		} else if (f.type == Feature.FACE) {
			setFace((Face) f);
		}
	}

	void setVert(Vertex v) {
		feat = v;
		vert = v;
		xformVertex();
	}

	void setEdge(Edge e) {
		feat = e;
		edge = e;
		xformEdge();
	}

	void setFace(Face f) {
		feat = f;
		face = f;
	}
}
