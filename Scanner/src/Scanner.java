import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import tintor.geometry.GMath;
import tintor.geometry.Vector2;

interface IFigureSink {
	void startFigure(Vector2 p);

	void addLine(Vector2 p);

	void endFigure();
}

class BoundingBox implements IFigureSink {
	float xmin, xmax, ymin, ymax;

	@Override public void startFigure(final Vector2 p) {
		xmin = xmax = p.x;
		ymin = ymax = p.y;
	}

	@Override public void addLine(final Vector2 p) {
		if (p.x < xmin)
			xmin = p.x;
		else if (p.x > xmax) xmax = p.x;
		if (p.y < ymin)
			ymin = p.y;
		else if (p.y > ymax) ymax = p.y;
	}

	@Override public void endFigure() {

	}
}

class Chain implements Comparable<Chain> {
	Object meta;
	int cmp;
	int cursor;
	final List<Vector2> list = new ArrayList<Vector2>();

	void invariant() {
		assert 0 <= cursor && cursor < list.size();
		for (int i = 1; i < list.size(); i++)
			assert cmp == compare(list.get(i), list.get(i - 1));
	}

	public void inc() {
		cursor += cmp;
	}

	public boolean end() {
		return cmp == 1 ? cursor == list.size() - 1 : cursor == 0;
	}

	@Override public int compareTo(final Chain c) {
		return compare(list.get(cursor), c.list.get(cursor));
	}

	public static int compare(final Vector2 a, final Vector2 b) {
		return Vector2.compare(a.y, b.y, a.x, b.x);
	}
}

class ChainBuilder implements IFigureSink {
	Object meta;
	Scanner scanner;
	private Chain first, current;

	@Override public void startFigure(final Vector2 p) {
		assert current == null && first == null;
		first = current = new Chain();
		first.meta = meta;
		first.list.add(p);
	}

	@Override public void addLine(final Vector2 p) {
		assert current != null && first != null;
		assert current.list.size() > 1 || current.list.size() == 1 && first == current;

		final int cmp = Chain.compare(p, current.list.get(current.list.size() - 1));
		if (cmp == 0) throw new RuntimeException("Equal consecutive points.");
		assert cmp == -1 || cmp == 1;

		if (cmp != current.cmp) {
			// break current chain
			final Chain c = new Chain();
			c.meta = meta;
			c.cmp = cmp;
			c.list.add(p);

			add(current);
			current = c;
		}

		current.list.add(p);
	}

	@Override public void endFigure() {
		assert current != null && first != null;

		// assume figure is closed
		assert Chain.compare(current.list.get(current.list.size() - 1), first.list.get(0)) == 0;

		if (first != current && first.cmp == current.cmp) for (int i = 1; i < first.list.size(); i++)
			current.list.add(first.list.get(i));

		add(current);
		first = current = null;
	}

	private void add(final Chain c) {
		c.cursor = c.cmp == 1 ? 0 : c.list.size() - 1;
		scanner.queue.add(c);
	}
}

class LineIntersection {
	Vector2 i; // intersection point if exists
	Vector2 r; // coeficients of intersection point

	enum Result {
		None, Point, Line
	}

	public Result intersect(final Vector2 a, final Vector2 b, final Vector2 c, final Vector2 d) {
		assert Chain.compare(a, b) < 0 && Chain.compare(c, d) < 0;

		// init
		i = r = null;

		// check trivial solutions
		final int cmp_bc = Chain.compare(b, c);
		if (cmp_bc == 0) {
			i = b;
			r = new Vector2(1, 0);
			return Result.Point;
		}
		if (cmp_bc < 0) return Result.None;

		final int cmp_da = Chain.compare(d, a);
		if (cmp_da == 0) {
			i = d;
			r = new Vector2(0, 1);
			return Result.Point;
		}
		if (cmp_da < 0) return Result.None;

		// solve linear system
		r = GMath.solveLinear(b.sub(a), c.sub(d), a.sub(c));
		if (!GMath.isFinite(r.x) || !GMath.isFinite(r.y)) return Result.Line;
		if (r.x < 0 || 1 < r.x || r.y < 0 && 1 < r.y) return Result.None;
		i = Vector2.linear(a, b, r.x);
		return Result.Point;
	}
}

public class Scanner {
	final List<Chain> list = new ArrayList<Chain>(); // contains only active chains, sorted from left to right
	final Queue<Chain> queue = new PriorityQueue<Chain>(); // contains non-dead chains

	public void scan() {
		while (!queue.isEmpty()) {
			final Chain p = queue.remove();
			if (!p.end()) {
				final Vector2 a = p.list.get(p.cursor);
				p.inc();
				final Vector2 b = p.list.get(p.cursor);

				// TODO find first intersection with chains in list
				if (intersection) {

				} else {

				}
			}
		}
	}

	public void onFork(final Vector2 p, final List<Chain> out) {

	}

	public void onVertex(final Vector2 p, final Chain inout) {

	}

	public void onMerge(final Vector2 p, final List<Chain> in) {

	}

	public void onJunction(final Vector2 p, final List<Chain> in, final List<Chain> out) {

	}

	public static void main(final String[] args) {
		final Scanner scanner = new Scanner();
		final ChainBuilder b = new ChainBuilder();
		b.scanner = scanner;
	}
}

class Contains extends Scanner {

}

// relations: contains, overlaps, disjoint