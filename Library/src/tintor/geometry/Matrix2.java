package tintor.geometry;

import tintor.patterns.Immutable;
import tintor.util.Hash;

//Based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

@Immutable public final class Matrix2 {
	// Fields
	public final Vector2 a, b; // rows

	// Constants
	public final static Matrix2 Zero = new Matrix2(Vector2.Zero, Vector2.Zero);
	public final static Matrix2 Identity = new Matrix2(Vector2.X, Vector2.Y);

	// Constructors
	public Matrix2(final Vector2 a, final Vector2 b) {
		this.a = a;
		this.b = b;
	}

	public Matrix2(final float ax, final float by) {
		this(ax, 0, 0, by);
	}

	public Matrix2(final float ax, final float ay, final float bx, final float by) {
		this(new Vector2(ax, ay), new Vector2(bx, by));
	}

	// Operations
	public Matrix2 add(final Matrix2 m) {
		return new Matrix2(a.add(m.a), b.add(m.b));
	}

	public Matrix2 add(final Matrix2 m, final float i) {
		return new Matrix2(a.add(i, m.a), b.add(i, m.b));
	}

	public Matrix2 sub(final Matrix2 m) {
		return new Matrix2(a.sub(m.a), b.sub(m.b));
	}

	public Matrix2 mul(final float v) {
		return new Matrix2(a.mul(v), b.mul(v));
	}

	public Vector2 mul(final Vector2 v) {
		if (this == Identity) return v;
		return new Vector2(a.dot(v), b.dot(v));
	}

	public Matrix2 mul(final Matrix2 m) {
		if (m == Identity) return this;
		if (this == Identity) return m;
		return new Matrix2(a.mul(m), b.mul(m));
	}

	public Matrix2 similar(final Matrix2 m) {
		return m.mul(this).mul(m.transpose());
	}

	public Matrix2 transpose() {
		return this == Identity ? Identity : new Matrix2(colX(), colY());
	}

	public float det() {
		return a.x * b.y - a.y * b.x;
	}

	public float trace() {
		return a.x + b.y;
	}

	// Fetchers
	public Vector2 colX() {
		return new Vector2(a.x, b.x);
	}

	public Vector2 colY() {
		return new Vector2(a.y, b.y);
	}

	// Equals
	public boolean equals(final Matrix2 o) {
		return this == o ? true : o != null && a.equals(o.a) && b.equals(o.b);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Matrix2 && equals((Matrix2) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Matrix2.class.hashCode(), a.hashCode(), b.hashCode());
	}

	@Override public String toString() {
		return String.format("[%f %f / %f %f]", a.x, a.y, b.x, b.y);
	}
}