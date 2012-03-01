package tintor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Loader {
	public static void main(final String[] args) throws Exception {
		final Class<?> c = create("marko.Hello", "public class Hello {public String toString() {return \"Zdravo!\";}}");
		System.out.println(c.newInstance());
	}

	final static File temp = new File("data");

	static Class<?> create(final String name, final String code) throws Exception {
		//final Timer timer = new Timer();
		//timer.restart();
		try {
			final int dot = name.lastIndexOf('.');
			final String packageName = dot != -1 ? name.substring(0, dot) : null;
			final String className = dot != -1 ? name.substring(dot + 1, name.length()) : name;
			final File javaFile = new File(temp, className + ".java");

			// write source
			final Writer w = new FileWriter(javaFile);
			if (packageName != null) w.write("package " + packageName + ";\r\n");
			w.write(code);
			w.close();

			// generate class
			final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
			final int ret = javac.run(System.in, System.out, System.err, "-d", temp.getAbsolutePath(), javaFile
					.getAbsolutePath());
			if (ret != 0) throw new RuntimeException();

			// load class
			final URL url = new URL("file://" + temp.getAbsolutePath() + "/");
			final URLClassLoader loader = URLClassLoader.newInstance(new URL[] { url });
			return loader.loadClass(name);
		} finally {
			//System.out.println(timer);
			//timer.stop();
		}
	}
}