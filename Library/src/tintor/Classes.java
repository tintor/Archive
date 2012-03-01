package tintor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Classes {
	private static String decode(String a) {
		final StringBuilder b = new StringBuilder();
		while (true) {
			final int i = a.indexOf('%');
			if (i == -1) break;

			b.append(a.substring(0, i));
			b.append((char) Integer.parseInt(a.substring(i + 1, i + 3), 16));
			a = a.substring(i + 3);
		}
		if (b.length() == 0) return a;
		b.append(a);
		return b.toString();
	}

	public static List<Class<?>> getClassesInPackage(final String packageName) throws ClassNotFoundException {
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) throw new ClassNotFoundException("Can't get class loader");

		final URL resource = loader.getResource(packageName.replace('.', '/'));
		if (resource == null) throw new ClassNotFoundException("No resource for " + packageName.replace('.', '/'));

		final File directory = new File(decode(resource.getFile()));
		if (!directory.exists())
			throw new ClassNotFoundException(packageName + " does not appear to be a valid DIRECTORY package");

		final List<Class<?>> classes = new ArrayList<Class<?>>();
		for (final String element : directory.list())
			if (element.endsWith(".class"))
				classes.add(Class.forName(packageName + '.' + element.substring(0, element.length() - 6)));
		return classes;
	}
}
