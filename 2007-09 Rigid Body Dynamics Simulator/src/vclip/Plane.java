///
// Based on the vclip C++ struct Plane
// See the file COPYRIGHT for copyright information.
//

package vclip;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A plane in space.
 *
 * @author Brian Mirtich (C++ version)
 * @author <a href="http://www.cs.ubc.ca/~eddybox">Eddy Boxerman</a>
 * (Java port)
 * @see <a href="{@docRoot}/copyright.html">Copyright information</a>
 */
public class Plane {
	/* Instance Variables */

	protected Vector3dX normal;
	protected double offset;

	static final double DBL_EPSILON = 2.2204460492503131e-16;

	/* Methods */

	/**
	 * Constructs a Plane with zero normal and offset.
	 */
	public Plane() {
		normal = new Vector3dX();
		offset = 0;
	}

	/**
	 * Constructs a Plane based on a normal vector and a point.
	 * 
	 * @param normal normal to the plane. This should be of unit length
	 * if {@link #distance distance} is to work properly
	 * @param point point on the plane
	 */
	public Plane(Vector3d normal, Point3d point) {
		set(normal, point);
	}

	/** 
	 * Sets this plane based on a normal vector and a point.
	 * 
	 * @param normal normal to the plane. This should be of unit length
	 * if {@link #distance distance} is to work properly
	 * @param point point on the plane
	 */
	public final void set(Vector3d normal, Point3d point) {
		this.normal = new Vector3dX(normal);
		offset = -this.normal.dot(point);
	}

	/** 
	 * Sets this plane based on a normal vector and an offset.
	 * The offset is defined
	 * as the negative of the dot product of the plane normal
	 * with any point on the plane.
	 *
	 * @param normal normal to the plane. This should be of unit length
	 * if {@link #distance distance} is to work properly
	 * @param offset offset for the plane
	 */
	public final void set(Vector3d normal, double offset) {
		this.normal = new Vector3dX(normal);
		this.offset = offset;
	}

	/**
	 * Computes a signed distance from a point to this plane,
	 * by taking the dot product of the point and the normal,
	 * and adding the plane offset. A unit length normal
	 * is assumed.
	 *
	 * @param point point to compute the distance from
	 */
	public final double distance(Point3d point) {
		return normal.dot(point) + offset;
	}

	/** 
	 * Returns a string representation of this plane.
	 *
	 * @return string representation
	 */
	public final String toString() {
		return new String("normal: " + normal.toString() + ", offset = " + offset);
	}

	/**
	 * Gets the normal of this plane.
	 *
	 * @return plane normal
	 */
	public Vector3d getNormal() {
		return normal;
	}

	/**
	 * Gets the offset of this plane. The offset is defined
	 * as the negative of the dot product of the plane normal
	 * with any point on the plane.
	 *
	 * @return plane offset
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * Returns a 4x4 matrix which projects points onto
	 * this plane.
	 */
	public void projectionMatrix(Matrix4d P) {
		Vector3d n = normal;
		double mag2 = n.lengthSquared();

		P.m00 = 1 - n.x * n.x / mag2;
		P.m01 = -n.y * n.x / mag2;
		P.m02 = -n.z * n.x / mag2;
		P.m03 = -offset / mag2 * n.x;

		P.m10 = -n.x * n.y / mag2;
		P.m11 = 1 - n.y * n.y / mag2;
		P.m12 = -n.z * n.y / mag2;
		P.m13 = -offset / mag2 * n.y;

		P.m20 = -n.x * n.z / mag2;
		P.m21 = -n.y * n.z / mag2;
		P.m22 = 1 - n.z * n.z / mag2;
		P.m23 = -offset / mag2 * n.z;

		P.m30 = 0;
		P.m31 = 0;
		P.m32 = 0;
		P.m33 = 1;
	}

	/**
	 * Returns a 4x4 matrix which maps points into a
	 * coordinate system with a specified origin and
	 * whose x-y plane is parallel to this plane.
	 */
	public void planeTransform(Matrix4dX P, Point3d origin) {
		Vector3d u = new Vector3d(normal.y, -normal.x, 0);
		double s = u.length();
		if (s > DBL_EPSILON) {
			double ang = Math.atan2(s, normal.z);
			P.setAxisAngle(u.x, u.y, u.z, ang);
		} else if (normal.z > 0) {
			P.setIdentity();
		} else // normal.z < 0
		{
			P.setAxisAngle(0, 1, 0, Math.PI);
		}
		u.set(origin);
		P.transform(u);
		P.m03 = -u.x;
		P.m13 = -u.y;
		P.m23 = -u.z;
	}
}
