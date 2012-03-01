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

public final class Transform3 {
	// Fields
	public final Matrix3 m; // Matrix must be Rotational!
	public final Vector3 v;
	private final Vector3 iv; // = -v*M

	// Constants
	public final static Transform3 Identity = new Transform3(Matrix3.Identity, Vector3.Zero);

	// Factory Methods
	public static Transform3 translate(final Vector3 a) {
		return new Transform3(Matrix3.Identity, a);
	}

	public static Transform3 rotate(final Vector3 axis, final float angle) {
		return new Transform3(Quaternion.make(axis, angle), Vector3.Zero);
	}

	// Constructors
	public Transform3(final Quaternion q, final Vector3 v) {
		this(q.matrix(), v);
	}

	/** ASSUME m is rotational! */
	public Transform3(final Matrix3 m, final Vector3 v) {
		this.m = m;
		this.v = v;
		iv = new Vector3(-v.x, -v.y, -v.z).mul(m);
	}

	// Direct Transformations
	public Vector3 applyP(final Vector3 p) {
		return v.add(m, p);
	}

	public Vector3 applyV(final Vector3 p) {
		return m.mul(p);
	}

	public Plane3 apply(final Plane3 p) {
		final Vector3 n = applyV(p.normal).unit();
		return new Plane3(n, p.offset - v.dot(n));
	}

	public Ray3 apply(final Ray3 p) {
		return new Ray3(applyP(p.origin), applyV(p.dir));
	}

	public Line3 apply(final Line3 p) {
		return new Line3(applyP(p.a), applyP(p.b));
	}

	// Inverse Transformations
	public Vector3 iapplyP(final Vector3 p) {
		return iv.add(p, m);
	}

	public Vector3 iapplyV(final Vector3 p) {
		return p.mul(m);
	}

	public Plane3 iapply(final Plane3 p) {
		return new Plane3(iapplyV(p.normal).unit(), p.offset + v.dot(p.normal));
	}

	public Ray3 iapply(final Ray3 p) {
		return new Ray3(iapplyP(p.origin), iapplyV(p.dir));
	}

	public Line3 iapply(final Line3 p) {
		return new Line3(p.a.mul(m).add(iv), p.b.mul(m).add(iv));
	}

	// Misc
	public float[] columnMajorArray() {
		return new float[] { m.a.x, m.b.x, m.c.x, 0, m.a.y, m.b.y, m.c.y, 0, m.a.z, m.b.z, m.c.z, 0, v.x, v.y, v.z, 1 };
	}

	public Transform3 combine(final Transform3 a) {
		return new Transform3(a.m.mul(m), a.m.mul(v).add(a.v));
	}

	public Transform3 icombine(final Transform3 a) {
		return new Transform3(a.m.transpose().mul(m), v.sub(a.v).mul(a.m));
	}

	public Transform3 inverse() {
		// for rotational matrices, inverse(M) = transpose(M)
		// inv(M,V) = (invM, -invM*V) = (transposeM, -V*M)
		return new Transform3(m.transpose(), iv);
	}

	// From Object
	public boolean equals(final Transform3 o) {
		return this == o ? true : m.equals(o.m) && v.equals(o.v);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Transform3 && equals((Transform3) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Transform3.class.hashCode(), m.hashCode(), v.hashCode());
	}
}