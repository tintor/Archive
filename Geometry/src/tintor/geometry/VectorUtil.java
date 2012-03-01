/*
Copyright (C) 2007 Marko Tintor <tintor@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
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
			return new Plane3(new Vector3(d(1), d(2), d(3)), d(4));
		}
	}, new Type(Plane2.class, "(%f,%f|%f)") {
		@Override Object instance() {
			return new Plane2(new Vector2(d(1), d(2)), d(4));
		}
	}, new Type(Quaternion.class, "(%f,%f,%f:%f)") {
		@Override Object instance() {
			return new Quaternion(d(1), d(2), d(3), d(4));
		}
	}, new Type(Vector3.class, "(%f,%f,%f)") {
		@Override Object instance() {
			return new Vector3(d(1), d(2), d(3));
		}
	}, new Type(Vector2.class, "(%f,%f)") {
		@Override Object instance() {
			return new Vector2(d(1), d(2));
		}
	}, };

	private static @SuppressWarnings("serial") class Z extends HashMap<Class<?>, Stack<Pair<String, Pattern>>> {
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

	static double d(final int i) {
		return Double.valueOf(m.group(i));
	}

	private static Pattern compile(final String pattern) {
		return Pattern.compile(Pattern.compile("%f").matcher(pattern).replaceAll(decimal));
	}
}