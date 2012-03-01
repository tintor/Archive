package tintor.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Property {
	public interface Listener {
		void propertyChange(String key, String oldValue, String newValue);
	}

	public final String key;
	private String value;
	private final List<Listener> listeners = new ArrayList<Listener>();

	protected final static Map<String, Property> properties = new HashMap<String, Property>();

	// Properties
	// ==========

	public final static Property WebServerEnabled = instance(Property.class, "WebServerEnabled", false);
	public final static Property WebServerPort = instance(Property.class, "WebServerPort", 80);

	public final static Property AutoReadEnabled = instance(Property.class, "AutoReadEnabled", false);
	public final static Property AutoReadURL = instance(Property.class, "AutoReadURL", "");
	public final static Property AutoReadInterval = instance(Property.class, "AutoReadInterval", 0);

	public final static Property AutoWriteEnabled = instance(Property.class, "AutoWriteEnabled", false);
	public final static Property AutoWriteURL = instance(Property.class, "AutoWriteURL", "");
	public final static Property AutoWriteInterval = instance(Property.class, "AutoWriteInterval", 0);

	// Class methods
	// =============

	private final static String DefaultFile = "properties";

	static {
		if (new File(DefaultFile).exists()) try {
			read(new FileReader(DefaultFile));
		} catch (final IOException e) {}
	}

	public static Property instance(final Class<?> kc, final String name, final Object init) {
		return instance(kc.getName() + "." + name, init);
	}

	public static synchronized Property instance(String key, final Object init) {
		if (init == null) throw new IllegalArgumentException();

		key = key.intern();
		if (properties.containsKey(key)) return properties.get(key);

		final Property p = new Property(key, init);
		properties.put(key, p);
		return p;
	}

	public static synchronized void set(final String key, final Object value) {
		if (value == null) throw new IllegalArgumentException();

		if (properties.containsKey(key))
			properties.get(key).set(value);
		else
			properties.put(key, new Property(key, value));
	}

	public static synchronized void read(final Reader reader) throws IOException {
		final BufferedReader b = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		String line = null;
		while ((line = b.readLine()) != null) {
			final int f = line.indexOf('=');
			if (f == -1) throw new RuntimeException();
			set(line.substring(0, f), line.substring(f + 1, line.length()));
		}
	}

	public static synchronized void write(final Writer writer) throws IOException {
		final String[] keys = properties.keySet().toArray(new String[properties.keySet().size()]);
		Arrays.sort(keys);
		for (final String key : keys) {
			writer.write(key);
			writer.write('=');
			writer.write(properties.get(key).value.toString());
			writer.write('\n');
		}
	}

	// Object methods
	// ==============

	Property(final String key, final Object value) {
		this.key = key;
		this.value = value.toString();
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public Object get() {
		return value;
	}

	public int getInt() {
		return Integer.parseInt(value.toString());
	}

	public boolean getBool() {
		if (value.equals("true") || value.equals("on")) return true;
		if (value.equals("false") || value.equals("off")) return false;
		throw new RuntimeException();
	}

	@Override public String toString() {
		return value.toString();
	}

	public void set(final Object newValue) {
		if (value == null) throw new IllegalArgumentException();

		final String n = newValue.toString();
		for (final Listener listener : listeners)
			listener.propertyChange(key, value, n);
		value = n;
	}
}