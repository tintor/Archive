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

import javax.vecmath.Vector3d;

import tintor.geometry.floatN.Vector3;

public class GMath {
	// Scalar functions
	public static boolean isFinite(final float a) {
		return !Float.isNaN(a) && !Float.isInfinite(a);
	}

	public static boolean isFinite(final double a) {
		return !Double.isNaN(a) && !Double.isInfinite(a);
	}

	public static float floor(final float a) {
		return (float) Math.floor(a);
	}

	public static double floor(final double a) {
		return Math.floor(a);
	}

	public static float ceil(final float a) {
		return (float) Math.ceil(a);
	}

	public static double ceil(final double a) {
		return Math.ceil(a);
	}

	public static float sin(final float a) {
		return (float) Math.sin(a);
	}

	public static double sin(final double a) {
		return Math.sin(a);
	}

	public static float cos(final float a) {
		return (float) Math.cos(a);
	}

	public static double cos(final double a) {
		return Math.cos(a);
	}

	public static float sqrt(final float a) {
		return (float) Math.sqrt(a);
	}

	public static double sqrt(final double a) {
		return Math.sqrt(a);
	}

	public static float acos(final float a) {
		return (float) Math.acos(a);
	}

	public static double acos(final double a) {
		return Math.acos(a);
	}

	public static float atan2(final float y, final float x) {
		return (float) Math.atan2(y, x);
	}

	public static double atan2(final double y, final double x) {
		return Math.atan2(y, x);
	}

	public static float sqr(final float a) {
		return a * a;
	}

	public static double sqr(final double a) {
		return a * a;
	}

	/** (A*R, B*R, C*R) = D */
	public static Vector3 solveLinearRow(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		final float q1 = b.x * d.z - c.x * d.y;
		final float q2 = b.y * d.z - c.y * d.y;
		final float q3 = b.z * d.z - c.z * d.y;
		final float x = a.y * q3 - a.z * q2 + d.x * (b.y * c.z - b.z * c.y);
		final float y = a.x * q3 - a.z * q1 + d.x * (b.x * c.z - b.z * c.x);
		final float z = a.x * q2 - a.y * q1 + d.x * (b.x * c.y - b.y * c.x);
		final float q = 1 / a.mixed(b, c);
		return new Vector3(x * q, y * q, z * q);
	}

	/** (A*R, B*R, C*R) = (0, dy, dz) */
	public static Vector3 solveLinearRow(final Vector3 a, final Vector3 b, final Vector3 c, final float dy, final float dz) {
		final float q1 = b.x * dz - c.x * dy;
		final float q2 = b.y * dz - c.y * dy;
		final float q3 = b.z * dz - c.z * dy;
		final float det = 1 / a.mixed(b, c);
		return new Vector3((a.y * q3 - a.z * q2) * det, (a.x * q3 - a.z * q1) * det, (a.x * q2 - a.y * q1) * det);
	}

	public static Vector3d solveLinearRow(final Vector3d a, final Vector3d b, final Vector3d c, final double dy,
			final double dz) {
		final double q1 = b.x * dz - c.x * dy;
		final double q2 = b.y * dz - c.y * dy;
		final double q3 = b.z * dz - c.z * dy;
		final double det = 1 / a.mixed(b, c);
		return new Vector3d((a.y * q3 - a.z * q2) * det, (a.x * q3 - a.z * q1) * det, (a.x * q2 - a.y * q1) * det);
	}
}