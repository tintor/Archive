package tintor.graph.sandbox;

public class NumberUtils {
	public static Object zero(Class<?> clazz) {
		if (clazz.equals(Integer.class)) return Integer.valueOf(0);
		if (clazz.equals(Double.class)) return Double.valueOf(0);
		throw new RuntimeException();
	}

	public static Object infinite(Class<?> clazz) {
		if (clazz.equals(Integer.class)) return Integer.MAX_VALUE;
		if (clazz.equals(Double.class)) return Double.POSITIVE_INFINITY;
		throw new RuntimeException();
	}

	public static boolean less(Object a, Object b) {
		if (a instanceof Integer) return (int) (Integer) a < (int) (Integer) b;
		if (a instanceof Double) return (double) (Double) a < (double) (Double) b;
		throw new RuntimeException();
	}

	public static Object add(Number a, Number b) {
		if (a instanceof Integer) return (int) (Integer) a + (int) (Integer) b;
		if (a instanceof Double) return (double) (Double) a + (double) (Double) b;
		throw new RuntimeException();
	}
}