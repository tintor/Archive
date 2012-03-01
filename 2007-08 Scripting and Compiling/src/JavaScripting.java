import java.io.FileReader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JavaScripting {
	public static void main(final String[] args) throws Exception {
		final ScriptEngine engine = new ScriptEngineManager().getEngineByName("java");
		final long a = System.nanoTime();
		final ScriptContext c = engine.getContext();
		c.setAttribute(name, value, scope)
		engine.eval(new FileReader("Hello.java"));
		System.out.println(System.nanoTime() - a);
	}
}