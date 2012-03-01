package tintor.sokoban.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

import tintor.properties.Properties;

public class SokobanProperties extends Properties {
	private static final String PropertiesFile = "sokoban.properties";
	private static final Pattern pointPattern = Pattern.compile("([-]?\\d+)\\s*,\\s*([-]?\\d+)");

	public static final Properties properties = new SokobanProperties();

	SokobanProperties() {
		read(PropertiesFile, false);
		start(PropertiesFile, true, false);
	}

	@Override protected Object decode(final String a) {
		assert pointPattern != null;
		final Matcher m = pointPattern.matcher(a);
		if (m.matches()) return new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));

		return super.decode(a);
	}

	@Override protected String encode(final Object o) {
		if (o instanceof Point) {
			final Point p = (Point) o;
			return String.format("%d, %d", p.x, p.y);
		}
		return super.encode(o);
	}
}