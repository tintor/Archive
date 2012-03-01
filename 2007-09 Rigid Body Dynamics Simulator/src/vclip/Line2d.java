package vclip;

import java.io.IOException;
import java.io.StreamTokenizer;

import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

class Line2d {
	Vector2d u; // vector in the direction of the line
	Point2d q; // some point on the line

	public Line2d() {
		u = new Vector2d();
		q = new Point2d();
	}

	public Line2d(Point2d q, Vector2d u) {
		this();
		set(q, u);
	}

	public void set(Point2d q, Vector2d u) {
		this.q.set(q);
		this.u.set(u);
	}

	void getPoint(Point2d p, double lam) {
		p.scaleAdd(lam, u, q);
	}

	int side(Tuple2d v, double tol) {
		double s = u.x * (v.y - q.y) - u.y * (v.x - q.x);
		if (s > tol) {
			return 1;
		} else if (s < -tol) {
			return -1;
		} else {
			return 0;
		}
	}

	double project(Vector2d res, Point2d p1) {
		double dx = p1.x - q.x;
		double dy = p1.y - q.y;
		double l = (dx * u.x + dy * u.y) / u.lengthSquared();

		if (res != null) {
			res.x = q.x + l * u.x;
			res.y = q.y + l * u.y;
		}
		return l;
	}

	private void scanCharacter(StreamTokenizer stok, int c) throws IOException {
		stok.nextToken();
		if (stok.ttype != c) {
			throw new IOException("'" + (char) c + "' expected");
		}
	}

	public void scan(StreamTokenizer stok) throws IOException {
		scanCharacter(stok, '[');
		q.x = TokenScanner.scanDouble(stok);
		q.y = TokenScanner.scanDouble(stok);
		scanCharacter(stok, ']');
		scanCharacter(stok, '[');
		u.x = TokenScanner.scanDouble(stok);
		u.y = TokenScanner.scanDouble(stok);
		scanCharacter(stok, ']');
	}

	public String toString() {
		return ("[ " + q.x + " " + q.y + " ]" + "[ " + u.x + " " + u.y + " ]");
	}
}
