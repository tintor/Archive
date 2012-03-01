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
package tintor.geometry;

import tintor.util.Hash;

public final class Vector4 {
	// Fields
	public final double x, y, z, w;

	// Constants
	public final static Vector4 Zero = new Vector4(0, 0, 0, 0);
	public final static Vector4 X = new Vector4(1, 0, 0, 0);
	public final static Vector4 Y = new Vector4(0, 1, 0, 0);
	public final static Vector4 Z = new Vector4(0, 0, 1, 0);
	public final static Vector4 W = new Vector4(0, 0, 0, 1);

	// Constructors
	public Vector4(final double x, final double y, final double z, final double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	// Basic Operations
	public Vector4 add(final Vector4 a) {
		return new Vector4(x + a.x, y + a.y, z + a.z, w + a.w);
	}

	public Vector4 sub(final Vector4 a) {
		return new Vector4(x - a.x, y - a.y, z - a.z, w - a.w);
	}

	public Vector4 add(final Vector4 a, final double b) {
		return new Vector4(x + a.x * b, y + a.y * b, z + a.z * b, w + a.w * b);
	}

	public Vector4 sub(final Vector4 a, final double b) {
		return new Vector4(x - a.x * b, y - a.y * b, z - a.z * b, w - a.w * b);
	}

	public Vector4 neg() {
		return new Vector4(-x, -y, -z, -w);
	}

	public Vector4 mul(final double a) {
		return new Vector4(x * a, y * a, z * a, w * a);
	}

	public Vector4 div(final double a) {
		return mul(1 / a);
	}

	public double dot(final Vector4 a) {
		return x * a.x + y * a.y + z * a.z + w * a.w;
	}

	public Vector4 unit() {
		return div(length());
	}

	public double quad() {
		return x * x + y * y + z * z + w * w;
	}

	public double quad(final Vector4 a) {
		return sub(a).quad();
	}

	public double length() {
		return Math.sqrt(quad());
	}

	// Unique
	public double dot(final Vector3 a, final double aw) {
		return x * a.x + y * a.y + z * a.z + w * aw;
	}

	public Vector3 toVector3() {
		return new Vector3(x, y, z);
	}

	// from Object
	@Override public String toString() {
		return x + "," + y + "," + z + "," + w;
	}

	public boolean equals(final Vector4 a) {
		return this == a ? true : equals(a, Side.eps);
	}

	public boolean equals(final Vector4 a, final double ε) {
		return quad(a) <= ε * ε;
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Vector4 && equals((Vector4) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Vector4.class.hashCode(), Hash.hash(x), Hash.hash(y), Hash.hash(z), Hash.hash(w));
	}
}