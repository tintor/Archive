/////////////////////////////////////////////////////////////////////////////
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

import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

//import acme.misc.ScanfReader;

//import convexhull3d.ConvexHull3D;
//import convexhull3d.SpatialPoint;

/**
 * A hierarchically organized collection of
 * convex polyhedra and the primary class for
 * invoking {@link #vclip vclip}. 
 * The convex decomposition of
 * a non-convex polyhedron can be represented as a PolyTree.
 *
 * <p>
 * A PolyTree is either <i>atomic</i> or <i>compound</i>. An atomic
 * PolyTree is composed of a single convex polyhedron, which can
 * be obtained using the {@link #getPolyhedron getPolyhedron} method.
 * A compound PolyTree consists of one of more PolyTree components,
 * plus a convex polyhedron which acts as a bounding hull
 * for the components. For compound PolyTrees,
 * {@link #getPolyhedron getPolyhedron} returns the bounding hull.
 *
 * <p>The number of components in a compound PolyTree is returned
 * by {@link #numComponents numComponents}. The number of <i>nodes</i>
 * is the total number of PolyTrees in the hierarchy, and is returned
 * by {@link #numNodes numNodes}. The number of leaves is the total
 * number of atomic PolyTrees in the hierarchy, and is returned by
 * {@link #numLeaves numLeaves}.
 *
 * <p>
 * Each PolyTree has a <i>local</i> coordinate frame with respect
 * to which components are attached and polyhedron vertices are represented.
 * All components within a PolyTree also share a common <i>reference</i>
 * frame, with respect to which <code>vclip</code> is called.
 * The transformation from the local frame to the reference frame
 * call be obtained using the method
 * {@link #getTransform getTransform}, and can be changed
 * using {@link #setTransform setTransform}.
 * When a PolyTree is added to another as a component (see
 * {@link #addComponent(String,PolyTree,Matrix4d) addComponent}), the
 * component's reference frame is changed to that of the parent
 * PolyTree.
 *
 * <p>The distance, and associated closest points and features,
 * between a pair of PolyTrees is determined using {@link #vclip vclip},
 * which is based on Brian Mirtich's Vclip algorithm, published as
 * <a href="http://www.cs.ubc.ca/~lloyd/java/doc/vclip/vclip.ps">
 * ``V-Clip: Fast and Robust Polyhedral Collision Detection''</a>,
 * ACM Transactions on Graphics, July, 1997.
 *
 * <p>A PolyTree can be created by reading information in from a 
 * stream, specified either in a constructor or in a method
 * such as {@link #scan(Reader,Map,boolean,boolean) scan}.
 * A whole collection of PolyTrees can be read into a Map
 * using the {@link #scanLibrary scanLibrary} method.
 * An atomic PolyTree can
 * also be constructed directly from a {@link ConvexPolyhedron
 * ConvexPolyhedron}, and compound PolyTrees can be created
 * explictly using the method {@link
 * #addComponent(String,PolyTree,Matrix4d) addComponent}
 * and then calling {@link #buildBoundingHull(int) buildBoundingHull}.
 * More information on PolyTree creation, and bounding hulls,
 * can be found in the {@link vclip vclip} package documentation.
 *
 * <p>Mass and volume properties are computed automatically
 * for the polyhedron associated with a PolyTree, and can be
 * obtained using the methods
 * {@link #volume volume}, 
 * {@link #firstMomentOfVolume firstMomentOfVolume}, 
 * {@link #secondMomentOfVolume secondMomentOfVolume}, 
 * {@link #productOfVolume productOfVolume}, and 
 * {@link #radius radius}.
 * These quantities are computed using algorithms described in Brian Mirtich,
 * <a href="http://www.cs.ubc.ca/~lloyd/java/src/doc/polyMassProp.ps">
 * ``Fast and Accurate Computation of Polyhedral
 * Mass Properties''</a>, <i>Journal of Graphics Tools</i>, Volume 1,
 * Number 2, 1996.
 * Assuming a uniform mass density, they can be used to determine
 * the center-of-mass and the inertia tensor,
 * which for a mass of unity, can be obtained using
 * {@link #centerOfMass centerOfMass} and
 * {@link #inertiaTensor inertiaTensor}.
 * All of these quantities are computed with
 * respect to the PolyTree's local coordinate frame.
 *
 * @author Brian Mirtich (original C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a> (Java port)
 */
public class PolyTree {
	private final Point3dX compCp1 = new Point3dX();
	private final Point3dX compCp2 = new Point3dX();

	private final Vector components = new Vector(10);

	private final FeatureBlock featureBlock1 = new FeatureBlock(null);
	private final FeatureBlock featureBlock2 = new FeatureBlock(null);

	private PolyTree parent;

	/* data members */
	String name;

	// Pointer to a ConvexPolyhedron.  For an atomic PolyTree, this is the
	// geometry of the PolyTree itself; for a compound PolyTree, this is
	// the geometry of the convex hull.
	ConvexPolyhedron poly_;

	// Volume integrals, relative to this PolyTree's reference frame
	private double vol_; // volume: vol    = int(dV)
	// 1st moment of volume: mov1.x = int(x dV)
	private final Vector3dX mov1_ = new Vector3dX();
	// undiagonalized 2nd moment of volume: mov2.x = int(x^2 dV)
	private final Vector3dX mov2_ = new Vector3dX();
	// product of volume: pov.x  = int(yz dV) 
	private final Vector3dX pov_ = new Vector3dX();

	private double rad_; // "radius" of PolyTree, 
	//    relative to center of volume

	// An entire PolyTree shares a common reference frame (r).  Tpr_ and
	// Trp_ are the transformations between each PolyTree's local frame
	// (p) and the PolyTree reference frame.  These fields are
	// recomputed each time the PolyTree is included (replicated) into
	// another hierarchy.  By default, the reference frame is simply the
	// local frame of the root PolyTree, so for the root node Tpr_ and
	// Trp_ are identity transformations.  In some cases, however, it is
	// advantageous to store transformations with respect to a different
	// frame.  For example, in rigid body simulation, an object's body
	// frame is the most useful frame, so all of the PolyTree nodes in
	// it's geometry use that as the reference frame.  The reference
	// frame of a hierarchy is updated using the setTransform()
	// method.  Xp_r and Xrp_ are the MatX_ equivalents of Tpr_ and Trp_.

	//  private Se3  Tpr_, Trp_;
	private final Matrix4dX Xlr_ = new Matrix4dX();

	// list<PolyTree> components;  // children in convex decomp'n, if any

	/**
	 * There is no bounding hull. This will
	 * be the case for atomic PolyTrees, or compound PolyTrees
	 * for which a bounding hull has not yet been created.
	 */
	static public final int NO_HULL = 0;

	/**
	 * Bounding hull is an oriented-bounding box.
	 */
	static public final int OBB_HULL = 1;

	/**
	 * Bounding hull is a convex hull.
	 */
	static public final int CONVEX_HULL = 2;

	/**
	 * Bounding hull is a custom polyhedron, specified with
	 * {@link #setPolyhedron setPolyhedron}.
	 */
	static public final int CUSTOM_HULL = 3;

	static private int defaultBoundingHullType = CONVEX_HULL;
	private int boundingHullType = NO_HULL;

	/**
	 * Sets the default bounding hull type, which will
	 * be used when the bounding hull is created automatically
	 * (such as when a PolyTree is created using
	 * {@link #scan(Reader,Map,boolean,boolean) scan}).
	 *
	 * @param type default bounding hull type. Must be either
	 * {@link #OBB_HULL OBB_HULL} or {@link #CONVEX_HULL CONVEX_HULL}
	 * @throws IllegalArgumentException if an illegal type is specified
	 * @see #getDefaultBoundingHullType
	 */
	static public void setDefaultBoundingHullType(final int type) {
		switch (type) {
		case OBB_HULL:
		case CONVEX_HULL: {
			defaultBoundingHullType = type;
			break;
		}
		default: {
			throw new IllegalArgumentException("default bounding hull must be OBB_HULL or CONVEX_HULL");
		}
		}
	}

	/**
	 * Returns the default bounding hull type.
	 *
	 * @return default bounding hull type
	 * @see #setDefaultBoundingHullType
	 */
	static public int getDefaultBoundingHullType() {
		return defaultBoundingHullType;
	}

	/**
	 * Gets the type of this PolyTree's bounding hull.
	 *
	 * @return bounding hull type (one of
	 * {@link #NO_HULL NO_HULL}, {@link #OBB_HULL OBB_HULL},
	 * {@link #CONVEX_HULL CONVEX_HULL}, 
	 * or {@link #CUSTOM_HULL CUSTOM_HULL})
	 */
	public int getBoundingHullType() {
		return boundingHullType;
	}

	/**
	 * Builds a bounding hull for this PolyTree, using the
	 * default bounding hull type.
	 * This method is called recursively on any component
	 * PolyTrees that do not have a bounding hull.
	 * For atomic PolyTrees, this routine does nothing.
	 *
	 * @see #getDefaultBoundingHullType
	 * @see #buildBoundingHull(int)
	 */
	public void buildBoundingHull() {
		buildBoundingHull(defaultBoundingHullType);
	}

	/**
	 * Builds a bounding hull of a prescribed type for this PolyTree.
	 * This method is called recursively on any component
	 * PolyTrees that do not have a bounding hull.
	 * For atomic PolyTrees, this routine does nothing.
	 *
	 * @param type type of bounding hull to build. Must be either
	 * {@link #OBB_HULL OBB_HULL} or {@link #CONVEX_HULL CONVEX_HULL}
	 * @see #getBoundingHullType
	 */
	public void buildBoundingHull(final int type) {
		if (type != OBB_HULL && type != CONVEX_HULL)
			throw new IllegalArgumentException("bounding object must be OBB_HULL or CONVEX_HULL");
		// recursively build bounding hulls where needed
		for (final Iterator it = components.iterator(); it.hasNext();) {
			final PolyTree comp = (PolyTree) it.next();
			if (comp.poly_ == null) comp.buildBoundingHull(type);
		}
		if (components.size() > 0) {
			boundingHullType = type;
			switch (type) {
			case OBB_HULL: {
				buildOBB();
				break;
			}
			case CONVEX_HULL: {
				buildConvexHull();
				break;
			}
			}
			computeVolInts();
		}
	}

	/**
	 * Recursively builds a bounding hull of a prescribed type
	 * for this PolyTree and every compound component PolyTree in
	 * the hierarchy.
	 * For atomic PolyTrees, this routine does nothing.
	 *
	 * @param type type of bounding hull to build. Must be either
	 * {@link #OBB_HULL OBB_HULL} or {@link #CONVEX_HULL CONVEX_HULL}
	 * @see #getBoundingHullType
	 */
	public void buildAllBoundingHulls(final int type) {
		if (type != OBB_HULL && type != CONVEX_HULL)
			throw new IllegalArgumentException("bounding object must be OBB_HULL or CONVEX_HULL");
		for (final Iterator it = components.iterator(); it.hasNext();) {
			final PolyTree comp = (PolyTree) it.next();
			comp.buildAllBoundingHulls(type);
		}
		buildBoundingHull(type);
	}

	/**
	 * Sets the convex polyhedron associated with the PolyTree.
	 * For a compound PolyTree, this polyhedron will define
	 * the bounding hull (of type {@link #CUSTOM_HULL CUSTOM_HULL}).
	 * Note that the polyhedron is <i>not</i> copied.
	 *
	 * @param poly convex polyhedron
	 */
	public void setPolyhedron(final ConvexPolyhedron poly) {
		poly_ = poly;
		if (components.size() > 0) boundingHullType = CUSTOM_HULL;
		computeVolInts();
	}

	/**
	 * Creates an empty PolyTree.
	 *
	 * @param name name of the PolyTree (optional;
	 * can be set to <code>null</code>).
	 */
	public PolyTree(final String name) {
		setName(name);
	}

	/**
	 * Creates a PolyTree by copying another.
	 *
	 * @param name name for the new PolyTree (optional;
	 * can be set to <code>null</code>).
	 * @param ptree the PolyTree to be copied
	 */
	public PolyTree(final String name, final PolyTree ptree) {
		set(ptree);
		setName(name);
	}

	/**
	 * Creates an atomic PolyTree from a convex polyhedron.
	 * Note that the polyhedron is <i>not</i> copied.
	 *
	 * @param name name of the PolyTree (optional;
	 * can be set to <code>null</code>).
	 * @param poly the polyhedron
	 */
	public PolyTree(final String name, final ConvexPolyhedron poly) {
		setPolyhedron(poly);
		setName(name);
	}

	/**
	 * Creates a PolyTree by copying one from a PolyTree
	 * library.
	 *
	 * @param name name of the PolyTree (optional;
	 * can be set to <code>null</code>).
	 * @param library PolyTree library (a map of string names
	 * to PolyTrees).
	 * @param origName name of the original PolyTree in the libary
	 * @throws IllegalArgumentException if the original PolyTree
	 * is not found in the library.
	 */
	public PolyTree(final String name, final Map library, final String origName) throws IllegalArgumentException {
		final PolyTree ptree = (PolyTree) library.get(origName);
		if (ptree == null) throw new IllegalArgumentException("Polytree " + origName + " not found in the library");
		set(ptree);
		setName(name);
	}

	/**
	 * Sets the name for this PolyTree.
	 *
	 * @param name for this PolyTree
	 */
	public void setName(String name) {
		if (name != null)
			this.name = new String(name);
		else
			name = null;
	}

	/**
	 * Gets the name of this PolyTree.
	 *
	 * @return name of this PolyTree
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns true if this PolyTree is compound (i.e., is
	 * composed of one or more PolyTree subcomponents).
	 *
	 * @return true if this PolyTree is compound
	 */
	public boolean isCompound() {
		return components.size() != 0;
	}

	/**
	 * Returns true if this PolyTree is atomic (i.e., is
	 * represented by only one polyhedron, and has no 
	 * subcomponents).
	 *
	 * @return true if this PolyTree is atomic
	 */
	public boolean isAtomic() {
		return components.size() == 0;
	}

	/**
	 * Sets this PolyTree to a copy of another PolyTree. All
	 * aspects of the PolyTree are copied, including the name.
	 *
	 * @param ptree the other PolyTree
	 */
	public void set(final PolyTree ptree) {
		poly_ = ptree.poly_; // should we copy this?
		vol_ = ptree.vol_;
		mov1_.set(ptree.mov1_);
		mov2_.set(ptree.mov2_);
		pov_.set(ptree.pov_);
		rad_ = ptree.rad_;
		Xlr_.set(ptree.Xlr_);
		//	   Xrp_.set(ptree.Xrp_);
		if (ptree.name != null)
			name = new String(ptree.name);
		else
			name = null;
		boundingHullType = ptree.boundingHullType;

		for (final Iterator it = ptree.components.iterator(); it.hasNext();) {
			final PolyTree p = new PolyTree(null, (PolyTree) it.next());
			components.add(p);
		}
	}

	/**
	 * Adds a component to this PolyTree. The component is
	 * made by copying the supplied PolyTree and giving
	 * the copy the supplied name. The reference frame
	 * of the original PolyTree is assumed to correspond
	 * to the local frame of this PolyTree.
	 *
	 * <p>
	 * Adding a component will
	 * automically turn this PolyTree into a compound PolyTree,
	 * and any polyhedron presently associated with this PolyTree
	 * will become (by default) the bounding hull polyhedron.
	 *
	 * @param name optional name for the component PolyTree
	 * @param orig PolyTree that is copied to form the component
	 */
	public void addComponent(final String name, final PolyTree orig) {
		final PolyTree comp = new PolyTree(name, orig);
		comp.setTransform(Xlr_);
		comp.parent = this;
		components.add(comp);
	}

	/**
	 * Adds a component to this PolyTree. The component is
	 * made by copying the supplied PolyTree and giving
	 * the copy the supplied name. The reference frame
	 * of the original PolyTree is related to the local
	 * frame of this PolyTree by the transform <code>Xrl</code>.
	 *
	 * <p>
	 * Adding a component will
	 * automically turn this PolyTree into a compound PolyTree,
	 * and any polyhedron presently associated with this PolyTree
	 * will become (by default) the bounding hull polyhedron.
	 *
	 * @param name optional name for the component PolyTree
	 * @param orig PolyTree that is copied to form the component
	 * @param Xrl transform from the reference frame of the
	 * original PolyTree to the local frame of this PolyTree
	 */
	public void addComponent(final String name, final PolyTree orig, final Matrix4d Xrl) {
		// Xform from component ref frame to this PolyTree ref frame
		final Matrix4dX Xrr = new Matrix4dX();
		final PolyTree comp = new PolyTree(name, orig);
		Xrr.mulTrans(Xlr_, Xrl);
		comp.setTransform(Xrr);
		comp.parent = this;
		components.add(comp);
	}

	/**
	 * Clear all the components in this PolyTree, and set the
	 * bounding hull type to {@link #NO_HULL NO_HULL}.
	 * Any existing polyhedron will not be changed.
	 */
	public void clearComponents() {
		components.clear();
	}

	/**
	 * Returns the components associated with this PolyTree.
	 *
	 * @return iterator for the components */
	public Iterator getComponents() {
		return components.iterator();
	}

	void buildConvexHull() {
		if (components.size() > 0) {
			final Point3dX vx = new Point3dX();
			// transform from part local frame to this local frame
			final Matrix4dX Xpl = new Matrix4dX();
			int numPoints = 0;
			for (final Iterator it = components.iterator(); it.hasNext();)
				numPoints += ((PolyTree) it.next()).getPolyhedron().getVerts().length;
			final double[] coordList = new double[numPoints * 3];
			int i = 0;
			for (final Iterator it = components.iterator(); it.hasNext();) {
				final PolyTree comp = (PolyTree) it.next();
				final Vertex[] verts = comp.getPolyhedron().getVerts();
				Xpl.mulInverseLeft(Xlr_, comp.Xlr_);
				for (final Vertex vert : verts) {
					// comp.Xlr_ = this.Xlr_ * Xpl

					Xpl.transform(vert.coords, vx);
					coordList[i * 3 + 0] = vx.x;
					coordList[i * 3 + 1] = vx.y;
					coordList[i * 3 + 2] = vx.z;
					i++;
				}
			}

			// 	      convexhull3d.ConvexHull3D chull =
			// 		 new convexhull3d.ConvexHull3D(coordList);
			// 	      convexhull3d.SpatialPoint[] verts =
			// 		 chull.getMergedFaceVertices();
			// 	      Point3dX[] pnts = new Point3dX[verts.length];
			// 	      for (i=0; i<pnts.length; i++)
			// 	       { pnts[i] = new Point3dX(verts[i].x, verts[i].y, verts[i].z);
			// 	       }
			// 	      poly_ = new ConvexPolyhedron (pnts, chull.getMergedFaces());

			final quickhull3d.QuickHull3D chull = new quickhull3d.QuickHull3D(coordList);
			final quickhull3d.Point3d[] verts = chull.getVertices();
			final Point3dX[] pnts = new Point3dX[verts.length];
			for (i = 0; i < pnts.length; i++)
				pnts[i] = new Point3dX(verts[i].x, verts[i].y, verts[i].z);
			poly_ = new ConvexPolyhedron(pnts, chull.getFaces());

		}
	}

	private double faceArea(final Tuple3d p, final Tuple3d q, final Tuple3d r) {
		final double ax = q.x - p.x;
		final double ay = q.y - p.y;
		final double az = q.z - p.z;

		final double bx = r.x - p.x;
		final double by = r.y - p.y;
		final double bz = r.z - p.z;

		final double cx = ay * bz - az * by;
		final double cy = az * bx - ax * bz;
		final double cz = ax * by - ay * bx;

		return Math.sqrt(cx * cx + cy * cy + cz * cz);
	}

	boolean debug = false;

	final void buildOBB() {
		if (components.size() > 0) {
			debug = true;
			buildConvexHull();
			debug = false;
			computeVolInts();
			final Vertex[] verts = poly_.getVerts();

			final Matrix3dX M = new Matrix3dX();
			final Matrix3dX U = new Matrix3dX();
			final Vector3dX s = new Vector3dX();

			inertiaTensor(M);
			M.symmetricSVD(U, s, null);

			if (U.determinant() < 0) {
				final Vector3dX c1 = new Vector3dX();
				final Vector3dX c2 = new Vector3dX();
				U.m02 = -U.m02;
				U.m12 = -U.m12;
				U.m22 = -U.m22;
			}
			final Point3dX cov = new Point3dX();
			cov.scale(1.0 / vol_, mov1_); // center of volume

			// cov and U give the position and orientation of
			// the OBB. Now compute the dimensions of the OBB
			final Vector3dX[] cols = new Vector3dX[3];
			for (int j = 0; j < 3; j++) {
				cols[j] = new Vector3dX();
				U.getColumn(j, cols[j]);
			}
			final Vector3dX v = new Vector3dX();
			final double[] lens = new double[3];
			for (final Vertex vert : verts)
				for (int j = 0; j < 3; j++) {
					v.sub(vert.coords, cov);
					final double l = Math.abs(cols[j].dot(v));
					if (l > lens[j]) lens[j] = l;
				}
			final Point3dX[] pnts = new Point3dX[8];
			final double[][] scale = new double[][] { { 1, 1, -1 }, { -1, 1, -1 }, { -1, -1, -1 }, { 1, -1, -1 },
					{ 1, 1, 1 }, { -1, 1, 1 }, { -1, -1, 1 }, { 1, -1, 1 } };
			for (int i = 0; i < 8; i++) {
				pnts[i] = new Point3dX(cov);
				for (int j = 0; j < 3; j++)
					pnts[i].scaleAdd(scale[i][j] * lens[j], cols[j], pnts[i]);
			}
			final int[][] obbFaces = new int[][] { { 2, 1, 0, 3 }, { 6, 7, 4, 5 }, { 2, 3, 7, 6 }, { 4, 0, 1, 5 },
					{ 3, 0, 4, 7 },

					{ 2, 6, 5, 1 } };

			poly_ = new ConvexPolyhedron(pnts, obbFaces);
		}
	}

	/**
	 * Sets the local-to-reference frame transform of this
	 * PolyTree. This will also cause all the components of the
	 * PolyTree to ``move'' relative to the reference frame.
	 *
	 * @param Xlr new local-to-reference frame transformation */
	public final void setTransform(final Matrix4d Xlr) {
		if (components.size() > 0) {
			final Matrix4dX Xpre = new Matrix4dX();
			Xpre.mulInverse(Xlr, Xlr_);
			recursivelyPremulTransform(Xpre);
		} else
			Xlr_.set(Xlr);
	}

	private final void recursivelyPremulTransform(final Matrix4d Xpre) {
		Xlr_.mulTrans(Xpre, Xlr_);
		for (final Iterator it = components.iterator(); it.hasNext();)
			((PolyTree) it.next()).recursivelyPremulTransform(Xpre);
	}

	/**
	 * Returns the transformation from this PolyTree's local
	 * frame to its reference frame.
	 *
	 * @return transfrom from local frame to reference frame
	 */
	public final Matrix4d getTransform() {
		return Xlr_;
	}

	/**
	 * Returns the polyhedron associated with this PolyTree.
	 * For compound PolyTrees, this will be the bounding polyhedron.
	 *
	 * <p>The vertex coordinates of this polyhedron are defined
	 * with respect to the PolyTree's local frame.
	 *
	 * @return this PolyTree's polyhedron
	 */
	public final ConvexPolyhedron getPolyhedron() {
		return poly_;
	}

	/**
	 * Returns the number of PolyTree components which are
	 * direct children of this PolyTree.
	 *
	 * @return the number of PolyTree components */
	public final int numComponents() {
		return components.size();
	}

	/**
	 * Returns the total number of PolyTrees located in the hierarchy
	 * under this PolyTree.
	 *
	 * @return total number of PolyTrees in the hierarchy
	 */
	public final int numNodes() {
		int num = 1;
		for (final Iterator it = components.iterator(); it.hasNext();)
			num += ((PolyTree) it.next()).numNodes();
		return num;
	}

	/**
	 * Returns the total number of atomic PolyTrees located in
	 * the hierarchy under this PolyTree.
	 *
	 * @return number of atomic PolyTrees under this PolyTree
	 */
	public final int numLeaves() {
		if (components.size() == 0)
			return 1;
		else {
			int num = 0;
			for (final Iterator it = components.iterator(); it.hasNext();)
				num += ((PolyTree) it.next()).numLeaves();
			return num;
		}
	}

	/**
	 * Returns a string representation of this PolyTree.
	 *
	 * @return string representation
	 */
	@Override
	public final String toString() {
		final StringBuffer out = new StringBuffer("PolyTree " + name + "\n");
		out.append("atomic\n");
		out.append("volume             " + vol_ + "\n");
		out.append("1st moment of vol  " + mov1_ + "\n");
		out.append("2nd moment of vol  " + mov2_ + "\n");
		out.append("product of vol     " + pov_ + "\n");
		out.append("radius             " + rad_ + "\n");

		return out.toString();
	}

	//private final String toStringRecur(int level) {;}

	/**
	 * Returns the volume of this PolyTree's polyhedron.
	 *
	 * @return polyhedron volume
	 */
	// volume integrals
	public final double volume() {
		return vol_;
	}

	/**
	 * Returns the first moment of volume for this PolyTree's polyhedron,
	 * with respect to the local frame.
	 * When divided by the
	 * volume of the polyhedron, this quantity yields the center
	 * of volume.
	 *
	 * @return first moment of volume
	 */
	public final Vector3d firstMomentOfVolume() {
		return mov1_;
	}

	/**
	 * Returns the second moment of volume for this PolyTree's polyhedron,
	 * with respect to the local frame.
	 * This quantity is used
	 * in computing the diagonal elements of the inertia tensor.
	 *
	 * @return second moment of volume
	 */
	public final Vector3d secondMomentOfVolume() {
		return mov2_;
	}

	/**
	 * Returns the product of volume for this PolyTree's polyhedron,
	 * with respect to the local frame. This quantity is used
	 * in computing the off-diagonal elements of the inertia tensor.
	 *
	 * @return product of volume
	 */
	public final Vector3d productOfVolume() {
		return pov_;
	}

	/**
	 * Returns the ``radius'' of the PolyTree, relative to the
	 * center of volume.
	 *
	 * @return radius of the PolyTree
	 */
	public final double radius() {
		return rad_;
	}

	/**
	 * Returns the center of mass of this PolyTree's polyhedron
	 * (assuming a uniform mass density), with respect to the local frame.
	 *
	 * @param com returns the center of mass
	 */
	public final void centerOfMass(final Vector3d com) {
		com.scale(1 / vol_, mov1_);
	}

	/**
	 * Returns the inertia tensor of this PolyTree's polyhedron
	 * assuming a uniform mass density and a mass of 1. The tensor is
	 * computed with respect to a frame that has
	 * the same orientation as the PolyTree's local
	 * frame, but is centered at the center of mass.
	 *
	 * @param J returns the inertia tensor
	 */
	public final void inertiaTensor(final Matrix3d J) {
		J.m00 = mov2_.y + mov2_.z - (mov1_.y + mov1_.z) / vol_;
		J.m11 = mov2_.x + mov2_.z - (mov1_.x + mov1_.z) / vol_;
		J.m22 = mov2_.x + mov2_.y - (mov1_.x + mov1_.y) / vol_;

		J.m01 = -pov_.z;
		J.m02 = -pov_.y;
		J.m12 = -pov_.x;

		J.m10 = J.m01;
		J.m20 = J.m02;
		J.m21 = J.m12;
	}

	// Compute volume integrals of PolyTree and the radius field.  For info
	// on algo, see "Fast and Accurate Computation of Polyhedral Mass
	// Properties," Brian Mirtich, journal of graphics tools, volume 1,
	// number 2, 1996.
	final void computeVolInts() {
		int a, b, c;
		final PolyTree comp;
		//Edge e;
		//Face f;
		double a0, a1, da;
		final double al;
		double b0, b1, db;
		double a0_2, a0_3, a0_4, b0_2, b0_3, b0_4;
		double a1_2, a1_3, b1_2, b1_3;
		double d, na, nb, nc, inv;
		double I, Ia, Ib, Iaa, Iab, Ibb, Iaaa, Iaab, Iabb, Ibbb;
		double Icc, Iccc, Ibbc, Icca;
		double C0, Ca, Caa, Caaa, Cb, Cbb, Cbbb;
		double Cab, Kab, Caab, Kaab, Cabb, Kabb;
		final Vector3dX h, w;
		Vector3dX v;
		Point3dX cov;
		final Matrix4dX X;

		vol_ = 0.0;

		mov1_.set(0, 0, 0);
		mov2_.set(0, 0, 0);
		pov_.set(0, 0, 0);

		final Face[] facelist = poly_.getFaces();
		for (final Face f : facelist) {
			// compute projection direction
			v = new Vector3dX();
			v.set(java.lang.Math.abs(f.plane.normal.x), java.lang.Math.abs(f.plane.normal.y), java.lang.Math
					.abs(f.plane.normal.z));
			c = v.x >= v.y ? (v.x >= v.z ? 0 : 2) : v.y >= v.z ? 1 : 2;
			a = (c + 1) % 3;
			b = (c + 2) % 3;

			I = Ia = Ib = Iaa = Iab = Ibb = Iaaa = Iaab = Iabb = Ibbb = 0.0;

			// walk around face
			for (final Enumeration enumFaceConeNode = f.cone.elements(); enumFaceConeNode.hasMoreElements();) {
				final Edge e = ((FaceConeNode) enumFaceConeNode.nextElement()).nbr;
				if (e.left == f) { // CCW edge
					a0 = e.tail.coords.get(a);
					b0 = e.tail.coords.get(b);
					a1 = e.head.coords.get(a);
					b1 = e.head.coords.get(b);
				} else { // CW edge
					a0 = e.head.coords.get(a);
					b0 = e.head.coords.get(b);
					a1 = e.tail.coords.get(a);
					b1 = e.tail.coords.get(b);
				}

				da = a1 - a0;

				db = b1 - b0;
				a0_2 = a0 * a0;
				a0_3 = a0_2 * a0;
				a0_4 = a0_3 * a0;
				b0_2 = b0 * b0;
				b0_3 = b0_2 * b0;
				b0_4 = b0_3 * b0;
				a1_2 = a1 * a1;
				a1_3 = a1_2 * a1;
				b1_2 = b1 * b1;
				b1_3 = b1_2 * b1;
				C0 = a1 + a0;
				Ca = a1 * C0 + a0_2;
				Caa = a1 * Ca + a0_3;
				Caaa = a1 * Caa + a0_4;
				Cb = b1 * (b1 + b0) + b0_2;
				Cbb = b1 * Cb + b0_3;
				Cbbb = b1 * Cbb + b0_4;
				Cab = 3 * a1_2 + 2 * a1 * a0 + a0_2;
				Kab = a1_2 + 2 * a1 * a0 + 3 * a0_2;
				Caab = a0 * Cab + 4 * a1_3;
				Kaab = a1 * Kab + 4 * a0_3;
				Cabb = 4 * b1_3 + 3 * b1_2 * b0 + 2 * b1 * b0_2 + b0_3;
				Kabb = b1_3 + 2 * b1_2 * b0 + 3 * b1 * b0_2 + 4 * b0_3;
				I += db * C0;
				Ia += db * Ca;
				Iaa += db * Caa;
				Iaaa += db * Caaa;
				Ib += da * Cb;
				Ibb += da * Cbb;
				Ibbb += da * Cbbb;
				Iab += db * (b1 * Cab + b0 * Kab);
				Iaab += db * (b1 * Caab + b0 * Kaab);
				Iabb += da * (a1 * Cabb + a0 * Kabb);
			}

			I /= 2.0;
			Ia /= 6.0;
			Iaa /= 12.0;
			Iaaa /= 20.0;
			Ib /= -6.0;
			Ibb /= -12.0;
			Ibbb /= -20.0;
			Iab /= 24.0;
			Iaab /= 60.0;
			Iabb /= -60.0;

			d = f.plane.offset;
			v = f.plane.normal;
			na = v.get(a);
			nb = v.get(b);
			nc = v.get(c);
			inv = 1.0 / nc;

			if (a == 0)
				vol_ += inv * na * Ia;
			else if (b == 0)
				vol_ += inv * nb * Ib;
			else
				vol_ -= (d * I + na * Ia + nb * Ib) / nc;

			Icc = (SQR(na) * Iaa + 2 * na * nb * Iab + SQR(nb) * Ibb + d * (2 * (na * Ia + nb * Ib) + d * I))
					* SQR(inv);
			mov1_.set(a, mov1_.get(a) + inv * na * Iaa);
			mov1_.set(b, mov1_.get(b) + inv * nb * Ibb);
			mov1_.set(c, mov1_.get(c) + Icc);

			Iccc = -(CUBE(na) * Iaaa + 3 * SQR(na) * nb * Iaab + 3 * na * SQR(nb) * Iabb + CUBE(nb) * Ibbb + 3
					* (SQR(na) * Iaa + 2 * na * nb * Iab + SQR(nb) * Ibb) * d + d * d
					* (3 * (na * Ia + nb * Ib) + d * I))
					* CUBE(inv);
			mov2_.set(a, mov2_.get(a) + inv * na * Iaaa);
			mov2_.set(b, mov2_.get(b) + inv * nb * Ibbb);
			mov2_.set(c, mov2_.get(c) + Iccc);

			Ibbc = -(d * Ibb + na * Iabb + nb * Ibbb) * inv;
			Icca = (SQR(na) * Iaaa + 2 * na * nb * Iaab + SQR(nb) * Iabb + d * (2 * (na * Iaa + nb * Iab) + d * Ia))
					* SQR(inv);

			pov_.set(c, pov_.get(c) + inv * na * Iaab);
			pov_.set(a, pov_.get(a) + inv * nb * Ibbc);
			pov_.set(b, pov_.get(b) + Icca);
		}

		mov1_.scale(0.5);
		mov2_.scale(1.0 / 3.0);
		pov_.scale(0.5);

		// Compute radius, defined as the maximum distance of any vertex on
		// the PolyTree's convex hull  from the center of volume.
		cov = new Point3dX();
		cov.scale(1.0 / vol_, mov1_); // center of volume
		rad_ = 0.0;

		final Vertex[] verts = poly_.getVerts();
		for (final Vertex vert : verts) {
			d = cov.distanceSquared(vert.coords);
			if (d > rad_) rad_ = d;
		}
		rad_ = java.lang.Math.sqrt(rad_);
	}

	// helper methods SQR, CUBE
	private final double SQR(final double x) {
		return x * x;
	}

	private final double CUBE(final double x) {
		return x * x * x;
	}

	private final PolyTreePair ptreepair = new PolyTreePair();
	private final ClosestPointPair cpair = new ClosestPointPair();
	private final Vector3d cnrml = new Vector3d();

	public final double vclip(final DistanceReport rep, final PolyTree ptree2, final Matrix4d Xr2r1,
			final double distLimit, final ClosestFeaturesHT ht) {
		return vclip(rep, ptree2, Xr2r1, distLimit, ht, null);
	}

	/**
	 * Computes the distance between this PolyTree and a second
	 * PolyTree. This is done by recursively computing the
	 * distance between all pairs of component PolyTrees
	 * and returning the minimum.
	 * 
	 * <p>In particular, this minimum distance in generally the
	 * minimum distance over all pairs of convex polyhedra
	 * associated with the two PolyTrees. However, if the
	 * argument <code>distLimit</code> is non-negative, and the bounding
	 * hull for a compound component A is determined to be at least
	 * <code>distLimit</code> away from another component B, then
	 * the bounding hull distance is used instead as the
	 * distance between A and B and the components of A are
	 * not examined. This speeds up computations for PolyTrees
	 * which are more than a certain distance from each other,
	 * but also means that the real distance between two PolyTrees
	 * may in fact be larger than the returned value.
	 *
	 * <p>If there is a collision between the two PolyTrees, then
	 * a non-positive distance value is returned.
	 *
	 * <p>The
	 * closest points and features associated with the minimum
	 * distance are returned in an optional {@link DistanceReport
	 * DistanceReport} object.  If <code>distLimit</code> is
	 * active, then these points and features may belong to the
	 * bounding hull of a compound component, rather than the
	 * polyhedron of an atomic PolyTree. In case of a collision,
	 * the associated closest points and features may not be that
	 * meaningfull.
	 *
	 * <p>The distance report may also return the closest
	 * point pairs between atomic PolyTrees that are within
	 * a certain maximum distance. See the {@link
	 * DistanceReport DistanceReport} documentation
	 * for details.
	 *
	 * <p><code>vclip</code> computations are often much faster
	 * if the closest features between pairs of PolyTrees
	 * are stored and then used as an initial guess
	 * in subsequent computations. The optional argument
	 * <code>ht</code> supplies a
	 * {@link ClosestFeaturesHT ClosestFeaturesHT} for storing
	 * such information.
	 *
	 * @param rep (optional) returns
	 * information about the closest pairs of points and features
	 * between the two PolyTrees.
	 * @param ptree2 the second PolyTree
	 * @param Xr2r1 transformation from the reference frame
	 * of the second PolyTree to the reference frame of the first
	 * @param distLimit  if non-negative, tells <code>vclip</code>
	 * to not consider distances to a PolyTree's components
	 * if the distance to its bounding hull
	 * equals or exceeds <code>distLimit</code>
	 * @param ht (optional) record of the most recent feature pairs
	 * computed between different PolyTrees
	 * @return distance beteen the PolyTrees. A non-positive value
	 * indicates a collision.
	 * @see DistanceReport */
	public final double vclip(final DistanceReport rep, final PolyTree ptree2, final Matrix4d Xr2r1,
			final double distLimit, final ClosestFeaturesHT ht, final FeaturePair startFeatures) {
		final PolyTree ptree1 = this;
		double dist = 0;

		// If either PolyTree is null, then return an infinite distance
		if (ptree1.poly_ == null && ptree1.isAtomic() || ptree2.poly_ == null && ptree2.isAtomic()) //  	      if (rep != null) 
			//  	       { rep.clear();
			//  	       }
			return Double.POSITIVE_INFINITY;

		if (ptree1.poly_ != null && ptree2.poly_ != null) {
			// find closest features
			ptreepair.first = ptree1;
			ptreepair.second = ptree2;
			FeaturePair features;
			Object featureObj = null;
			if (ht != null) featureObj = ht.get(ptreepair);
			if (featureObj == null) { // initialization:
				// set feature to first vertices on each PolyTree
				features = new FeaturePair(ptree1.poly_.verts[0], ptree2.poly_.verts[0]);
				if (ht != null) ht.put(ptreepair, features);
			} else
				features = (FeaturePair) featureObj;

			cpair.setFeatures(features.first, features.second);
			if (startFeatures != null) startFeatures.set(features);

			// call atomic algorithm on current pair
			final Matrix4dX X12 = featureBlock1.T;
			final Matrix4dX X21 = featureBlock2.T;
			X12.mulInverseLeft(Xr2r1, ptree1.Xlr_);
			X12.mulInverseLeft(ptree2.Xlr_, X12);
			X21.invertTrans(X12);
			ConvexPolyhedron.ptree1name = ptree1.name;
			ConvexPolyhedron.ptree2name = ptree2.name;
			dist = ptree1.poly_.vclip(cpair, ptree2.poly_, featureBlock1, featureBlock2);

			// transform cp's to reference frames
			ptree1.Xlr_.transform(cpair.pnt1);
			ptree2.Xlr_.transform(cpair.pnt2);
			//	      System.out.println ("normal * = " + cpair.nrml);
			cnrml.set(cpair.nrml);
			ptree1.Xlr_.transform(cpair.nrml);
			//	      System.out.println ("normal X = " + cpair.nrml);
			features.first = cpair.feat1;
			features.second = cpair.feat2;

			if (ptree1.isAtomic() && ptree2.isAtomic()) {
				if (rep != null) if (dist > 0 && dist <= rep.maxDist && rep.promoteFeatures)
					dist = promoteContactFeatures(rep, cpair, cnrml, ptree2);
				else {
					rep.setClosestPairIfNecessary(cpair);
					if (!cpair.feat1.hidden && !cpair.feat2.hidden) rep.addClosePairIfNecessary(cpair);
				}
				return dist;
			}
			if (distLimit >= 0 && dist > distLimit) {
				if (rep != null) rep.setClosestPairIfNecessary(cpair);
				return dist;
			}
		}

		double minDist = Double.POSITIVE_INFINITY;

		if (ptree1.components.size() != 0)
			for (final Iterator it = ptree1.components.iterator(); it.hasNext();) {
				final PolyTree ptree = (PolyTree) it.next();
				dist = ptree.vclip(rep, ptree2, Xr2r1, distLimit, ht);
				if (dist <= 0) return dist;
				if (dist < minDist) minDist = dist;
			}
		else
			for (final Iterator it = ptree2.components.iterator(); it.hasNext();) {
				final PolyTree ptree = (PolyTree) it.next();
				dist = ptree1.vclip(rep, ptree, Xr2r1, distLimit, ht);
				if (dist <= 0) return dist;
				if (dist < minDist) minDist = dist;
			}
		return minDist;
	}

	private double promoteContactFeatures(final DistanceReport rep, final ClosestPointPair cpair, final Vector3d nrml_1,
			final PolyTree ptree2) {
		final Feature oldFeat1 = cpair.feat1;
		final Feature oldFeat2 = cpair.feat2;

		final Matrix4dX X12 = featureBlock1.T;

		final Vector3d nrml_2 = new Vector3d();

		cpair.feat1 = cpair.feat1.promote(nrml_1, rep.promotionTol);
		X12.transform(nrml_1, nrml_2);
		cpair.feat2 = cpair.feat2.promote(nrml_2, rep.promotionTol);

		if (cpair.feat1.hidden || cpair.feat2.hidden) {
			rep.setClosestPairIfNecessary(cpair);
			return cpair.dist;
		}

		double newd = Double.POSITIVE_INFINITY;
		if (cpair.feat1 != oldFeat1 || cpair.feat2 != oldFeat2)
			if (cpair.feat1.type == Feature.FACE && cpair.feat2.type == Feature.EDGE)
				newd = doFaceEdge(rep, (Face) cpair.feat1, (Edge) cpair.feat2, this, ptree2, /*faceOnPtree1=*/true);
			else if (cpair.feat1.type == Feature.EDGE && cpair.feat2.type == Feature.FACE)
				newd = doFaceEdge(rep, (Face) cpair.feat2, (Edge) cpair.feat1, ptree2, this, /*faceOnPtree1=*/false);
			else if (cpair.feat1.type == Feature.FACE && cpair.feat2.type == Feature.FACE)
				newd = doFaceFace(rep, (Face) cpair.feat1, (Face) cpair.feat2, this, ptree2);
		if (newd == Double.POSITIVE_INFINITY) {
			rep.setClosestPairIfNecessary(cpair);
			rep.addClosePairIfNecessary(cpair);
			return cpair.dist;
		} else
			return newd;
	}

	private double doFaceEdge(final DistanceReport rep, final Face f, final Edge e, final PolyTree ptreeF,
			final PolyTree ptreeE, final boolean faceOnPtree1) {
		final ConvexPolygon poly = new ConvexPolygon();
		final Line2d line = new Line2d();
		final double[] lam = new double[2];
		final ClosestPointPair cpair = new ClosestPointPair();
		Point3d pntf, pnte;
		Matrix4dX XEF;

		if (faceOnPtree1) {
			cpair.feat1 = f;
			cpair.feat2 = e;
			XEF = featureBlock2.T;
			ptreeF.Xlr_.transform(f.plane.normal, cpair.nrml);
			pntf = cpair.pnt1;
			pnte = cpair.pnt2;
		} else {
			cpair.feat1 = e;
			cpair.feat2 = f;
			XEF = featureBlock1.T;
			featureBlock2.T.transform(f.plane.normal, cpair.nrml);
			ptreeE.Xlr_.transform(cpair.nrml, cpair.nrml);
			cpair.nrml.negate();
			pntf = cpair.pnt2;
			pnte = cpair.pnt1;
		}

		final Matrix4dX XFP = new Matrix4dX();
		final Matrix4dX XEP = new Matrix4dX();
		final Point3d xpnt = new Point3d();
		f.plane.planeTransform(XFP, f.coneNode0.nbr.head.coords);
		f.projectToPlane(poly, XFP, /*ccw=*/true);

		XEP.mul(XFP, XEF);
		e.projectToPlane(line, XEP);

		double minDist = Double.POSITIVE_INFINITY;

		final int cnt = poly.intersectLine(lam, line);
		if (cnt == 2) {
			if (lam[1] > 1) lam[1] = 1;
			if (lam[0] < 0) lam[0] = 0;
			if (lam[0] != lam[1]) {
				for (int i = 0; i < 2; i++) {
					xpnt.set(line.q.x + line.u.x * lam[i], line.q.y + line.u.y * lam[i], 0);
					XFP.inverseSpatialTransform(xpnt, pntf);
					pnte.scaleAdd(lam[i] * e.length(), e.dir, e.tail.coords);
					XEF.transform(pnte, xpnt);
					cpair.dist = pntf.distance(xpnt);
					if (cpair.dist < minDist) minDist = cpair.dist;
					ptreeF.Xlr_.transform(pntf, pntf);
					ptreeE.Xlr_.transform(pnte, pnte);
					rep.setClosestPairIfNecessary(cpair);
					rep.addClosePairIfNecessary(cpair);
				}
				return minDist;
			}
		}

		return minDist;
	}

	private double doFaceFace(final DistanceReport rep, final Face f1, final Face f2, final PolyTree ptree1,
			final PolyTree ptree2) {
		final ConvexPolygon poly1 = new ConvexPolygon();
		final ConvexPolygon poly2 = new ConvexPolygon();
		final ConvexPolygon polyi = new ConvexPolygon();
		final ClosestPointPair cpair = new ClosestPointPair();

		final Matrix4dX X12 = featureBlock1.T;
		final Matrix4dX X21 = featureBlock2.T;

		final Matrix4dX X1P = new Matrix4dX();
		final Matrix4dX X2P = new Matrix4dX();
		final Matrix4dX X1F2 = new Matrix4dX();
		final Point3d xpnt = new Point3d();
		f1.plane.planeTransform(X1P, f1.coneNode0.nbr.head.coords);
		f1.projectToPlane(poly1, X1P, /*ccw=*/true);
		X2P.mul(X1P, X21);
		f2.projectToPlane(poly2, X2P, /*ccw=*/false);

		cpair.feat1 = f1;
		cpair.feat2 = f2;
		ptree1.Xlr_.transform(f1.plane.normal, cpair.nrml);

		f2.plane.projectionMatrix(X1F2);
		X1F2.mul(X12);

		double minDist = Double.POSITIVE_INFINITY;

		polyi.intersect(poly1, poly2);
		if (polyi.numVertices() > 1) {
			final ConvexPolygon.VertexIterator it = polyi.getVertexIterator();
			while (it.hasNext()) {
				final Point2d vtx = it.nextVertex();
				xpnt.set(vtx.x, vtx.y, 0);
				X1P.inverseSpatialTransform(xpnt, cpair.pnt1);
				X1F2.transform(cpair.pnt1, cpair.pnt2);
				X21.transform(cpair.pnt2, xpnt);
				cpair.dist = cpair.pnt1.distance(xpnt);
				if (cpair.dist < minDist) minDist = cpair.dist;
				ptree1.Xlr_.transform(cpair.pnt1, cpair.pnt1);
				ptree2.Xlr_.transform(cpair.pnt2, cpair.pnt2);
				rep.setClosestPairIfNecessary(cpair);
				rep.addClosePairIfNecessary(cpair);
			}
			return minDist;
		}
		return minDist;
	}

	/**
	 * Reads in a PolyTree from a Reader. The format
	 * is specified below. The input may refer to other named
	 * PolyTrees. If it does, then a reference to each named
	 * PolyTree should appear in the supplied library.
	 *
	 * An atomic PolyTree is specified according to the format
	 * <pre>
	 * atomic <i>name</i> <i>polyhedron</i>
	 * </pre>
	 * where <i>name</i> is an identifier naming the PolyTree,
	 * and <i>polyhedron</i> is a specification for a polyhedron
	 * (which should take the form specified in either
	 * {@link ConvexPolyhedron#scanNamedFormat(Reader)} or
	 * {@link ConvexPolyhedron#scan(Reader)}, depending
	 * on whether <code>nameFormat</code> is <code>true</code>).
	 *
	 * A compound PolyTree is specified by giving a list of
	 * its component PolyTrees between square brackets, as in
	 * <pre>
	 * compound <i>name</i>
	 *  [  <i>transform</i> <i>PolyTree</i>
	 *     <i>transform</i> <i>PolyTree</i>
	 *         ...
	 *  ]
	 * </pre>
	 *<i>name</i> is an identifier naming the PolyTree.
	 *<p>
	 *<i>transform</i> specifies a transformation from the
	 * reference frame of the component to this PolyTree's
	 * local frame, and <i>PolyTree</i> is either a
	 * specification for a PolyTree (as described here),
	 * <i>or</i> a string identifier naming a PolyTree
	 * that can copied from the supplied library.
	 *
	 * <p>A transform specification consists of a series
	 * of translation and rotation terms listed between
	 * square brackets, as in:
	 * <pre>
	 * [ trans 1 2 3  rotx 30 roty 20 ],
	 * </pre>
	 * which describes a translation along (1,2,3), followed
	 * by a rotation of 30 degrees about the x axis, followed
	 * by a rotation of 20 degrees about the y axis.
	 *
	 * <p>
	 * Succesive translations and rotations are accumulated
	 * from left to right. The terms
	 * that can be specified are:
	 * <dl>
	 * <dt><code>trans <i>x</i> <i>y</i> <i>z</i></code>
	 * <dd>A translation along the vector (<i>x</i>, <i>y</i>, <i>z</i>).
	 * <dt><code>rotx <i>deg</i></code>
	 * <dd>A rotation of <i>deg</i> degrees about the x axis.
	 * <dt><code>roty <i>deg</i></code>
	 * <dd>A rotation of <i>deg</i> degrees about the y axis.
	 * <dt><code>rotz <i>deg</i></code>
	 * <dd>A rotation of <i>deg</i> degrees about the z axis.
	 * <dt><code>rot <i>ux</i> <i>uy</i> <i>uz</i> <i>deg</i></code>
	 * <dd>A rotation of <i>deg</i> degress about the vector
	 * (<i>ux</i>, <i>uy</i>, <i>uz</i>).
	 * </dl>
	 *
	 * <p>
	 * A simple example of a compound PolyTree specification is
	 * <pre>
	 * compound two-cubes
	 * [
	 *	[trans -1 0 0] unit-cube
	 *	[trans +1 0 0] unit-cube
	 * ]
	 * </pre>
	 * which creates a compound PolyTree named <code>two-cubes</code>,
	 * with two components, each a copy of a PolyTree named
	 * <code>unit-cube</code>, translated by -1 and
	 * 1 (respectively) along the x axis.
	 *
	 * @param reader the Reader which supplies the input
	 * @param library an optional library from which to look up referenced
	 * PolyTrees. The library is simply a map of string names
	 * onto PolyTree objects.
	 * @param updateLibrary if <code>true</code>, then 
	 * the input PolyTree will be inserted
	 * into the supplied library (assuming <code>library</code> is
	 * not <code>null</code>)
	 * @param namedFormat if <code>true</code>, then
	 * polyhedra will be read in using the <i>named</i> format
	 * used by {@link ConvexPolyhedron#scanNamedFormat(Reader)
	 * ConvexPolyhedron.scanNamedFormat}.
	 * Otherwise, the <code>.obj</code> format of 
	 * {@link ConvexPolyhedron#scan(Reader) ConvexPolyhedron.scan}
	 * will be used
	 * @throws IOException if an I/O or format error occurs
	 * @see #scan(StreamTokenizer,Map,boolean,boolean)
	 */
	public void scan(final Reader reader, final Map library, final boolean updateLibrary, final boolean namedFormat)
			throws IOException {
		final StreamTokenizer stok = new StreamTokenizer(reader);
		stok.commentChar('#');
		scan(stok, library, updateLibrary, namedFormat);
	}

	private void scanToken(final StreamTokenizer stok, final int token) throws IOException {
		stok.nextToken();
		if (stok.ttype != token) throw new IOException("Expecting token '" + (char) token + "', line " + stok.lineno());
	}

	/**
	 * Reads in a PolyTree from a StreamTokenizer. Otherwise
	 * identical to {@link #scan(Reader,Map,boolean,boolean)
	 * scan(Reader,Map,boolean,boolean)}.
	 *
	 * @param stok StreamTokenizer which supplies the input
	 * @param library an optional library from which to look up referenced
	 * PolyTrees. 
	 * @param updateLibrary if <code>true</code>, then 
	 * the input PolyTree will be inserted
	 * into the supplied library
	 * @param namedFormat if <code>true</code>, then
	 * polyhedra will be read in using the <i>named</i> format
	 * used by {@link ConvexPolyhedron#scanNamedFormat(Reader)
	 * ConvexPolyhedron.scanNamedFormat}.
	 * Otherwise, the <code>.obj</code> format of 
	 * {@link ConvexPolyhedron#scan(Reader) ConvexPolyhedron.scan}
	 * will be used
	 * @throws IOException if an I/O or format error occurs
	 * @see #scan(Reader,Map,boolean,boolean)
	 */
	public void scan(final StreamTokenizer stok, final Map library, final boolean updateLibrary, final boolean namedFormat)
			throws IOException {
		String keyword;

		clearComponents();
		keyword = TokenScanner.scanWord(stok);
		if (keyword.equals("atomic")) {
			name = TokenScanner.scanWord(stok);
			final ConvexPolyhedron poly = new ConvexPolyhedron();
			if (namedFormat)
				poly.scanNamedFormat(stok);
			else
				poly.scan(stok);
			setPolyhedron(poly);
			if (updateLibrary && library != null) library.put(name, this);
		} else if (keyword.equals("compound")) {
			name = TokenScanner.scanWord(stok);
			scanToken(stok, '[');
			final Matrix4dX T = new Matrix4dX();
			while (true) {
				final PolyTree ptree = new PolyTree("");
				try {
					T.scan("[%s]", stok);
				} catch (final IOException e) {
					break;
				}
				ptree.scan(stok, library, updateLibrary, namedFormat);
				ptree.setTransform(T);
				components.add(ptree);
			}
			scanToken(stok, ']');
			buildBoundingHull();
			if (updateLibrary && library != null) library.put(name, this);
		} else {
			name = keyword;
			if (library == null) throw new IOException("No library specified to find PolyTree");
			final PolyTree ptree = (PolyTree) library.get(name);
			if (ptree == null) throw new IOException("PolyTree " + name + " not found in library");
			set(ptree);
		}
	}

	/**
	 * Reads a library of PolyTrees from a file.
	 *
	 * @param fileName name of the file
	 * @param library library into which the PolyTrees read from
	 * the file will be placed. The library is simply
	 * a map of names onto PolyTree objects.
	 * @param namedFormat if <code>true</code>, then
	 * polyhedra will be read in using the <i>named</i> format
	 * used by {@link ConvexPolyhedron#scanNamedFormat(Reader)
	 * ConvexPolyhedron.scanNamedFormat}.
	 * Otherwise, the <code>.obj</code> format of
	 * {@link ConvexPolyhedron#scan(Reader) ConvexPolyhedron.scan}
	 * will be used
	 * @throws IOException if an I/O or format error occurs
	 * @see #scanLibrary(StreamTokenizer,Map,boolean)
	 */
	static public void scanLibrary(final String fileName, final Map library, final boolean namedFormat) throws IOException {
		final StreamTokenizer stok = new StreamTokenizer(new FileReader(fileName));
		stok.commentChar('#');
		scanLibrary(stok, library, namedFormat);
	}

	/**
	 * Reads a library of PolyTrees from a StreamTokenizer.
	 *
	 * @param stok tokenizer from which to read library
	 * @param library library into which the PolyTrees read from
	 * the file will be placed. The library is simply
	 * a map of names onto PolyTree objects.
	 * @param namedFormat if <code>true</code>, then
	 * polyhedra will be read in using the <i>named</i> format
	 * used by {@link ConvexPolyhedron#scanNamedFormat(Reader)
	 * ConvexPolyhedron.scanNamedFormat}.
	 * Otherwise, the <code>.obj</code> format of
	 * {@link ConvexPolyhedron#scan(Reader) ConvexPolyhedron.scan}
	 * will be used
	 * @throws IOException if an I/O or format error occurs
	 * @see #scanLibrary(String,Map,boolean)
	 */
	static public void scanLibrary(final StreamTokenizer stok, final Map library, final boolean namedFormat)
			throws IOException {
		try {
			while (true) {
				final PolyTree ptree = new PolyTree("");
				try {
					ptree.scan(stok, library, true, namedFormat);
				} catch (final EOFException eof) {
					break;
				}
			}
		} catch (final IOException e) {
			throw e;
		}
	}

	static public PolyTree createBox(final String name, final double wx, final double wy, final double wz) {
		return new PolyTree(name, ConvexPolyhedron.createBox(wx, wy, wz));
	}

	static public PolyTree createCylinder(final String name, final double r, final double h, final int nsides) {
		return new PolyTree(name, ConvexPolyhedron.createCylinder(r, h, nsides));
	}

	static public PolyTree createSphere(final String name, final double r, final int nslices) {
		return new PolyTree(name, ConvexPolyhedron.createSphere(r, nslices));
	}

	static public PolyTree createRoundedCylinder(final String name, final double r, final double h, final int nslices) {
		return new PolyTree(name, ConvexPolyhedron.createRoundedCylinder(r, h, nslices));
	}

	static public PolyTree createCone(final String name, final double rtop, final double rbot, final double h,
			final int nsides) {
		return new PolyTree(name, ConvexPolyhedron.createCone(rtop, rbot, h, nsides));
	}

	static public PolyTree createPrism(final String name, final double[] xyTop, final double[] xyBot, final double h) {
		return new PolyTree(name, ConvexPolyhedron.createPrism(xyTop, xyBot, h));
	}

	static public PolyTree createPrism(final String name, final double[] xy, final double h) {
		return new PolyTree(name, ConvexPolyhedron.createPrism(xy, h));
	}
};
