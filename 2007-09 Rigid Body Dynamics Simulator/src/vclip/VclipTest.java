package vclip;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Test class for the vclip package. In the vclip source directory, just run
 * <pre>
 *    java vclip.VclipTest
 * </pre>
 * 
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>
 */
public class VclipTest {
	Vector testlist;
	HashMap library;
	HashMap bodies = new HashMap(100);
	ClosestFeaturesHT closestFeaturesHT = new ClosestFeaturesHT();
	boolean exhaustive = true;

	//	private static boolean useLoad = false;
	private static final double TOL = 1e-6;

	private static final Vertex dummyVertex = new Vertex();
	private static final Face dummyFace = new Face();
	private static final Edge dummyEdge = new Edge();

	private static double MAX_DIST = 10.0;

	private class NormalDistPair {
		Vector3dX vec;
		double d;

		NormalDistPair(final double ux, final double uy, final double uz, final double d) {
			vec = new Vector3dX(ux, uy, uz);
			vec.normalize();
			this.d = d;
		}

		boolean check(final ClosestPointPair cpair) {
			final Vector3d del = new Vector3d();

			del.sub(cpair.pnt2, cpair.pnt1);
			if (Math.abs(del.length() - d) > TOL) return false;
			del.normalize();
			if (!vec.epsilonEquals(del, TOL)) return false;
			return true;
		}
	}

	private class Record {
		String poly1;
		String poly2;
		Matrix4dX X12;
		Matrix4dX X21;
		int lineNumber = 0;

		double minDist;
		Vector3d minNrml;
		double maxPairDist = 0;
		boolean promote = false;

		ClosestPointPair[] basePairs;
		ClosestPointPair[] testPairs;

		Record() {
			X12 = new Matrix4dX();
			X21 = new Matrix4dX();
		}

		DistanceReport createDistanceReport() {
			final DistanceReport rep = new DistanceReport();
			if (promote) rep.setFeaturePromotion(true, -1);
			if (maxPairDist != 0) {
				rep.setMaxClosePairs(100);
				rep.setMaxPairDistance(maxPairDist);
			}
			return rep;
		}

	}

	VclipTest(final Reader testDataReader, final StreamTokenizer polyTreeStream) {
		testlist = new Vector(100);
		library = new HashMap();
		try {
			PolyTree.scanLibrary(polyTreeStream, library, true);
		} catch (final Exception e) {
			System.out.println("Error polyTreeStream");
			e.printStackTrace();
			System.exit(1);
		}
		readTestList(testDataReader);
	}

	private String getFeatureType(final String s, final StreamTokenizer stok) throws IOException {
		if (s.equals("V"))
			return "vclip.Vertex";
		else if (s.equals("F"))
			return "vclip.Face";
		else if (s.equals("E"))
			return "vclip.Edge";
		else {
			System.out.println("Unknown feature " + s + ", line " + stok.lineno());
			System.exit(1);
			return null;
		}
	}

	private Feature getDummyFeature(final String s, final StreamTokenizer stok) throws IOException {
		if (s.equals("V"))
			return dummyVertex;
		else if (s.equals("F"))
			return dummyFace;
		else if (s.equals("E"))
			return dummyEdge;
		else {
			System.out.println("Unknown feature " + s + ", line " + stok.lineno());
			System.exit(1);
			return null;
		}
	}

	private String scanString(final StreamTokenizer stok) throws IOException {
		stok.nextToken();
		if (stok.ttype == StreamTokenizer.TT_EOF)
			throw new EOFException();
		else if (stok.ttype != StreamTokenizer.TT_WORD)
			throw new IOException("Expecting word, line " + stok.lineno());
		else
			return stok.sval;
	}

	private Record readTestRecord(final StreamTokenizer stok) throws IOException {
		final Record rec = new Record();
		final double[] vals;
		try {
			rec.poly1 = scanString(stok);
		} catch (final EOFException e) {
			return null;
		}
		rec.lineNumber = stok.lineno();
		rec.poly2 = scanString(stok);
		rec.X21.scan("[%s]", stok);
		rec.X12.invert(rec.X21);
		final Vector pairList = new Vector(10);
		String s;
		rec.minDist = Double.POSITIVE_INFINITY;

		while (true) {
			s = scanString(stok);
			if (s.equals("P"))
				rec.promote = true;
			else if (s.equals("D"))
				rec.maxPairDist = TokenScanner.scanDouble(stok);
			else
				break;
		}

		while (!s.equals("*")) {
			final ClosestPointPair pair = new ClosestPointPair();
			if (!s.equals("N")) {
				pair.feat1 = getDummyFeature(s, stok);
				pair.feat2 = getDummyFeature(scanString(stok), stok);
				final Point3dX p1 = new Point3dX();
				p1.scan("[%v]", stok);
				pair.pnt1.set(p1);
			}
			final Vector3dX dir = new Vector3dX();
			dir.scan("[%v]", stok);
			dir.normalize();
			pair.nrml.set(dir);
			pair.dist = TokenScanner.scanDouble(stok);

			if (!s.equals("N")) pair.pnt2.scaleAdd(pair.dist, pair.nrml, pair.pnt1);

			if (pair.dist < rec.minDist) {
				rec.minDist = pair.dist;
				rec.minNrml = new Vector3d(pair.nrml);
			}

			pairList.add(pair);
			s = scanString(stok);
		}
		rec.basePairs = (ClosestPointPair[]) pairList.toArray(new ClosestPointPair[0]);
		rec.testPairs = new ClosestPointPair[rec.basePairs.length];
		for (int i = 0; i < rec.testPairs.length; i++)
			rec.testPairs[i] = new ClosestPointPair(rec.basePairs[i]);
		return rec;
	}

	private void readTestList(final Reader reader) {
		final StreamTokenizer stok = new StreamTokenizer(reader);
		stok.commentChar('#');
		stok.wordChars('*', '*');

		Record rec = null;
		do {
			try {
				rec = readTestRecord(stok);
			} catch (final Exception e) {
				System.out.println("Error reading test data");
				e.printStackTrace();
				System.exit(1);
			}
			if (rec != null) testlist.add(rec);
		} while (rec != null);

		// create a body for every entry in the polytree library

		for (final Iterator it = library.values().iterator(); it.hasNext();) {
			final PolyTree pt = (PolyTree) it.next();
			bodies.put(pt.name, new PolyTree(null, pt));
		}
	}

	private void printResults(final DistanceReport rep, final FeaturePair fpair) {
		final Vector3dX del = new Vector3dX();
		final ClosestPointPair cpp = rep.getClosestPair();
		del.sub(cpp.pnt2, cpp.pnt1);
		System.out.println("Initial Features: " + fpair.first.getName() + " " + fpair.second.getName());
		System.out.println("dist=" + rep.getClosestDistance());
		System.out.println("del=" + del);
		System.out.println("pnt1_1=" + cpp.pnt1);
		System.out.println("pnt2_1=" + cpp.pnt2);
		System.out.println("nrml_1=" + cpp.nrml);
		if (cpp.feat1 != null)
			System.out.println("feature1=" + cpp.feat1.getClass().getName() + "  " + cpp.feat1.name);
		else
			System.out.println("feature1=null");
		if (cpp.feat2 != null)
			System.out.println("feature2=" + cpp.feat2.getClass().getName() + "  " + cpp.feat2.name);
		else
			System.out.println("feature2=null");
	}

	private boolean matchPair(final ClosestPointPair cpair, final ClosestPointPair check) {
		if (check.dist <= 0) { // then just verify collison
			if (cpair.dist > 0) return false;
		} else if (check.feat1 == null) {
			final Vector3d del = new Vector3d();
			del.sub(cpair.pnt2, cpair.pnt1);

			// then just check distance and direction
			if (Math.abs(check.dist - cpair.dist) > TOL || Math.abs(del.length() - check.dist) > TOL) return false;
			if (!check.nrml.epsilonEquals(cpair.nrml, TOL)) return false;
		} else {
			final Vector3d del = new Vector3d();
			del.sub(cpair.pnt2, cpair.pnt1);

			if (Math.abs(check.dist - cpair.dist) > TOL || Math.abs(del.length() - check.dist) > TOL) return false;
			if (!cpair.pnt1.epsilonEquals(check.pnt1, TOL)) return false;
			if (!cpair.pnt2.epsilonEquals(check.pnt2, TOL)) return false;
			if (cpair.feat1.getType() != check.feat1.getType() || cpair.feat2.getType() != check.feat2.getType())
				return false;
		}

		if (check.dist > 0) if (!cpair.nrml.epsilonEquals(check.nrml, TOL)) return false;
		return true;
	}

	private String pairToStr(final ClosestPointPair pair) {
		String s = "";
		if (pair.feat1 != null) {
			s += pair.feat1.getName() + " " + pair.feat2.getName() + " ";
			s += pair.pnt1 + " " + pair.pnt2 + " ";
		} else
			s += "N ";
		s += pair.nrml + " " + pair.dist;
		return s;
	}

	private void printError(final String msg, final Record rec, final DistanceReport rep, final FeaturePair fpair) {
		System.out.println("Error, line " + rec.lineNumber);
		if (msg != null) System.out.println(msg);
		System.out.println("Wanted:");
		for (final ClosestPointPair testPair : rec.testPairs)
			System.out.println(pairToStr(testPair));
		System.out.println("Got:");
		if (rep.numClosePairs() > 0) {
			final ClosestPointPair[] pairs = rep.getClosePairs();
			for (int i = 0; i < rep.numClosePairs(); i++)
				System.out.println(pairToStr(pairs[i]));
		} else
			System.out.println(pairToStr(rep.getClosestPair()));
		System.out.println("Initial Features: " + fpair.first.getName() + " " + fpair.second.getName());
		System.exit(1);
	}

	private void checkVclipResults(final Record rec, final Vector3d offset, final DistanceReport rep,
			final double retDist, final FeaturePair fpair) {
		for (int i = 0; i < rec.testPairs.length; i++) {
			rec.testPairs[i].pnt2.add(rec.basePairs[i].pnt2, offset);
			rec.testPairs[i].dist = rec.basePairs[i].dist + offset.dot(rec.minNrml);
		}

		if (rep.getClosestDistance() != retDist) {
			final String msg = "dist returned=" + retDist + " closestDistance=" + rep.getClosestDistance();
			printError(msg, rec, rep, fpair);
		}

		if (rep.numClosePairs() == 0) {
			final ClosestPointPair cpair = rep.getClosestPair();
			boolean expectedCollision = false;
			for (final ClosestPointPair testPair : rec.testPairs)
				if (testPair.dist <= 0) {
					expectedCollision = true;
					break;
				}
			if (expectedCollision) {
				if (cpair.dist > 0) printError("Expected collision", rec, rep, fpair);
			} else if (rec.testPairs.length != 1 || !matchPair(rep.getClosestPair(), rec.testPairs[0]))
				printError(null, rec, rep, fpair);
		} else {
			final int n = rep.numClosePairs();
			if (n != rec.testPairs.length) printError(null, rec, rep, fpair);
			final ClosestPointPair[] pairs = new ClosestPointPair[n];
			for (int i = 0; i < n; i++)
				pairs[i] = rep.getClosePairs()[i];
			for (int i = 0; i < n; i++) {
				int j;
				for (j = 0; j < n; j++)
					if (pairs[j] != null && matchPair(rec.testPairs[i], pairs[j])) {
						pairs[j] = null;
						break;
					}
				if (j == n) printError(null, rec, rep, fpair);
			}
		}
	}

	private Vector getAllPolyhdreonFeatures(final ConvexPolyhedron poly) {
		final Vector vec = new Vector(32);
		for (int i = 0; i < poly.getVerts().length; i++)
			vec.add(poly.getVerts()[i]);
		for (int i = 0; i < poly.getEdges().length; i++)
			vec.add(poly.getEdges()[i]);
		for (int i = 0; i < poly.getFaces().length; i++)
			vec.add(poly.getFaces()[i]);
		return vec;
	}

	private void singleCheck(final Record rec, final Vector3d offset, final PolyTree ptree1, final PolyTree ptree2,
			final String fname1, final String fname2) {
		final Matrix4dX X21 = new Matrix4dX(rec.X21);
		final Matrix4dX X12 = new Matrix4dX();

		X21.m03 += offset.x;
		X21.m13 += offset.y;
		X21.m23 += offset.z;

		X12.invert(X21);

		FeaturePair featurePair;
		if (fname1 != null) {
			Feature f1, f2;
			if (fname1.equals(""))
				f1 = ptree1.poly_.getVerts()[0];
			else
				f1 = ptree1.poly_.findFeature(fname1);
			if (fname1.equals(""))
				f2 = ptree2.poly_.getVerts()[0];
			else
				f2 = ptree2.poly_.findFeature(fname2);
			featurePair = new FeaturePair(f1, f2);
			final PolyTreePair ptreePair = new PolyTreePair(ptree1, ptree2);
			closestFeaturesHT.put(ptreePair, featurePair);
		} else {
			featurePair = (FeaturePair) closestFeaturesHT.get(new PolyTreePair(ptree1, ptree2));
			if (featurePair == null)
				featurePair = new FeaturePair(ptree1.poly_.getVerts()[0], ptree2.poly_.getVerts()[0]);
		}

		final DistanceReport rep = rec.createDistanceReport();
		final double d = ptree1.vclip(rep, ptree2, X21, MAX_DIST, closestFeaturesHT);
		rep.transformSecondPoints(X21);
		checkVclipResults(rec, offset, rep, d, featurePair);
	}

	private void exhaustiveCheck(final Record rec, final Vector3d offset, final PolyTree ptree1, final PolyTree ptree2) {
		final Matrix4dX X21 = new Matrix4dX(rec.X21);
		final Matrix4dX X12 = new Matrix4dX();

		X21.m03 += offset.x;
		X21.m13 += offset.y;
		X21.m23 += offset.z;

		X12.invert(X21);

		final Vector flist1 = getAllPolyhdreonFeatures(ptree1.getPolyhedron());
		final Vector flist2 = getAllPolyhdreonFeatures(ptree2.getPolyhedron());
		for (final Iterator it1 = flist1.iterator(); it1.hasNext();) {
			final Feature f1 = (Feature) it1.next();
			for (final Iterator it2 = flist2.iterator(); it2.hasNext();) {
				final Feature f2 = (Feature) it2.next();

				if (!(f1.type == Feature.FACE && f2.type == Feature.FACE)) {
					final FeaturePair featurePair = new FeaturePair(f1, f2);
					final PolyTreePair ptreePair = new PolyTreePair(ptree1, ptree2);
					closestFeaturesHT.put(ptreePair, featurePair);

					final DistanceReport rep = rec.createDistanceReport();
					final double d = ptree1.vclip(rep, ptree2, X21, MAX_DIST, closestFeaturesHT);
					rep.transformSecondPoints(X21);
					checkVclipResults(rec, offset, rep, d, featurePair);
				}
			}
		}
	}

	private void checkDistanceReport(final DistanceReport rep, final NormalDistPair[] checkpairs) throws Exception {
		if (rep.numClosePairs() != checkpairs.length)
			throw new Exception("Distance report has " + rep.numClosePairs() + " close pairs vs. " + checkpairs.length);
		final ClosestPointPair[] list = new ClosestPointPair[checkpairs.length];
		for (int i = 0; i < list.length; i++)
			list[i] = rep.getClosePairs()[i];
		for (final NormalDistPair checkpair : checkpairs) {
			int j;
			for (j = 0; j < list.length; j++)
				if (list[j] != null && checkpair.check(list[j])) {
					list[j] = null;
					break;
				}
			if (j == list.length)
				throw new Exception("close pair " + checkpair.vec + ", d=" + checkpair.d + " not found");
		}
	}

	private void dotest(final Record rec) {
		PolyTree ptree1;
		PolyTree ptree2;

		ptree1 = (PolyTree) bodies.get(rec.poly1);
		if (ptree1 == null) {
			System.err.println("PolyTree " + rec.poly1 + " not found");
			System.exit(1);
		}
		ptree2 = (PolyTree) bodies.get(rec.poly2);
		if (ptree2 == null) {
			System.err.println("PolyTree " + rec.poly2 + " not found");
			System.exit(1);
		}
		double[] dlist;
		final Vector3d offset = new Vector3d();
		if (ptree1.numNodes() > 0 || ptree2.numNodes() > 0)
			dlist = new double[] { -0.1, 0.1, rec.minDist };
		else
			dlist = new double[] { -0.1, 0.1, 1.0, rec.minDist };
		//	   dlist = new double[] { rec.minDist };
		for (final double element : dlist) {
			offset.scale(element - rec.minDist, rec.minNrml);
			if (exhaustive)
				exhaustiveCheck(rec, offset, ptree1, ptree2);
			else
				singleCheck(rec, offset, ptree1, ptree2, null, null);
		}
	}

	private void dotime(final Record rec, final boolean dryrun) {
		PolyTree ptree1;
		PolyTree ptree2;

		ptree1 = (PolyTree) bodies.get(rec.poly1);
		if (ptree1 == null) {
			System.err.println("PolyTree " + rec.poly1 + " not found");
			System.exit(1);
		}
		ptree2 = (PolyTree) bodies.get(rec.poly2);
		if (ptree2 == null) {
			System.err.println("PolyTree " + rec.poly2 + " not found");
			System.exit(1);
		}

		final DistanceReport rep = new DistanceReport();
		if (ptree1.isAtomic() && ptree2.isAtomic() && !dryrun)
			ptree1.vclip(rep, ptree2, rec.X21, MAX_DIST, closestFeaturesHT);
	}

	void specialTests() {}

	void vclipTests() {
		for (final Iterator it = testlist.iterator(); it.hasNext();) {
			final Record rec = (Record) it.next();
			dotest(rec);
		}
	}

	private boolean locatePoint(final Point3dX pnt, final Point3dX[] list) {
		for (int i = 0; i < list.length; i++)
			if (list[i] != null && pnt.epsilonEquals(list[i], 1e-6)) {
				list[i] = null;
				return true;
			}
		return false;
	}

	private void checkPolyVertices(final Point3d[] pnts, final PolyTree ptree) throws Exception {
		final Vertex[] verts = ptree.poly_.getVerts();
		final boolean[] marked = new boolean[verts.length];
		for (int i = 0; i < marked.length; i++)
			marked[i] = false;
		for (final Point3d pnt : pnts) {
			int j;
			for (j = 0; j < verts.length; j++)
				if (!marked[j] && verts[j].coords.epsilonEquals(pnt, 1e-8)) {
					marked[j] = true;
					break;
				}
			if (j == verts.length) throw new Exception("vertex " + pnt + " missing in hull for " + ptree.getName());
		}
	}

	private Point3d[] transformPoints(final Matrix4d X, final Point3d[] pnts) {
		final Point3d[] newpnts = new Point3d[pnts.length];
		for (int i = 0; i < newpnts.length; i++) {
			newpnts[i] = new Point3d(pnts[i]);
			X.transform(newpnts[i]);
		}
		return newpnts;
	}

	private Point3d[] appendPoints(final Point3d[] pnts1, final Point3d[] pnts2) {
		final Point3d[] newpnts = new Point3d[pnts1.length + pnts2.length];
		for (int i = 0; i < pnts1.length; i++)
			newpnts[i] = pnts1[i];
		for (int i = 0; i < pnts2.length; i++)
			newpnts[pnts1.length + i] = pnts2[i];
		return newpnts;
	}

	void polyTreeTests() throws Exception {
		final PolyTree ptree = new PolyTree("compound");
		final PolyTree unitCube = (PolyTree) library.get("unit-cube");
		final Matrix4dX M = new Matrix4dX();
		final double c = Math.cos(Math.toRadians(30));
		M.setRpy(Math.toRadians(30), Math.toRadians(0), Math.toRadians(20));
		final Vector3d p = new Vector3d(M.m01, M.m11, M.m21);
		//	   Vector3d p = new Vector3d (M.m00, M.m10, M.m20);
		M.setXyz(p.x, p.y, p.z);
		//	   M.setXyz (-0.5, c, 0);
		ptree.addComponent(null, unitCube, M);
		M.setXyz(-p.x, -p.y, -p.z);
		//	   M.setXyz ( 0.5,-c, 0);
		ptree.addComponent(null, unitCube, M);
		ptree.buildBoundingHull(PolyTree.CONVEX_HULL);
		Vertex[] verts = ptree.getPolyhedron().getVerts();
		final Point3dX[] hullPnts = new Point3dX[verts.length];
		for (int i = 0; i < hullPnts.length; i++)
			hullPnts[i] = new Point3dX(verts[i].coords);
		ptree.buildBoundingHull(PolyTree.OBB_HULL);
		verts = ptree.getPolyhedron().getVerts();
		final Point3dX px = new Point3dX();
		for (final Vertex vert : verts) {
			M.transform(vert.coords, px);
			px.set(vert.coords);
		}
		for (int i = 0; i < verts.length; i++)
			if (!locatePoint(verts[i].coords, hullPnts)) throw new Exception("Can't locate point " + verts[i].coords);

		M.setIdentity();
		final PolyTree cubepair = new PolyTree("cubepair");
		final Point3d[] cubepairHullCheck = new Point3d[] { new Point3d(1.5, 0.5, 0.5), new Point3d(1.5, 0.5, -0.5),
				new Point3d(1.5, -0.5, 0.5), new Point3d(1.5, -0.5, -0.5), new Point3d(-1.5, 0.5, 0.5),
				new Point3d(-1.5, 0.5, -0.5), new Point3d(-1.5, -0.5, 0.5), new Point3d(-1.5, -0.5, -0.5), };
		M.setXyz(1, 0, 0);
		cubepair.addComponent(null, unitCube, M);
		M.setXyz(-1, 0, 0);
		cubepair.addComponent(null, unitCube, M);

		cubepair.buildBoundingHull(PolyTree.OBB_HULL);
		checkPolyVertices(cubepairHullCheck, cubepair);
		cubepair.buildBoundingHull(PolyTree.CONVEX_HULL);
		checkPolyVertices(cubepairHullCheck, cubepair);

		Point3d[] jackHullCheck;
		final PolyTree jack = new PolyTree("jack");

		jack.addComponent("jack_0", cubepair);

		M.setXyz(0, 0, 0);
		M.setRpy(Math.toRadians(90), 0, 0);
		jack.addComponent("jack_1", cubepair, M);
		final Matrix4dX MI = new Matrix4dX();
		MI.invertTrans(M);
		jackHullCheck = appendPoints(cubepairHullCheck, transformPoints(MI, cubepairHullCheck));

		M.setRpy(0, Math.toRadians(90), 0);
		jack.addComponent("jack_2", cubepair, M);
		MI.invertTrans(M);
		jackHullCheck = appendPoints(jackHullCheck, transformPoints(MI, cubepairHullCheck));

		jack.buildBoundingHull(PolyTree.CONVEX_HULL);
		checkPolyVertices(jackHullCheck, jack);

		jack.buildAllBoundingHulls(PolyTree.CONVEX_HULL);
		checkPolyVertices(jackHullCheck, jack);

		int i = 0;
		for (final Iterator it = jack.getComponents(); it.hasNext();) {
			final PolyTree part = (PolyTree) it.next();
			part.setName("jack_" + i);
			checkPolyVertices(cubepairHullCheck, part);
			i++;
		}

		final Point3d[] jackOBBHullCheck = new Point3d[] { new Point3d(1.5, 1.5, 1.5), new Point3d(1.5, 1.5, -1.5),
				new Point3d(1.5, -1.5, 1.5), new Point3d(1.5, -1.5, -1.5), new Point3d(-1.5, 1.5, 1.5),
				new Point3d(-1.5, 1.5, -1.5), new Point3d(-1.5, -1.5, 1.5), new Point3d(-1.5, -1.5, -1.5), };
		jack.buildAllBoundingHulls(PolyTree.OBB_HULL);
		checkPolyVertices(jackOBBHullCheck, jack);
		for (final Iterator it = jack.getComponents(); it.hasNext();)
			checkPolyVertices(cubepairHullCheck, (PolyTree) it.next());

		final PolyTree cross1 = new PolyTree(null, library, "cross");
		final PolyTree cross2 = new PolyTree(null, library, "cross");
		final DistanceReport rep = new DistanceReport(10);
		final double del = 0.1;
		M.setRpy(0, 0, 0);
		M.setXyz(1.5 + del, -1.0 - del, 0);
		rep.setMaxPairDistance(2 * del);
		cross1.vclip(rep, cross2, M, 10.0, null);
		rep.transformSecondPoints(M);
		checkDistanceReport(rep, new NormalDistPair[] { new NormalDistPair(1, 0, 0, del),
				new NormalDistPair(1, 0, 0, del), new NormalDistPair(0, -1, 0, del) });
	}

	void timingTests() {
		final int timingcount = 5000;
		long t0, t1, t2;

		// exercise the algorithm to let hot-spot do some compiling
		for (int i = 0; i < timingcount; i++)
			for (final Iterator it = testlist.iterator(); it.hasNext();) {
				final Record rec = (Record) it.next();
				dotime(rec, /*dry=*/false);
			}
		t0 = System.currentTimeMillis();
		for (int i = 0; i < timingcount; i++)
			for (final Iterator it = testlist.iterator(); it.hasNext();) {
				final Record rec = (Record) it.next();
				dotime(rec, /*dry=*/true);
			}
		t1 = System.currentTimeMillis();
		for (int i = 0; i < timingcount; i++)
			for (final Iterator it = testlist.iterator(); it.hasNext();) {
				final Record rec = (Record) it.next();
				dotime(rec, /*dry=*/false);
			}
		t2 = System.currentTimeMillis();
		final int n = timingcount * testlist.size();
		System.out.println("\ntime=" + 1000.0 * (t2 - t1 - (t1 - t0)) / n + " usec");
	}

	/**
	 * Runs a set of tests on the vclip package, and
	 * prints <code>Passed</code> if all is well.
	 * Otherwise, an error message and stack trace
	 * are produced.
	 */
	public static void main(final String[] args) {
		boolean doTiming = false;
		String testFileName = null; // "vcliptest.txt";
		boolean exhaustiveCheck = true;

		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-timing"))
				doTiming = true;
			else if (args[i].equals("-f")) {
				if (++i == args.length) {
					System.err.println("Error: file name expected after '-f' option");
					System.exit(1);
				}
				testFileName = args[i];
			} else if (args[i].equals("-noex"))
				exhaustiveCheck = false;
			else {
				System.err.println("Usage: java vclip.VclipTest [-timing] [-f <testfile>] [-noex]");
				System.exit(1);
			}

		Reader testFileReader = null;
		if (testFileName != null)
			try {
				testFileReader = new BufferedReader(new FileReader(testFileName));
			} catch (final Exception e) {
				System.out.println("Error openning file " + testFileName);
				e.printStackTrace();
				System.exit(1);
			}
		else
			testFileReader = new StringReader(vcliptest);

		try {
			final VclipTest tester = new VclipTest(testFileReader, new StreamTokenizer(new StringReader(
					polyTreeExamples)));
			tester.exhaustive = exhaustiveCheck;
			if (doTiming) {
				tester.timingTests();
				//		 ConvexPolyhedron.printTimers (0, 9);
				System.out.println("calls: " + ConvexPolyhedron.callCount);
				System.out.println("iters: " + ConvexPolyhedron.iterCount);
				System.out.println("avg/iters: " + ConvexPolyhedron.iterCount / (double) ConvexPolyhedron.callCount);
			} else {
				tester.specialTests();
				tester.vclipTests();
				tester.polyTreeTests();
				System.out.println("calls: " + ConvexPolyhedron.callCount);
				System.out.println("loops: " + ConvexPolyhedron.loopingCount);
				System.out.println("detected loops: " + ConvexPolyhedron.detectedLoopCount);
				System.out.println("max iters: " + ConvexPolyhedron.maxIterCount);
				System.out.println("max total features: " + ConvexPolyhedron.maxFeatureCount);
				System.out.println("\nPassed\n");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	// inline version of PolyTreeExamples.txt
	static String polyTreeExamples =

	// tetrahedron in first octant

	"atomic tetra\n"
			+ "origin	0 0 0\n"
			+ "x	1 0 0\n"
			+ "y	0 1 0\n"
			+ "z	0 0 1\n"
			+ "*\n"
			+ "xy	origin y x\n"
			+ "yz	origin z y\n"
			+ "zx	origin x z\n"
			+ "base	x y z\n"
			+ "*\n"
			+

			// unit cube centered at origin

			"atomic unit-cube\n" + "ne0	+0.5	+0.5	-0.5\n" + "nw0	-0.5	+0.5	-0.5\n" + "sw0	-0.5	-0.5	-0.5\n"
			+ "se0	+0.5	-0.5	-0.5\n" + "ne1	+0.5	+0.5	+0.5\n" + "nw1	-0.5	+0.5	+0.5\n" + "sw1	-0.5	-0.5	+0.5\n"
			+ "se1	+0.5	-0.5	+0.5\n" + "*\n" + "bottom	sw0 nw0 ne0 se0\n" + "top	sw1 se1 ne1 nw1\n"
			+ "south	sw0 se0 se1 sw1\n" + "north	ne1 ne0 nw0 nw1\n" + "east	se0 ne0 ne1 se1\n"
			+ "west	sw0 sw1 nw1 nw0\n" + "*\n" +

			"atomic triangle-cube\n" + "ne0	+0.5	+0.5	-0.5\n" + "nw0	-0.5	+0.5	-0.5\n" + "sw0	-0.5	-0.5	-0.5\n"
			+ "se0	+0.5	-0.5	-0.5\n" + "ne1	+0.5	+0.5	+0.5\n" + "nw1	-0.5	+0.5	+0.5\n" + "sw1	-0.5	-0.5	+0.5\n"
			+ "se1	+0.5	-0.5	+0.5\n" + "*\n" + "f0    	sw0 nw0 ne0\n" + "f1      ne0 se0 sw0     \n"
			+ "f2 	sw1 se1 ne1\n" + "f3      ne1 nw1 sw1\n" + "f4   	sw0 se0 se1\n" + "f5      se1 sw1 sw0\n"
			+ "f6   	ne1 ne0 nw0\n" + "f7	nw0 nw1 ne1\n" + "f8	se0 ne0 ne1\n" + "f9	ne1 se1 se0\n"
			+ "f10 	sw0 sw1 nw1\n" + "f11	nw1 nw0 sw0\n" + "*\n" +

			"atomic cone\n" + "top 0 0 0.3 \n" + "v0 0.5 0 -0.3 \n" + "v1 -0.5 0.5 -0.3 \n" + "v2 -0.5 -0.5 -0.3\n"
			+ "*\n" + "Fbottom v0 v2 v1\n" + "F0 top v0 v1\n" + "F1 top v1 v2\n" + "F2 top v2 v0\n" + "*\n" +

			"atomic brick\n" + "ne0  1.0  0.5 -0.5\n" + "nw0 -1.0  0.5 -0.5\n" + "sw0 -1.0 -0.5 -0.5\n"
			+ "se0  1.0 -0.5 -0.5\n" + "ne1  1.0  0.5  0.5\n" + "nw1 -1.0  0.5  0.5\n" + "sw1 -1.0 -0.5  0.5\n"
			+ "se1  1.0 -0.5  0.5\n" + "*\n" + "bottom  sw0 nw0 ne0 se0 \n"
			+ "top     sw1 se1 ne1 nw1 \n"
			+ "south   sw0 se0 se1 sw1 \n"
			+ "north   ne1 ne0 nw0 nw1 \n"
			+ "east    se0 ne0 ne1 se1 \n"
			+ "west    sw0 sw1 nw1 nw0 \n"
			+ "*\n"
			+

			"atomic skew \n"
			+ "n0  0.0  0.5 -0.5\n"
			+ "w0 -0.5  0.0 -0.5\n"
			+ "s0  0.0 -0.5 -0.5\n"
			+ "e0  0.5  0.0 -0.5\n"
			+ "n1  0.0  0.5  0.5\n"
			+ "w1 -0.5  0.0  0.5\n"
			+ "s1  0.0 -0.5  0.5\n"
			+ "e1  0.5  0.0  0.5 \n"
			+ "*\n"
			+ "bottom  s0 w0 n0 e0 \n"
			+ "top     s1 e1 n1 w1 \n"
			+ "south   s0 e0 e1 s1 \n"
			+ "north   n1 n0 w0 w1 \n"
			+ "east    e0 n0 n1 e1 \n"
			+ "west    s0 s1 w1 w0 \n"
			+ "*\n"
			+

			"atomic diamond\n"
			+ "uu  0.0  0.5  0.0\n"
			+ "mr  0.5  0.0  0.0\n"
			+ "mb  0.0  0.0 -0.5\n"
			+ "ml -0.5  0.0  0.0\n"
			+ "mf  0.0  0.0  0.5\n"
			+ "dd  0.0 -1.0  0.0\n"
			+ "*\n"
			+ "usiderf  uu mf mr \n"
			+ "usiderb  uu mr mb \n"
			+ "usidelb  uu mb ml \n"
			+ "usidelf  uu ml mf \n"
			+ "dsiderf  dd mr mf \n"
			+ "dsiderb  dd mb mr \n"
			+ "dsidelb  dd ml mb \n"
			+ "dsidelf  dd mf ml \n"
			+ "*\n"
			+

			// two unit cubes with a unit gap between them

			"compound two-cubes\n" + "[\n" + "[trans -1 0 0] unit-cube\n" + "[trans +1 0 0] unit-cube\n" + "]\n" +

			"compound cross\n" + "[\n" + "[ rotz 90 ] two-cubes\n" + "[ ] brick\n" + "]\n" +

			"atomic hull\n" + "v0 -1.0 -0.5 0.5\n" + "v1 1.0 -0.5 0.5\n" + "v2 0.5 1.5 -0.5\n" + "v3 -0.5 -1.5 0.5\n"
			+ "v4 0.5000000000000001 1.5 0.5\n" + "v5 -0.5000000000000001 -1.5 -0.5\n" + "v6 -1.0 0.5 0.5\n"
			+ "v7 1.0 0.5 0.5\n" + "v8 1.0 -0.5 -0.5\n" + "v9 -1.0 -0.5 -0.5\n" + "v10 -1.0 0.5 -0.5\n"
			+ "v11 1.0 0.5 -0.5\n" + "v12 -0.4999999999999999 1.5 0.5\n" + "v13 -0.4999999999999999 1.5 -0.5\n"
			+ "v14 0.4999999999999999 -1.5 -0.5\n" + "v15 0.4999999999999999 -1.5 0.5\n" + "*\n" + "f0 v2 v5 v13 \n"
			+ "f1 v1 v4 v0 \n" + "f2 v13 v4 v2 \n" + "f3 v2 v8 v5 \n" + "f4 v4 v12 v0 \n" + "f5 v13 v12 v4 \n"
			+ "f6 v0 v15 v1 \n" + "f7 v1 v15 v8 \n" + "f8 v5 v10 v13 \n" + "f9 v13 v10 v12 \n" + "f10 v0 v9 v5 \n"
			+ "f11 v10 v9 v0 \n" + "f12 v5 v9 v10 \n" + "f13 v12 v6 v0 \n" + "f14 v10 v6 v12 \n" + "f15 v0 v6 v10 \n"
			+ "f16 v5 v3 v0 \n" + "f17 v15 v3 v5 \n" + "f18 v0 v3 v15 \n" + "f19 v8 v14 v5 \n" + "f20 v15 v14 v8 \n"
			+ "f21 v5 v14 v15 \n" + "f22 v4 v11 v2 \n" + "f23 v8 v11 v1 \n" + "f24 v2 v11 v8 \n" + "f25 v1 v7 v4 \n"
			+ "f26 v11 v7 v1 \n" + "f27 v4 v7 v11 \n" + "*\n" +

			"atomic rod\n" + "v0 0.85 0.0 5.0\n" + "v1 0.7361215932167728 0.42499999999999993 5.0\n"
			+ "v2 0.4250000000000001 0.7361215932167728 5.0\n" + "v3 5.204748896376251e-17 0.85 5.0\n"
			+ "v4 -0.4249999999999998 0.7361215932167728 5.0\n" + "v5 -0.7361215932167728 0.42499999999999993 5.0\n"
			+ "v6 -0.85 1.0409497792752501E-16 5.0\n" + "v7 -0.736121593216773 -0.42499999999999977 5.0\n"
			+ "v8 -0.4250000000000004 -0.7361215932167727 5.0\n" + "v9 -1.5614246689128752E-16 -0.85 5.0\n"
			+ "v10 0.4250000000000001 -0.7361215932167728 5.0\n" + "v11 0.7361215932167726 -0.4250000000000004 5.0\n"
			+ "v12 0.85 0.0 -5.0\n" + "v13 0.7361215932167728 0.42499999999999993 -5.0\n"
			+ "v14 0.4250000000000001 0.7361215932167728 -5.0\n" + "v15 5.204748896376251E-17 0.85 -5.0\n"
			+ "v16 -0.4249999999999998 0.7361215932167728 -5.0\n"
			+ "v17 -0.7361215932167728 0.42499999999999993 -5.0\n" + "v18 -0.85 1.0409497792752501E-16 -5.0\n"
			+ "v19 -0.736121593216773 -0.42499999999999977 -5.0\n"
			+ "v20 -0.4250000000000004 -0.7361215932167727 -5.0\n" + "v21 -1.5614246689128752E-16 -0.85 -5.0\n"
			+ "v22 0.4250000000000001 -0.7361215932167728 -5.0\n"
			+ "v23 0.7361215932167726 -0.4250000000000004 -5.0\n" + "*\n" + "f0 v12 v13 v1 v0 \n"
			+ "f1 v13 v14 v2 v1 \n" + "f2 v14 v15 v3 v2 \n" + "f3 v15 v16 v4 v3 \n" + "f4 v16 v17 v5 v4 \n"
			+ "f5 v17 v18 v6 v5 \n" + "f6 v18 v19 v7 v6 \n" + "f7 v19 v20 v8 v7 \n" + "f8 v20 v21 v9 v8 \n"
			+ "f9 v21 v22 v10 v9 \n" + "f10 v22 v23 v11 v10 \n" + "f11 v23 v12 v0 v11 \n"
			+ "f12 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v0 \n"
			+ "f13 v22 v21 v20 v19 v18 v17 v16 v15 v14 v13 v12 v23 \n" + "*\n" +

			"atomic wedge\n" + "v0 0.49999999999999994 0.8660254037844387 0.5\n" + "v1 2.886751345948128 5.0 0.5\n"
			+ "v2 0.0 5.0 0.5\n" + "v3 0.0 1.0 0.5\n" + "v4 0.49999999999999994 0.8660254037844387 -0.5\n"
			+ "v5 2.886751345948128 5.0 -0.5\n" + "v6 0.0 5.0 -0.5\n" + "v7 0.0 1.0 -0.5\n" + "*\n"
			+ "f0 v4 v5 v1 v0 \n" + "f1 v5 v6 v2 v1 \n" + "f2 v6 v7 v3 v2 \n" + "f3 v7 v4 v0 v3 \n"
			+ "f4 v1 v2 v3 v0 \n" + "f5 v6 v5 v4 v7 \n" + "*\n";

	// inline version of vcliptest.txt

	static String vcliptest = "rod wedge [ matrix\n" + "1.8369701987210297E-16 1.0 1.2246467991473532E-16 0.0\n"
			+ "1.0 -1.8369701987210297E-16 0.0 -6.386076441342869\n"
			+ "2.24963967399278E-32 1.22464679914735E-16 -1.0 5.20744041955834 ]\n"
			+ "N [ 0.499999999999999 -0.8660254037844389 6.123233995736762E-17 ]\n" + "4.680504428 * \n" +

			"unit-cube unit-cube [ trans 2 2 0 ]\n" + "N [ 1 1 0 ] 1.4142135 *\n"
			+ "unit-cube tetra [ trans 2 2 2 ]\n" + "V V [0.5 0.5 0.5]  [1 1 1] 2.5980762 *\n"
			+ "unit-cube tetra [ trans -2 -2 1 ]\n" + "V E [ -0.5 -0.5 0.5 ] [-1 -1 0.5] 1.5 *\n"
			+ "unit-cube tetra [ trans -1.5 -1.5 -1.5 ]\n" + "V F [-0.5 -0.5 -0.5  ] [-1 -1 -1] 1.1547005 *\n"
			+ "unit-cube tetra [ trans 1 0 0 ]\n" + "N [ 1 0 0 ] 0.5 *\n"
			+ "unit-cube tetra [ trans 1 0 0 roty 45 ]\n" + "N [ 1 0 0 ] 0.5 *\n"
			+ "unit-cube tetra [ trans 1 1 0 roty 1 ]\n" + "E V [ 0.5 0.5 0 ]  [1 1 0] 0.70710675 *\n"
			+ "unit-cube tetra [ trans -2 -2 0 ]\n" + "E E [ -0.5 -0.5 0 ] [-1 -1 0] 1.4142135 *\n"
			+ "unit-cube tetra [ trans 0 0 -2 ]\n" + "F V [ 0.0 0.0 -0.5 ]  [0 0 -1] 0.5 *\n"
			+ "unit-cube unit-cube [ trans 2 0.1 0.1 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "unit-cube unit-cube [ trans 2 2 2 ]\n" + "V V [ 0.5 0.5 0.5 ] [1 1 1 ] 1.7320508 *\n"
			+ "unit-cube unit-cube [ trans 1 1 2 rotz 45 ]\n"
			+ "V E [ 0.5 0.5 0.5 ] [ 0.14644660 0.14644660 1 ] 1.02122143 *\n"
			+ "unit-cube unit-cube [ trans 2 2 2 rot -1 1 0 54.735610 ]\n"
			+ "V F [ 0.5 0.5 0.5 ] [1.2113248 1.2113248 1.2113248] 2.0980762 *\n"
			+ "unit-cube unit-cube [ trans 0 2 2 rotz 45 ]\n" + "E V [ 0 0.5 0.5 ]  [ 0 0.7928932 1 ] 1.27619734 *\n"
			+ "unit-cube unit-cube [ trans 2 2 0 rot 1 1 0 90 ]\n" + "E E [ 0.5 0.5 0 ] [1 1 0 ] 1.41421356 *\n"
			+ "unit-cube unit-cube [ trans 0 3 0 rotz 45 rot 1 -1 0 35.264389 ]\n"
			+ "F V [ 0 0.5 0 ] [ 0 1.6339745 0 ] 1.6339745 *\n" + "unit-cube unit-cube [ trans 2 0 0 roty 45 ]\n"
			+ "N [ 1 0 0 ] 0.79289321 *\n" + "unit-cube tetra [trans -1  -1  -1 ]\n"
			+ "V F [ -0.5 -0.5 -0.5 ] [ -1 -1 -1 ] 0.2886751345 *\n" + "cone unit-cube [ trans 0 0 1 ]\n"
			+ "V F [ 0 0 .3 ] [ 0 0 1 ] 0.2 *\n" + "brick diamond  [ trans 3 0 0 ]\n"
			+ "F V [ 1 0 0 ] [ 1 0 0 ] 1.5 *\n" + "unit-cube brick [ trans 2.5 0 0 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "unit-cube brick [ trans 2.5 0.1 0.1 ]\n" + "N [ 1 0 0 ] 1 *\n" + "unit-cube brick [ trans 2.5 1 0 ]\n"
			+ "N [ 1 0 0 ] 1 *\n" + "unit-cube diamond [ trans 2 .5 0 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "unit-cube diamond [ trans 2 .5 .25 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "unit-cube diamond [ trans 2 .5000001 .25 ]\n" + "E V [ .5 .5 .25 ] [ 1 0 0 ] 1 *\n"
			+ "unit-cube diamond [ trans 2 .4999999 .25 ]\n" + "F V [ .5 .5 .25 ] [ 1 0 0 ] 1 *\n"
			+ "unit-cube skew [ trans 2 0 0 ]\n" + "N [ 1 0 0 ] 1 *\n" + "unit-cube skew [ trans 2 0.5 0 ]\n"
			+ "N [ 1 0 0 ] 1 *\n" + "unit-cube skew [ trans 2 0.5 1.1 ]\n" + "N [ 1 0 .1 ] 1.0049875 *\n"
			+ "unit-cube skew [ trans 2 0.5000001 1.1 ]\n" + "V V [ 0.5 0.5 0.5 ] [ 1 0 .1 ] 1.0049875 *\n"
			+ "unit-cube skew [ trans 2 0.4999999 1.1 ]\n" + "E V [ 0.5 0.5 0.5 ] [ 1 0 .1 ] 1.0049875 *\n"
			+ "skew diamond [ trans 2 0 0 ]\n" + "E V  [ 0.5 0 0 ] [ 1 0 0 ] 1  *\n"
			+ "skew diamond [ trans 2 1 0 ]\n" + "E E [ 0.5 0 0 ] [ 2 1 0 ] 1.34164078 *\n"
			+ "skew diamond [ trans 2 0 2 ]\n" + "V E [ 0.5 0 0.5 ] [ 1 0 1 ] 1.767766952 *\n"
			+ "skew skew [ trans 2 0 0 ]\n" + "N [ 1 0 0 ] 1 *\n" + "skew skew [ trans 2 0 1 ]\n"
			+ "N [ 1 0 0 ] 1 *\n" + "skew skew [ trans 2 0 1.000001 ]\n" + "V V [ .5 0 .5 ] [ 1 0 0.000001 ] 1 *\n"
			+ "cross unit-cube [ trans 1.25 1.5 0 ]\n" + "N [ 1 0 0 ] .25 *\n"
			+ "cross unit-cube [ trans 1.25 1.1 0 ]\n" + "N [ 0 1 0 ] .1 *\n" +

			"triangle-cube triangle-cube [ trans 2 0 0 ]\n" + "N [1 0 0 ] 1.0 *\n"
			+ "triangle-cube tetra [ trans 2 2 2 ]\n" + "V V [0.5 0.5 0.5]  [1 1 1] 2.5980762 *\n"
			+ "triangle-cube tetra [ trans -2 -2 1 ]\n" + "V E [ -0.5 -0.5 0.5 ] [-1 -1 0.5] 1.5 *\n"
			+ "triangle-cube tetra [ trans -1.5 -1.5 -1.5 ]\n" + "V F [-0.5 -0.5 -0.5  ] [-1 -1 -1] 1.1547005 *\n"
			+ "triangle-cube tetra [ trans 1 0 0 ]\n" + "N [ 1 0 0 ] 0.5 *\n"
			+ "triangle-cube tetra [ trans 1 0 0 roty 45 ]\n" + "N [ 1 0 0 ] 0.5 *\n"
			+ "triangle-cube tetra [ trans 1 1 0 roty 1 ]\n" + "E V [ 0.5 0.5 0 ]  [1 1 0] 0.70710675 *\n"
			+ "triangle-cube tetra [ trans -2 -2 0 ]\n" + "E E [ -0.5 -0.5 0 ] [-1 -1 0] 1.4142135 *\n"
			+ "triangle-cube tetra [ trans 0 0 -2 ]\n" + "N  [0 0 -1] 0.5 *\n"
			+ "triangle-cube triangle-cube [ trans 2 2 2 ]\n" + "V V [ 0.5 0.5 0.5 ] [1 1 1 ] 1.7320508 *\n"
			+ "triangle-cube triangle-cube [ trans 1 1 2 rotz 45 ]\n"
			+ "V E [ 0.5 0.5 0.5 ] [ 0.14644660 0.14644660 1 ] 1.02122143 *\n"
			+ "triangle-cube triangle-cube [ trans 2 2 2 rot -1 1 0 54.735610 ]\n" + "N [ 0.5 0.5 0.5 ] 2.0980762 *\n"
			+ "triangle-cube triangle-cube [ trans 0 2 2 rotz 45 ]\n"
			+ "E V [ 0 0.5 0.5 ]  [ 0 0.7928932 1 ] 1.27619734 *\n"
			+ "triangle-cube triangle-cube [ trans 2 2 0 rot 1 1 0 90 ]\n"
			+ "E E [ 0.5 0.5 0 ] [1 1 0 ] 1.41421356 *\n" + "triangle-cube triangle-cube "
			+ "   [ trans 0 3 0 rotz 45 rot 1 -1 0 35.264389 ]\n" + "F V [ 0 0.5 0 ] [ 0 1.6339745 0 ] 1.6339745 *\n"
			+ "triangle-cube triangle-cube [ trans 2 0.1 0.1 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "triangle-cube triangle-cube [ trans 2 2 0 ]\n" + "N [ 1 1 0 ] 1.4142135 *\n"
			+ "triangle-cube tetra [trans -1  -1  -1 ]\n" + "V F [ -0.5 -0.5 -0.5 ] [ -1 -1 -1 ] 0.2886751345 *\n"
			+ "cone triangle-cube [ trans 0 0 1 ]\n" + "N [ 0 0 1 ] 0.2 *\n"
			+ "triangle-cube brick [ trans 2.5 0 0 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "triangle-cube brick [ trans 2.5 0.1 0.1 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "triangle-cube brick [ trans 2.5 1 0 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "triangle-cube diamond [ trans 2 .5 0 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "triangle-cube diamond [ trans 2 .5 .25 ]\n" + "N [ 1 0 0 ] 1 *\n"
			+ "triangle-cube skew [ trans 2 0 0 ]\n" + "N [ 1 0 0 ] 1 *\n" + "triangle-cube skew [ trans 2 0.5 0 ]\n"
			+ "N [ 1 0 0 ] 1 *\n" + "triangle-cube skew [ trans 2 0.5 1.1 ]\n" + "N [ 1 0 .1 ] 1.0049875 *\n"
			+ "cross triangle-cube [ trans 1.25 1.5 0 ]\n" + "N [ 1 0 0 ] .25 *\n"
			+ "cross triangle-cube [ trans 1.25 1.1 0 ]\n" + "N [ 0 1 0 ] .1 *\n" +

			"unit-cube unit-cube " + "   [ trans 1.20710678112 1.20710678112 0 rotz 45 ] P D 2\n"
			+ "E F [ .5 .5 -.5 ] [ 1 1 0 ] 0.5\n" + "E F [ .5 .5  .5 ] [ 1 1 0 ] 0.5 *\n" + "unit-cube unit-cube "
			+ "   [ trans 1.20710678112 1.20710678112 .5 rotz 45 ] P D 2\n" + "E F [ .5 .5  0 ] [ 1 1 0 ] 0.5\n"
			+ "E F [ .5 .5  .5 ] [ 1 1 0 ] 0.5 *\n" + "unit-cube unit-cube "
			+ "   [ trans 1.20710678112 1.20710678112 -.5 rotz 45 ] P D 2\n" + "E F [ .5 .5 -.5 ] [ 1 1 0 ] 0.5\n"
			+ "E F [ .5 .5  0 ] [ 1 1 0 ] 0.5 *\n" + "unit-cube unit-cube "
			+ "   [ trans 1.20710678112 1.20710678112 -1 rotz 45 ] P D 2\n" + "E F [ .5 .5 -.5 ] [ 1 1 0 ] 0.5 *\n"
			+ "unit-cube unit-cube " + "   [ trans 1.20710678112 1.20710678112 1 rotz 45 ] P D 2\n"
			+ "E F [ .5 .5 .5 ] [ 1 1 0 ] 0.5 *\n" +

			"unit-cube unit-cube [ trans 1.7071067811 0 0 roty 45 ] P D 2\n" + "F E [ .5  .5 0 ] [ 1 0 0 ] 0.5 \n"
			+ "F E [ .5 -.5 0 ] [ 1 0 0 ] 0.5 *\n"
			+ "unit-cube unit-cube [ trans 1.7071067811 0.5 0 roty 45 ] P D 2\n" + "F E [ .5  .5 0 ] [ 1 0 0 ] 0.5 \n"
			+ "F E [ .5  0  0 ] [ 1 0 0 ] 0.5 *\n"
			+ "unit-cube unit-cube [ trans 1.7071067811 -0.5 0 roty 45 ] P D 2\n" + "F E [ .5  0 0 ] [ 1 0 0 ] 0.5 \n"
			+ "F E [ .5  -0.5  0 ] [ 1 0 0 ] 0.5 *\n"
			+ "unit-cube unit-cube [ trans 1.7071067811 -1 0 roty 45 ] P D 2\n"
			+ "F E [ .5  -0.5  0 ] [ 1 0 0 ] 0.5 *\n"
			+ "unit-cube unit-cube [ trans 1.7071067811 1 0 roty 45 ] P D 2\n" + "F E [ .5  .5  0 ] [ 1 0 0 ] 0.5 *\n"
			+

			"unit-cube unit-cube [ trans 2 0 0 ] P D 2\n" + "F F [ .5 .5 .5 ] [ 1 0 0 ] 1 \n"
			+ "F F [ .5 -.5 .5 ] [ 1 0 0 ] 1 \n" + "F F [ .5 .5 -.5 ] [ 1 0 0 ] 1 \n"
			+ "F F [ .5 -.5 -.5 ] [ 1 0 0 ] 1 *\n" + "unit-cube unit-cube [ trans 2 .5 .5 ] P D 2\n"
			+ "F F [ .5 .5 .5 ] [ 1 0 0 ] 1 \n" + "F F [ .5 0 .5 ] [ 1 0 0 ] 1 \n" + "F F [ .5 .5 0 ] [ 1 0 0 ] 1 \n"
			+ "F F [ .5 0 0 ] [ 1 0 0 ] 1 *\n" + "unit-cube unit-cube [ trans 2 -.5 0 ] P D 2\n"
			+ "F F [ .5 0 .5 ] [ 1 0 0 ] 1 \n" + "F F [ .5 0 -.5 ] [ 1 0 0 ] 1 \n"
			+ "F F [ .5 -.5 -.5 ] [ 1 0 0 ] 1 \n" + "F F [ .5 -.5 .5 ] [ 1 0 0 ] 1 *\n"
			+ "unit-cube unit-cube [ trans 2 1 1 ] P D 2\n" + "F F [ .5 .5 .5 ] [ 1 0 0 ] 1 *\n";

}
