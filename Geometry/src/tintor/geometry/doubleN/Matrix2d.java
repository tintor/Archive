/*
Copyright (C) 2007 Marko Tintor <tintor@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package tintor.geometry.doubleN;

import tintor.util.Hash;

//Based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

public final class Matrix2d {
	// Fields
	public final Vector2d a, b; // rows

	// Constants
	public final static Matrix2d Zero = new Matrix2d(Vector2d.Zero, Vector2d.Zero);
	public final static Matrix2d Identity = new Matrix2d(Vector2d.X, Vector2d.Y);

	// Constructors
	public Matrix2d(final Vector2d a, final Vector2d b) {
		this.a = a;
		this.b = b;
	}

	public Matrix2d(final double ax, final double by) {
		this(ax, 0, 0, by);
	}

	public Matrix2d(final double ax, final double ay, final double bx, final double by) {
		this(new Vector2d(ax, ay), new Vector2d(bx, by));
	}

	// Operations
	public Matrix2d add(final Matrix2d m) {
		return new Matrix2d(a.add(m.a), b.add(m.b));
	}

	public Matrix2d add(final Matrix2d m, final double i) {
		return new Matrix2d(a.add(i, m.a), b.add(i, m.b));
	}

	public Matrix2d sub(final Matrix2d m) {
		return new Matrix2d(a.sub(m.a), b.sub(m.b));
	}

	public Matrix2d mul(final double v) {
		return new Matrix2d(a.mul(v), b.mul(v));
	}

	public Vector2d mul(final Vector2d v) {
		if (this == Identity) return v;
		return new Vector2d(a.dot(v), b.dot(v));
	}

	public Matrix2d mul(final Matrix2d m) {
		if (m == Identity) return this;
		if (this == Identity) return m;
		return new Matrix2d(a.mul(m), b.mul(m));
	}

	public Matrix2d similar(final Matrix2d m) {
		return m.mul(this).mul(m.transpose());
	}

	public Matrix2d transpose() {
		return this == Identity ? Identity : new Matrix2d(colX(), colY());
	}

	public double det() {
		return a.x * b.y - a.y * b.x;
	}

	public double trace() {
		return a.x + b.y;
	}

	// Fetchers
	public Vector2d colX() {
		return new Vector2d(a.x, b.x);
	}

	public Vector2d colY() {
		return new Vector2d(a.y, b.y);
	}

	// Equals
	public boolean equals(final Matrix2d o) {
		return this == o ? true : o != null && a.equals(o.a) && b.equals(o.b);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Matrix2d && equals((Matrix2d) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Matrix2d.class.hashCode(), a.hashCode(), b.hashCode());
	}

	@Override public String toString() {
		return String.format("[%f %f / %f %f]", a.x, a.y, b.x, b.y);
	}
}