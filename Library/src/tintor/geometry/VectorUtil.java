package tintor.geometry;

import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.util.SimpleThreadLocal;

class Pair<A, B> {
	public final A first;
	public final B second;

	public Pair(final A a, final B b) {
		first = a;
		second = b;
	}
}

public class VectorUtil {
	private static final String decimal = "([+-]?\\d+(?:\\.\\d+)?)";
	private static Matcher m;

	public static abstract class Type {
		public final Class<?> clazz;
		public final String format;

		Type(final Class<?> clazz, final String format) {
			this.clazz = clazz;
			this.format = format;
		}

		abstract Object instance();
	}

	public static Type[] types = new Type[] { new Type(Matrix3.class, "(%f,%f,%f|%f,%f,%f|%f,%f,%f)") {
		@Override Object instance() {
			return new Matrix3(d(1), d(2), d(3), d(4), d(5), d(6), d(7), d(8), d(9));
		}
	}, new Type(Plane3.class, "(%f,%f,%f|%f)") {
		@Override Object instance() {
			return new Plane3(new Vector3(d(1), d(2), d(3)).intern(), d(4));
		}
	}, new Type(Plane2.class, "(%f,%f|%f)") {
		@Override Object instance() {
			return new Plane2(new Vector2(d(1), d(2)).intern(), d(4));
		}
	}, new Type(Quaternion.class, "(%f,%f,%f:%f)") {
		@Override Object instance() {
			return new Quaternion(d(1), d(2), d(3), d(4));
		}
	}, new Type(Vector3.class, "(%f,%f,%f)") {
		@Override Object instance() {
			return new Vector3(d(1), d(2), d(3)).intern();
		}
	}, new Type(Vector2.class, "(%f,%f)") {
		@Override Object instance() {
			return new Vector2(d(1), d(2)).intern();
		}
	}, };

	private static class Z extends HashMap<Class<?>, Stack<Pair<String, Pattern>>> {
		Z() {
			for (final Type type : types) {
				put(type.clazz, new Stack<Pair<String, Pattern>>());
				pushPattern(type.clazz, type.format);
			}
		}
	}

	private static final ThreadLocal<Z> defaultPattern = new SimpleThreadLocal<Z>(new Z());

	public static boolean supported(final Class<?> clazz) {
		for (final Type type : types)
			if (type.clazz == clazz) return true;
		return false;
	}

	public static void pushPattern(final Class<?> clazz, final String pattern) {
		System.out.println(defaultPattern);
		System.out.println(defaultPattern.get());
		System.out.println(clazz);
		assert defaultPattern.get().get(clazz) != null;
		defaultPattern.get().get(clazz).push(new Pair<String, Pattern>(pattern, compile(pattern)));
	}

	public static void popPattern(final Class<?> clazz) {
		final Stack<?> a = defaultPattern.get().get(clazz);
		if (a.size() <= 1) throw new RuntimeException();
		a.pop();
	}

	public static Pair<String, Pattern> peekPattern(final Class<?> clazz) {
		return defaultPattern.get().get(clazz).peek();
	}

	public static Object valueOf(final String str) {
		for (final Type type : types) {
			final Pattern p = peekPattern(type.clazz).second;
			m = p.matcher(str);
			if (m.matches()) return type.instance();
		}
		throw new RuntimeException();
	}

	static float d(final int i) {
		return Float.valueOf(m.group(i));
	}

	private static Pattern compile(final String pattern) {
		return Pattern.compile(Pattern.compile("%f").matcher(pattern).replaceAll(decimal));
	}
}