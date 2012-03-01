package tintor.geometry;

import tintor.patterns.Immutable;
import tintor.util.Hash;

@Immutable public final class Vector4 {
	// Fields
	public final float x, y, z, w;

	// Constants
	public final static Vector4 Zero = new Vector4(0, 0, 0, 0);
	public final static Vector4 X = new Vector4(1, 0, 0, 0);
	public final static Vector4 Y = new Vector4(0, 1, 0, 0);
	public final static Vector4 Z = new Vector4(0, 0, 1, 0);
	public final static Vector4 W = new Vector4(0, 0, 0, 1);

	// Constructors
	public Vector4(final float x, final float y, final float z, final float w) {
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

	public Vector4 add(final Vector4 a, final float b) {
		return new Vector4(x + a.x * b, y + a.y * b, z + a.z * b, w + a.w * b);
	}

	public Vector4 sub(final Vector4 a, final float b) {
		return new Vector4(x - a.x * b, y - a.y * b, z - a.z * b, w - a.w * b);
	}

	public Vector4 neg() {
		return new Vector4(-x, -y, -z, -w);
	}

	public Vector4 mul(final float a) {
		return new Vector4(x * a, y * a, z * a, w * a);
	}

	public Vector4 div(final float a) {
		return mul(1 / a);
	}

	public float dot(final Vector4 a) {
		return x * a.x + y * a.y + z * a.z + w * a.w;
	}

	public Vector4 unit() {
		return div(length());
	}

	public float quad() {
		return x * x + y * y + z * z + w * w;
	}

	public float quad(final Vector4 a) {
		return sub(a).quad();
	}

	public float length() {
		return GMath.sqrt(quad());
	}

	// Unique
	public float dot(final Vector3 a, final float aw) {
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

	public boolean equals(final Vector4 a, final float eps) {
		return quad(a) <= eps * eps;
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Vector4 && equals((Vector4) o);
	}

	@Override public int hashCode() {
		return Hash.hash(Vector4.class.hashCode(), Hash.hash(x), Hash.hash(y), Hash.hash(z), Hash.hash(w));
	}
}