package tintor.geometry;

public class Transform2 {
	private float cosa, sina;
	private Vector2 pos;

	public Transform2() {
		pos = Vector2.Zero;
		cosa = 1;
	}

	public Transform2(final Vector2 position, final float angle) {
		set(position, angle);
	}

	public void set(final Vector2 position, final float angle) {
		pos = position;
		cosa = GMath.cos(angle);
		sina = GMath.sin(angle);
	}

	public Vector2 translation() {
		return pos;
	}

	public float angle() {
		return (float) Math.atan2(sina, cosa);
	}

	public static Transform2 comb(final Transform2 a, final Transform2 b) {
		final Transform2 z = new Transform2();
		z.cosa = a.cosa * b.cosa - a.sina * b.sina;
		z.sina = a.cosa * b.sina + a.sina * b.cosa;
		z.pos = b.rotate(a.pos).add(b.pos);
		return z;
	}

	public Transform2 invert() {
		final Transform2 z = new Transform2();
		z.cosa = cosa;
		z.sina = -sina;
		// z.pos = new Vector2d(-pos.x * cosa - pos.y * sina, pos.x * sina -
		// pos.y * cosa);
		z.pos = pos.rotate(cosa, -sina).neg();
		return z;
	}

	public Vector2 apply(final Vector2 v) {
		// return new Vector2d(v.x * cosa - v.y * sina + pos.x, v.x * sina +
		// v.y
		// * cosa + pos.y);
		return v.rotate(cosa, sina).add(pos);
	}

	public Plane2 apply(final Plane2 v) {
		final Vector2 nn = v.normal.rotate(cosa, sina);
		return new Plane2(nn, v.offset - pos.dot(nn));
	}

	public Vector2 iapply(final Vector2 v) {
		return v.sub(pos).rotate(cosa, -sina);
		// return new Vector2d((v.x - pos.x) * cosa + (v.y - pos.y) * sina,
		// (v.x
		// - pos.x) * -sina + (v.y - pos.y)
		// * cosa);
	}

	public Vector2 rotate(final Vector2 v) {
		return v.rotate(cosa, sina);
	}

	public Vector2 irotate(final Vector2 v) {
		return v.rotate(cosa, -sina);
	}

	//	@Override
	//	public boolean equals(Object o) {
	//		if (!(o instanceof Transform2)) return false;
	//		Transform2 a = (Transform2) o;
	//		return cosa == a.cosa && sina == a.sina && pos.equals(a.pos);
	//	}

	@Override public String toString() {
		return "(angle=" + angle() + ", pos=" + pos.toString() + ")";
	}
}