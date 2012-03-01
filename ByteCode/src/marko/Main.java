package marko;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tintor.geometry.Vector3;

class Point {
	public Vector3 position = new Vector3(2, 3, 0);
	public Object b;
	public final List<Integer> list = Arrays.asList(1, 2, 3);
	int x = 5;
	private final int y = 3;
}

public class Main {
	static void escape(final String s, final Writer w) throws Exception {
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '\t':
				w.write("\\t");
				break;
			case '\n':
				w.write("\\n");
				break;
			case '\'':
				w.write("\\'");
				break;
			case '\"':
				w.write("\\\"");
				break;
			default:
				if (c < 20) {
					w.write('\\');
					w.write(Integer.toOctalString(c));
				} else
					w.write(c);
			}
		}
	}

	public static void describe(final Object a, final Writer w, final boolean verbose) throws Exception {
		if (a == null) {
			w.write("null");
			return;
		}
		final Class<?> c = a.getClass();

		if (c.isArray()) {
			w.write('[');
			final int length = Array.getLength(a);
			if (length > 0) {
				describe(Array.get(a, 0), w, true);
				for (int i = 1; i < length; i++) {
					w.write(", ");
					describe(Array.get(a, i), w, true);
				}
			}
			w.write(']');
		} else if (a instanceof Collection) {
			w.write('{');
			boolean first = true;
			for (final Object x : (Collection) a) {
				if (first)
					first = false;
				else
					w.write(", ");
				describe(x, w, true);
			}
			w.write('}');
		} else if (a instanceof Map) {
			w.write('{');
			boolean first = true;
			for (final Object e : ((Map) a).entrySet()) {
				if (first)
					first = false;
				else
					w.write(", ");
				describe(((Map.Entry) e).getKey(), w, true);
				w.write(':');
				describe(((Map.Entry) e).getValue(), w, true);
			}
			w.write('}');
		} else if (c.isEnum()) {
			w.write(c.getName());
			w.write('.');
			w.write(((Enum<?>) a).name());
		} else if (c == Boolean.class || c == Integer.class || c == Byte.class || c == Short.class || c == Long.class
				|| c == Double.class || c == Float.class)
			w.write(a.toString());
		else if (c == Character.class) {
			w.write('\'');
			escape(a.toString(), w);
			w.write('\'');
		} else if (c == String.class) {
			w.write('"');
			escape((String) a, w);
			w.write('"');
		} else {
			if (verbose) w.write(c.getName());
			w.write('(');
			final Method m = c.getMethod("toString");
			if (m.getDeclaringClass() != Object.class)
				w.write(a.toString());
			else {
				boolean first = true;
				for (final Field field : c.getFields())
					if ((field.getModifiers() & Modifier.STATIC) == 0) {
						if (first)
							first = false;
						else
							w.write(", ");
						w.write(field.getName());
						w.write(':');
						final Object f = field.get(a);
						describe(f, w, f == null || field.getType() != f.getClass());
					}
			}
			w.write(')');
		}
	}

	public static void test(final Object a) throws Exception {
		final Writer w = new OutputStreamWriter(System.out);
		describe(a, w, true);
		w.flush();
		System.out.println();
	}

	static enum Token {
		Less, Greater
	}

	public static void main(final String[] args) throws Exception {
		final Map<String, String> map = new HashMap<String, String>();
		map.put("marko", "mare");

		final Set<String> set = new HashSet<String>();
		set.add("marko");

		test(System.out);
		test(map);
		test(set);
		test(new Point());
		test(new ArrayList<Integer>());
	}
}