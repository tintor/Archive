import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import tintor.Timer;
import tintor.util.IdentityHashSet;

class Rect {
	int xmin, ymin, xmax, ymax;
	int[] color;

	Rect(final int xmin, final int ymin, final int xmax, final int ymax, final int[] color) {
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
		this.color = color;
	}

	boolean intersects(final Rect a) {
		return intervals(xmin, xmax, a.xmin, a.xmax) && intervals(ymin, ymax, a.ymin, a.ymax);
	}

	private static boolean intervals(final int amin, final int amax, final int bmin, final int bmax) {
		return amax >= bmin && bmax >= amin;
	}

	int surface() {
		return (xmax - xmin + 1) * (ymax - ymin + 1);
	}

	int width() {
		return xmax - xmin + 1;
	}

	int height() {
		return ymax - ymin + 1;
	}
}

class Rectangles implements Iterable<Rect> {
	Set<Rect> set = new IdentityHashSet<Rect>();
	List<Rect>[][] cells;

	@SuppressWarnings("unchecked") Rectangles(final int width, final int height) {
		cells = new List[width + (1 << bits) - 1 >> bits][height + (1 << bits) - 1 >> bits];
	}

	Iterable<Rect> intersections(final Rect r) {
		final Set<Rect> set = new IdentityHashSet<Rect>();
		final Vector2i a = cell(r.xmin, r.ymin), b = cell(r.xmax, r.ymin);
		for (int x = a.x; x <= b.x; x++)
			for (int y = a.y; y <= b.y; y++)
				if (cells[x][y] != null) for (final Rect z : cells[x][y])
					if (z.intersects(r)) set.add(z);
		return set;
	}

	final static int bits = 7;

	static Vector2i cell(final int x, final int y) {
		return new Vector2i(x >> bits, y >> bits);
	}

	void remove(final Rect r) {
		set.remove(r);
		final Vector2i a = cell(r.xmin, r.ymin), b = cell(r.xmax, r.ymin);
		for (int x = a.x; x <= b.x; x++)
			for (int y = a.y; y <= b.y; y++)
				if (cells[x][y] != null) cells[x][y].remove(r);
	}

	void add(final Rect r) {
		set.add(r);
		final Vector2i a = cell(r.xmin, r.ymin), b = cell(r.xmax, r.ymin);
		for (int x = a.x; x <= b.x; x++)
			for (int y = a.y; y <= b.y; y++) {
				if (cells[x][y] == null) cells[x][y] = new ArrayList<Rect>();
				cells[x][y].add(r);
			}
	}

	public Iterator<Rect> iterator() {
		return set.iterator();
	}
}

public class Marko {
	static BufferedImage image;
	static final int[] pixel = new int[3], blue = new int[] { 0, 0, 255 }, black = new int[] { 0, 0, 0 },
			xblack = new int[] { 0, 1, 0 }, red = new int[] { 255, 0, 0 };

	static int xmin, xmax, ymin, ymax;
	static final Deque<Vector2i> queue = new ArrayDeque<Vector2i>();

	static List<Rect> symbols = new ArrayList<Rect>();

	static double error(final Rect a, final Rect b) {
		final int w = Math.min(a.width(), b.width()), h = Math.min(a.height(), b.height());
		final int s = Math.max(a.width(), b.width()) * Math.max(a.height(), b.height());

		int m = 0;
		for (int x = -2; x <= 2; x++)
			for (int y = -2; y <= 2; y++)
				m = Math.max(m, matches(a.xmin, a.ymin, b.xmin + x, b.xmax + y, w, h));
		return (float) (s - m) / s;
	}

	static int matches(final int ax, final int ay, final int bx, final int by, final int w, final int h) {
		//		if (Math.max(ax, bx) + w >= image.getWidth()) w = image.getWidth() - Math.max(ax, bx);
		//		if (Math.max(ay, by) + h >= image.getHeight()) h = image.getHeight() - Math.max(ay, by);

		int c = 0;
		final int[] a = new int[3], b = new int[3];
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				image.getRaster().getPixel(ax + x, ay + y, a);
				image.getRaster().getPixel(bx + x, by + y, b);
				if (a[0] > 100 == b[0] > 100) c++;
			}
		return c;
	}

	public static void main(final String[] args) throws IOException {
		final Timer timer = new Timer();
		timer.restart();

		image = ImageIO.read(new File("c:/temp/super pamcenje/010.png"));
		//symbols = new Rectangles(image.getWidth(), image.getHeight());

		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++) {
				image.getRaster().getPixel(x, y, pixel);
				if (is(black)) {
					xmin = xmax = x;
					ymin = ymax = y;

					queue.clear();
					queue.offer(new Vector2i(x, y));
					image.getRaster().setPixel(x, y, xblack);

					while (queue.size() > 0) {
						final Vector2i a = queue.poll();

						if (a.x < xmin) xmin = a.x;
						if (a.x > xmax) xmax = a.x;
						if (a.y < ymin) ymin = a.y;
						if (a.y > ymax) ymax = a.y;

						visit(a.x - 1, a.y);
						visit(a.x + 1, a.y);
						visit(a.x, a.y - 1);
						visit(a.x, a.y + 1);
					}

					final int w = xmax - xmin + 1, h = ymax - ymin + 1;
					if (w <= 5 || h <= 5) continue;

					if (Math.min(xmin, image.getWidth() - 1 - xmax) < image.getWidth() * 0.03) continue;
					if (Math.min(ymin, image.getHeight() - 1 - ymax) < image.getHeight() * 0.03)
						continue;

					symbols.add(new Rect(xmin, ymin, xmax, ymax, blue));
				}
			}

		Collections.sort(symbols, new Comparator<Rect>() {
			@Override public int compare(final Rect a, final Rect b) {
				if (a.ymax < b.ymin) return -1;
				if (b.ymax < a.ymin) return 1;

				if (a.xmax < b.xmin) return -1;
				if (b.xmax < a.xmin) return 1;
				return 0;
			}
		});

		//		final Rect a = symbols.get(100);
		//		a.color = red;
		//		for (final Rect b : symbols)
		//			if (b != a) {
		//				final double e = error(a, b);
		//				if (e <= 0.2)
		//					b.color = red;
		//				else if (e <= 0.25)
		//					b.color = new int[] { 255, 127, 0 };
		//				else if (e <= 0.3)
		//					b.color = new int[] { 0, 255, 0 };
		//				else if (e <= 0.35) b.color = new int[] { 0, 255, 255 };
		//			}

		Rect prev = symbols.get(0);

		final Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setColor(Color.red);
		final int i = 0;
		for (final Rect rect : symbols) {
			//			for (int x = rect.xmin; x <= rect.xmax; x++)
			//				for (int y = rect.ymin; y <= rect.ymax; y++)
			//					blend(x, y, blue);

			g2.drawLine(prev.xmin, prev.ymax, rect.xmin, rect.ymax);
			//g2.drawString(String.valueOf(++i), rect.xmin, rect.ymin);
			prev = rect;
		}

		System.gc();
		ImageIO.write(image, "PNG", new File("c:/temp/super pamcenje/out.png"));

		System.out.println("symbols: " + symbols.size());

		timer.stop();
		System.out.println(timer);
	}

	static void blend(final int x, final int y, final int[] color) {
		image.getRaster().getPixel(x, y, pixel);
		for (int j = 0; j < 3; j++)
			pixel[j] = (pixel[j] + color[j]) / 2;
		image.getRaster().setPixel(x, y, pixel);
	}

	static void visit(final int x, final int y) {
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return;
		image.getRaster().getPixel(x, y, pixel);
		if (is(black)) {
			image.getRaster().setPixel(x, y, xblack);
			queue.offer(new Vector2i(x, y));
		}
	}

	static boolean is(final int[] color) {
		return pixel[0] == color[0] && pixel[1] == color[1] && pixel[2] == color[2];
	}
}