package tintor.puzzle;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

abstract class Cutting {
	protected double width, height;

	final int angles;

	Cutting(final int angles) {
		this.angles = angles;
	}

	abstract List<Piece> cut(final int columns, final int rows, final double width, Vector2 offset);

	abstract Vector2i cannon(Vector2 a);

	abstract Vector2 uncannon(Vector2i a);

	static Path2D createRegularShape(final int edges, final double scale, final double angle) {
		final Path2D.Double path = new Path2D.Double();

		path.moveTo(Math.cos(angle) * scale, Math.sin(angle) * scale);
		for (int i = 1; i < edges; i++) {
			final double a = 2 * Math.PI / edges * i + angle;
			path.lineTo(Math.cos(a) * scale, Math.sin(a) * scale);
		}
		path.closePath();

		return path;
	}
}

class Squares extends Cutting {
	Squares() {
		super(4);
	}

	@Override List<Piece> cut(final int columns, final int rows, final double width, final Vector2 offset) {
		final List<Piece> list = new ArrayList<Piece>();
		this.width = width;
		height = width;

		final Random rand = new Random();
		final Shape shape = new Rectangle2D.Double(-width * 0.5, -height * 0.5, width, height);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++) {
				final Piece p = new Piece(new Vector2i(column, row));
				list.add(p);

				//p.angle = rand.nextInt(4);
				p.shape = shape;
				p.cannon = p.id;
				p.position = uncannon(p.id);
				p.texture = new Vector2i(p.position.add(offset));
			}
		return list;
	}

	@Override Vector2i cannon(final Vector2 a) {
		return new Vector2i(a.x / width - 0.5, a.y / height - 0.5);
	}

	@Override Vector2 uncannon(final Vector2i a) {
		return new Vector2((a.x + 0.5) * width, (a.y + 0.5) * height);
	}
}

class Hexagons extends Cutting {
	Hexagons() {
		super(6);
	}

	@Override List<Piece> cut(final int columns, final int rows, final double width, final Vector2 offset) {
		final List<Piece> list = new ArrayList<Piece>();
		this.width = width;
		height = width * Math.sqrt(3) / 2;

		final Random rand = new Random();
		final Shape shape = createRegularShape(6, width / 2, 0);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++) {
				if ((column & 1) == (row & 1)) continue;
				final Piece p = new Piece(new Vector2i(column, row));
				list.add(p);

				//p.angle = rand.nextInt(6);
				p.shape = shape;
				p.cannon = p.id;
				p.position = uncannon(p.id);

				p.texture = new Vector2i(p.position.add(offset));
			}
		return list;
	}

	@Override Vector2i cannon(final Vector2 a) {
		return new Vector2i((a.x / width - 0.5) / 0.75, a.y / height * 2 - 1);
	}

	@Override Vector2 uncannon(final Vector2i a) {
		return new Vector2(width * (a.x * 0.75 + 0.5), (a.y + 1) * height * 0.5);
	}
}

class Triangles extends Cutting {
	Triangles() {
		super(6);
	}

	@Override List<Piece> cut(final int columns, final int rows, final double width, final Vector2 offset) {
		final List<Piece> list = new ArrayList<Piece>();
		this.width = width;
		height = width / Math.sqrt(3) * 2;

		final Random rand = new Random();
		final Shape shapeRight = createRegularShape(3, width * 2.0 / 3, 0);
		final Shape shapeLeft = createRegularShape(3, width * 2.0 / 3, Math.PI);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++) {
				final Piece p = new Piece(new Vector2i(column, row));
				list.add(p);

				final boolean left = (column & 1) == (row & 1);
				p.shape = left ? shapeLeft : shapeRight;

				//p.angle = rand.nextInt(6);
				p.cannon = p.id;
				p.position = uncannon(p.id);
				p.texture = new Vector2i(p.position.add(offset));
			}
		return list;
	}

	@Override Vector2i cannon(final Vector2 a) {
		return new Vector2i(a.x / width - 0.5, a.y / height * 2 - 1);
	}

	@Override Vector2 uncannon(final Vector2i a) {
		final boolean left = (a.x & 1) == (a.y & 1);
		final double x = (a.x + 0.5) * width + (left ? width / 6 : -width / 6);
		final double y = (a.y + 1) * height * 0.5;
		return new Vector2(x, y);
	}
}
