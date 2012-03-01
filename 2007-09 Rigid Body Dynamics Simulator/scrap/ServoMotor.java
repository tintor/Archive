package model.motor;

import model.Motor;
import model.World;
import model.joint.Axis;

public class ServoMotor extends Motor {
	public Axis axis;
	public double maxForce;
	public double distance;
	public boolean active = false;

	@Override
	protected void applyForces(World world) {
		if (!active) return;
		//		double r = axis.distance() - distance;
		//		double v = axis.velocity();
		//		// goal: r = 0, v = 0, t = min
		//		double amax = maxForce / axis.mass();
	}

//	/** r - position
//	 *  v - velocity
//	 * dt - time during which accelerations will be constant */
//	static double prev = 0, i = 0;
//	static double P = -2, I = -0.02, D = -0.1;
//	static double servo(double r, double v, double as, double dt) {
//		double e = r + v;
//		i = i + e;
//
//		double a = P*e + I*i + D*(e-prev);
//		prev = e;
//		return GMath.clamp(a, -as, as);

//		double e = v * v / 2, i = as * r, t1 = 0, t2 = 0, a = 0;
//		// (r,v) << -b | t+v/b << b | t = (0,0)
//		if ((r <= 0 && e + i > 0) || (r > 0 && i >= e)) {
//			// (r <= 0 && e + i > 0) || (r > 0 && v >= 0) || (v < 0 && i >= e)
//			t2 = Math.sqrt(e + i) / as;
//			a = -as;
//		} else if((r >= 0 && i < e) || (r < 0 && v <= 0) || (v > 0 && e + i <= 0)) {
//			t2 = Math.sqrt(e - i) / as;
//			a = as;
//		}
//		t1 = t2 - v / a;
//		//System.out.printf("t1=%.4f t2=%.4f a=%.4f e=%.4f i=%.4f |  ", t1, t2, a, e,i);
//
//		if(t1 >= dt) return a;
//		if(t2 >= dt) return -a;
//		if(t1 > 0) return a * t1/dt;
//		if(t2 > 0) return -a * t2/dt;
//		return 0;
//	}

//	public static void main(String[] args) {
//		double r = 5, v = 0.5, a, dt = 1;
//		for (int i = 0; i < 10000; i++) {
//			a = servo(r, v, 0.5, dt) + 0.3;
//			r += v * dt + a * dt * dt / 2;
//			v += a * dt;
//			//r += v * dt;
//			System.out.printf("a=%.4f r=%.4f v=%.4f\n", a, r, v);
//			if(Math.abs(r) < 0.0001 && Math.abs(v) < 0.0001) {System.out.println(i); break;}
//		}
//	}

//	static double servo(double r, double v, double ae, double as, double dt) {
//		// 0 = v + p*t1 + q*t2
//		// 0 = r + v*t1 + p*t1^2/2 + (v + p*t1)*t2 + q*t2^2/2
//
//		double a = Double.NaN;
//		double tmin = GMath.Infinity;
//
//		double p = ae - as, q = ae + as;
//
//		double t2 = Math.sqrt((2 * r * p - v * v) / (q * (p - q)));
//		double t1 = -(t2 * q + v) / p;
//		if (GMath.isFinite(t1) && GMath.isFinite(t2) && t1 >= 0 && t2 >= 0 && t1 + t2 < tmin) {
//			tmin = t1 + t2;
//			a = average(dt, p - ae, t1, q - ae, t2);
//		}
//
//		double t = p;
//		p = q;
//		q = t;
//
//		t2 = Math.sqrt((2 * r * p - v * v) / (q * (p - q)));
//		t1 = -(t2 * q + v) / p;
//		if (GMath.isFinite(t1) && GMath.isFinite(t2) && t1 >= 0 && t2 >= 0 && t1 + t2 < tmin) {
//			tmin = t1 + t2;
//			a = average(dt, p - ae, t1, q - ae, t2);
//		}
//
//		if (!GMath.isFinite(a)) {
//			// 0 = r + v*t1 + p*t1^2/2
//			t1 = (Math.sqrt(v * v - 2 * p * r) - v) / p;
//			if (GMath.isFinite(t1) && t1 >= 0 && t1 < tmin) {
//				tmin = t1;
//				a = average(dt, p - ae, t1, 0, 0);
//			}
//
//			p = q;
//			t1 = (Math.sqrt(v * v - 2 * p * r) - v) / p;
//			if (GMath.isFinite(t1) && t1 >= 0 && t1 < tmin) {
//				tmin = t1;
//				a = average(dt, p - ae, t1, 0, 0);
//			}
//		}
//
//		return a;
//	}

//	/* f(x) = y1 (0 < x < x1), y2 (x1 < x < x1 + x2), 0 (x1 + x2 < x)
//	 * return average from 0 to x */
//	static double average(double x, double y1, double x1, double y2, double x2) {
//		if (x <= x1) return y1;
//		if (x <= x1 + x2) return (y1 * x1 + y2 * (x - x1)) / x;
//		return (y1 * x1 + y2 * x2) / x;
//	}
}