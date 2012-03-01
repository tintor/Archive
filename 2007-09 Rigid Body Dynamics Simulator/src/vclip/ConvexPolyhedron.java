///////////////////////////////////////////////////////////////////////////////
//
//  Copyright 1997 Mitsubishi Electric Information Technology Center
//  America (MEITCA).  All Rights Reserved.
//
//  Permission to use, copy, modify and distribute this software and
//  its documentation for educational, research and non-profit
//  purposes, without fee, and without a written agreement is hereby
//  granted, provided that the above copyright notice and the
//  following three paragraphs appear in all copies.
//
//  Permission to incorporate this software into commercial products
//  may be obtained from MERL - A Mitsubishi Electric Research Lab, 201
//  Broadway, Cambridge, MA 02139.
//
//  IN NO EVENT SHALL MEITCA BE LIABLE TO ANY PARTY FOR DIRECT,
//  INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING
//  LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
//  DOCUMENTATION, EVEN IF MEITCA HAS BEEN ADVISED OF THE POSSIBILITY
//  OF SUCH DAMAGES.
//
//  MEITCA SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS ON
//  AN "AS IS" BASIS, AND MEITCA HAS NO OBLIGATIONS TO PROVIDE
//  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
//
//  Original Author:
//    Brian Mirtich
//    mirtich@merl.com
//    617.621.7573
//    www.merl.com/people/mirtich
//
//  Java port:
//    John E. Lloyd and Eddy Boxerman
//    University of British Columbia
//    
///////////////////////////////////////////////////////////////////////////////

package vclip;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A convex polyhedron.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class ConvexPolyhedron {

	/* Static Variables */
	protected static final int CONTINUE = 0;
	protected static final int DISJOINT = 1;
	protected static final int PENETRATION = 2;

	private static final double DBL_EPSILON = 2.2204460492503131e-16;
	private static final double EPS = 1e-15;
	static String ptree1name, ptree2name;

	private final FeatureBlock featureBlock = new FeatureBlock(null);
	private int numFeats;

	private final FeaturePair prevFeaturePair0 = new FeaturePair();
	private final FeaturePair prevFeaturePair1 = new FeaturePair();

	private final Point3d avgVtx = new Point3d();

	static class DoublePtr {
		double d;

		DoublePtr(final double d) {
			this.d = d;
		}
	}

	static int callCount = 0;
	static int iterCount = 0;
	static int maxIterCount = 0;
	static int maxFeatureCount = 0;
	static int loopingCount = 0;
	static int detectedLoopCount = 0;

	//	static AcmeTime timer = new AcmeTime();

	//  	static private class TimerRecord
	//  	 {
	//  	   long t0 = 0;
	//  	   long t1 = 0;
	//  	   int cnt = 0;

	//  	   public double total ()
	//  	    { if (cnt > 0)
	//  	       { return (t1-t0)/cnt; 
	//  	       }
	//  	      else
	//  	       { return 0; 
	//  	       }
	//  	    }

	//  	   public void start()
	//  	    {  timer.setSystemTime();
	//  	       t0 += timer.getTime();
	//  	    }

	//  	   public void stop()
	//  	    {  timer.setSystemTime();
	//  	       t1 += timer.getTime();
	//  	       cnt++;
	//  	    }
	//  	 }

	//  	static TimerRecord[] timers = new TimerRecord[100];

	//  	 {
	//  	   for (int i=0; i<timers.length; i++)
	//  	    { timers[i] = new TimerRecord();
	//  	    }
	//  	 }

	//  	static void printTimers (int i0, int i1)
	//  	 {
	//  	   PrintfFormat dfmt = new PrintfFormat("%5d");
	//  	   PrintfFormat ffmt = new PrintfFormat("%9.1f");
	//  	   for (int i=i0; i<i1; i++)
	//  	    { System.out.println (dfmt.tostr(i) + " " +
	//  				  dfmt.tostr(timers[i].cnt) + " " +
	//  				  ffmt.tostr(timers[i].total()) + " " +
	//  				  ffmt.tostr(timers[i].total()*timers[i].cnt));
	//  	    }
	//  	 }

	private static final Vector3dX zeroVect = new Vector3dX(0, 0, 0);

	/* Instance Variables */
	protected Vertex[] verts;
	protected Edge[] edges;
	protected Face[] faces;

	private Vector elist; // temp edge list used when creating polyhedron
	private Vector flist; // temp face list used when creating polyhedron

	public Feature findFeature(final String name) {
		for (final Vertex vert : verts)
			if (vert.getName().equals(name)) return vert;
		for (final Edge edge : edges)
			if (edge.getName().equals(name)) return edge;
		for (final Face face : faces)
			if (face.getName().equals(name)) return face;
		return null;
	}

	/**
	 * Creates an empty ConvexPolyhedron, which should then be
	 * initialized (such as with a call to
	 * {@link #scan(Reader) scan}).
	 */
	public ConvexPolyhedron() {
		elist = new Vector();
		flist = new Vector();
	}

	/**
	 * Constructs a polyhedron given a list of vertices and
	 * a list of face indices. Each face is represented
	 * by an integer array listing its vertex indices
	 * in counter-clockwise order. The index of a vertex
	 * gives its location within the vertex list.
	 *
	 * @param vlist vertices for the polyhedron
	 * @param faceIndices face index arrays
	 * @throws IllegalArgumentException if the vertex and face
	 * information does not describe a proper convex polyhedron
	 */
	public ConvexPolyhedron(final Point3d[] vlist, final int[][] faceIndices) {
		set(vlist, faceIndices);
	}

	/**
	 * Constructs a polyhedron given a list of vertices and
	 * a list of face indices. Each face is represented
	 * by an integer array listing its vertex indices
	 * in counter-clockwise order. The index of a vertex
	 * gives its location within the coordinate list.
	 *
	 * @param coords coordinates of the polyhedron vertices,
	 * giving the x, y, and z values of each vertex, in order.
	 * The length of this array will be three times
	 * the number of vertices.
	 * @param faceIndices face index arrays
	 * @throws IllegalArgumentException if the vertex and face
	 * information does not describe a proper convex polyhedron
	 */
	public ConvexPolyhedron(final double[] coords, final int[][] faceIndices) {
		set(coords, faceIndices);
	}

	/**
	 * Constructs a ConvexPolyhedron by reading information from
	 * the supplied reader.
	 *
	 * @param reader supplies the information used to define
	 * the polyhedron.
	 * @param namedFormat if <code>true</code>, then the polyhedron
	 * will be read using the <i>named</i> format used by
	 * {@link #scanNamedFormat(Reader) scanNamedFormat}.
	 * Otherwise, the <code>.obj</code> format of
	 * {@link #scan(Reader) scan} will be used
	 * @throws IOException if an I/O or format error occured
	 * @see #scanNamedFormat(Reader)
	 * @see #scan(Reader)
	 */
	public ConvexPolyhedron(final Reader reader, final boolean namedFormat) throws IOException {
		final StreamTokenizer stok = new StreamTokenizer(reader);
		stok.commentChar('#');
		if (namedFormat)
			scanNamedFormat(stok);
		else
			scan(stok);
	}

	/**
	 * Constructs a ConvexPolyhedron by reading information from
	 * a StreamTokenizer.
	 *
	 * @param stok supplies the information used to define
	 * the polyhedron.
	 * @param namedFormat if <code>true</code>, then the polyhedron
	 * will be read using the <i>named</i> format used by
	 * {@link #scanNamedFormat(Reader) scanNamedFormat}.
	 * Otherwise, the <code>.obj</code> format of
	 * {@link #scan(Reader) scan} will be used
	 * @throws IOException if an I/O or format error occured
	 * @see #scanNamedFormat(Reader)
	 * @see #scan(Reader)
	 */
	public ConvexPolyhedron(final StreamTokenizer stok, final boolean namedFormat) throws IOException {
		if (namedFormat)
			scanNamedFormat(stok);
		else
			scan(stok);
	}

	/**
	 * Sets this polyhedron given a list of vertices and
	 * a list of face indices. Each face is represented
	 * by an integer array listing its vertex indices
	 * in counter-clockwise order. The index of a vertex
	 * gives its location within the vertex list.
	 *
	 * @param vlist vertices for the polyhedron
	 * @param faceIndices face index arrays
	 * @throws IllegalArgumentException if the vertex and face
	 * information does not describe a proper convex polyhedron
	 */
	public void set(final Point3d[] vlist, final int[][] faceIndices) {
		verts = new Vertex[vlist.length];

		elist = new Vector();
		flist = new Vector();

		for (int i = 0; i < verts.length; i++)
			verts[i] = new Vertex("v" + i, vlist[i]);
		buildFacesAndCheck(faceIndices);
	}

	/**
	 * Sets this polyhedron given a list of vertices and
	 * a list of face indices. Each face is represented
	 * by an integer array listing its vertex indices
	 * in counter-clockwise order. The index of a vertex
	 * gives its location within the coordinate list.
	 *
	 * @param coords coordinates of the polyhedron vertices,
	 * giving the x, y, and z values of each vertex, in order.
	 * The length of this array will be three times
	 * the number of vertices.
	 * @param faceIndices face index arrays
	 * @throws IllegalArgumentException if the vertex and face
	 * information does not describe a proper convex polyhedron
	 */
	public void set(final double[] coords, final int[][] faceIndices) {
		final int numv = coords.length / 3;
		verts = new Vertex[numv];

		elist = new Vector();
		flist = new Vector();

		for (int i = 0; i < numv; i++)
			verts[i] = new Vertex("v" + i, coords[i * 3], coords[i * 3 + 1], coords[i * 3 + 2]);
		buildFacesAndCheck(faceIndices);
	}

	private void buildFacesAndCheck(final int[][] faceIndices) {
		final Vector facevertlist = new Vector(16);
		for (int i = 0; i < faceIndices.length; i++) {
			final int[] idxs = faceIndices[i];
			facevertlist.clear();
			for (final int idx : idxs)
				if (idx < 0 || idx >= verts.length)
					System.err.println("ConvexPolyhedron(FileReader): no vertex " + idx + " on ConvexPolyhedron");
				else
					facevertlist.add(verts[idx]);
			addFace("f" + i, facevertlist);
		}
		final String errorMsg = check();
		if (errorMsg != null) throw new IllegalArgumentException(errorMsg);
		optimize(); // optimize data structures
		avgVtx.set(0, 0, 0);
		for (final Vertex vert : verts)
			avgVtx.add(vert.coords);
		avgVtx.scale(1 / (double) verts.length);
	}

	/**
	 * Sets the polyhedron using information obtained
	 * from a reader. This information should be supplied
	 * according the <i>named</i> format used in Brian
	 * Mirtich's original V-Clip implementation:
	 *
	 * <p>First, the vertices of the polyhedra are listed, each
	 * with a name, followed by three numbers
	 * giving the coordinates. If the vertex name is given as
	 * <code>-</code>, then a name of the form
	 * <code>v</code><i>n</i> will be automatically generated,
	 * where <i>n</i> denotes the number of the vertex.  The
	 * character <code>*</code> is used to terminate the vertex
	 * list.
	 *
	 * <p>Second, the faces are listed, each with a name,
	 * followed by a list (on the same
	 * line) of the names of the vertices (in counter-clockwise
	 * order). If the face name is given as
	 * <code>-</code>, then a name of the form
	 * <code>f</code><i>n</i> will be generated, where <i>n</i>
	 * is the number of the face. The
	 * character <code>*</code> is used to terminate the face
	 * list.
	 * 
	 * <p>A simple example defining a tetrahedron is
	 * <pre>
	 * origin 0 0 0
	 * x      1 0 0
	 * y      0 1 0
	 * z      0 0 1
	 * *
	 * xy	origin y x
	 * yz	origin z y
	 * zx	origin x z
	 * base	x y z
	 * </pre>
	 *
	 * <p>Using automatic naming, the same example would
	 * look like
	 * <pre>
	 * - 0 0 0
	 * - 1 0 0
	 * - 0 1 0
	 * - 0 0 1
	 * *
	 * - v0 v2 v1
	 * - v0 v3 v2
	 * - v0 v1 v3
	 * - v1 v2 v3
	 * </pre>
	 *
	 * @param reader supplies the information used to describe
	 * the polyhedron.
	 * @throws IOException if an I/O or format error occured
	 */
	public void scanNamedFormat(final Reader reader) throws IOException {
		final StreamTokenizer stok = new StreamTokenizer(reader);
		stok.commentChar('#');
		scanNamedFormat(stok);
	}

	/**
	 * Sets the polyhedron using information obtained from a
	 * StreamTokenizer. Otherwise identical to
	 * {@link #scanNamedFormat(Reader) scanNamedFormat(Reader)}.
	 *
	 * @param stok supplies the information used to describe
	 * the polyhedron.
	 * @throws IOException if an I/O or format error occured
	 * @see #scanNamedFormat(Reader)
	 */
	public void scanNamedFormat(final StreamTokenizer stok) throws IOException {
		String name = null;
		final Vector vlist = new Vector();
		final Vector facevertlist = new Vector(100);

		elist.clear();
		flist.clear();

		// scan vertices first
		while (true) {
			name = TokenScanner.scanWord(stok, "*-", false);
			if (name.equals("*"))
				break;
			else if (name.equals("-")) name = "v" + vlist.size();
			final double x = TokenScanner.scanDouble(stok);
			final double y = TokenScanner.scanDouble(stok);
			final double z = TokenScanner.scanDouble(stok);
			vlist.add(new Vertex(name, new Point3d(x, y, z)));
		}
		verts = (Vertex[]) vlist.toArray(new Vertex[0]);
		while (true) {
			String faceName = null;
			name = TokenScanner.scanWord(stok, "*-", false);
			if (name.equals("*"))
				break;
			else if (name.equals("-"))
				faceName = "f" + flist.size();
			else
				faceName = name;
			facevertlist.clear();
			stok.eolIsSignificant(true);
			while (true) {
				stok.nextToken();
				if (stok.ttype == StreamTokenizer.TT_NUMBER) {
					final int i = (int) stok.nval;
					if (i < 0 || i >= vlist.size()) throw new IOException("Illegal vertex index " + i);
					facevertlist.add(vlist.get(i));
				} else if (stok.ttype == StreamTokenizer.TT_WORD) {
					name = stok.sval;
					int i;
					for (i = 0; i < verts.length; i++)
						if (verts[i].name.equals(name)) {
							facevertlist.add(verts[i]);
							break;
						}
					if (i == verts.length) throw new IOException("No vertex " + name);
				} else if (stok.ttype == StreamTokenizer.TT_EOL)
					break;
				else
					throw new IOException("Expected name or index value, line " + stok.lineno());
			}
			stok.eolIsSignificant(false);
			addFace(faceName, facevertlist);
		}
		final String errorMsg = check();
		if (errorMsg != null) throw new IllegalArgumentException(errorMsg);
		optimize(); // optimize data structures
	}

	/**
	 * Sets the polyhedron using information obtained from a
	 * StreamTokenizer. Otherwise identical to
	 * {@link #scan(Reader) scan(Reader)}.
	 *
	 * @param stok supplies the information used to describe
	 * the polyhedron.
	 * @throws IOException if an I/O or format error occured
	 * @see #scan(Reader)
	 */
	public void scan(final StreamTokenizer stok) throws IOException {
		String keyword = null;
		final Vector vlist = new Vector();
		final Vector facevertlist = new Vector(100);

		elist.clear();
		flist.clear();

		do {
			keyword = TokenScanner.scanWord(stok, "*-" + TokenScanner.EOFstring, false);
			if (keyword.equals(TokenScanner.EOFstring)) break; // EOF
			if (keyword.equals("f")) {
				facevertlist.clear();
				stok.eolIsSignificant(true);
				while (true) {
					stok.nextToken();
					if (stok.ttype == StreamTokenizer.TT_NUMBER) {
						final int i = (int) stok.nval;
						if (i < 1 || i > vlist.size()) throw new IOException("Illegal vertex index " + i);
						facevertlist.add(vlist.get(i - 1));
					} else if (stok.ttype == StreamTokenizer.TT_EOL)
						break;
					else
						throw new IOException("Expected index value, line " + stok.lineno());
				}
				stok.eolIsSignificant(false);
				addFace("f" + flist.size(), facevertlist);
			} else if (keyword.equals("v")) {
				final int i = vlist.size();
				final double x = TokenScanner.scanDouble(stok);
				final double y = TokenScanner.scanDouble(stok);
				final double z = TokenScanner.scanDouble(stok);
				vlist.add(new Vertex("v" + i, new Point3d(x, y, z)));
			}
		} while (!keyword.equals("end"));
		verts = (Vertex[]) vlist.toArray(new Vertex[0]);
		final String errorMsg = check();
		if (errorMsg != null) throw new IllegalArgumentException(errorMsg);
		optimize(); // optimize data structures
	}

	/**
	 * Sets the polyhedron using information obtained
	 * from a reader. This information should be supplied
	 * in an Alias Wavefront <code>.obj</code> format:
	 *
	 * <p>First, the vertices of the polyhedra are listed, each
	 * preceded with the character <code>v</code>,
	 * followed by three numbers
	 * giving the coordinates. 
	 *
	 * <p>Second, the faces are listed, each
	 * preceded by the character <code>f</code>,
	 * followed by a counter-clockwise list of the vertex indices.
	 * Each index
	 * refers to the vertex's location within the
	 * vertex list, with the first vertex numbered as <code>1</code>.
	 * The keyword <code>end</code>
	 * is used to terminate the face list.
	 * 
	 * <p>A simple example defining a tetrahedron is
	 * <pre>
	 * v 0.0 0.0 0.0
	 * v 1.0 0.0 0.0
	 * v 0.0 1.0 0.0
	 * v 0.0 0.0 1.0
	 * f 0 2 1
	 * f 0 3 2
	 * f 0 1 3
	 * f 1 2 3
	 * end
	 * </pre>
	 *
	 * @param reader supplies the information used to describe
	 * the polyhedron.
	 * @throws IOException if an I/O or format error occured
	 */
	public void scan(final Reader reader) throws IOException {
		final StreamTokenizer stok = new StreamTokenizer(reader);
		stok.commentChar('#');
		scan(stok);
	}

	private void optimize() {
		for (final Vertex vert : verts) {
			VertexConeNode prev = null;
			for (final Iterator ci = vert.cone.iterator(); ci.hasNext();) {
				final VertexConeNode vcn = (VertexConeNode) ci.next();
				if (prev == null)
					vert.coneNode0 = vcn;
				else
					prev.next = vcn;
				prev = vcn;
			}
		}

		for (final Face face : faces) {
			FaceConeNode prev = null;
			for (final Iterator ci = face.cone.iterator(); ci.hasNext();) {
				final FaceConeNode fcn = (FaceConeNode) ci.next();
				if (prev == null)
					face.coneNode0 = fcn;
				else
					prev.next = fcn;
				prev = fcn;
			}
		}
	}

	/** Accessors */

	/**
	 * Returns the vertex features for this polyhedron.
	 *
	 * @return vertex feature array
	 */
	public final Vertex[] getVerts() {
		return verts;
	}

	public final Vertex getVert(final int idx) {
		return verts[idx];
	}

	/**
	 * Returns the edge features for this polyhedron.
	 *
	 * @return edge feature array
	 */
	public final Edge[] getEdges() {
		return edges;
	}

	public final Edge getEdge(final int vidx1, final int vidx2) {
		final Vertex v1 = verts[vidx1];
		final Vertex v2 = verts[vidx2];
		for (final Edge e : edges)
			if (e.head == v1 && e.tail == v2 || e.tail == v1 && e.head == v2) return e;
		return null;
	}

	/**
	 * Returns the face features for this polyhedron.
	 *
	 * @return face feature array
	 */
	public final Face[] getFaces() {
		return faces;
	}

	public final Face getFace(final int idx) {
		return faces[idx];
	}

	/**
	 * Returns the total number of features (vertices, edges,
	 * faces) on this polyhedron.
	 *
	 * @return number of features
	 */
	public final int numFeatures() {
		return numFeats;
	}

	void addFace(final String name, final Vector verts) {
		addFace(name, verts, 0);
	}

	/** 
	 * adds a Face to this ConvexPolyhedron
	 * 
	 * @param name name of face (String)
	 * @param verts list of this face's vertices (Vector of class Vertex)
	 * @param clockwise [optional, default = ccw] 0 = ccw, 1 = cw (int)
	 */
	void addFace(final String name, final Vector verts, final int clockwise) {
		final int i;
		int vi, cni;
		Face f0, f;
		FaceConeNode last;
		Vector3dX u, v, normal;

		f0 = new Face();
		f0.sides = verts.size();
		f0.setName(name);

		// compute face support plane
		u = new Vector3dX();
		v = new Vector3dX();
		normal = new Vector3dX();
		u.sub(((Vertex) verts.get(1)).coords, ((Vertex) verts.get(0)).coords);
		v.sub(((Vertex) verts.get(2)).coords, ((Vertex) verts.get(1)).coords);
		normal.cross(u, v);
		normal.normalize();
		if (clockwise == 1) normal.negate();

		f0.plane.set(normal, ((Vertex) verts.get(0)).coords);

		// add to list of faces
		flist.add(f0);
		f = f0;

		// build edges around face
		if (clockwise == 1) {
			for (vi = verts.size() - 1; vi > 0; vi--)
				processEdge(f, (Vertex) verts.get(vi), (Vertex) verts.get(vi - 1));
			// close the loop
			processEdge(f, (Vertex) verts.firstElement(), (Vertex) verts.lastElement());
		} else {
			for (vi = 0; vi < verts.size() - 1; vi++)
				processEdge(f, (Vertex) verts.get(vi), (Vertex) verts.get(vi + 1));
			// close the loop
			processEdge(f, (Vertex) verts.lastElement(), (Vertex) verts.firstElement());
		}

		// compute ccw and cw links around FaceConeNodes, 
		// cn indices, and f->sides
		for (cni = 0, last = (FaceConeNode) f.cone.lastElement(); cni < f.cone.size(); last = (FaceConeNode) f.cone
				.get(cni), ++cni) {
			last.ccw = (FaceConeNode) f.cone.get(cni);
			last.ccw.cw = last;
			last.ccw.idx = cni;
		}
	}

	/**
	 * Checks the convexity of this ConvexPolyhedron and verifies
	 * the Euler formula. Also assignes each feature an index.
	 *
	 * @return <code>null</code> if there is no problem,
	 * otherwise, an error message.
	 */
	String check() {
		int nv, ne, nf;
		Edge e, e1, e2;
		Face f;
		FaceConeNode fcn;
		double dp;
		final Vector3dX v = new Vector3dX();

		String errMsg = "";

		edges = (Edge[]) elist.toArray(new Edge[0]);
		faces = (Face[]) flist.toArray(new Face[0]);
		elist.clear();
		flist.clear();

		// assign an index to each vertex
		for (int i = 0; i < verts.length; i++)
			verts[i].index = numFeats++;

		// check to make sure all edges are convex. Identify
		// "coplanar" edges. Assign each edge an index
		for (final Edge edge : edges) {
			e = edge;
			v.cross(e.lplane.normal, e.rplane.normal);
			if ((dp = e.dir.dot(v)) >= 0)
				if (dp > EPS)
					errMsg += "\tnonconvex edge:  " + " tail=" + e.tail.name + " head=" + e.head.name + " left="
							+ e.left.name + " rght=" + e.right.name + " angle=" + java.lang.Math.asin(-dp)
							+ " dot=" + dp + "\n";
				else { // simply hack the edge
					v.negate(e.lplane.getNormal());
					e.rplane.set(v, -e.lplane.getOffset());
					v.cross(e.lplane.normal, e.rplane.normal);
					if (e.dir.dot(v) != 0) errMsg += "Unable to fix coplanar edge";
					e.isCoplanar = true;
					// set up a bottom clipping plane
					v.cross(e.lplane.normal, e.dir);
					v.normalize();
					e.bplane = new Plane(v, e.tail.coords);
				}
			e.index = numFeats++;
		}

		// check to make sure all faces are convex polygons.
		// Assign each face an index
		for (final Face face : faces) {
			f = face;
			for (final Iterator it = f.cone.iterator(); it.hasNext();) {
				fcn = (FaceConeNode) it.next();

				e1 = fcn.nbr;
				e2 = fcn.ccw.nbr;
				v.cross(e1.dir, e2.dir);
				if (e1.tail == e2.tail || e1.head == e2.head) v.negate();
				if ((dp = v.dot(f.plane.normal)) <= -EPS)
					errMsg += "\tnonconvex face:  " + f.name + "  vertex="
							+ (e1.left == f ? e1.head : e1.tail).name + "  angle=" + java.lang.Math.asin(-dp)
							+ "\n";
			}
			f.index = numFeats++;
		}

		// Check if Euler formula (#V - #E + #F - 2 = 0) is satisfied
		nv = verts.length;
		ne = edges.length;
		nf = faces.length;
		if (nv - ne + nf - 2 != 0)
			errMsg += "\tpolyhedral Euler formula failure: " + "nv=" + nv + " ne=" + ne + " nf=" + nf + "\n";

		if (errMsg.equals(""))
			return null;
		else
			return "Malformed polyhedron\n" + errMsg;
	}

	/**
	 * Outputs a string representation of this polyhedron,
	 * using the format
	 * used by {@link #scan(Reader) scan(Reader)}.
	 *
	 * @return <code>.obj</code> representation of this polyhedron
	 */
	public String sprintf() {
		String s = "";
		for (final Vertex vert : verts)
			s += "v " + vert.coords.x + " " + vert.coords.y + " " + vert.coords.z + "\n";
		for (int i = 0; i < faces.length; i++) {
			s += "f ";
			FaceConeNode fcn;
			for (fcn = faces[i].coneNode0; fcn != null; fcn = fcn.next) {
				Vertex vtx;
				if (faces[i] == fcn.nbr.left)
					vtx = fcn.nbr.head;
				else
					vtx = fcn.nbr.tail;
				for (int k = 0; k < verts.length; k++)
					if (vtx == verts[k]) s += k + 1 + " ";
			}
			if (i < faces.length - 1) s += "\n";
		}
		return s;
	}

	/**
	 * Outputs a string representation of this polyhedron,
	 * using the format
	 * used by {@link #scanNamedFormat(Reader) scanNamedFormat(Reader)}.
	 *
	 * @return named format representation of this polyhedron
	 */
	public String sprintfNamedFormat() {
		String s = "";
		for (final Vertex vert : verts)
			s += vert.name + " " + vert.coords.x + " " + vert.coords.y + " " + vert.coords.z + "\n";
		s += "*\n";
		for (final Face face : faces) {
			s += face.name + " ";
			FaceConeNode fcn;
			for (fcn = face.coneNode0; fcn != null; fcn = fcn.next) {
				Vertex vtx;
				if (face == fcn.nbr.left)
					vtx = fcn.nbr.head;
				else
					vtx = fcn.nbr.tail;
				for (final Vertex vert : verts)
					if (vtx == vert) s += vert.name + " ";
			}
			s += "\n";
		}
		s += "*";
		return s;
	}

	/**
	 * Generates a string representation of this polyhedron,
	 * using {@link #sprintfNamedFormat() sprintfNamedFormat()}.
	 *
	 * @return named format representation of this polyhedron
	 * @see #sprintfNamedFormat
	 */
	@Override
	public String toString() {
		return sprintfNamedFormat();
	}

	/* Private Methods */

	/** processEdge
	 *
	 * This is called when the vertex sequence (tail, head) is
	 * encountered in Face f's boundary walk.  Either a new edge
	 * is created and added to the edgeList, or the previously
	 * existing one from head to tail is updated.
	 * @param f (Face)
	 * @param tail (Vertex)
	 * @param head (Vertex)
	 */
	private void processEdge(final Face f, final Vertex tail, final Vertex head) {
		int vci;
		final int fci;
		final VertexConeNode vcnTail = new VertexConeNode();
		final VertexConeNode vcnHead = new VertexConeNode();
		final FaceConeNode fcn = new FaceConeNode();
		Edge e, e0;
		final Vector3dX v = new Vector3dX();

		// check if the reverse edge (from head to tail) already exists
		for (vci = 0; vci < head.cone.size(); vci++) {
			e = ((VertexConeNode) head.cone.get(vci)).nbr;
			if (e.head == tail) { // set pointer to right, rplane
				e.right = f;
				v.cross(e.dir, f.plane.normal);
				v.normalize();
				e.rplane.set(v, head.coords);
				// tell right about e
				fcn.nbr = e;
				fcn.plane = e.rplane;
				f.cone.add(fcn);
				return;
			}
		}

		// set direction, length
		e0 = new Edge();
		e0.setName(tail.name + ":" + head.name);
		e0.dir.sub(head.coords, tail.coords);
		e0.len = e0.dir.length();
		e0.dir.normalize();
		// set pointers to tail, head, left
		e0.tail = tail;
		e0.head = head;
		e0.left = f;
		// set tplane, hplane, and lplane
		v.negate(e0.dir);
		e0.tplane.set(v, tail.coords);
		e0.hplane.set(e0.dir, head.coords);
		v.cross(f.plane.normal, e0.dir);
		v.normalize();
		e0.lplane.set(v, tail.coords);
		// link into list of edges
		elist.add(e0);
		e = e0;
		// tell tail about e
		vcnTail.nbr = e;
		vcnTail.plane = e.tplane;
		tail.cone.add(vcnTail);
		// tell head about e
		vcnHead.nbr = e;
		vcnHead.plane = e.hplane;
		head.cone.add(vcnHead);
		// tell left about e
		fcn.nbr = e;
		fcn.plane = e.lplane;
		f.cone.add(fcn);
	}

	private int vertVertTest(final FeatureBlock vb1, final FeatureBlock vb2, final DoublePtr dist) {
		final Vector cone; // vector of VertexConeNode
		final Point3dX xcoords;
		final Vertex vtx1 = vb1.vert;
		final Vertex vtx2 = vb2.vert;

		// check if v2 lies in v1's cone
		for (VertexConeNode vcn = vtx1.coneNode0; vcn != null; vcn = vcn.next)
			if (vcn.plane.distance(vb2.xcoords) < 0) {
				vb1.setEdge(vcn.nbr);
				// System.out.println ("VV continue 0");
				return CONTINUE;
			}

		// check if v1 lies in v2's cone
		for (VertexConeNode vcn = vtx2.coneNode0; vcn != null; vcn = vcn.next)
			if (vcn.plane.distance(vb1.xcoords) < 0) {
				vb2.setEdge(vcn.nbr);
				// System.out.println ("VV continue 1");
				return CONTINUE;
			}

		vb1.pnt.set(vtx1.coords);
		vb2.pnt.set(vtx2.coords);
		dist.d = vb2.pnt.distance(vb1.xcoords);
		// System.out.println ("VV disjoint or penetration");
		return dist.d > 0 ? DISJOINT : PENETRATION; // (dist could be 0)
	}

	private int vertFaceTest(final FeatureBlock vb, final FeatureBlock xf, final Face[] allFaces, final DoublePtr dist) {
		int update;
		Edge e;
		final Vector vcone; // Vector of VertexConeNode
		Vector fcone; // Vector of FaceConeNode
		double d, d2, dmin;

		// check if v lies in f's cone
		update = 0;
		dmin = 0.0;
		fcone = xf.face.cone;

		for (FaceConeNode fcn = xf.face.coneNode0; fcn != null; fcn = fcn.next)
			if ((d = fcn.plane.distance(vb.xcoords)) < dmin) {
				xf.setEdge(fcn.nbr);
				dmin = d;
				update = 1;
			}
		if (update == 1) return CONTINUE;

		// check that none of the edges of v point toward f
		if ((d = xf.face.plane.distance(vb.xcoords)) == 0) {
			vb.pnt.set(vb.vert.coords);
			xf.pnt.set(vb.xcoords);
			dist.d = 0;
			// System.out.println ("VF penetration 0");
			return PENETRATION;
		}

		for (VertexConeNode vcn = vb.vert.coneNode0; vcn != null; vcn = vcn.next) {
			e = vcn.nbr;
			vb.T.transform(e.tail == vb.feat ? e.head.coords : e.tail.coords, vb.xpnt);
			d2 = xf.face.plane.distance(vb.xpnt);
			if (d < 0 && d2 > d || d > 0 && d2 < d) {
				if (e.tail == vb.feat) {
					vb.xtail.set(vb.xcoords);
					vb.xhead.set(vb.xpnt);
				} else {
					vb.xtail.set(vb.xpnt);
					vb.xhead.set(vb.xcoords);
				}
				vb.xseg.sub(vb.xhead, vb.xtail);
				vb.setEdge(e);
				// System.out.println ("VF continue 1");
				return CONTINUE;
			}
		}
		if (d > 0) {
			dist.d = d;
			vb.pnt.set(vb.vert.coords);
			xf.pnt.scaleAdd(-d, xf.face.plane.normal, vb.xcoords);
			// System.out.println ("VF disjoint 0");
			return DISJOINT;
		}

		// v is in local min on back side of f's cone
		for (final Face facei : allFaces)
			if ((d2 = facei.plane.distance(vb.xcoords)) > d) {
				d = d2;
				xf.setFace(facei);
			}
		if (d > 0) return CONTINUE;
		dist.d = d;
		vb.pnt.set(vb.vert.coords);
		xf.pnt.scaleAdd(-d, xf.face.plane.normal, vb.xcoords);
		// System.out.println ("VF penetration 1");
		return PENETRATION;
	}

	private int vertEdgeTest(final FeatureBlock vb, final FeatureBlock eb, final DoublePtr dist) {
		Edge minNbr, maxNbr;
		double min, max, lambda, dt, dh;

		// check if v lies within edge cone planes
		double ld, rd;
		if (eb.edge.tplane.distance(vb.xcoords) > 0) {
			eb.setVert(eb.edge.tail);
			// System.out.println ("VE continue 0");
			return CONTINUE;
		}
		if (eb.edge.hplane.distance(vb.xcoords) > 0) {
			eb.setVert(eb.edge.head);
			// System.out.println ("VE continue 1");
			return CONTINUE;
		}
		if ((ld = eb.edge.lplane.distance(vb.xcoords)) > 0) {
			eb.setFace(eb.edge.left);
			// System.out.println ("VE continue 2");
			return CONTINUE;
		}
		if ((rd = eb.edge.rplane.distance(vb.xcoords)) > 0) {
			eb.setFace(eb.edge.right);
			// System.out.println ("VE continue 3");
			return CONTINUE;
		}

		if (ld == 0 && rd == 0) { // edge planes must be coplanar in this case,
			// so we check v WRT the plane
			eb.xvec.cross(eb.edge.dir, eb.edge.lplane.normal);
			final double d = eb.xvec.dot(vb.xcoords);
			if (d < 0) {
				eb.setFace(eb.edge.left);
				// System.out.println ("VE continue 4");
				return CONTINUE;
			} else if (d == 0) {
				vb.pnt.set(vb.vert.coords);
				eb.pnt.set(vb.xcoords);
				dist.d = 0;
				// System.out.println ("VE penetration 0");
				return PENETRATION;
			}
		}

		// clip e against v's cone
		min = 0;
		max = 1;
		minNbr = maxNbr = null;

		final int i = 0;

		VertexConeNode vcn = null;
		for (vcn = vb.vert.coneNode0; vcn != null; vcn = vcn.next) {
			dt = vcn.plane.distance(eb.xtail);
			dh = vcn.plane.distance(eb.xhead);

			if (dt >= 0) {
				if (dh >= 0) continue;
				if ((lambda = dt / (dt - dh)) < max) {
					max = lambda;
					maxNbr = vcn.nbr;
					if (max < min) break;
				}
			} else { // dt < 0
				if (dh < 0) {
					minNbr = maxNbr = vcn.nbr;
					break;
				}
				if ((lambda = dt / (dt - dh)) > min) {
					min = lambda;
					minNbr = vcn.nbr;
					if (min > max) break;
				}
			}
		}

		if (vcn != null && minNbr == maxNbr) {
			vb.setEdge(minNbr);
			// System.out.println ("VE continue 5");
			return CONTINUE;
		}

		// analyze derivatives at boundaries
		if (minNbr != null || maxNbr != null) {
			if (minNbr != null) {
				eb.xvec.scaleAdd(min, eb.xseg, eb.xtail);
				eb.xvec.sub(vb.vert.coords);
				if (eb.xvec.equals(zeroVect)) {
					vb.pnt.set(vb.vert.coords);
					eb.pnt.set(vb.xcoords);
					dist.d = 0;
					// System.out.println ("VE penetration 1");
					return PENETRATION;
				}
				if (eb.xvec.dot(eb.xseg) > 0) {
					vb.setEdge(minNbr);
					// System.out.println ("VE continue 6");
					return CONTINUE;
				}
			}
			if (maxNbr != null) {
				eb.xvec.scaleAdd(max, eb.xseg, eb.xtail);
				eb.xvec.sub(vb.vert.coords);
				if (eb.xvec.equals(zeroVect)) {
					vb.pnt.set(vb.vert.coords);
					eb.pnt.set(vb.xcoords);
					dist.d = 0;
					// System.out.println ("VE penetration 2");
					return PENETRATION;
				}
				if (eb.xvec.dot(eb.xseg) < 0) {
					vb.setEdge(maxNbr);
					// System.out.println ("VE continue 7");
					return CONTINUE;
				}
			}
		}

		vb.pnt.set(vb.vert.coords);
		vb.xvec.sub(vb.xcoords, eb.edge.tail.coords);
		eb.pnt.scaleAdd(vb.xvec.dot(eb.edge.dir), eb.edge.dir, eb.edge.tail.coords);
		dist.d = eb.pnt.distance(vb.xcoords);
		// System.out.println ("VE disjoint 0");
		return DISJOINT;
	}

	private int edgeEdgeSubtest(final FeatureBlock eb1, final FeatureBlock eb2, final Face[] facelist1, Point3dX cp) {
		int i;
		Vertex vminNbr = null;
		Vertex vmaxNbr = null;
		Face fminNbr = null;
		Face fmaxNbr = null;
		Face nbr;
		Plane plane;
		double dt, dh, lambda, min, max, vmin, vmax, dmin, dmax;
		min = 0;
		max = 1;
		vmin = 0;
		vmax = 1;

		// clip against tail vertex plane
		dt = -eb1.edge.tplane.distance(eb2.xtail);
		dh = -eb1.edge.tplane.distance(eb2.xhead);
		if (dt < 0) {
			if (dh < 0) {
				eb1.setVert(eb1.edge.tail);
				// System.out.println ("EE continue 0");
				return CONTINUE;
			}
			min = dt / (dt - dh);
			vminNbr = eb1.edge.tail;
		} else if (dh < 0) {
			max = dt / (dt - dh);
			vmaxNbr = eb1.edge.tail;
		}

		// clip against head vertex plane
		dt = -eb1.edge.hplane.distance(eb2.xtail);
		dh = -eb1.edge.hplane.distance(eb2.xhead);
		if (dt < 0) {
			if (dh < 0) {
				eb1.setVert(eb1.edge.head);
				// System.out.println ("EE continue 1");
				return CONTINUE;
			}
			min = dt / (dt - dh);
			vminNbr = eb1.edge.head;
		} else if (dh < 0) {
			max = dt / (dt - dh);
			vmaxNbr = eb1.edge.head;
		}

		if (vminNbr != null) vmin = min;
		if (vmaxNbr != null) vmax = max;

		boolean outside = false;
		// clip against left & right face planes
		for (i = 0; i < 2; i++) {
			if (i > 0) {
				plane = eb1.edge.rplane;
				nbr = eb1.edge.right;
			} else {
				plane = eb1.edge.lplane;
				nbr = eb1.edge.left;
			}
			dt = -plane.distance(eb2.xtail);
			dh = -plane.distance(eb2.xhead);

			if (dt < 0) {
				if (dh < 0) { // completely clipped by a face plane - check vertex derivs
					if (vminNbr != null) {
						eb2.xpnt.scaleAdd(vmin, eb2.xseg, eb2.xtail);
						eb2.xpnt.sub(vminNbr.coords);
						if (eb2.xpnt.equals(zeroVect)) {
							cp = vminNbr.coords;
							// System.out.println ("EE penetration 0");
							return PENETRATION;
						}
						if (eb2.xpnt.dot(eb2.xseg) > 0) {
							eb1.setVert(vminNbr);
							// System.out.println ("EE continue 2");
							return CONTINUE;
						}
					}
					if (vmaxNbr != null) {
						eb2.xpnt.scaleAdd(vmax, eb2.xseg, eb2.xtail);
						eb2.xpnt.sub(vmaxNbr.coords);
						if (eb2.xpnt.equals(zeroVect)) {
							cp = vmaxNbr.coords;
							// System.out.println ("EE penetration 1");
							return PENETRATION;
						}
						if (eb2.xpnt.dot(eb2.xseg) < 0) {
							eb1.setVert(vmaxNbr);
							// System.out.println ("EE continue 3");
							return CONTINUE;
						}
					}
					eb1.setFace(nbr);
					// System.out.println ("EE continue 4");
					return CONTINUE;
				} else if ((lambda = dt / (dt - dh)) > min) {
					min = lambda;
					fminNbr = nbr;
					vminNbr = null;
					if (min > max) {
						outside = true;
						break;
					}
				}
			} else if (dh < 0) if ((lambda = dt / (dt - dh)) < max) {
				max = lambda;
				fmaxNbr = nbr;
				vmaxNbr = null;
				if (max < min) {
					outside = true;
					break;
				}
			}
		}

		double ddmin = 0, ddmax = 0;
		if (min <= max && eb1.edge.bplane != null) {
			final Point3dX maxpnt = eb2.xpnt;
			final Point3dX minpnt = eb1.xpnt;

			maxpnt.interpolate(eb2.xtail, eb2.xhead, max);
			minpnt.interpolate(eb2.xtail, eb2.xhead, min);
			ddmax = eb1.edge.bplane.distance(maxpnt);
			ddmin = eb1.edge.bplane.distance(minpnt);

			if (ddmax > 0 && ddmin > 0)
				outside = true;
			else if (ddmax * ddmin <= 0) { // penetration
				lambda = ddmin == 0 ? 0.5 : ddmin / (ddmin - ddmax);
				eb2.pnt.interpolate(minpnt, maxpnt, lambda);
				// System.out.println ("EE penetration 2");
				return PENETRATION;
			}
		}

		if (outside) { // edge lies outside the voronoi region

			if (vminNbr != null) {
				eb2.xpnt.scaleAdd(min, eb2.xseg, eb2.xtail);
				eb2.xpnt.sub(vminNbr.coords);
				if (eb2.xpnt.equals(zeroVect)) {
					cp = vminNbr.coords;
					// System.out.println ("EE penetration 3");
					return PENETRATION;
				}
				if (eb2.xpnt.dot(eb2.xseg) >= 0)
					eb1.setVert(vminNbr);
				else if (vmaxNbr != null)
					eb1.setVert(vmaxNbr);
				else if (fmaxNbr != null)
					eb1.setFace(fmaxNbr);
				else
					eb1.setVert(vminNbr);
				// System.out.println ("EE continue 5");
				return CONTINUE;
			}
			if (vmaxNbr != null) {
				eb2.xpnt.scaleAdd(max, eb2.xseg, eb2.xtail);
				eb2.xpnt.sub(vmaxNbr.coords);
				if (eb2.xpnt.equals(zeroVect)) {
					cp = vmaxNbr.coords;
					// System.out.println ("EE penetration 4");
					return PENETRATION;
				}
				if (eb2.xpnt.dot(eb2.xseg) <= 0)
					eb1.setVert(vmaxNbr);
				else if (vminNbr != null)
					eb1.setVert(vminNbr);
				else if (fminNbr != null)
					eb1.setFace(fminNbr);
				else
					eb1.setVert(vmaxNbr);
				// System.out.println ("EE continue 6");
				return CONTINUE;
			}

			if (fmaxNbr == null && fminNbr == null) // and edge2 is parallel and beneath it.
			{
				if (eb2.edge.hplane.distance(eb1.xhead) > 0 || eb2.edge.hplane.distance(eb1.xtail) > 0
						|| eb2.edge.tplane.distance(eb1.xhead) > 0 || eb2.edge.tplane.distance(eb1.xtail) > 0)
					return DISJOINT;
				else {
					eb1.setVert(eb1.edge.head);
					// System.out.println ("EE continue 7");
					return CONTINUE;
				}
			} else if (fminNbr == null) {
				eb1.setFace(fmaxNbr);
				// System.out.println ("EE continue 8");
				return CONTINUE;
			} else if (fmaxNbr == null) {
				eb1.setFace(fminNbr);
				// System.out.println ("EE continue 9");
				return CONTINUE;
			}
			// complete clipping by combination of both face planes
			dt = fminNbr.plane.distance(eb2.xtail);
			dh = fminNbr.plane.distance(eb2.xhead);
			dmin = dt + min * (dh - dt);

			if (dmin == 0) {
				cp.scaleAdd(min, eb2.xseg, eb2.xtail);
				// System.out.println ("EE penetration 5");
				return PENETRATION;
			}

			if (dt < 0 && dh < 0) {
				final double eps = 10 * DBL_EPSILON * eb2.edge.len;
				if (Math.abs(dt - dh) < eps) return edgeEdgeLocalMinEscape(eb2, eb1, facelist1);
			}

			eb1.setFace(dmin > 0 ? (dt < dh ? fminNbr : fmaxNbr) : dt > dh ? fminNbr : fmaxNbr);
			// System.out.println ("EE continue 10");
			return CONTINUE;
		}

		// edge intersects V-region; analyze derivs

		double dotmax = Double.NEGATIVE_INFINITY;
		double dotmin = Double.POSITIVE_INFINITY;

		if (fminNbr != null) {
			dt = fminNbr.plane.distance(eb2.xtail);
			dh = fminNbr.plane.distance(eb2.xhead);
			dmin = dt + min * (dh - dt);
			dmax = fmaxNbr != null || vmaxNbr != null ? dt + max * (dh - dt) : dh;
			if (dmin == 0) {
				cp.scaleAdd(min, eb2.xseg, eb2.xtail);
				// System.out.println ("EE penetration 6");
				return PENETRATION;
			}
			// evaluate derivative using the plane normal
			// This is more robust than in the orginal vclip
			dotmin = eb2.xseg.dot(fminNbr.plane.getNormal()) * dmin;
			if (dotmin > 0) {
				eb1.setFace(fminNbr);
				// System.out.println ("EE continue 11");
				return CONTINUE;
			}
		} else if (vminNbr != null) {
			eb2.xpnt.scaleAdd(min, eb2.xseg, eb2.xtail);
			eb2.xpnt.sub(vminNbr.coords);
			if (eb2.xpnt.equals(zeroVect)) {
				cp = vminNbr.coords;
				// System.out.println ("EE penetration 7");
				return PENETRATION;
			}
			if (eb2.xpnt.dot(eb2.xseg) > 0) {
				eb1.setVert(vminNbr);
				// System.out.println ("EE continue 12");
				return CONTINUE;
			}
		}

		if (fmaxNbr != null) {
			dt = fmaxNbr.plane.distance(eb2.xtail);
			dh = fmaxNbr.plane.distance(eb2.xhead);
			dmin = fminNbr != null || fmaxNbr != null ? dt + min * (dh - dt) : dt;
			dmax = dt + max * (dh - dt);
			if (dmax == 0) {
				cp.scaleAdd(max, eb2.xseg, eb2.xtail);
				// System.out.println ("EE penetration 8");
				return PENETRATION;
			}
			// evaluate derivative using the plane normal
			// This is more robust than in the orginal vclip
			dotmax = eb2.xseg.dot(fmaxNbr.plane.getNormal()) * dmax;
			if (dotmax < 0) {
				eb1.setFace(fmaxNbr);
				// System.out.println ("EE continue 13");
				return CONTINUE;
			}
		} else if (vmaxNbr != null) {
			eb2.xpnt.scaleAdd(max, eb2.xseg, eb2.xtail);
			eb2.xpnt.sub(vmaxNbr.coords);
			if (eb2.xpnt.equals(zeroVect)) {
				cp = vmaxNbr.coords;
				// System.out.println ("EE penetration 9");
				return PENETRATION;
			}
			if (eb2.xpnt.dot(eb2.xseg) < 0) {
				eb1.setVert(vmaxNbr);
				// System.out.println ("EE continue 14");
				return CONTINUE;
			}
		}
		// System.out.println ("EE disjoint 1");
		return DISJOINT;
	}

	private int edgeEdgeTest(final FeatureBlock eb1, final FeatureBlock eb2, final Face[] faceList1,
			final Face[] faceList2, final DoublePtr dist) {
		int res;
		double k, lambda, num, denom;

		// clip e1 against e2's cone
		if ((res = edgeEdgeSubtest(eb2, eb1, faceList2, eb2.pnt)) == PENETRATION) {
			eb2.T.transform(eb2.pnt, eb1.pnt);
			dist.d = 0;
		}
		if (res != DISJOINT) return res;
		// clip e2 against e1's cone
		if ((res = edgeEdgeSubtest(eb1, eb2, faceList1, eb1.pnt)) == PENETRATION) {
			eb1.T.transform(eb1.pnt, eb2.pnt);
			dist.d = 0;
		}
		if (res != DISJOINT) return res;

		// disjoint - compute closest points & distance

		eb2.T.transform(eb2.edge.dir, eb2.xvec);
		k = eb2.xvec.dot(eb1.edge.dir);
		eb1.xvec.scaleAdd(-k, eb2.xvec, eb1.edge.dir);
		final Point3dX tp2 = eb2.xtail;
		final Point3dX tp1 = eb1.edge.tail.coords;
		num = eb1.xvec.x * (tp2.x - tp1.x) + eb1.xvec.y * (tp2.y - tp1.y) + eb1.xvec.z * (tp2.z - tp1.z);
		denom = 1 - k * k;
		if (denom == 0.0) { // then the two edges are parallel. Since they are
			// the closest features, they must also have at least
			// one point of overlap. If e1.head is in the overlap
			// zone, we chose it as the closest point. Otherwise,
			// we chose e1.tail
			if (k > 0)
				eb1.xvec.sub(eb2.xhead, eb1.edge.head.coords);
			else
				eb1.xvec.sub(eb2.xtail, eb1.edge.head.coords);
			if (eb1.xvec.dot(eb1.edge.dir) >= 0)
				eb1.pnt.set(eb1.edge.head.coords);
			else
				eb1.pnt.set(eb1.edge.tail.coords);
		} else {
			lambda = num / denom;
			if (lambda < 0)
				lambda = 0;
			else if (lambda > eb1.edge.len) lambda = eb1.edge.len;
			eb1.pnt.scaleAdd(lambda, eb1.edge.dir, eb1.edge.tail.coords);
		}
		// now compute pnt2
		eb1.T.transform(eb1.pnt, eb1.xpnt);
		eb2.xvec.sub(eb1.xpnt, eb2.edge.tail.coords);
		lambda = eb2.xvec.dot(eb2.edge.dir);
		eb2.pnt.scaleAdd(lambda, eb2.edge.dir, eb2.edge.tail.coords);

		final Point3d p = new Point3d(eb2.pnt);
		eb2.T.transform(p);
		dist.d = eb2.pnt.distance(eb1.xpnt);
		// System.out.println ("EE disjoint 2");
		return DISJOINT;
	}

	private int edgeEdgeLocalMinEscape(final FeatureBlock eb1, // edge that is "below" 
			final FeatureBlock eb2, final Face[] allFaces2) {
		double min = 0;
		double max = 1;
		double maxd = Double.NEGATIVE_INFINITY;
		Face maxFace = allFaces2[0];
		double lambda;

		for (final Face facei : allFaces2) {
			final double dt = facei.plane.distance(eb1.xtail);
			final double dh = facei.plane.distance(eb1.xhead);

			if (dt > maxd) {
				maxd = dt;
				maxFace = facei;
			}
			if (dh > maxd) {
				maxd = dh;
				maxFace = facei;
			}
			if (dh > 0 && dt > 0) {
				max = 0.0;
				min = 1.0;
			}
			lambda = dt / (dt - dh);
			if (dh > 0) {
				if (lambda < max) max = lambda;
			} else if (lambda > min) min = lambda;
		}

		if (max < min) { // then we are disjoint
			eb2.setFace(maxFace);
			// System.out.println ("EE continue 15");
			return CONTINUE;
		} else { // penetration
			eb1.pnt.interpolate(eb1.edge.tail.coords, eb1.edge.head.coords, min);
			eb2.pnt.interpolate(eb1.xtail, eb1.xhead, min);
			// System.out.println ("EE continue 10");
			return PENETRATION;
		}
	}

	private int loopEscape(final FeatureBlock fblk1, final FeatureBlock fblk2, final ConvexPolyhedron poly2,
			final DoublePtr dist) {
		final Vector3dX d12 = new Vector3dX();
		final Vector3dX dvtx = new Vector3dX();
		final Point3dX xavgVtx2 = new Point3dX();

		fblk2.T.transform(poly2.avgVtx, xavgVtx2);
		d12.sub(xavgVtx2, avgVtx);
		final double dist12 = d12.length();
		if (dist12 == 0) {
			dist.d = 0;
			return PENETRATION;
		}
		d12.scale(1 / dist12);

		//	   System.out.println ("d12: " + d12.toString ("8.3"));

		double maxDot = Double.NEGATIVE_INFINITY;
		Vertex vert = null;
		for (final Vertex vert2 : verts) {
			dvtx.sub(vert2.coords, avgVtx);
			final double d = d12.dot(dvtx);
			//  	      System.out.println (verts[i].getName() + " " +
			//  				  dvtx.toString ("8.3") + " d=" + d);
			if (d > maxDot) {
				maxDot = d;
				vert = vert2;
			}
		}
		fblk1.setVert(vert);

		double minDot = Double.POSITIVE_INFINITY;
		vert = null;

		for (final Vertex vert2 : poly2.verts) {
			dvtx.sub(vert2.coords, poly2.avgVtx);
			fblk2.T.transform(dvtx);
			final double d = d12.dot(dvtx);
			//  	      System.out.println (poly2.verts[i].getName() + " " +
			//  				  dvtx.toString ("8.3") + " d=" + d);
			if (d < minDot) {
				minDot = d;
				vert = vert2;
			}
		}
		fblk2.setVert(vert);

		if (maxDot - minDot < dist12) { // then d12 is the normal of a separating plane and
			// so we know that the polyhedra are disjoint.

		}

		return CONTINUE;
	}

	private int edgeFaceTest(final FeatureBlock eb, final FeatureBlock fb, final DoublePtr dist) {
		final int INSIDE = 1;
		final int OUTSIDE = 2;
		final int MIN = 3;
		final int MAX = 4;

		final int i;
		int intersect;
		FaceConeNode cn, prev, next, maxCn, minCn, chopCn;
		Edge s = null;
		Vertex minv, maxv;
		double lambda, min, max, dt, dh, dmin, dmax;
		int c = 0;
		double l = 0;

		min = 0;
		max = 1;
		minCn = maxCn = chopCn = null;

		for (FaceConeNode fcn = fb.face.coneNode0; fcn != null; fcn = fcn.next) {
			dt = fcn.plane.distance(eb.xtail);
			dh = fcn.plane.distance(eb.xhead);
			if (dt >= 0)
				if (dh >= 0)
					c = INSIDE;
				else { // dh < 0
					c = MAX;
					if ((l = dt / (dt - dh)) < max) {
						max = l;
						maxCn = fcn;
					}
				}
			else // dt < 0
			if (dh >= 0) {
				c = MIN;
				if ((l = dt / (dt - dh)) > min) {
					min = l;
					minCn = fcn;
				}
			} else { // dh < 0
				c = OUTSIDE;
				chopCn = fcn;
			}
			fcn.code = c;
			fcn.lam = l;
		}

		if (chopCn != null || min > max) {
			if (chopCn != null)
				cn = chopCn;
			else
				cn = min + max > 1.0 ? minCn : maxCn;

			prev = null;
			next = cn;
			intersect = 0;
			while (next != prev) {
				prev = cn;
				cn = next;
				s = cn.nbr;
				minv = maxv = null;

				// test edge plane
				final int val = cn.code;

				if (val == INSIDE)
					break;
				else if (val == OUTSIDE) {
					min = 0;
					max = 1;
				} else if (val == MIN) {
					min = 0;
					max = cn.lam;
				} else if (val == MAX) {
					min = cn.lam;
					max = 1;
				}

				// test tail plane
				dt = -s.tplane.distance(eb.xtail);
				dh = -s.tplane.distance(eb.xhead);
				if (dt >= 0) {
					if (dh < 0) if ((lambda = dt / (dt - dh)) < max) {
						max = lambda;
						maxv = s.tail;
						if (min > max) {
							if (intersect != 0) break;
							next = s.left == fb.feat ? cn.cw : cn.ccw;
							continue;
						}
					}
				} else { // dt < 0
					if (dh < 0) {
						next = s.left == fb.feat ? cn.cw : cn.ccw;
						continue;
					}
					if ((lambda = dt / (dt - dh)) > min) {
						min = lambda;
						minv = s.tail;
						if (min > max) {
							if (intersect != 0) break;
							next = s.left == fb.feat ? cn.cw : cn.ccw;
							continue;
						}
					}
				}

				// test head plane
				dt = -s.hplane.distance(eb.xtail);
				dh = -s.hplane.distance(eb.xhead);
				if (dt >= 0) {
					if (dh < 0) if ((lambda = dt / (dt - dh)) < max) {
						max = lambda;
						maxv = s.head;
						if (min > max) {
							if (intersect != 0) break;
							next = s.left == fb.feat ? cn.ccw : cn.cw;
							continue;
						}
					}
				} else { // dt < 0
					if (dh < 0) {
						next = s.left == fb.feat ? cn.ccw : cn.cw;
						continue;
					}
					if ((lambda = dt / (dt - dh)) > min) {
						min = lambda;
						minv = s.head;
						if (min > max) {
							if (intersect != 0) break;
							next = s.left == fb.feat ? cn.ccw : cn.cw;
							continue;
						}
					}
				}

				// we've found an edge Voronoi region that's intersected
				intersect = 1;

				if (minv != null) {
					eb.xpnt.scaleAdd(min, eb.xseg, eb.xtail);
					eb.xpnt.sub(minv.coords);
					if (eb.xpnt.dot(eb.xseg) > 0) {
						next = s.left == fb.feat ? (s.tail == minv ? cn.cw : cn.ccw) : s.tail == minv ? cn.ccw
								: cn.cw;
						continue;
					}
				}

				if (maxv != null) {
					eb.xpnt.scaleAdd(max, eb.xseg, eb.xtail);
					eb.xpnt.sub(maxv.coords);
					if (eb.xpnt.dot(eb.xseg) < 0) {
						next = s.left == fb.feat ? (s.head == maxv ? cn.ccw : cn.cw) : s.head == maxv ? cn.cw
								: cn.ccw;
						continue;
					}
				}

				fb.setEdge(s);
				// System.out.println ("EF continue 0");
				return CONTINUE;
			}

			fb.setVert(cn.ccw == prev ? (s.left == fb.feat ? s.head : s.tail) : s.left == fb.feat ? s.tail : s.head);
			// System.out.println ("EF continue 1");
			return CONTINUE;
		}

		// edge intersects faces cone - check derivatives

		dt = fb.face.plane.distance(eb.xtail);
		dh = fb.face.plane.distance(eb.xhead);
		dmin = minCn != null ? dt + min * (dh - dt) : dt;
		dmax = maxCn != null ? dt + max * (dh - dt) : dh;
		if (dmin <= 0) {
			if (dmax >= 0) {
				dist.d = dmin;
				eb.pnt.scaleAdd(min * eb.edge.len, eb.edge.dir, eb.edge.tail.coords);
				fb.pnt.scaleAdd(min, eb.xseg, eb.xtail);
				fb.pnt.scaleAdd(-dmin, fb.face.plane.normal, fb.pnt);
				// System.out.println ("EF penetration 0");
				return PENETRATION;
			}
		} else if (dmax <= 0) {
			dist.d = dmax;
			eb.pnt.scaleAdd(max * eb.edge.len, eb.edge.dir, eb.edge.tail.coords);
			fb.pnt.scaleAdd(max, eb.xseg, eb.xtail);
			fb.pnt.scaleAdd(-dmax, fb.face.plane.normal, fb.pnt);
			// System.out.println ("EF penetration 1");
			return PENETRATION;
		}

		// at this point, dmin & dmax are both +ve or both -ve

		// if (dmin > 0 && dt <= dh || dmin < 0 && dt >= dh)
		//
		// We evaluate derivative using the dot product with the plane
		// normal so as to be consistent with the method used in
		// edgeEdgeSubtest
		if (fb.face.plane.normal.dot(eb.xseg) * dmin > 0) {
			if (minCn != null)
				fb.setEdge(minCn.nbr);
			else {
				eb.xcoords.set(eb.xtail);
				eb.setVert(eb.edge.tail);
			}
		} else if (maxCn != null)
			fb.setEdge(maxCn.nbr);
		else {
			eb.xcoords.set(eb.xhead);
			eb.setVert(eb.edge.head);
		}
		// System.out.println ("EF continue 2");
		return CONTINUE;
	}

	/**
	 * Computes the distance, along with the closest points and
	 * features, between this polyhedron and a second one.
	 * The <code>cpair</code> parameter is used to
	 * return the closest point and feature information.  To
	 * facilitate fast computation, this parameter should also be
	 * initialized with a guess of the closest features between
	 * the two polyhedra (using {@link ClosestPointPair#setFeatures
	 * ClosestPointPair.setFeatures}). This guess is usually just the
	 * features that were determined the last time <code>vclip</code>
	 * was called for this pair of polyhedra. If no feature
	 * guess is given, then <code>vclip</code> starts with the
	 * first vertex of each polyhedron.
	 * 
	 * <p> If the two polyhedra are
	 * interpenetrating, the routine returns a non-positive
	 * number giving a <i>rough</i>stimate of the interpenetration
	 * distance. The closest feature information in this
	 * case is not particularly useful.
	 *
	 * @param cpair returns closest point pair information, and
	 * supplies an initial guess of the closest features.
	 * @param poly2 the second polyhedron
	 * @param X12 spatial transform from the reference frame of the first
	 * polyhedron to the reference frame of the second
	 * @param X21 spatial transform from the reference frame of the
	 * second polyhedron to the reference frame of the first
	 * @return distance between the two polyhedra, or a number
	 * <= 0 if the two polyhedra are interpenetrating.
	 */
	public double vclip(final ClosestPointPair cpair, final ConvexPolyhedron poly2, final Matrix4d X12, final Matrix4d X21) {
		FeatureBlock fblk1, fblk2;

		fblk1 = featureBlock;
		if (this == poly2)
			fblk2 = new FeatureBlock(null);
		else
			fblk2 = poly2.featureBlock;
		fblk1.T.setTrans(X12);
		fblk2.T.setTrans(X21);

		return vclip(cpair, poly2, fblk1, fblk2);
	}

	private static final int VERT_VERT = (Feature.VERTEX << 2) + Feature.VERTEX;
	private static final int VERT_EDGE = (Feature.VERTEX << 2) + Feature.EDGE;
	private static final int VERT_FACE = (Feature.VERTEX << 2) + Feature.FACE;

	private static final int EDGE_VERT = (Feature.EDGE << 2) + Feature.VERTEX;
	private static final int EDGE_EDGE = (Feature.EDGE << 2) + Feature.EDGE;
	private static final int EDGE_FACE = (Feature.EDGE << 2) + Feature.FACE;

	private static final int FACE_VERT = (Feature.FACE << 2) + Feature.VERTEX;
	private static final int FACE_EDGE = (Feature.FACE << 2) + Feature.EDGE;
	private static final int FACE_FACE = (Feature.FACE << 2) + Feature.FACE;

	private void computeNormal(final Vector3d nrm, final FeatureBlock fblk1, final FeatureBlock fblk2) {
		switch ((fblk1.feat.type << 2) + fblk2.feat.type) {
		case VERT_VERT:
		case EDGE_VERT: {
			nrm.sub(fblk2.xcoords, fblk1.pnt);
			nrm.normalize();
			break;
		}
		case VERT_EDGE: {
			nrm.sub(fblk2.pnt, fblk1.xcoords);
			fblk2.T.transform(nrm);
			nrm.normalize();
			break;
		}
		case EDGE_EDGE: {
			fblk2.T.transform(fblk2.pnt, fblk2.xpnt);
			nrm.sub(fblk2.xpnt, fblk1.pnt);
			nrm.normalize();
			break;
		}
		case FACE_VERT:
		case FACE_EDGE:
		case FACE_FACE: {
			nrm.set(fblk1.face.plane.normal);
			break;
		}
		case VERT_FACE:
		case EDGE_FACE: {
			fblk2.T.transform(fblk2.face.plane.normal, nrm);
			nrm.negate();
			break;
		}
		}
	}

	protected double vclip(final ClosestPointPair cpair, final ConvexPolyhedron poly2, final FeatureBlock fblk1,
			final FeatureBlock fblk2) {
		//	   timers[8].start();
		int result = -1;
		int loop = 0;
		boolean edgeEscapeTried = false;
		boolean loopEscapeTried = false;

		final int maxIters = 2 * (numFeatures() + poly2.numFeatures());

		// use the distance pointer from the first feature block
		final DoublePtr dist = fblk1.dist;
		fblk1.setFeature(cpair.feat1);
		fblk2.setFeature(cpair.feat2);

		prevFeaturePair0.set(null, null);

		do {
			prevFeaturePair1.set(prevFeaturePair0);
			prevFeaturePair0.set(fblk1.feat, fblk2.feat);

			if (false)
				System.out.println(fblk1.feat.typeName() + "-" + fblk2.feat.typeName() + " " + fblk1.feat.getName()
						+ "-" + fblk2.feat.getName());
			switch ((fblk1.feat.type << 2) + fblk2.feat.type) {
			case VERT_VERT: {
				//timers[0].start();
				result = vertVertTest(fblk1, fblk2, dist);
				//timers[0].stop();
				break;
			}
			case VERT_EDGE: {
				//timers[1].start();
				result = vertEdgeTest(fblk1, fblk2, dist);
				//timers[1].stop();
				break;
			}
			case EDGE_VERT: {
				//timers[1].start();
				result = vertEdgeTest(fblk2, fblk1, dist);
				//timers[1].stop();
				break;
			}
			case VERT_FACE: {
				//timers[2].start();
				result = vertFaceTest(fblk1, fblk2, poly2.faces, dist);
				//timers[2].stop();
				break;
			}
			case FACE_VERT: {
				//timers[2].start();
				result = vertFaceTest(fblk2, fblk1, faces, dist);
				//timers[2].stop();
				break;
			}
			case EDGE_EDGE: {
				//timers[3].start();
				result = edgeEdgeTest(fblk1, fblk2, faces, poly2.faces, dist);
				//timers[3].stop();
				break;
			}
			case EDGE_FACE: {
				//timers[4].start();
				result = edgeFaceTest(fblk1, fblk2, dist);
				//timers[4].stop();
				break;
			}
			case FACE_EDGE: {
				//timers[4].start();
				result = edgeFaceTest(fblk2, fblk1, dist);
				//timers[4].stop();
				break;
			}
			default: {
				System.err.println("\ninvalid feature pair combination in vclip");
				System.exit(1);
			}
			}
			// simple cycle detection:
			//    		 System.out.println (loop + " " + 
			//    				     fblk1.feat.getName() + " " +
			//    				     fblk2.feat.getName() + " " +
			//  				     dist.d);

			if (result == CONTINUE && prevFeaturePair1.equals(fblk1.feat, fblk2.feat)) //     	         System.out.println ("prev1: " + prevFeaturePair1);
				//  		 System.out.println ("prev0: " + prevFeaturePair0);
				//      		 System.out.println (loop + " " + 
				//      				     fblk1.feat.getName() + " " +
				//      				     fblk2.feat.getName() + " " +
				//    				     dist.d);
				if (!edgeEscapeTried && (fblk1.feat.type << 2) + fblk2.feat.type == EDGE_EDGE) {
					if (prevFeaturePair0.first == fblk1.feat) // assume this is the one below
						result = edgeEdgeLocalMinEscape(fblk2, fblk1, faces);
					else
						result = edgeEdgeLocalMinEscape(fblk1, fblk2, poly2.faces);
					//  		    System.out.println ("after " + 
					//      				     fblk1.feat.getName() + " " +
					//      				     fblk2.feat.getName() + " " +
					//    				     dist.d);
					edgeEscapeTried = true;
				} else if (!loopEscapeTried) {
					result = loopEscape(fblk1, fblk2, poly2, dist);
					loopEscapeTried = true;
				} else {
					loop = maxIters;
					detectedLoopCount++;
				}
			if (loop++ == maxIters) break;
		} while (result == CONTINUE);

		if (loop > 1) System.out.println(loop);
		// uh oh...
		if (loop > maxIters) {
			final int i;
			//  	      System.out.println ("\nVCLIP CYCLE DETECTED!! ("
			//  				  + ptree1name + ", " + ptree2name + ")");
			//  	      System.out.println ("feat1: " + cpair.feat1.getName());
			//  	      System.out.println ("feat2: " + cpair.feat2.getName());
			//  	      System.out.println ("X21:\n" + fblk2.T.sprintf());
			loopingCount++;
			dist.d = 0;
		}

		//timers[8].stop();
		cpair.pnt1.set(fblk1.pnt);
		cpair.pnt2.set(fblk2.pnt);
		computeNormal(cpair.nrml, fblk1, fblk2);
		cpair.setFeatures(fblk1.feat, fblk2.feat);
		cpair.dist = dist.d;
		callCount++;
		iterCount += loop;
		if (loop <= maxIters && loop > maxIterCount) maxIterCount = loop;
		final int fcount = numFeatures() + poly2.numFeatures();
		if (fcount > maxFeatureCount) maxFeatureCount = fcount;
		return dist.d;
	}

	public static ConvexPolyhedron createBox(final double wx, final double wy, final double wz) {
		final double[] xy = new double[] { wx / 2, wy / 2, -wx / 2, wy / 2, -wx / 2, -wy / 2, wx / 2, -wy / 2 };

		return createPrism(xy, xy, wz);
	}

	public static ConvexPolyhedron createCylinder(final double r, final double h, final int nsides) {
		if (nsides < 3) throw new IllegalArgumentException("argument nsides must be at least 3");
		return createCone(r, r, h, nsides);
	}

	public static ConvexPolyhedron createSphere(final double r, final int nslices) {
		if (nslices % 2 != 0) throw new IllegalArgumentException("argument nsides must be even");
		final int numv = (nslices / 2 - 1) * nslices + 2;
		final Point3d[] vlist = new Point3d[numv];
		final int[][] faces = new int[nslices * nslices / 2][];
		vlist[0] = new Point3d(0, 0, r);
		vlist[numv - 1] = new Point3d(0, 0, -r);
		for (int i = 1; i < nslices / 2; i++) {
			final double phi = Math.PI / 2 - Math.PI * i / (nslices / 2);
			final double cphi = Math.cos(phi);
			final double sphi = Math.sin(phi);
			for (int j = 0; j < nslices; j++) {
				final double the = 2 * Math.PI * j / nslices;
				final double cthe = Math.cos(the);
				final double sthe = Math.sin(the);
				vlist[1 + (i - 1) * nslices + j] = new Point3d(r * cphi * cthe, r * cphi * sthe, r * sphi);
			}
		}
		for (int i = 0; i < nslices / 2; i++)
			for (int j = 0; j < nslices; j++) {
				final int j_n = (j + 1) % nslices;
				int[] idxs;
				if (i == 0)
					idxs = new int[] { j + 1, j_n + 1, 0 };
				else if (i == nslices / 2 - 1) {
					final int i0 = (i - 1) * nslices + 1;
					idxs = new int[] { numv - 1, i0 + j_n, i0 + j };
				} else {
					final int i0 = (i - 1) * nslices + 1;
					final int i1 = i * nslices + 1;
					idxs = new int[] { i1 + j, i1 + j_n, i0 + j_n, i0 + j };
				}
				faces[i * nslices + j] = idxs;
			}
		return new ConvexPolyhedron(vlist, faces);
	}

	public static ConvexPolyhedron createRoundedCylinder(final double r, final double h, final int nslices) {
		if (nslices % 2 != 0) throw new IllegalArgumentException("argument nsides must be even");
		final int numv = (nslices / 4 + 1) * nslices + 1;
		final Point3d[] vlist = new Point3d[numv];
		final int numf = nslices * (nslices / 4 + 1) + 1;
		final int[][] faces = new int[numf][];
		vlist[0] = new Point3d(0, 0, r);
		for (int i = 1; i <= nslices / 4 + 1; i++) {
			final double phi = Math.PI / 2 - Math.PI * i / (nslices / 2);
			final double cphi = Math.cos(phi);
			final double sphi = Math.sin(phi);
			for (int j = 0; j < nslices; j++) {
				final double the = 2 * Math.PI * j / nslices;
				final double cthe = Math.cos(the);
				final double sthe = Math.sin(the);
				if (i < nslices / 4 + 1)
					vlist[1 + (i - 1) * nslices + j] = new Point3d(r * cphi * cthe, r * cphi * sthe, r * sphi);
				else
					vlist[1 + (i - 1) * nslices + j] = new Point3d(r * cthe, r * sthe, -h);
			}
		}
		for (int i = 0; i <= nslices / 4; i++)
			for (int j = 0; j < nslices; j++) {
				final int j_n = (j + 1) % nslices;
				if (i == 0)
					faces[j] = new int[] { j + 1, j_n + 1, 0 };
				else {
					final int i0 = (i - 1) * nslices + 1;
					final int i1 = i * nslices + 1;
					faces[i * nslices + j] = new int[] { i1 + j, i1 + j_n, i0 + j_n, i0 + j };
				}
			}
		faces[numf - 1] = new int[nslices];
		for (int j = 0; j < nslices; j++)
			faces[numf - 1][j] = numv - 1 - j;
		return new ConvexPolyhedron(vlist, faces);
	}

	public static ConvexPolyhedron createCone(final double rtop, final double rbot, final double h, final int nsides) {
		double[] xyTop;
		double[] xyBot;

		if (nsides < 3) throw new IllegalArgumentException("argument nsides must be at least 3");
		if (rtop == 0)
			xyTop = new double[] { 0, 0 };
		else
			xyTop = new double[2 * nsides];
		if (rbot == 0)
			xyBot = new double[] { 0, 0 };
		else
			xyBot = new double[2 * nsides];
		for (int i = 0; i < nsides; i++) {
			final double c = Math.cos(i * 2 * Math.PI / nsides);
			final double s = Math.sin(i * 2 * Math.PI / nsides);
			if (rtop != 0) {
				xyTop[2 * i + 0] = c * rtop;
				xyTop[2 * i + 1] = s * rtop;
			}
			if (rbot != 0) {
				xyBot[2 * i + 0] = c * rbot;
				xyBot[2 * i + 1] = s * rbot;
			}
		}
		return createPrism(xyTop, xyBot, h);
	}

	public static ConvexPolyhedron createPrism(final double[] xy, final double h) {
		if (xy.length / 2 < 3) throw new IllegalArgumentException("argument xy must have at least 2*3 elements");
		return createPrism(xy, xy, h);
	}

	// For a prism with a non-degenerate top and bottom,
	// with n sides, the faces are indexed as followes:
	//
	// sides: 0 ... n-1 counter-clockwise about the z axies
	// top: n
	// bottom: n+1
	//
	public static ConvexPolyhedron createPrism(final double[] xyTop, final double[] xyBot, final double h) {
		final int nsides = Math.min(xyTop.length / 2, xyBot.length / 2);
		Point3d[] vlist;
		int[][] faces;

		if (nsides < 3) throw new IllegalArgumentException("either xyTop or xyBot must have at least 2*3 elements");
		if (xyTop.length / 2 < 3 || xyBot.length / 2 < 3) {
			vlist = new Point3d[nsides + 1];
			faces = new int[nsides + 1][];
			faces[nsides] = new int[nsides];
		} else {
			vlist = new Point3d[2 * nsides];
			faces = new int[nsides + 2][];
			faces[nsides] = new int[nsides];
			faces[nsides + 1] = new int[nsides];
		}

		if (xyTop.length / 2 < 3) { // top has a single point
			vlist[0] = new Point3d(xyTop[0], xyTop[1], h / 2);
			for (int i = 0; i < nsides; i++) {
				vlist[i + 1] = new Point3d(xyBot[i * 2], xyBot[i * 2 + 1], -h / 2);
				final int i_next = (i + 1) % nsides;
				faces[i] = new int[] { 0, i + 1, i_next + 1 };
				faces[nsides][i] = nsides - i;
			}
		} else if (xyBot.length / 2 < 3) { // bottom has a single point
			vlist[nsides] = new Point3d(xyBot[0], xyBot[1], -h / 2);
			for (int i = 0; i < nsides; i++) {
				vlist[i + 1] = new Point3d(xyTop[i * 2], xyTop[i * 2 + 1], h / 2);
				final int i_next = (i + 1) % nsides;
				faces[i] = new int[] { 0, nsides, i_next };
				faces[nsides][i] = i;
			}
		} else
			for (int i = 0; i < nsides; i++) {
				vlist[i] = new Point3d(xyTop[i * 2], xyTop[i * 2 + 1], h / 2);
				vlist[i + nsides] = new Point3d(xyBot[i * 2], xyBot[i * 2 + 1], -h / 2);
				final int i_next = (i + 1) % nsides;
				faces[i] = new int[] { i, i + nsides, i_next + nsides, i_next };
				faces[nsides][i] = i;
				faces[nsides + 1][i] = 2 * nsides - 1 - i;
			}
		return new ConvexPolyhedron(vlist, faces);
	}

	public void hideVerts(final int[] vlist) {
		for (final int element : vlist)
			verts[element].setHidden(true);
	}

	public void hideFaces(final int[] flist) {
		for (final int element : flist)
			faces[element].setHidden(true);
	}

	public void hideEdges(final int[] elist) {
		for (int i = 0; i < elist.length / 2; i++)
			getEdge(elist[2 * i], elist[2 * i + 1]).setHidden(true);
	}

	public void hidePrismFace(final int fi) {
		final int nfaces = faces.length;
		final int nsides = nfaces - 2;
		if (fi < 0 || fi >= nfaces)
			throw new IndexOutOfBoundsException("index " + fi + " out of bounds [0," + (nfaces - 1) + "]");
		if (fi < nfaces - 2) { // one of the side faces
			final int ni = (fi + 1) % nsides;
			hideVerts(new int[] { fi, ni, fi + nsides, ni + nsides });
			hideEdges(new int[] { fi, ni, fi + nsides, ni + nsides, fi, fi + nsides, ni, ni + nsides });
		} else if (fi == nfaces - 2)
			for (int i = 0; i < nsides; i++) {
				final int ni = (i + 1) % nsides;
				verts[i].setHidden(true);
				getEdge(i, ni).setHidden(true);
			}
		else
			for (int i = 0; i < nsides; i++) {
				final int ni = (i + 1) % nsides;
				verts[i + nsides].setHidden(true);
				getEdge(i + nsides, ni + nsides).setHidden(true);
			}
		faces[fi].setHidden(true);
	}

	static void main(final String[] args) {
		final ConvexPolyhedron poly = new ConvexPolyhedron();

		try {
			poly.scanNamedFormat(new FileReader("unit-cube.txt"));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		System.out.println(poly.sprintfNamedFormat());
	}
}
