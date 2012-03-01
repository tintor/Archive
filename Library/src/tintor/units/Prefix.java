package tintor.units;

import tintor.geometry.Vector3;

public class Prefix {
	// Interface
	public static String prefix(final int e) {
		return e <= 24 && e >= -24 && e % 3 == 0 ? prefix[8 - e / 3] : "e" + e;
	}

	// Static Interface
	public static String toString(final double v) {
		return toString(v, "%f");
	}

	public static String toString(double v, final String format) {
		int e = 0;
		while (Math.abs(v) >= 1e3) {
			e += 3;
			v *= 1e-3;
		}
		while (v != 0 && Math.abs(v) < 1) {
			e -= 3;
			v *= 1e3;
		}
		return String.format(format, v) + prefix(e);
	}

	public static String toString(final Vector3 v) {
		return v.toString();
	}

	public static String toString(final Vector3 v, final String format) {
		double d = Math.max(Math.max(Math.abs(v.x), Math.abs(v.y)), Math.abs(v.z)), f = 1;

		int e = 0;
		while (Math.abs(d) >= 1e3) {
			e += 3;
			d *= 1e-3;
			f *= 1e-3;
		}
		while (d != 0 && Math.abs(d) <= 1e-3) {
			e -= 3;
			d *= 1e3;
			f *= 1e3;
		}

		return v.mul(f).toString(format) + prefix(e);
	}

	// Implementation
	private static final String[] prefix = { "Y", "Z", "E", "P", "T", "G", "M", "k", "", "m", "u", "n", "p", "f", "a",
			"z", "y" };

	private Prefix() {}
}