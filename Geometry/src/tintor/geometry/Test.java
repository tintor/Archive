package tintor.geometry;


abstract class Factory {
	abstract Vector3 vector3(double x, double y, double z);
}

abstract class Vector3 {
	abstract Vector3 add(Vector3 v);

	abstract Vector3 add(double a, Vector3 v);
}

final class Vector3f extends Vector3 {
	float x, y, z;

	Vector3f(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	Vector3f add(final Vector3f v) {
		return new Vector3f(x + v.x, y + v.y, z + v.z);
	}

	void addm(final Vector3f v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	@Override Vector3 add(final Vector3 v) {
		return add((Vector3f) v);
	}

	Vector3f add(final float a, final Vector3f v) {
		return new Vector3f(x + a * v.x, y + a * v.y, z + a * v.z);
	}

	@Override Vector3 add(final double a, final Vector3 v) {
		return add((float) a, (Vector3f) v);
	}

	@Override public String toString() {
		return String.format("(%s %s %s)", x, y, z);
	}
}

final class Vector3d extends Vector3 {
	double x, y, z;

	Vector3d(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	Vector3d add(final Vector3d a) {
		return new Vector3d(x + a.x, y + a.y, z + a.z);
	}

	void addm(final Vector3d v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	@Override Vector3 add(final Vector3 a) {
		return add((Vector3d) a);
	}

	Vector3d add(final double a, final Vector3d v) {
		return new Vector3d(x + a * v.x, y + a * v.y, z + a * v.z);
	}

	@Override Vector3 add(final double a, final Vector3 v) {
		return add(a, (Vector3d) v);
	}
}

public class Test {
	final Factory Float = new Factory() {
		@Override Vector3 vector3(double x, double y, double z) {
			return new Vector3f((float) x, (float) y, (float) z);
		}
	};
	final Factory Double = new Factory() {
		@Override Vector3 vector3(double x, double y, double z) {
			return new Vector3d(x, y, z);
		}
	};

	public static void main(final String[] args) throws Exception {
		// 72 / 321 = 4.46
		//mut_imut();

		// 65 / 66 = 1.01
		//float_double_mut();

		// 316 / 428 = 1.35
		//float_double_imut();

		// 64 / 451 = 7.05
		// server: 50 / 416 = 8.32
		hard_easy();

		// double_imut / float_mut = 428 / 65 = 6.58
	}

	static void hard_easy() throws Exception {
		while (true) {
			hard_easy_loop();
			System.out.flush();
		}
	}

	static void hard_easy_loop() {
		final Vector3f am = new Vector3f(1, -1, 0);
		final Vector3f bm = new Vector3f(2, -2, 0);

		long t;

		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++) {
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
			bm.addm(am);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		final Vector3 ai = new Vector3d(1, -1, 0);
		Vector3 bi = new Vector3d(2, -2, 0);
		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++) {
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
			bi = bi.add(ai);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		System.out.println("\t" + bm + " " + bi);
	}

	static void mut_imut() throws Exception {
		mut_imut_loop();
		mut_imut_loop();
		mut_imut_loop();
		mut_imut_loop();
		mut_imut_loop();
		mut_imut_loop();
		mut_imut_loop();
	}

	static void mut_imut_loop() {
		final Vector3f af = new Vector3f(1, -1, 0);
		final Vector3f bm = new Vector3f(2, -2, 0);
		Vector3f bi = new Vector3f(2, -2, 0);

		long t;
		t = System.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
			bm.addm(af);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		t = System.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
			bi = bi.add(af);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		System.out.println("\t" + bm + " " + bi);
	}

	static void float_double_mut() {
		float_double_mut_loop();
		float_double_mut_loop();
		float_double_mut_loop();
		float_double_mut_loop();
		float_double_mut_loop();
		float_double_mut_loop();
		float_double_mut_loop();
	}

	static void float_double_mut_loop() {
		final Vector3f af = new Vector3f(1, -1, 0);
		final Vector3d ad = new Vector3d(1, -1, 0);
		final Vector3f bf = new Vector3f(2, -2, 0);
		final Vector3d bd = new Vector3d(2, -2, 0);

		long t;
		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++) {
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
			bf.addm(af);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++) {
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
			bd.addm(ad);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		System.out.println("\t" + bf + " " + bd);
	}

	static void float_double_imut() {
		float_double_imut_loop();
		float_double_imut_loop();
		float_double_imut_loop();
		float_double_imut_loop();
		float_double_imut_loop();
		float_double_imut_loop();
		float_double_imut_loop();
	}

	static void float_double_imut_loop() {
		final Vector3f af = new Vector3f(1, -1, 0);
		final Vector3d ad = new Vector3d(1, -1, 0);
		Vector3f bf = new Vector3f(2, -2, 0);
		Vector3d bd = new Vector3d(2, -2, 0);

		long t;
		t = System.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
			bf = bf.add(af);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		t = System.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
			bd = bd.add(ad);
		}
		t = System.nanoTime() - t;
		System.out.print(t);
		System.out.print(' ');

		System.out.println("\t" + bf + " " + bd);
	}

	static void gen_spec() {
		final Vector3f af = new Vector3f(1, -1, 0);
		Vector3f bf = new Vector3f(2, -2, 0);

		final Vector3 a = af;
		Vector3 b = bf;
		long t;

		System.gc();
		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++)
			b = b.add(a);
		t = System.nanoTime() - t;
		System.out.println(t + " " + b);

		System.gc();
		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++)
			bf = bf.add(af);
		t = System.nanoTime() - t;
		System.out.println(t + " " + bf);

		System.gc();
		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++)
			b = b.add(a);
		t = System.nanoTime() - t;
		System.out.println(t + " " + b);

		System.gc();
		t = System.nanoTime();
		for (int i = 0; i < 10000000; i++)
			bf = bf.add(af);
		t = System.nanoTime() - t;
		System.out.println(t + " " + bf);
	}
}