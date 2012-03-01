package vclip;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

/**
 * A 2d convex polygon used by Vclip is handling face-face contact.
 */
public class ConvexPolygon {

	static final double DBL_EPSILON = 2.2204460492503131e-16;

	static private final Point2d zeroPoint = new Point2d(0, 0);

	static public final int NO_OVERLAP = 0;
	static public final int OVERLAP = 1;
	static public final int EDGE_WITHIN = 2;
	static public final int POLY_WITHIN = 3;

	private class IsectCtx {
		Edge ep1; // current edge on poly1 
		Edge ep2; // current edge on poly2
		Edge pp1; // last edge on poly1
		Edge pp2; // last edge on poly1
		Point2d ipnt; // generic intersection point
		Point2d q0; // first intersection point
		Point2d q1; // second intersection point
		int inflag; // which polytope is currently inside 
		int pcnt;
		int qcnt;
		double ah1;
		double ah2;
		double at1;
		double at2;
		double cross;
		boolean colinear;
		Vector2d dh2;
		Vector2d dt2;
		Vector2d dp;
		double tol;
		int lastAdvance;

		IsectCtx() {
			ipnt = new Point2d();
			q0 = new Point2d();
			q1 = new Point2d();
			dh2 = new Vector2d();
			dt2 = new Vector2d();
			dp = new Vector2d();
		}
	}

	class VertexIterator implements Iterator {
		Edge estart;
		Edge e;

		VertexIterator(Edge ehead) {
			e = estart = ehead;
		}

		public boolean hasNext() {
			return e != null;
		}

		public Object next() throws NoSuchElementException {
			if (e == null) {
				throw new NoSuchElementException();
			} else {
				Point2d vtx = e.vtx;
				if ((e = e.next) == estart) {
					e = null;
				}
				return vtx;
			}
		}

		public Point2d nextVertex() throws NoSuchElementException {
			if (e == null) {
				throw new NoSuchElementException();
			} else {
				Point2d vtx = e.vtx;
				if ((e = e.next) == estart) {
					e = null;
				}
				return vtx;
			}
		}

		public void remove() throws UnsupportedOperationException, IllegalStateException {
			throw new UnsupportedOperationException();
		}
	}

	private class Edge {
		Edge prev;
		Edge next;

		Point2d vtx;
		Vector2d u;

		Edge() {
			vtx = new Point2d();
			u = new Vector2d();
			prev = this;
			next = this;
		}

		Edge(double x, double y) {
			this();
			set(x, y);
		}

		Edge(Point2d p) {
			this();
			vtx.set(p);
		}

		void set(double x, double y) {
			vtx.x = x;
			vtx.y = y;
		}

		void append(Edge e) {
			e.next = next;
			e.prev = this;
			next.prev = e;
			next = e;
		}

		void prepend(Edge e) {
			e.prev = prev;
			e.next = this;
			prev.next = e;
			prev = e;
		}

		double intersect(Point2d res, Line2d l2, double tol) {
			/*
			Suppose we have two line segments, given by

			v = q1 + lam1 u1   and    v = q2 + lam2 u2

			where q1, u1, q2, and u2 are 2 vectors. In general,
			finding the intersection of these two points amounts
			to solving

			[ u1x  -u2x ] [ lam1 ]
			[           ] [      ] = q2 - q1 = del;
			[ u1y  -u2y ] [ lam2 ] 

			for which we get

			       -u2y delx + u2x dely    u2 X del
			lam1 = --------------------  = -------
				 u2x u1y - u1x u2y     u2 X u1

			       -u1y delx + u1x dely    u1 X del
			lam2 = --------------------  = -------
				 u2x u1y - u1x u2y     u2 X u1
			*/

			double dx = l2.q.x - vtx.x;
			double dy = l2.q.y - vtx.y;

			double denom = l2.u.x * u.y - l2.u.y * u.x;
			if (Math.abs(denom) <= tol) { // then the lines are parallel
				if (res != null) {
					res.scale(dx * u.x + dy * u.y, u);
					res.add(res, vtx);
				}
				return (Double.POSITIVE_INFINITY);
			} else {
				double l = (l2.u.x * dy - l2.u.y * dx) / denom;
				if (res != null) {
					res.scale(l, u);
					res.add(res, vtx);
				}
				return (l);
			}
		}
	}

	// bounding box info
	double xmin, xmax;
	double ymin, ymax;

	private Edge elistHead;
	//	private Edge elistTail;
	private int numv;

	double chardist = -1.0;
	double eps = -1.0;

	IsectCtx ctx = null;

	private void appendEdge(Edge e) {
		if (elistHead == null) {
			elistHead = e;
		} else {
			elistHead.prepend(e);
		}
	}

	private void prependEdge(Edge e) {
		if (elistHead == null) {
			elistHead = e;
		} else {
			elistHead.append(e);
		}
	}

	private void addEdgeAfter(Edge ref, Edge e) {
		ref.append(e);
		//  	   if (ref == elistTail)
		//  	    { elistTail = e;
		//  	    }
	}

	private void removeEdge(Edge e) {
		if (e.next == e) {
			elistHead = null;
		} else {
			if (e == elistHead) {
				elistHead = e.next;
			}
			e.next.prev = e.prev;
			e.prev.next = e.next;
		}
	}

	private int removeEdgesBetween(Edge e1, Edge e2) {
		int cnt = 0;
		// remove edges in between e1 and e2
		if (e1.next != e2) {
			for (Edge e = e1.next; e != e2; e = e.next) {
				if (e == elistHead) {
					elistHead = e1;
				}
				cnt++;
			}
			e1.next = e2;
			e2.prev = e1;
		}
		return cnt;
	}

	public ConvexPolygon() {
		clear();
	}

	public ConvexPolygon(double[] vlist) {
		this();
		set(vlist);
	}

	private void clear() {
		numv = 0;
		elistHead = null;
		//	   elistTail = null;
		if (chardist == -1) {
			eps = -1;
		}
		xmin = Double.POSITIVE_INFINITY;
	}

	public void set(double[] vlist) {
		clear();
		numv = vlist.length / 2;
		for (int i = 0; i < numv; i++) {
			Edge edge = new Edge(vlist[i * 2 + 0], vlist[i * 2 + 1]);
			if (elistHead != null) {
				edge.u.sub(edge.vtx, elistHead.prev.vtx);
			}
			appendEdge(edge);
		}
		if (numv > 0) {
			elistHead.u.sub(elistHead.vtx, elistHead.prev.vtx);
		}
	}

	public void beginDef() {
		clear();
	}

	public void addVertex(double x, double y, boolean ccw) {
		Edge edge = new Edge(x, y);
		if (ccw) // counter-clockwise
		{
			appendEdge(edge);
		} else // clockwise
		{
			prependEdge(edge);
		}
		numv++;
	}

	public void endDef() {
		Edge e;
		if ((e = elistHead) != null) {
			do {
				e.u.sub(e.vtx, e.prev.vtx);
				e = e.next;
			} while (e != elistHead);
		}
	}

	public void set(ConvexPolygon poly) {
		clear();
		numv = poly.numv;
		Edge copyEdge = poly.elistHead;
		for (int i = 0; i < numv; i++) {
			Edge edge = new Edge(copyEdge.vtx.x, copyEdge.vtx.y);
			appendEdge(edge);
			edge.u.set(copyEdge.u);
			copyEdge = copyEdge.next;
		}
		if (chardist == -1) {
			eps = poly.eps;
		}
		xmin = poly.xmin;
		xmax = poly.xmax;
		ymin = poly.ymin;
		ymax = poly.ymax;
	}

	public void scan(StreamTokenizer stok) throws IOException {
		Vector vlist = new Vector(10);
		stok.nextToken();
		if (stok.ttype != '[') {
			throw new IOException("'[' expected");
		}
		while (true) {
			stok.nextToken();
			if (stok.ttype != stok.TT_NUMBER) {
				break;
			}
			vlist.add(new Double(stok.nval));
			//	      System.out.println ("adding " + stok.nval);
			stok.nextToken();
			if (stok.ttype != stok.TT_NUMBER) {
				throw new IOException("number expected");
			}
			vlist.add(new Double(stok.nval));
			//	      System.out.println ("adding " + stok.nval);
		}
		if (stok.ttype != ']') {
			throw new IOException("'[' expected");
		}
		double[] varray = new double[vlist.size()];
		int i = 0;
		for (Iterator it = vlist.iterator(); it.hasNext();) {
			varray[i++] = ((Double) it.next()).doubleValue();
		}
		set(varray);
	}

	public String sprintf() {
		String s = "[ ";
		Edge e;
		if ((e = elistHead) != null) {
			do {
				s += ("" + e.vtx.x + " " + e.vtx.y);
				e = e.next;
				if (e != elistHead) {
					s += "\n  ";
				}
			} while (e != elistHead);
		}
		s += " ]";
		return s;
	}

	public boolean epsilonEquals(ConvexPolygon poly, double prec) {
		Edge e1, e2;
		Vector2d del = new Vector2d();
		double tol;

		if (numv != poly.numv) {
			return (false);
		}
		if (chardist == -1) {
			double x = Math.max(getCharDist(), poly.getCharDist());
			tol = x * x * prec;
		} else {
			tol = chardist * chardist * prec;
		}
		if ((e1 = elistHead) != null) {
			e2 = poly.elistHead;
			do {
				del.sub(e2.vtx, e1.vtx);
				if (del.lengthSquared() <= tol) { // found a beginning to second polytope
					boolean equal = true;
					e1 = e1.next;
					e2 = e2.next;
					while (e1 != elistHead) {
						del.sub(e2.vtx, e1.vtx);
						if (del.lengthSquared() > tol) {
							equal = false;
							break;
						}
						e1 = e1.next;
						e2 = e2.next;
					}
					return (equal);
				}
				e2 = e2.next;
			} while (e2 != poly.elistHead);
			return (false);
		}
		return (true);
	}

	public boolean consistencyCheck(boolean print) {
		int nvtx = 0;

		Edge e;
		Vector2d ucheck = new Vector2d();
		if ((e = elistHead) != null) {
			Point2d lastVtx = elistHead.prev.vtx;
			double tol = getCharDist() * DBL_EPSILON;
			do {
				Point2d vtx = e.vtx;
				ucheck.sub(vtx, lastVtx);
				if (e.next.prev != e) {
					if (print) {
						System.out.println("ConvexPolygon.consistencyCheck: e.next.prev != e, vertex " + nvtx);
					}
					return false;
				}
				if (!ucheck.epsilonEquals(e.u, tol)) {
					if (print) {
						System.out.println("ConvexPolygon.consistencyCheck: u inconsistent, vertex " + nvtx
								+ ", tol=" + tol);
						System.out.println("  vtx: " + vtx);
						System.out.println("  lastVtx: " + lastVtx);
						System.out.println("  u: " + e.u);
					}
					return false;
				}
				if (numv > 1 && vtx.epsilonEquals(lastVtx, tol)) {
					if (print) {
						System.out.println("ConvexPolygon.consistencyCheck: repeated vertex at " + nvtx
								+ ", tol=" + tol);
						System.out.println("  vtx: " + vtx);
						System.out.println("  lastVtx: " + lastVtx);
					}
					return false;
				}
				nvtx++;
				lastVtx = vtx;
				e = e.next;
			} while (e != elistHead);
		}
		if (nvtx != numv) {
			if (print) {
				System.out.println("ConvexPolygon.consistencyCheck: numv=" + numv + ", actual number is " + nvtx);
			}
			return false;
		}
		return true;
	}

	double getEps() {
		double d;

		if (eps == -1) {
			if (numv == 1) {
				d = elistHead.vtx.distance(zeroPoint);
			} else {
				calcBoundingBox();
				d = (xmax - xmin) + (ymax - ymin);
			}
			eps = d * d * DBL_EPSILON;
		}
		return (eps);
	}

	double getCharDist() {
		double cdist;

		if (chardist != -1) {
			cdist = chardist;
		} else {
			cdist = Math.sqrt(getEps() / DBL_EPSILON);
		}
		return (cdist);
	}

	void setCharDist(double cdist) {
		chardist = cdist;
		eps = cdist * cdist * DBL_EPSILON;
	}

	void calcBoundingBox() {
		Edge e;

		if (xmin == Double.POSITIVE_INFINITY) {
			if ((e = elistHead) != null) {
				xmin = e.vtx.x;
				xmax = e.vtx.x;
				ymin = e.vtx.y;
				ymax = e.vtx.y;

				for (e = e.next; e != elistHead; e = e.next) {
					if (e.vtx.x < xmin) {
						xmin = e.vtx.x;
					} else if (e.vtx.x > xmax) {
						xmax = e.vtx.x;
					}
					if (e.vtx.y < ymin) {
						ymin = e.vtx.y;
					} else if (e.vtx.y > ymax) {
						ymax = e.vtx.y;
					}
				}
			} else {
				xmin = xmax = 0;
				ymin = ymax = 0;
			}
		}
	}

	public ConvexPolygon intersect(Line2d hp) {
		Edge e1, e2;
		Edge firstNotIn = null;
		Edge lastNotIn = null;
		boolean insideVtxExists = false;
		int side1, side2;
		Point2d qnew = new Point2d();

		if (numv == 0) {
			return (this);
		}
		getEps();

		e1 = elistHead;
		side1 = hp.side(e1.vtx, eps);
		do {
			e2 = e1.next;

			if (side1 == 1) {
				insideVtxExists = true;
			}

			side2 = hp.side(e2.vtx, eps);
			if (side1 == 1 && side2 != 1) {
				if (side2 == -1) {
					e2.intersect(qnew, hp, eps);
					firstNotIn = new Edge();
					firstNotIn.vtx.set(qnew);
					firstNotIn.u.sub(qnew, e1.vtx);
					addEdgeAfter(e1, firstNotIn);
					numv++;
				} else {
					firstNotIn = e2;
				}
			} else if (side1 != 1 && side2 == 1) {
				if (side1 == -1) {
					e2.intersect(qnew, hp, eps);
					lastNotIn = new Edge();
					e2.u.sub(e2.vtx, qnew);
					lastNotIn.vtx.set(qnew);
					addEdgeAfter(e1, lastNotIn);
					numv++;
				} else {
					lastNotIn = e1;
				}
			}
			e1 = e2;
			side1 = side2;
		} while (e1 != elistHead && (firstNotIn == null || lastNotIn == null));

		if (firstNotIn == null && lastNotIn == null) {
			if (!insideVtxExists) {
				clear();
			}
		} else if (firstNotIn != null && lastNotIn != null) {
			if (firstNotIn != lastNotIn) {
				numv -= removeEdgesBetween(firstNotIn, lastNotIn);
				lastNotIn.u.sub(lastNotIn.vtx, firstNotIn.vtx);
			}
		} else {
			System.err.println("Internal error: ConvexPolygon.intersect(Line2d):");
			System.err.println("firstNotIn=" + firstNotIn + "lastNotIn=" + lastNotIn);
			System.exit(1);
		}
		return (this);
	}

	public ConvexPolygon intersect(ConvexPolygon poly, Line2d hp) {
		if (poly != this) {
			set(poly);
		}
		return intersect(hp);
	}

	static void updateLam(double[] lam, Point2d p, Line2d hp) {
		double l;

		l = hp.project(null, p);
		if (l < lam[0]) {
			lam[0] = l;
		}
		if (l > lam[1]) {
			lam[1] = l;
		}
	}

	public void centroid(Point2d p) {
		/* Compute the centroid of a polygon 'pgn'. */

		Edge ep;
		double x, y;

		x = y = 0;
		if ((ep = elistHead) != null) {
			do {
				x += ep.vtx.x;
				y += ep.vtx.y;
				ep = ep.next;
			} while (ep != elistHead);
		}
		p.x = x / numv;
		p.y = y / numv;
	}

	public double area() {
		/* Compute the area of the polygon */

		Edge e;
		double v0x, v0y; /* coordinates of first vertex v0 */
		double d1x, d1y; /* vector from v0 to some vertex vi */
		double d2x, d2y; /* vector from v0 to vertex v(i+1) */
		double area; /* accumulated area */

		area = 0;

		e = elistHead;
		v0x = e.vtx.x;
		v0y = e.vtx.y;

		e = e.next;
		d2x = e.vtx.x - v0x;
		d2y = e.vtx.y - v0y;

		for (e = e.next; e != elistHead; e = e.next) {
			d1x = d2x;
			d1y = d2y;
			d2x = e.vtx.x - v0x;
			d2y = e.vtx.y - v0y;
			area += (d1x * d2y - d1y * d2x);
		}
		return (area / 2);
	}

	public int isInside(Point2d p) {
		// returns 1 if p is inside, 0 if it is on the boundary,
		// and -1 if it is outside.

		Edge e;
		double prod;

		getEps();
		if ((e = elistHead) != null) {
			do {
				prod = e.u.x * (p.y - e.vtx.y) - e.u.y * (p.x - e.vtx.x);
				if (prod <= eps) {
					return (prod < -eps ? -1 : 0);
				}
				e = e.next;
			} while (e != elistHead);
			return 1;
		} else {
			return -1;
		}
	}

	private int isectEdge(Edge e1, IsectCtx ctx) {
		double[] lam = new double[2];
		Line2d line = new Line2d();
		int n;

		if (e1.next == null) { // degenerate edge; single vertex
			if (isInside(e1.vtx) >= 0) {
				ctx.q0.set(e1.vtx);
				ctx.pcnt = 1;
				return EDGE_WITHIN;
			} else {
				ctx.pcnt = 0;
				return NO_OVERLAP;
			}
		} else {
			line.set(e1.prev.vtx, e1.u);
			n = intersectLine(lam, line, ctx.tol);

			if (n == 1) {
				if (lam[0] < -ctx.tol || lam[0] > 1 + ctx.tol) {
					n = 0;
				}
				lam[0] = Math.max(0.0, Math.min(1.0, lam[0]));
			} else if (n == 2) {
				if (lam[0] > 1 + ctx.tol || lam[1] < -ctx.tol) {
					n = 0;
				} else {
					lam[0] = Math.max(0.0, Math.min(1.0, lam[0]));
					lam[1] = Math.max(0.0, Math.min(1.0, lam[1]));
					if (Math.abs(lam[0] - lam[1]) <= 1.0 * ctx.tol) {
						n = 1;
					}
				}
			}
			ctx.pcnt = n;
			if (n == 0) {
				return NO_OVERLAP;
			} else if (n == 1) {
				ctx.q0.scaleAdd(lam[0], line.u, line.q);
				return OVERLAP;
			} else if (n == 2) {
				ctx.q0.scaleAdd(lam[0], line.u, line.q);
				ctx.q1.scaleAdd(lam[1], line.u, line.q);
				if (lam[0] == 0 && lam[1] == 1) {
					return EDGE_WITHIN;
				} else if (lam[0] > 0 && lam[1] < 1 && numv == 2) {
					return POLY_WITHIN;
				} else {
					return OVERLAP;
				}
			} else {
				System.err.println("ConvexPolygon.isectEdge: internal error: n=" + n);
				System.exit(1);
				return -1;
			}
		}
	}

	private ConvexPolygon intersectLineSeg(ConvexPolygon poly1, ConvexPolygon poly2) {
		// Special case intersection when poly1 is a 2-edge line segment

		Edge e1, e2;

		int code = poly2.isectEdge(poly1.elistHead, ctx);
		if (ctx.pcnt == 1) {
			e1 = new Edge(ctx.q0);
			e1.u.set(0, 0);
			appendEdge(e1);
			numv = 1;
		} else if (ctx.pcnt == 2) {
			e1 = new Edge(ctx.q0);
			e2 = new Edge(ctx.q1);
			appendEdge(e1);
			e1.u.sub(e1.vtx, e2.vtx);
			appendEdge(e2);
			e2.u.sub(e2.vtx, e1.vtx);
			numv = 2;
		}
		if (code == EDGE_WITHIN) {
			return poly1;
		} else if (code == POLY_WITHIN) {
			return poly2;
		} else {
			return this;
		}
	}

	public int intersectLine(double[] lam, Line2d line) {
		return intersectLine(lam, line, getEps());
	}

	public int intersectLine(double[] lam, Line2d line, double tol) {
		Edge e1, e2;
		Point2d pi = new Point2d();
		double at, ah;

		lam[0] = Double.POSITIVE_INFINITY;
		lam[1] = -Double.POSITIVE_INFINITY;

		//	   DEBUG_STATEMENT(printf ("tol=%g\n", tol));

		if ((e1 = elistHead) == null) {
			return 0;
		} else {
			// at = line.u X (e1.vtx-line.q)
			at = line.u.x * (e1.vtx.y - line.q.y) - line.u.y * (e1.vtx.x - line.q.x);
			do {
				e2 = e1.next;
				// ah = line.u X (e2.vtx-line.q)
				ah = line.u.x * (e2.vtx.y - line.q.y) - line.u.y * (e2.vtx.x - line.q.x);
				//		 DEBUG_STATEMENT(printf ("at=%g ah=%g %d %d\n", at, ah, 
				//					 Math.abs(ah) <= tol, Math.abs(at) <= tol));
				if (Math.abs(ah) <= tol || Math.abs(at) <= tol) {
					if (Math.abs(ah) <= tol) {
						updateLam(lam, e2.vtx, line);
						//		       DEBUG_STATEMENT(printf ("ah lam=%g %g\n", lam[0], lam[1]));
					}
					if (Math.abs(at) <= tol) {
						updateLam(lam, e1.vtx, line);
						//		       DEBUG_STATEMENT(printf ("at lam=%g %g\n", lam[0], lam[1]));
					}
				} else {
					if (ah * at < 0) {
						pi.scaleAdd(-ah / (ah - at), e2.u, e2.vtx);
						updateLam(lam, pi, line);
						//		       DEBUG_STATEMENT(printf ("xx lam=%g %g\n", lam[0], lam[1]));
					} else if (lam[0] != Double.POSITIVE_INFINITY && lam[0] != lam[1]) { // short-cut: we have found a finite interval, and we
						// didn't add to it this time, so was are done.
						//		       DEBUG_STATEMENT(printf ("break\n"));
						break;
					}
				}
				e1 = e2;
				at = ah;
			} while (e1 != elistHead);
		}
		if (lam[0] == Double.POSITIVE_INFINITY) {
			double tmp = lam[0];
			lam[0] = lam[1];
			lam[1] = tmp;
			return (0);
		} else if (lam[0] != lam[1]) {
			return (2);
		} else {
			return (1);
		}
	}

	private int addIsectPoint(Point2d p) {
		Edge e1 = null, e2;
		double u2x = 0;
		double u2y = 0;

		if (elistHead != null) {
			e1 = elistHead.prev;
			u2x = p.x - e1.vtx.x;
			u2y = p.y - e1.vtx.y;
			if (Math.abs(u2x) <= ctx.tol * 100 && Math.abs(u2y) <= ctx.tol * 100) {
				return 0;
			}
		}
		e2 = new Edge();
		e2.vtx.set(p);
		if (e1 != null) {
			e2.u.set(u2x, u2y);
		}
		appendEdge(e2);
		numv++;
		return 1;
	}

	private ConvexPolygon finishIsect() {
		/* Complete the Polygon structure describing the intersection
		polygon. */

		Edge ep, pp;

		if (numv == 0) {
			return (this);
		}

		ep = elistHead;
		pp = elistHead.prev;
		ep.u.sub(ep.vtx, pp.vtx);

		if (numv > 1) {
			if (Math.abs(ep.u.x) <= ctx.tol * 100 && Math.abs(ep.u.y) <= ctx.tol * 100) {
				removeEdge(pp);
				pp = elistHead.prev;
				ep.u.sub(ep.vtx, pp.vtx);
				numv--;
			}
		}
		return (this);
	}

	private double withinTol(double x, double tol) {
		if (x <= tol && x >= -tol) {
			return (0);
		} else {
			return (x);
		}
	}

	private boolean isectLineSegs(Point2d pnt) {
		double ah2at2;
		double ah1at1;

		ctx.colinear = false;

		if ((ah2at2 = ctx.ah2 * ctx.at2) > 0 || (ah1at1 = ctx.ah1 * ctx.at1) > 0) {
			return (false);
		} else if (ah2at2 < 0 && ah1at1 < 0) {
			pnt.scaleAdd(ctx.ah1 / (ctx.at1 - ctx.ah1), ctx.ep1.u, ctx.ep1.vtx);
			ctx.inflag = ctx.ah1 > 0 ? 'p' : 'q';
			return (true);
		} else if ((ctx.ah1 == 0 && ctx.at1 == 0) || (ctx.ah2 == 0 && ctx.at2 == 0)) {
			ctx.colinear = true;
			//  	      if (colinearOK)
			//  	       { return (doColinear (pnts, ctx));
			//  	       }
			//  	      else
			{
				return (false);
			}
		} else if (ah2at2 < 0) {
			pnt.set(ctx.ah1 == 0 ? ctx.ep1.vtx : ctx.pp1.vtx);
		} else if (ah1at1 < 0) {
			pnt.set(ctx.ah2 == 0 ? ctx.ep2.vtx : ctx.pp2.vtx);
		} else if (ctx.at1 == 0) {
			if (ctx.at2 == 0) {
				ctx.dp.sub(ctx.pp1.vtx, ctx.pp2.vtx);
				pnt.set(ctx.dp.dot(ctx.ep2.u) >= 0 ? ctx.pp2.vtx : ctx.pp1.vtx);
			} else // ctx.ah2 == 0
			{
				ctx.dp.sub(ctx.pp1.vtx, ctx.ep2.vtx);
				pnt.set(ctx.dp.dot(ctx.ep2.u) >= 0 ? ctx.ep2.vtx : ctx.pp1.vtx);
			}
		} else if (ctx.ah1 == 0) {
			if (ctx.at2 == 0) {
				ctx.dp.sub(ctx.ep1.vtx, ctx.pp2.vtx);
				pnt.set(ctx.dp.dot(ctx.ep2.u) >= 0 ? ctx.pp2.vtx : ctx.ep1.vtx);
			} else // ctx.ah2 == 0
			{
				ctx.dp.sub(ctx.ep1.vtx, ctx.ep2.vtx);
				pnt.set(ctx.dp.dot(ctx.ep2.u) >= 0 ? ctx.ep2.vtx : ctx.ep1.vtx);
			}
		} else {
			System.err.println("ConvexPolygon.isectLineSegs: help! we forgot a case!\n");
			System.exit(1);
		}

		if (ctx.ah1 > 0) {
			if (ah2at2 < 0) {
				ctx.inflag = 'p';
			} else if (ctx.at2 == 0) {
				ctx.dp.sub(ctx.ep1.vtx, ctx.pp2.vtx);
				if (crossProd(ctx.ep2.prev.u, ctx.dp) > ctx.tol) {
					ctx.inflag = 'p';
				}
			}
		} else if (ctx.ah2 > 0) {
			if (ah1at1 < 0) {
				ctx.inflag = 'q';
			} else if (ctx.at1 == 0) {
				ctx.dp.sub(ctx.ep2.vtx, ctx.pp1.vtx);
				if (crossProd(ctx.ep1.prev.u, ctx.dp) > ctx.tol) {
					ctx.inflag = 'q';
				}
			}
		}
		return (true);
	}

	private void advanceQ(String s) {
		if (ctx.inflag == 'q') {
			addIsectPoint(ctx.ep2.vtx);
		}
		ctx.pp2 = ctx.ep2;
		ctx.ep2 = ctx.ep2.next;

		ctx.dh2.sub(ctx.ep2.vtx, ctx.ep1.vtx);
		ctx.dt2.sub(ctx.pp2.vtx, ctx.ep1.vtx);

		ctx.at2 = ctx.ah2;
		ctx.ah2 = withinTol(crossProd(ctx.ep1.u, ctx.dh2), ctx.tol);
		ctx.ah1 = withinTol(-crossProd(ctx.ep2.u, ctx.dh2), ctx.tol);
		ctx.at1 = withinTol(ctx.ah2 - ctx.at2 + ctx.ah1, ctx.tol);

		ctx.qcnt++;
		ctx.lastAdvance = 'q';
	}

	private void advanceP(String s) {
		if (ctx.inflag == 'p') {
			addIsectPoint(ctx.ep1.vtx);
		}
		ctx.pp1 = ctx.ep1;
		ctx.ep1 = ctx.ep1.next;

		ctx.dh2.sub(ctx.ep2.vtx, ctx.ep1.vtx);
		ctx.dt2.sub(ctx.pp2.vtx, ctx.ep1.vtx);

		ctx.at1 = ctx.ah1;
		ctx.ah1 = withinTol(-crossProd(ctx.ep2.u, ctx.dh2), ctx.tol);
		ctx.ah2 = withinTol(crossProd(ctx.ep1.u, ctx.dh2), ctx.tol);
		ctx.at2 = withinTol(ctx.ah2 - ctx.at1 + ctx.ah1, ctx.tol);

		ctx.pcnt++;
		ctx.lastAdvance = 'p';
	}

	public ConvexPolygon intersect(ConvexPolygon poly1, ConvexPolygon poly2) {
		return intersect(poly1, poly2, false);
	}

	public ConvexPolygon intersect(ConvexPolygon poly1, boolean boundingBoxCheck) {
		ConvexPolygon tmp = new ConvexPolygon();
		ConvexPolygon res = tmp.intersect(this, poly1, boundingBoxCheck);
		set(tmp);
		return (res == tmp ? this : res);
	}

	public ConvexPolygon intersect(ConvexPolygon poly1) {
		return intersect(poly1, false);
	}

	private double crossProd(Tuple2d v1, Tuple2d v2) {
		return v1.x * v2.y - v1.y * v2.x;
	}

	public ConvexPolygon intersect(ConvexPolygon poly1, ConvexPolygon poly2, boolean boundingBoxCheck) {
		ConvexPolygon polyi = this;

		clear();

		if (ctx == null) {
			ctx = new IsectCtx();
		}

		//	   DEBUG_STATEMENT (printf ("\n"));

		// Do the bounding box check, if called for

		if (boundingBoxCheck) {
			if (poly1.xmin == Double.POSITIVE_INFINITY) {
				poly1.calcBoundingBox();
			}
			if (poly2.xmin == Double.POSITIVE_INFINITY) {
				poly2.calcBoundingBox();
			}
			if (poly1.xmax <= poly2.xmin || poly1.xmin >= poly2.xmax || poly1.ymax <= poly2.ymin
					|| poly1.ymin >= poly2.ymax) {
				return (polyi);
			}
		}

		if (chardist == -1) {
			double eps1, eps2;

			eps1 = poly1.getEps();
			eps2 = poly2.getEps();
			eps = Math.max(eps1, eps2);
			ctx.tol = eps;
		}

		// Trap degeneracies here. In theory, the general algorithm should
		// be able to handle degeneracies, but in practice it can only
		// do so with a bunch of extra special case code that reduces
		// the overall efficiency.

		if (poly1.numv == 2) {
			return (intersectLineSeg(poly1, poly2));
		} else if (poly2.numv == 2) {
			return (intersectLineSeg(poly2, poly1));
		}

		ctx.inflag = 0;
		ctx.pcnt = 0;
		ctx.qcnt = 0;
		//	   ctx.esupply = RxEdge2::allocList (poly1.numv + poly2.numv);
		//	   ctx.vsupply = RxVrtx2::allocList (poly1.numv + poly2.numv);

		ctx.ep1 = poly1.elistHead;
		ctx.ep2 = poly2.elistHead;
		ctx.pp1 = ctx.ep1.prev;
		ctx.pp2 = ctx.ep2.prev;

		ctx.dh2.sub(ctx.ep2.vtx, ctx.ep1.vtx);
		ctx.dt2.sub(ctx.pp2.vtx, ctx.ep1.vtx);

		ctx.ah2 = withinTol(crossProd(ctx.ep1.u, ctx.dh2), ctx.tol);
		ctx.at2 = withinTol(crossProd(ctx.ep1.u, ctx.dt2), ctx.tol);
		ctx.ah1 = withinTol(-crossProd(ctx.ep2.u, ctx.dh2), ctx.tol);
		ctx.at1 = withinTol(ctx.ah2 - ctx.at2 + ctx.ah1, ctx.tol);
		ctx.lastAdvance = 0;

		do {
			double cross;

			cross = ctx.ah2 - ctx.at2;

			/* Check for intersection of both segments */

			if (isectLineSegs(ctx.ipnt)) {
				if (numv == 0) {
					ctx.pcnt = ctx.qcnt = 0;
					ctx.q0.set(ctx.ipnt);
				} else if (numv > 1) {
					if (Math.abs(ctx.ipnt.x - ctx.q0.x) <= ctx.tol * 100
							&& Math.abs(ctx.ipnt.y - ctx.q0.y) <= ctx.tol * 100) {
						return (finishIsect());
					}
				}
				addIsectPoint(ctx.ipnt);
			}

			//	      DEBUG_STATEMENT (printf ("cross=%g\n", cross));

			if (ctx.colinear) {
				if (ctx.ep1.u.dot(ctx.ep2.u) < 0) // well defined 
				{
					if (ctx.lastAdvance == 'p') {
						advanceQ("A");
					} else {
						advanceP("B");
					}
				} else {
					if (ctx.ep1.u.dot(ctx.dh2) >= 0) {
						advanceP("AA");
					} else {
						advanceQ("BB");
					}
				}
			}

			// These next two rules say: If the head of X lies on the line 
			// of Y, is behind Y, and X is pointing away from Y, advance Y

			else if (ctx.ah1 == 0 && cross * ctx.at2 > 0) {
				advanceQ("JJ");
			} else if (ctx.ah2 == 0 && cross * ctx.at1 < 0) {
				advanceP("KK");
			} else if (cross >= 0) {
				if (ctx.ah2 > 0) {
					advanceP("C");
				} else {
					advanceQ("D");
				}
			} else {
				if (ctx.ah1 > 0) {
					advanceQ("E");
				} else {
					advanceP("F");
				}
			}
		} while ((ctx.pcnt < poly1.numv || ctx.qcnt < poly2.numv)
				&& (ctx.pcnt < 2 * poly1.numv && ctx.qcnt < 2 * poly2.numv));

		if (numv > 0) {
			return (finishIsect());
		}

		/* If we got this far, we know thctx.at either (1) there is
		no intersection, (2) poly1 is contained in poly2, or
		(3) poly2 is contained in poly1. */
		{
			/* Contact did not occur. This implies either 
			 there is no intersection, or one polygon is completely
			 inside the other. */

			boolean insideTheOther = false;
			double a1, a2; /* polygon areas */

			poly1.centroid(ctx.q0);
			if (poly2.isInside(ctx.q0) == 1) {
				insideTheOther = true;
			} else {
				poly2.centroid(ctx.q1);
				if (poly1.isInside(ctx.q1) == 1) {
					insideTheOther = true;
				}
			}
			if (!insideTheOther) {
				return (this);
			}

			a1 = poly1.area();
			a2 = poly2.area();
			if (a1 > a2) {
				set(poly2);
				return poly2;
			} else {
				set(poly1);
				return poly1;
			}
		}
	}

	public int numVertices() {
		return numv;
	}

	public Iterator getVertices() {
		return new VertexIterator(elistHead);
	}

	public VertexIterator getVertexIterator() {
		return new VertexIterator(elistHead);
	}

	void shiftVertices(int n) {
		Edge e;
		if ((e = elistHead) != null) {
			for (int i = 0; i < n; i++) {
				e = e.next;
			}
			elistHead = e;
			//	      elistTail = e.prev;
		}
	}

	public void xform(ConvexPolygon poly, double px, double py, double ang) {
		if (poly != this) {
			set(poly);
		}
		double s = Math.sin(ang);
		double c = Math.cos(ang);

		Edge e;
		if ((e = elistHead) != null) {
			do {
				e.vtx.set(e.vtx.x * c - e.vtx.y * s + px, e.vtx.x * s + e.vtx.y * c + py);
				e.u.set(e.u.x * c - e.u.y * s, e.u.x * s + e.u.y * c);
				e = e.next;
			} while (e != elistHead);
		}
		if (xmin != Double.POSITIVE_INFINITY) {
			double x, y;
			x = xmin * c - ymin * s + px;
			y = xmin * s + ymin * c + py;
			xmin = x;
			ymin = y;
			x = xmax * c - ymax * s + px;
			y = xmax * s + ymax * c + py;
			xmax = x;
			ymax = y;
		}
	}

	public String toString() {
		String s = "[ ";
		Edge e = elistHead;
		if (e != null) {
			do {
				s += (e.vtx.x + " " + e.vtx.y);
				e = e.next;
				if (e != elistHead) {
					s += "\n";
				}
			} while (e != elistHead);
		}
		s += " ]";
		return s;
	}
}
