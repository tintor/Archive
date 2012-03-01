package scrap;

import geometry.base.Vector3;
import geometry.base.Vector4;

//Based on Matrix and Quaternion FAQ, http://mccammon.ucsd.edu/~adcock/matrixfaq.html

@util.Immutable
public final class Matrix43 {
	// Fields
	public final Vector4 a, b, c; // rows

	// Constants
	public final static Matrix43 Identity = new Matrix43(Vector4.X, Vector4.Y, Vector4.Z);

	// Factory Methods
	public static Matrix43 translate(Vector3 a) {
		return new Matrix43(new Vector4(1,0,0,a.x), new Vector4(0,1,0,a.y), new Vector4(0,0,1,a.z));
	}

	public static Matrix43 scale(double a) {
		return new Matrix43(new Vector4(a,0,0,0), new Vector4(0,a,0,0), new Vector4(0,0,a,0));
	}

	public static Matrix43 rotate(Vector3 axis, double angle) {
		return null;
		//return Quaternion.axisAngle(axis, angle).matrix();
	}

	// Constructors
	public Matrix43(Vector4 a, Vector4 b, Vector4 c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	// Operations
	public Vector3 mul(Vector3 v) {
		return new Vector3(a.dot(v), b.dot(v), c.dot(v));
	}
}