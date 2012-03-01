package tintor.util;

public class Hash {
	public static int hash(final boolean a) {
		return a ? 1 : 0;
	}

	public static int hash(final float a) {
		return Float.floatToIntBits(a);
	}

	public static int hash(final double a) {
		return hash(Double.doubleToLongBits(a));
	}

	public static int hash(final long a) {
		return (int) (a >> 32) * 37 ^ (int) a;
	}

	public static int hash(final int a, final int b) {
		return a + b * 211;
	}

	public static int hash(final int a, final int b, final int c) {
		return hash(hash(a, b), c);
	}

	public static int hash(final int a, final int b, final int c, final int d) {
		return hash(hash(a, b, c), d);
	}

	public static int hash(final int a, final int b, final int c, final int d, final int e) {
		return hash(hash(a, b, c, d), e);
	}
}