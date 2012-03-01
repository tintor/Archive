package scrap;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.NumberFormat;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Implements some useful extensions to the class Matrix4d.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>
 */
@SuppressWarnings("serial")
class Matrix4dX extends Matrix4d implements java.io.Serializable {

	private Matrix4dX Tmp;

	public Matrix4dX() {
		super();
		setIdentity();
	}

	public Matrix4dX(double[] v) {
		super(v);
	}

	public Matrix4dX(Matrix4d m1) {
		super(m1);
	}

	public void invertTrans() {
		invertTrans(this);
	}

	final public void setTrans(Matrix4d m1) {
		m00 = m1.m00;
		m01 = m1.m01;
		m02 = m1.m02;
		m03 = m1.m03;
		m10 = m1.m10;
		m11 = m1.m11;
		m12 = m1.m12;
		m13 = m1.m13;
		m20 = m1.m20;
		m21 = m1.m21;
		m22 = m1.m22;
		m23 = m1.m23;
	}

	static public final double EPSILON = 1e-14;

	public void invertTrans(Matrix4d m1) {
		double px, py, pz;
		double ox, ax, ay;

		px = -(m1.m03 * m1.m00 + m1.m13 * m1.m10 + m1.m23 * m1.m20);
		py = -(m1.m03 * m1.m01 + m1.m13 * m1.m11 + m1.m23 * m1.m21);
		pz = -(m1.m03 * m1.m02 + m1.m13 * m1.m12 + m1.m23 * m1.m22);

		ox = m1.m01;
		ax = m1.m02;
		ay = m1.m12;

		m03 = px;
		m13 = py;
		m23 = pz;

		m00 = m1.m00;
		m11 = m1.m11;
		m22 = m1.m22;

		m01 = m1.m10;
		m02 = m1.m20;
		m12 = m1.m21;

		m10 = ox;
		m20 = ax;
		m21 = ay;

		m30 = 0;
		m31 = 0;
		m32 = 0;
		m33 = 1;
	}

	public final void mulTrans(Matrix4d m1, Matrix4d m2) {
		double nx, ox, ax;
		double ny, oy, ay;
		double nz, oz, az;
		double px, py, pz;

		nx = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20;
		ox = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21;
		ax = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22;
		px = m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03;

		ny = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20;
		oy = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21;
		ay = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22;
		py = m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13;

		nz = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20;
		oz = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21;
		az = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22;
		pz = m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23;

		m00 = nx;
		m10 = ny;
		m20 = nz;

		m01 = ox;
		m11 = oy;
		m21 = oz;

		m02 = ax;
		m12 = ay;
		m22 = az;

		m03 = px;
		m13 = py;
		m23 = pz;

		m30 = 0;
		m31 = 0;
		m32 = 0;
		m33 = 1;
	}

	public void mulInverse(Matrix4d m1) {
		mulInverse(this, m1);
	}

	public void mulInverse(Matrix4d m1, Matrix4d m2) {
		double nx, ox, ax;
		double ny, oy, ay;
		double nz, oz, az;
		double px, py, pz;

		nx = m1.m00 * m2.m00 + m1.m01 * m2.m01 + m1.m02 * m2.m02;
		ox = m1.m00 * m2.m10 + m1.m01 * m2.m11 + m1.m02 * m2.m12;
		ax = m1.m00 * m2.m20 + m1.m01 * m2.m21 + m1.m02 * m2.m22;

		ny = m1.m10 * m2.m00 + m1.m11 * m2.m01 + m1.m12 * m2.m02;
		oy = m1.m10 * m2.m10 + m1.m11 * m2.m11 + m1.m12 * m2.m12;
		ay = m1.m10 * m2.m20 + m1.m11 * m2.m21 + m1.m12 * m2.m22;

		nz = m1.m20 * m2.m00 + m1.m21 * m2.m01 + m1.m22 * m2.m02;
		oz = m1.m20 * m2.m10 + m1.m21 * m2.m11 + m1.m22 * m2.m12;
		az = m1.m20 * m2.m20 + m1.m21 * m2.m21 + m1.m22 * m2.m22;

		m00 = nx;
		m10 = ny;
		m20 = nz;

		m01 = ox;
		m11 = oy;
		m21 = oz;

		m02 = ax;
		m12 = ay;
		m22 = az;

		px = -m2.m03;
		py = -m2.m13;
		pz = -m2.m23;
		m03 = m1.m03 + px * m00 + py * m01 + pz * m02;
		m13 = m1.m13 + px * m10 + py * m11 + pz * m12;
		m23 = m1.m23 + px * m20 + py * m21 + pz * m22;

		m30 = 0;
		m31 = 0;
		m32 = 0;
		m33 = 1;
	}

	public void mulInverseLeft(Matrix4d m1) {
		mulInverseLeft(this, m1);
	}

	public void mulInverseLeft(Matrix4d m1, Matrix4d m2) {
		double nx, ox, ax;
		double ny, oy, ay;
		double nz, oz, az;
		double px, py, pz;

		px = m2.m03 - m1.m03;
		py = m2.m13 - m1.m13;
		pz = m2.m23 - m1.m23;
		m03 = px * m1.m00 + py * m1.m10 + pz * m1.m20;
		m13 = px * m1.m01 + py * m1.m11 + pz * m1.m21;
		m23 = px * m1.m02 + py * m1.m12 + pz * m1.m22;

		nx = m2.m00 * m1.m00 + m2.m10 * m1.m10 + m2.m20 * m1.m20;
		ny = m2.m00 * m1.m01 + m2.m10 * m1.m11 + m2.m20 * m1.m21;
		nz = m2.m00 * m1.m02 + m2.m10 * m1.m12 + m2.m20 * m1.m22;

		ox = m2.m01 * m1.m00 + m2.m11 * m1.m10 + m2.m21 * m1.m20;
		oy = m2.m01 * m1.m01 + m2.m11 * m1.m11 + m2.m21 * m1.m21;
		oz = m2.m01 * m1.m02 + m2.m11 * m1.m12 + m2.m21 * m1.m22;

		ax = m2.m02 * m1.m00 + m2.m12 * m1.m10 + m2.m22 * m1.m20;
		ay = m2.m02 * m1.m01 + m2.m12 * m1.m11 + m2.m22 * m1.m21;
		az = m2.m02 * m1.m02 + m2.m12 * m1.m12 + m2.m22 * m1.m22;

		m00 = nx;
		m10 = ny;
		m20 = nz;

		m01 = ox;
		m11 = oy;
		m21 = oz;

		m02 = ax;
		m12 = ay;
		m22 = az;
	}

	public void mulXyz(double x, double y, double z) {
		if (Tmp == null) {
			Tmp = new Matrix4dX();
		} else {
			Tmp.setIdentity();
		}
		Tmp.setTranslation(new Vector3d(x, y, z));
		mul(Tmp);
	}

	public void setXyz(double x, double y, double z) {
		m03 = x;
		m13 = y;
		m23 = z;
	}

	public void mulAxisAngle(double x, double y, double z, double ang) {
		if (Tmp == null) {
			Tmp = new Matrix4dX();
		} else {
			Tmp.setIdentity();
		}
		Tmp.setAxisAngle(x, y, z, ang);
		mul(Tmp);
	}

	public void mulAxisAngle(AxisAngle4d axisAng) {
		if (Tmp == null) {
			Tmp = new Matrix4dX();
		} else {
			Tmp.setIdentity();
		}
		Tmp.setAxisAngle(axisAng);
		mul(Tmp);
	}

	public void setAxisAngle(double x, double y, double z, double ang) {
		double mag;

		mag = Math.sqrt(x * x + y * y + z * z);
		Matrix3d R = new Matrix3d();
		R.set(new AxisAngle4d(x / mag, y / mag, z / mag, ang));
		setRotationScale(R);
	}

	public void setAxisAngle(AxisAngle4d axisAng) {
		setAxisAngle(axisAng.x, axisAng.y, axisAng.z, axisAng.angle);
	}

	public void setRpy(double roll, double pitch, double yaw) {
		double sroll, spitch, syaw, croll, cpitch, cyaw;

		sroll = Math.sin(roll);
		croll = Math.cos(roll);
		spitch = Math.sin(pitch);
		cpitch = Math.cos(pitch);
		syaw = Math.sin(yaw);
		cyaw = Math.cos(yaw);

		m00 = croll * cpitch;
		m10 = sroll * cpitch;
		m20 = -spitch;

		m01 = croll * spitch * syaw - sroll * cyaw;
		m11 = sroll * spitch * syaw + croll * cyaw;
		m21 = cpitch * syaw;

		m02 = croll * spitch * cyaw + sroll * syaw;
		m12 = sroll * spitch * cyaw - croll * syaw;
		m22 = cpitch * cyaw;
	}

	public void setRpy(double[] angs) {
		setRpy(angs[0], angs[1], angs[2]);
	}

	public void inverseSpatialTransform(Point3d p, Point3d res) {
		double px = p.x - m03;
		double py = p.y - m13;
		double pz = p.z - m23;

		double x = m00 * px + m10 * py + m20 * pz;
		double y = m01 * px + m11 * py + m21 * pz;
		double z = m02 * px + m12 * py + m22 * pz;
		res.set(x, y, z);
	}

	public void inverseSpatialTransform(Point3d p) {
		inverseSpatialTransform(p, p);
	}

	public void inverseSpatialTransform(Vector3d v) {
		inverseSpatialTransform(v, v);
	}

	public void inverseSpatialTransform(Vector3d v, Vector3d res) {
		double x = m00 * v.x + m10 * v.y + m20 * v.z;
		double y = m01 * v.x + m11 * v.y + m21 * v.z;
		double z = m02 * v.x + m12 * v.y + m22 * v.z;
		res.set(x, y, z);
	}

	private final double[] xunit = new double[] { 1.0, 0.0, 0.0 };
	private final double[] yunit = new double[] { 0.0, 1.0, 0.0 };
	private final double[] zunit = new double[] { 0.0, 0.0, 1.0 };

	public void scan(String s, Reader r) throws IOException, IllegalArgumentException {
		scan(new TupleFormat(s), r);
	}

	public void scan(String s, StreamTokenizer stok) throws IOException, IllegalArgumentException {
		scan(new TupleFormat(s), stok);
	}

	public void scan(TupleFormat fmt, Reader r) throws IOException, IllegalArgumentException {
		scan(fmt, new StreamTokenizer(r));
	}

	public void scan(TupleFormat fmt, StreamTokenizer stok) throws IOException, IllegalArgumentException {
		int c;

		if ((c = fmt.getPrefix()) != (char) -1) {
			stok.nextToken();
			if (stok.ttype != c) {
				stok.pushBack();
				throw new IOException("'" + (char) c + "' expected, line " + stok.lineno());
			}
		}

		switch (fmt.getCode()) {
		case 'm': {
			double[] vals = new double[16];
			for (int i = 0; i < 12; i++) {
				vals[i] = TokenScanner.scanDouble(stok);
			}
			vals[12] = 0;
			vals[13] = 0;
			vals[14] = 0;
			vals[15] = 1;
			set(vals);
			break;
		}
		case 'v': {
			double[] vals = TokenScanner.scanDoubles(stok, 7);
			setXyz(vals[0], vals[1], vals[2]);
			setAxisAngle(vals[3], vals[4], vals[5], Math.toRadians(vals[6]));
			break;
		}
		case 's': {
			setIdentity();
			while (true) {
				String keyword = null;
				double[] axis = null;

				keyword = TokenScanner.scanWord(stok, null, true);
				if (keyword == null) {
					break;
				}
				if (keyword.equalsIgnoreCase("trans")) {
					double[] xyz = TokenScanner.scanDoubles(stok, 3);
					mulXyz(xyz[0], xyz[1], xyz[2]);
				} else if (keyword.equalsIgnoreCase("rot")) {
					axis = TokenScanner.scanDoubles(stok, 3);
				} else if (keyword.equalsIgnoreCase("rotx")) {
					axis = xunit;
				} else if (keyword.equalsIgnoreCase("roty")) {
					axis = yunit;
				} else if (keyword.equalsIgnoreCase("rotz")) {
					axis = zunit;
				} else if (keyword.equalsIgnoreCase("matrix")) {
					double[] vals = new double[16];
					for (int i = 0; i < 12; i++) {
						vals[i] = TokenScanner.scanDouble(stok);
					}
					vals[12] = 0;
					vals[13] = 0;
					vals[14] = 0;
					vals[15] = 1;
					set(vals);
				} else {
					throw new IllegalArgumentException("Illegal keyword " + keyword);
				}
				if (axis != null) {
					double angle = TokenScanner.scanDouble(stok);
					mulAxisAngle(axis[0], axis[1], axis[2], Math.toRadians(angle));
				}
			}
			break;
		}
		default: {
			throw new IllegalArgumentException("Illegal conversion code '" + fmt.getCode() + "'");
		}
		}

		if ((c = fmt.getSuffix()) != (char) -1) {
			if (fmt.getCode() != 's') {
				stok.nextToken();
			}
			if (stok.ttype != c) {
				stok.pushBack();
				throw new IOException("'" + (char) c + "' expected, line " + stok.lineno());
			}
		}
	}

	public String toString(NumberFormat fmt) {
		String s = "";
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				s += fmt.format(getElement(i, j));
				if (j < 3) {
					s += " ";
				}
			}
			if (i < 3) {
				s += "\n";
			}
		}
		return s;
	}

	public String toString(String fmtStr) {
		return toString(new FloatFormat(fmtStr));
	}
}
