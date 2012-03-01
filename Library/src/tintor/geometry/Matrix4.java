package tintor.geometry;

import tintor.patterns.Immutable;
import tintor.util.Hash;

// Partialy based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

@Immutable
public final class Matrix4 {
	// Fields
	public final Vector4 a, b, c, d; // rows

	// Constants
	public final static Matrix4 Zero = new Matrix4(Vector4.Zero, Vector4.Zero, Vector4.Zero, Vector4.Zero);
	public final static Matrix4 Identity = new Matrix4(Vector4.X, Vector4.Y, Vector4.Z, Vector4.W);

	// Constructors
	public Matrix4(Vector4 a, Vector4 b, Vector4 c, Vector4 d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	// Addition
	public Matrix4 add(Matrix4 m) {
		return new Matrix4(a.add(m.a), b.add(m.b), c.add(m.c), d.add(m.d));
	}

	public Matrix4 sub(Matrix4 m) {
		return new Matrix4(a.sub(m.a), b.sub(m.b), c.sub(m.c), d.sub(m.d));
	}

	// Multiplication
	public Matrix4 mul(float v) {
		return new Matrix4(a.mul(v), b.mul(v), c.mul(v), d.mul(v));
	}

	public Vector3 mul(Vector3 v, float w) {
		return new Vector3(a.dot(v, w), b.dot(v, w), c.dot(v, w));
	}

	public Vector4 mul(Vector4 v) {
		return new Vector4(v.dot(a), v.dot(b), v.dot(c), v.dot(d));
	}

	public Matrix4 mul(Matrix4 m) {
		Matrix4 p = m.transpose();
		return new Matrix4(p.mul(a), p.mul(b), p.mul(c), p.mul(d));
	}

//	public Vector3 iapply(Vector3 v) {
//		// ASSUME matrix is orthogonal
//		v = v.sub(new Vector3(a.w, b.w, c.w));
//		return new Vector3(colX().dot(v, 0), colY().dot(v, 0), colZ().dot(v, 0));
//	}
//
//	public Vector3 iapply(Vector3 v) {
//		// ASSUME matrix is orthogonal
//		return new Vector3(colX().dot(v, 0), colY().dot(v, 0), colZ().dot(v, 0));
//	}

	public Matrix4 inv() {
		throw new RuntimeException();
	}

	// Misc
	public Matrix4 transpose() {
		return new Matrix4(colX(), colY(), colZ(), colW());
	}

	public float det() {
		assert false;
		throw new RuntimeException();
	}

	public float trace() {
		return a.x + b.y + c.z + d.w;
	}

	// Fetchers
	public Vector4 colX() {
		return new Vector4(a.x, b.x, c.x, d.x);
	}

	public Vector4 colY() {
		return new Vector4(a.y, b.y, c.y, d.y);
	}

	public Vector4 colZ() {
		return new Vector4(a.z, b.z, c.z, d.z);
	}

	public Vector4 colW() {
		return new Vector4(a.w, b.w, c.w, d.w);
	}

	// Equals
	public boolean equals(Matrix4 o) {
		return this == o ? true : a.equals(o.a) && b.equals(o.b) && c.equals(o.c) && d.equals(o.d);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Matrix4 && equals((Matrix4) o);
	}

	@Override
	public int hashCode() {
		return Hash.hash(Matrix4.class.hashCode(), a.hashCode(), b.hashCode(), c.hashCode(), d.hashCode());
	}
}