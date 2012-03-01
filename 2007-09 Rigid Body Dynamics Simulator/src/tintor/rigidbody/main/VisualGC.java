package tintor.rigidbody.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisualGC {
	public static void main(final String[] args) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("jps")
				.getInputStream()));
		final Pattern p = Pattern.compile("(\\d+)\\s+" + args[0]);

		String line = null;
		while ((line = in.readLine()) != null) {
			final Matcher m = p.matcher(line);
			if (m.find()) {
				Runtime.getRuntime().exec("visualgc.cmd " + m.group(1));
				System.exit(0);
			}
		}
	}
}