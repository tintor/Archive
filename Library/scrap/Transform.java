package geometry.base;

@util.Immutable
public class Transform {
	// Fields
	public final double sx, sy, sz;
	public final Quaternion r;
	public final Vector3 t;

	// Constants
	public final static Transform Identity = new Transform(1, 1, 1, Quaternion.Identity, Vector3.Zero);

	// Constructors
	public Transform(double sx, double sy, double sz, Quaternion r, Vector3 t) {
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
		this.r = r;
		this.t = t;
	}

	// Operations
	/** 29 muls, 25 adds (verify this!)*/
	public Vector3 applyP(Vector3 a) {
		if(this == Identity) return a;
		return r.rotate(a.mul(sx, sy, sz)).add(t);
	}

	public Vector3 applyV(Vector3 a) {
		if(this == Identity) return a;
		return r.rotate(a.mul(sx, sy, sz));
	}

	public Vector3 iapplyP(Vector3 a) {
		if(this == Identity) return a;
		return r.irotate(a.sub(t)).div(sx, sy, sz);
	}

	public Vector3 iapplyV(Vector3 a) {
		if(this == Identity) return a;
		return r.irotate(a).div(sx, sy, sz);
	}

//	public Transform combine(Transform a) {
//		if(this == Identity) return a;
//		if(a == Identity) return this;
//		return new Transform(s * a.s, a.r.mul(r), a.apply(t));
//	}

//	public Transform inverse() {
//		if(this == Identity) return Identity;
//		return new Transform(1 / sx, 1 / sy, 1 / sz, r.inv(), r.irotate(t.neg()).mul(1 / s));
//	}

//	public Transform translate(Vector3 a) {
//		return new Transform(s, r, t.add(a));
//	}
//
//	public Transform rotate(Quaternion q) {
//		return new Transform(s, q.mul(r), q.rotate(t));
//	}
//
//	public Transform scale(double a) {
//		return new Transform(s * a, r, t.mul(a));
//	}
}