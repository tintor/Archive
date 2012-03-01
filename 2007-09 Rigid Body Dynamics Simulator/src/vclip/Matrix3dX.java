package vclip;

import java.text.NumberFormat;

import javax.vecmath.Matrix3d;

// import acme.misc.PrintfFormat;

/**
 * Implements some useful extensions to the class Matrix3d.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>
 */
class Matrix3dX extends Matrix3d {
	/**
	 * Precision of a double.
	 */
	static private final double DBL_EPSILON = 2.2204460492503131e-16;

	public Matrix3dX(Matrix3d m) {
		super(m);
	}

	public Matrix3dX() {
		super();
	}

	public Matrix3dX(double[] v) {
		super(v);
	}

	private Vector3dX m0;
	private Vector3dX m1;
	private Vector3dX m2;

	private Vector3dX u0;
	private Vector3dX u1;
	private Vector3dX u2;

	private Vector3dX vx;

	private void allocateSVDWorkSpace() {
		m0 = new Vector3dX();
		m1 = new Vector3dX();
		m2 = new Vector3dX();

		u0 = new Vector3dX();
		u1 = new Vector3dX();
		u2 = new Vector3dX();

		vx = new Vector3dX();
	}

	/**
	 * Compute eigenvalues of this matrix, assuming that
	 * it is symmetric (by using only the upper triangular
	 * components). The idea is to compute these fast,
	 * using cube roots, at perhaps the expense of some
	 * accuracy.
	 *
	 * @param eig Resulting eigenvalues
	 */
	public void symmetricEigenValues(Vector3dX eig) {
		// set up the coefficients of the cubic:
		double m01sqr = m01 * m01;
		double m02sqr = m02 * m02;
		double m12sqr = m12 * m12;

		double a1 = -(m00 + m11 + m22);
		double a2 = m00 * m22 + m11 * m22 + m00 * m11 - m01sqr - m02sqr - m12sqr;
		double a3 = m00 * m12sqr + m11 * m02sqr + m22 * m01sqr - 2 * m01 * m02 * m12 - m00 * m11 * m22;
		double q = (a1 * a1 - 3 * a2) / 9;

		if (q < 0) // q could be slightly negative due to round off 
		{
			q = 0;
		}
		double r = ((2 * a1 * a1 - 9 * a2) * a1 + 27 * a3) / 54;
		double q3rd = q * q * q;
		double theta;

		if (q3rd - r * r <= 0) { // then just assume r^2 == q^3
			theta = (r < 0 ? Math.PI : 0);
		} else {
			double arg = r / Math.sqrt(q3rd);
			if (arg > 1.0) {
				arg = 1.0;
			} else if (arg < -1.0) {
				arg = -1.0;
			}
			theta = Math.acos(arg);
		}
		double s = -2 * Math.sqrt(q);
		eig.x = s * Math.cos(theta / 3) - a1 / 3;
		eig.y = s * Math.cos((theta + 2 * Math.PI) / 3) - a1 / 3;
		eig.z = s * Math.cos((theta + 4 * Math.PI) / 3) - a1 / 3;
	}

	private void perpVector(Vector3dX vr, Vector3dX v0) {
		// find a vector perpendicular to v0
		int i = v0.minAbsIndex();
		if (i == 0) {
			vr.x = 0;
			vr.y = v0.z;
			vr.z = -v0.y;
		} else if (i == 1) {
			vr.x = -v0.z;
			vr.y = 0;
			vr.z = v0.x;
		} else {
			vr.x = v0.y;
			vr.y = -v0.x;
			vr.z = 0;
		}
		vr.normalize();
	}

	private void bestPerpVector(Vector3dX vr, Vector3dX v0, Vector3dX v1, Vector3dX v2, double eps) {
		vr.cross(v0, v1);
		vx.cross(v0, v2);
		if (vx.lengthSquared() > vr.lengthSquared()) {
			vr.set(vx);
		}
		if (vr.lengthSquared() <= eps * eps) { // then the subspace has dimension 1, and we just find
			// a good vector perp. to v0
			perpVector(vr, v0);
		} else {
			vr.normalize();
		}
	}

	/**
	 * Computes the singular value decomposition of the matrix, U
	 * S V', under the assumption that it is symmetric (only the
	 * upper triangular elements are referenced). This routine
	 * works by solving a cubic equation to obtain the
	 * eigenvalues, and so is around 5 times faster than the
	 * corresponding routine in <code>javax.vecmath</code>, though
	 * possibly at the expense of some precision.
	 * 
	 * @param U if non-null, used to store the first orthogonal 
	 * matrix in the decomposition.
	 * @param sig required parameter, used to store the 3 singular values
	 * of the matrix, sorted from largest to smallest.
	 * @param V if non-null, used to store the second orthogonal
	 * matrix in the decomposition.
	 */
	public void symmetricSVD(Matrix3d U, Vector3dX sig, Matrix3d V) {
		if (m0 == null) {
			allocateSVDWorkSpace();
		}

		// Get the eigenvalues of the matrix ...
		symmetricEigenValues(sig);

		//	   System.out.println ("eig=" + sig);

		// If we only want the singular values, we are done.
		if (U == null && V == null) {
			sig.sortAbsolute();
			sig.absolute();
			return;
		}

		// Given the eigenvalues, we now need to solve for the
		// eigenvectors. The eigenvector associated with the
		// most distinct eigenvalue should be the best-conditioned
		// one to solve for, so we start with that.

		// sort the eigenvalues values from largest to smallest
		sig.sort();

		// set lam0 to be the most distinct eigenvalue, and
		// set lam1 to be the farthest eigenvalue from lam0
		double lam0, lam1, lam2;
		if ((sig.x - sig.y) >= (sig.y - sig.z)) { // first eigenvalue is most distinct
			lam0 = sig.x;
			lam1 = sig.z;
		} else { // last eigenvalue is most distinct
			lam0 = sig.z;
			lam1 = sig.x;
		}

		double epsilon = 3 * DBL_EPSILON * 1.01 * sig.norm1();

		// set [ m0 m1 m2 ] = (M - lam0 I).
		m0.x = m00 - lam0;
		m1.x = m01;
		m2.x = m02;
		m0.y = m01;
		m1.y = m11 - lam0;
		m2.y = m12;
		m0.z = m02;
		m1.z = m12;
		m2.z = m22 - lam0;

		// [ m0 m1 m2 ] should be singular, and so should define
		// a subspace of dimension 2 or less. Any vector perpendicular
		// to this subspace is an eigenvector associated with lam0.
		// To find a perpendicular, we start with the column that
		// has the largest norm, and then examine the cross products
		// with the adjacent columns.
		double max, lenSqr;
		Vector3dX mx = m0;
		max = m0.lengthSquared();
		if ((lenSqr = m1.lengthSquared()) > max) {
			max = lenSqr;
			mx = m1;
		}
		if ((lenSqr = m2.lengthSquared()) > max) {
			max = lenSqr;
			mx = m2;
		}
		if (max <= epsilon * epsilon) { // then the subspace has dimension 0 and any vector will do
			u0.set(1, 0, 0);
		} else {
			if (mx == m0) {
				bestPerpVector(u0, m0, m1, m2, epsilon);
			} else if (mx == m1) {
				bestPerpVector(u0, m1, m0, m2, epsilon);
			} else // mx == m2
			{
				bestPerpVector(u0, m2, m0, m1, epsilon);
			}
		}

		//	   System.out.println ("u0=" + u0);
		// Given the first eigenvector u0, we construct vectors u1 
		// and u2 to be mutually perpendicular to each other
		// and to u0. These will then be rotated about u0
		// to find the correct eigenvectors.
		perpVector(u2, u0);
		u1.cross(u2, u0);

		//	   System.out.println ("u1=" + u1);
		//	   System.out.println ("u2=" + u2);

		// Now, we have 
		//           T                 [ lam0   0   0  ]
		// [u0 u1 u2]  M [u0 u1 u2] =  [  0    a00 a01 ]
		//                             [  0    a10 a11 ]
		// 
		// and so we wish to diagonalize the lower 2x2 submatrix using
		//        T
		// [ c s ]  [a00 a01] [ c s ]
		// [-s c ]  [a10 a11] [-s c ]
		//
		double a00, a01, a11;
		double tau, t, c, s;

		// compute m1 = M u1
		m1.x = m00 * u1.x + m01 * u1.y + m02 * u1.z;
		m1.y = m10 * u1.x + m11 * u1.y + m12 * u1.z;
		m1.z = m20 * u1.x + m21 * u1.y + m22 * u1.z;

		// compute m2 = M u2
		m2.x = m00 * u2.x + m01 * u2.y + m02 * u2.z;
		m2.y = m10 * u2.x + m11 * u2.y + m12 * u2.z;
		m2.z = m20 * u2.x + m21 * u2.y + m22 * u2.z;

		a00 = u1.dot(m1);
		a01 = u1.dot(m2);
		a11 = u2.dot(m2);

		//	   System.out.println (a00 + " " + a01 + " " + a11);

		if (Math.abs(a01) <= epsilon) {
			c = 1;
			s = 0;
		} else {
			tau = (a11 - a00) / (2 * a01);
			//	      System.out.println ("tau=" + tau);
			t = 1 / (Math.abs(tau) + Math.sqrt(1 + tau * tau));
			if (tau < 0) {
				t = -t;
			}
			c = 1 / Math.sqrt(1 + t * t);
			s = t * c;
		}
		//	   System.out.println ("c=" + c + " s=" + s);

		// Compute the corresponding eigenvectors m1 and m2,
		// and recompute the corresponding eigenvalues

		m1.combine(c, u1, -s, u2);
		m2.combine(s, u1, c, u2);
		lam1 = c * c * a00 - 2 * s * c * a01 + s * s * a11;
		lam2 = c * c * a11 + 2 * s * c * a01 + s * s * a00;
		//	   lam1 = c*c*a00 - s*s*a11;
		//	   lam2 = c*c*a11 - s*s*a00;
		//	   System.out.println ("lam1=" + lam1 + " lam2=" + lam2);
		m0.set(u0);

		// The eigenvectors corresponding to
		// lam0, lam1, lam2 are now m0, m1, m2
		// 
		// Resort these in order in descreasing absolute value

		Vector3dX[] vecs = { m0, m1, m2 };
		double[] lams = { lam0, lam1, lam2 };

		for (int i = 0; i < 2; i++) {
			for (int j = i + 1; j < 3; j++) {
				if (Math.abs(lams[j]) > Math.abs(lams[i])) {
					double l = lams[j];
					lams[j] = lams[i];
					lams[i] = l;
					Vector3dX v = vecs[j];
					vecs[j] = vecs[i];
					vecs[i] = v;
				}
			}
		}
		if (U != null) {
			U.m00 = vecs[0].x;
			U.m10 = vecs[0].y;
			U.m20 = vecs[0].z;
			U.m01 = vecs[1].x;
			U.m11 = vecs[1].y;
			U.m21 = vecs[1].z;
			U.m02 = vecs[2].x;
			U.m12 = vecs[2].y;
			U.m22 = vecs[2].z;
		}

		// Take the absolute value of the eigenvectors to
		// get the singular values. The columns of V
		// are equal to the columns U, except that they
		// are negated if the corresponding eigenvalue is
		// negative.

		for (int i = 0; i < 3; i++) {
			if (lams[i] < 0) {
				lams[i] = -lams[i];
				vecs[i].negate();
			}
		}
		sig.set(lams[0], lams[1], lams[2]);
		if (V != null) {
			V.m00 = vecs[0].x;
			V.m10 = vecs[0].y;
			V.m20 = vecs[0].z;
			V.m01 = vecs[1].x;
			V.m11 = vecs[1].y;
			V.m21 = vecs[1].z;
			V.m02 = vecs[2].x;
			V.m12 = vecs[2].y;
			V.m22 = vecs[2].z;
		}
	}

	public String toString(NumberFormat fmt) {
		String s = "";
		s += fmt.format(m00) + " " + fmt.format(m01) + " " + fmt.format(m02);
		s += "\n";
		s += fmt.format(m10) + " " + fmt.format(m11) + " " + fmt.format(m12);
		s += "\n";
		s += fmt.format(m20) + " " + fmt.format(m21) + " " + fmt.format(m22);
		return s;
	}

	public String toString(String fmtStr) {
		return toString(new FloatFormat(fmtStr));
	}
}
