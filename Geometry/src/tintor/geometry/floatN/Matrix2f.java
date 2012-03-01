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
package tintor.geometry.floatN;

import tintor.util.Hash;

//Based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

public final class Matrix2f {
	// Fields
	public final Vector2 a, b; // rows

	// Constants
	public final static Matrix2f Zero = new Matrix2f(Vector2.Zero, Vector2.Zero);
	public final static Matrix2f Identity = new Matrix2f(Vector2.X, Vector2.Y);

	// Constructors
	public Matrix2f(final Vector2 a, final Vector2 b) {
		this.a = a;
		this.b = b;
	}

	public Matrix2f(final float ax, final float by) {
		this(ax, 0, 0, by);
	}

	public Matrix2f(final float ax, final float ay, final float bx, final float by) {
		this(new Vector2(ax, ay), new Vector2(bx, by));
	}

	// Operations
	public Matrix2f add(final Matrix2f m) {
		return new Matrix2f(a.add(m.a), b.add(m.b));
	}

	public Matrix2f add(final Matrix2f m, final float i) {
		return new Matrix2f(a.add(i, m.a), b.add(i, m.b));
	}

	public Matrix2f sub(final Matrix2f m) {
		return new Matrix2f(a.sub(m.a), b.sub(m.b));
	}

	public Matrix2f mul(final float v) {
		return new Matrix2f(a.mul(v), b.mul(v));
	}

	public Vector2 mul(final Vector2 v) {
		if (this == Identity) return v;
		return new Vector2(a.dot(v), b.dot(v));
	}

	public Matrix2f mul(final Matrix2f m) {
		if (m == Identity) return this;
		if (this == Identity) return m;
		return new Matrix2f(a.mul(m), b.mul(m));
	}

	public Matrix2f similar(final Matrix2f m) {
		return m.mul(this).mul(m.transpose());
	}

	public Matrix2f transpose() {
		return this == Identity ? Identity : new Matrix2f(colX(), colY());
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
	public boolean equals(final Matrix2f o) {
		return this == o ? true : o != null && a.equals(o.a) && b.equals(o.b);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Matrix2f && equals((Matrix2f) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Matrix2f.class.hashCode(), a.hashCode(), b.hashCode());
	}

	@Override public String toString() {
		return String.format("[%f %f / %f %f]", a.x, a.y, b.x, b.y);
	}
}