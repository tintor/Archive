import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JRubyScripting {
	public static void main(final String[] args) throws Exception {
		final ScriptEngine engine = new ScriptEngineManager().getEngineByName("jruby");
		final long a = System.nanoTime();
		engine.eval("puts 'Hello, World'");
		System.out.println(System.nanoTime() - a);
	}
}