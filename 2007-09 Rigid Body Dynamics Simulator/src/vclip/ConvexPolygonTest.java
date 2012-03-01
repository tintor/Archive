package vclip;

import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;


class ConvexPolygonTest {
	static public final double EPS = 1e-7;
	static public final double DBL_EPSILON = ConvexPolygon.DBL_EPSILON;

	static private class LineIsec {
		double[] lam = new double[2];
		int n;

		boolean epsilonEquals(LineIsec isec, double eps) {
			if (n != isec.n) {
				return false;
			}
			for (int i = 0; i < n; i++) {
				if (Math.abs(lam[i] - isec.lam[i]) > eps) {
					return false;
				}
			}
			return true;
		}

		String sprintf() {
			String s = "" + n + ": ";
			for (int i = 0; i < n; i++) {
				if (lam[i] == Double.POSITIVE_INFINITY) {
					s += "+I";
				} else if (lam[i] == Double.NEGATIVE_INFINITY) {
					s += "-I";
				} else {
					s += lam[i];
				}
				if (i < n - 1) {
					s += " ";
				}
			}
			return s;
		}

		void scan(StreamTokenizer stok) throws IOException {
			stok.nextToken();
			if (stok.ttype != stok.TT_NUMBER) {
				throw new IOException("number expected");
			}
			if (stok.nval != 0 && stok.nval != 1 && stok.nval != 2) {
				throw new IOException("'0:', '1:', or '2:' expected");
			}
			n = (int) stok.nval;
			stok.nextToken();
			if (stok.ttype != ':') {
				throw new IOException("':' expected");
			}
			for (int i = 0; i < n; i++) {
				stok.nextToken();
				if (stok.ttype == stok.TT_NUMBER) {
					lam[i] = stok.nval;
				} else if (stok.ttype == stok.TT_WORD && stok.sval.equals("-I")) {
					lam[i] = Double.NEGATIVE_INFINITY;
				} else if (stok.ttype == stok.TT_WORD && stok.sval.equals("+I")) {
					lam[i] = Double.POSITIVE_INFINITY;
				} else {
					throw new IOException("number or +I or -I expected");
				}
			}
		}
	}

	public static final int DO_QUIT = 0;
	public static final int DO_ADD = 1;
	public static final int DO_ISECT = 2;
	public static final int DO_RESET = 3;
	public static final int DO_INTERSECT = 4;

	static ConvexPolygon workPoly = new ConvexPolygon();
	static LineIsec workIsec = new LineIsec();
	static ConvexPolygon polyRes = new ConvexPolygon();
	static ConvexPolygon xformTestPoly = new ConvexPolygon();

	int cmd;
	int line;
	Line2d hp;
	ConvexPolygon testPoly;
	ConvexPolygon testPolyRef;
	LineIsec testIsec;
	ConvexPolygon poly1;
	ConvexPolygon poly2;

	ConvexPolygonTest() {
		hp = new Line2d();
		testPoly = new ConvexPolygon();
		testIsec = new LineIsec();
		poly1 = new ConvexPolygon();
		poly2 = new ConvexPolygon();
	}

	ConvexPolygon getNamedPoly(String name) {
		if (name.equals("poly1")) {
			return (poly1);
		} else if (name.equals("poly2")) {
			return (poly2);
		} else if (name.equals("res")) {
			return (polyRes);
		} else {
			return (null);
		}
	}

	String getPolyName(ConvexPolygon poly) {
		if (poly == poly1) {
			return ("poly1");
		} else if (poly == poly2) {
			return ("poly2");
		} else if (poly == polyRes) {
			return ("res");
		} else {
			return (null);
		}
	}

	void readTestPoly(StreamTokenizer stok) throws IOException {
		stok.nextToken();
		if (stok.ttype != stok.TT_WORD) {
			throw new IOException("polygon name expected");
		}
		testPolyRef = getNamedPoly(stok.sval);
		if (testPolyRef == null) {
			throw new IOException("bogus polygon name " + stok.sval);
		}
		if (testPolyRef == polyRes) {
			testPoly.scan(stok);
		} else {
			testPoly.set(testPolyRef);
		}
	}

	public void scan(StreamTokenizer stok) throws IOException {
		stok.nextToken();
		line = stok.lineno();
		if (stok.ttype == StreamTokenizer.TT_EOF) {
			cmd = DO_QUIT;
			return;
		} else if (stok.ttype != StreamTokenizer.TT_WORD) {
			throw new IOException("Command expected");
		}
		if (stok.sval.equals("ADD")) {
			cmd = DO_ADD;
			hp.scan(stok);
			testPoly.scan(stok);
		} else if (stok.sval.equals("INTERSECT")) {
			cmd = DO_INTERSECT;
			poly2.scan(stok);
			readTestPoly(stok);
		} else if (stok.sval.equals("ISECT")) {
			cmd = DO_ISECT;
			hp.scan(stok);
			testIsec.scan(stok);
		} else if (stok.sval.equals("RESET")) {
			workPoly.scan(stok);
			poly1.set(workPoly);
			cmd = DO_RESET;
		} else {
			throw new IOException("Unknown command " + stok.sval);
		}
	}

	public boolean execute(boolean check) {
		switch (cmd) {
		case DO_ADD: {
			if (!workPoly.consistencyCheck(/*print=*/true)) {
				System.out.println("Error: ADD, near line " + line + ": bogus input");
				System.out.println("Got: " + workPoly.sprintf());
				return false;
			}
			workPoly.intersect(hp);
			if (check) {
				if (!workPoly.consistencyCheck(/*print=*/true)) {
					System.out.println("Error ADD, near line " + line + ": bogus result");
					System.out.println("Wanted: " + testPoly.sprintf());
					System.out.println("   Got: " + workPoly.sprintf());
					return false;
				}
				if (!workPoly.epsilonEquals(testPoly, DBL_EPSILON)) {
					System.out.println("Error ADD, near line " + line + ":");
					System.out.println("Wanted: " + testPoly.sprintf());
					System.out.println("   Got: " + workPoly.sprintf());
					return false;
				}
			} else {
				System.out.println(workPoly.sprintf());
			}
			break;
		}
		case DO_ISECT: {
			workIsec.n = workPoly.intersectLine(workIsec.lam, hp);
			if (check) {
				if (!workIsec.epsilonEquals(testIsec, EPS)) {
					System.out.println("Error in ISECT near line " + line + ":");
					System.out.println("Wanted: " + testIsec.sprintf());
					System.out.println("   Got: " + workIsec.sprintf());
					return false;
				}
			} else {
				System.out.println(workIsec.sprintf());
			}
			break;
		}
		case DO_INTERSECT: {
			if (check) {
				ConvexPolygon poly1Save = new ConvexPolygon();
				ConvexPolygon poly2Save = new ConvexPolygon();
				ConvexPolygon xformTestPoly = new ConvexPolygon();
				ConvexPolygon resPtr;
				double ang = Math.PI / 3;

				poly1Save.set(poly1);
				poly2Save.set(poly2);

				if (!poly1.consistencyCheck(/*print=*/true)) {
					System.out.println("Error: ADD, near line " + line + ": bogus input 1");
					System.out.println("   Got: " + poly1.sprintf());
					return false;
				}
				if (!poly2.consistencyCheck(/*print=*/true)) {
					System.out.println("Error: ADD, near line " + line + ": bogus input 2");
					System.out.println("   Got: " + poly2.sprintf());
					return false;
				}
				// #if 1
				for (int i = 0; i < poly1.numVertices(); i++) {
					if (i > 0) poly1.shiftVertices(1);
					for (int j = 0; j < poly2.numVertices(); j++) {
						if (j > 0) poly2.shiftVertices(1);
						resPtr = polyRes.intersect(poly1, poly2);
						if (!resPtr.epsilonEquals(testPoly, DBL_EPSILON)) {
							System.out.println("Error in INTERSECT (" + i + "," + j + ") near line " + line
									+ ":");
							System.out.println("Wanted: " + getPolyName(testPolyRef));
							System.out.println("   Got: " + getPolyName(resPtr));
							System.out.println(polyRes.sprintf());
							System.out.println("poly1\n" + poly1.sprintf());
							System.out.println("poly2\n" + poly1.sprintf());
							return false;
						}
						if (!polyRes.consistencyCheck(/*print=*/true)) {
							System.out.println("Error: INTERSECT, near line " + line + ": bogus result");
							return false;
						}
					}
				}
				// #endif
				// #if 1
				poly1.intersect(poly2);
				if (!poly1.epsilonEquals(testPoly, DBL_EPSILON)) {
					System.out.println("Error in self INTERSECT near line " + line + ":");
					System.out.println("Wanted: " + getPolyName(testPolyRef));
					System.out.println("   Got: " + getPolyName(poly1));
					System.out.println(poly1.sprintf());
					return false;
				}
				xformTestPoly.xform(testPoly, 0, 0, ang);
				poly1.xform(poly1Save, 0, 0, ang);
				poly2.xform(poly2Save, 0, 0, ang);

				resPtr = polyRes.intersect(poly1, poly2);
				// #endif
				// #if 1
				if (!polyRes.epsilonEquals(xformTestPoly, DBL_EPSILON)) {
					System.out.println("Error in xform INTERSECT near line " + line + ":");
					System.out.println("Wanted: " + getPolyName(testPolyRef));
					System.out.println(xformTestPoly.sprintf());
					System.out.println("   Got: " + getPolyName(resPtr));
					System.out.println(polyRes.sprintf());
					System.out.println("poly1\n" + poly1.sprintf());
					System.out.println("poly2\n" + poly2.sprintf());
					return false;
				}
				// #endif
				poly1.set(poly1Save);
				poly2.set(poly2Save);
			} else {
				polyRes.intersect(poly1, poly2);
				System.out.println(polyRes.sprintf());
			}
			break;
		}
		}
		return true;
	}

	public static void main(String[] args) {
		try {
			String testFileName = "ConvexPolygonTest.txt";
			StreamTokenizer stok = new StreamTokenizer(new FileReader(testFileName));

			stok.commentChar('#');
			stok.parseNumbers();
			ConvexPolygonTest test = new ConvexPolygonTest();
			while (true) {
				do {
					try {
						test.scan(stok);
					} catch (IOException ie) {
						System.out.println("Error reading file, line " + stok.lineno());
						ie.printStackTrace();
						System.exit(1);
					}
					if (test.cmd == DO_QUIT) {
						System.out.println("\nPassed\n");
						System.exit(0);
					}
				} while (test.cmd == DO_RESET);
				if (!test.execute(/*check=*/true)) {
					System.exit(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
