package tintor.properties;

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

public class Test {
	public static void main(final String[] args) throws Exception {
		if (new File("properties").exists()) Property.read(new FileReader("properties"));
		final Property size = Property.instance(Test.class, "size", -1);

		Property.set("marko", true);
		System.out.println(size.getInt() + 2);

		size.set(2);
		final StringWriter w = new StringWriter();
		Property.write(w);
		System.out.println(w);
	}
}