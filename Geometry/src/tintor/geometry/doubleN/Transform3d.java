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

public final class Transform3d {
	// Fields
	public final Matrix3 m; // Matrix must be Rotational!
	public final Vector3d v;
	private final Vector3d iv; // = -v*M

	// Constants
	public final static Transform3d Identity = new Transform3d(Matrix3.Identity, Vector3d.Zero);

	// Factory Methods
	public static Transform3d translate(final Vector3d a) {
		return new Transform3d(Matrix3.Identity, a);
	}

	public static Transform3d rotate(final Vector3d axis, final double angle) {
		return new Transform3d(Quaterniond.make(axis, angle), Vector3d.Zero);
	}

	// Constructors
	public Transform3d(final Quaterniond q, final Vector3d v) {
		this(q.matrix(), v);
	}

	/** ASSUME m is rotational! */
	public Transform3d(final Matrix3 m, final Vector3d v) {
		this.m = m;
		this.v = v;
		iv = new Vector3d(-v.x, -v.y, -v.z).mul(m);
	}

	// Direct Transformations
	public Vector3d applyP(final Vector3d p) {
		return v.add(m, p);
	}

	public Vector3d applyV(final Vector3d p) {
		return m.mul(p);
	}

	public Plane3d apply(final Plane3d p) {
		final Vector3d n = applyV(p.normal).unit();
		return new Plane3d(n, p.offset - v.dot(n));
	}

	public Ray3d apply(final Ray3d p) {
		return new Ray3d(applyP(p.origin), applyV(p.dir));
	}

	public Line3 apply(final Line3 p) {
		return new Line3(applyP(p.a), applyP(p.b));
	}

	// Inverse Transformations
	public Vector3d iapplyP(final Vector3d p) {
		return iv.add(p, m);
	}

	public Vector3d iapplyV(final Vector3d p) {
		return p.mul(m);
	}

	public Plane3d iapply(final Plane3d p) {
		return new Plane3d(iapplyV(p.normal).unit(), p.offset + v.dot(p.normal));
	}

	public Ray3d iapply(final Ray3d p) {
		return new Ray3d(iapplyP(p.origin), iapplyV(p.dir));
	}

	public Line3 iapply(final Line3 p) {
		return new Line3(p.a.mul(m).add(iv), p.b.mul(m).add(iv));
	}

	// Misc
	public double[] columnMajorArray() {
		return new double[] { m.a.x, m.b.x, m.c.x, 0, m.a.y, m.b.y, m.c.y, 0, m.a.z, m.b.z, m.c.z, 0, v.x, v.y, v.z, 1 };
	}

	public Transform3d combine(final Transform3d a) {
		return new Transform3d(a.m.mul(m), a.m.mul(v).add(a.v));
	}

	public Transform3d icombine(final Transform3d a) {
		return new Transform3d(a.m.transpose().mul(m), v.sub(a.v).mul(a.m));
	}

	public Transform3d inverse() {
		// for rotational matrices, inverse(M) = transpose(M)
		// inv(M,V) = (invM, -invM*V) = (transposeM, -V*M)
		return new Transform3d(m.transpose(), iv);
	}

	// From Object
	public boolean equals(final Transform3d o) {
		return this == o ? true : m.equals(o.m) && v.equals(o.v);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Transform3d && equals((Transform3d) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Transform3d.class.hashCode(), m.hashCode(), v.hashCode());
	}
}