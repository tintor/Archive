package tintor.commander;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

public class Icons {
	private static final String IconDir = "c:/workspace2/icons/";

	private final static Map<String, Image> icons = new HashMap<String, Image>();

	public static Image get(final String name) {
		Image image = icons.get(name);
		if (image == null) {
			image = new Image(null, IconDir + name + ".png");
			icons.put(name, image);
		}
		return image;
	}

	private Icons() {}

	public static void dispose() {
		for (final String key : icons.keySet())
			icons.get(key).dispose();
		icons.clear();
	}
}