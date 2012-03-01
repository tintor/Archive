package packageA;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Main {
	public static void main(final String[] args) throws Exception {
		final Class<?> c = Class.forName("packageB.Marko");
		final Object a = c.newInstance();

		for (final Class cc : c.getDeclaredClasses())
			System.out.println(cc);
		for (final Method m : c.getDeclaredMethods()) {
			m.setAccessible(true);
			System.out.println(m);
			m.invoke(a);
		}
		for (final Field f : c.getDeclaredFields()) {
			f.setAccessible(true);
			System.out.println(f);
			System.out.println(f.get(a));
		}
	}
}