import javax.tools.Tool;
import javax.tools.ToolProvider;

public class Compile {
	public static void main(final String[] args) {
		final Tool javac = ToolProvider.getSystemJavaCompiler();
		javac.run(System.in, System.out, System.err, "Hello.java");
	}
}