package tintor.properties.scrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

import tintor.XArrays;
import tintor.geometry.Vector2;

public final class Properties {
	public static final String DefaultFile = "properties";
	public static final Properties properties = new Properties();

	private final Map<String, Object> map = new HashMap<String, Object>();
	transient int modifications = 0;

	public Properties() {}

	public Properties(final String file) {
		read(file);
	}

	public Properties(final Reader r) {
		read(r);
	}

	// Advanced Input/Output system
	private File propertiesFile;
	private transient Thread thread = null;
	int lastFileModification;

	// TODO enable reading properties from web interface
	// TODO enable changing properties from web interface
	// TODO enable updating properties from web page
	// TODO also monitor for changes in PropertiesFile, and then synchronize if file is not corrupted
	public synchronized void start(final String file, final boolean save, final boolean load) {
		if (!save && !load) throw new RuntimeException();

		// TODO loading

		if (thread == null) {
			thread = new Thread() {
				@Override public void run() {
					final int time = get("properties.sleep_time", 1000);
					try {
						while (true) {
							sleep(time);
							if (lastFileModification != modifications) lastFileModification = save();
						}
					} catch (final InterruptedException e) {}
					if (lastFileModification != modifications) lastFileModification = save();
				}
			};
			thread.setName("Properties");
			thread.setDaemon(true);

			propertiesFile = new File(file);
			lastFileModification = 0;

			thread.start();
		}
	}

	public void stop() {
		// FIXME not thread safe! stop must NOT be synchronized!
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (final InterruptedException ex) {}
			thread = null;
		}
	}

	synchronized int save() {
		try {
			// preparing transaction
			final String prefix = propertiesFile.getPath() + "." + thread.getId();
			final File a = File.createTempFile(prefix, ".tmp", new File("."));
			write(a.getPath());
			final File b = File.createTempFile(prefix, ".tmp", new File("."));
			b.delete();

			// flip! NOTE should be atomic!
			propertiesFile.renameTo(b);
			final boolean ok = a.renameTo(propertiesFile);

			// cleanup transaction
			if (!ok) throw new Exception("Properties AutoSave failed!");
			b.delete();
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return modifications;
	}

	// Notification system
	private transient final Map<String, PropertyListener[]> listeners = new HashMap<String, PropertyListener[]>();
	private transient final List<PropertyListener> rootListeners = new ArrayList<PropertyListener>();

	public synchronized void addListener(final PropertyListener listener) {
		if (listener == null) throw new NullPointerException();
		rootListeners.add(listener);
	}

	public synchronized void removeListener(final PropertyListener listener) {
		rootListeners.remove(listener);
	}

	synchronized void internalAddListener(final String key, final PropertyListener listener) {
		PropertyListener[] list = listeners.get(key);
		list = list != null ? XArrays.add(list, listener) : new PropertyListener[] { listener };
		listeners.put(key, list);
	}

	public void addListener(final String key, final PropertyListener listener) {
		checkKey(key);
		internalAddListener(key, listener);
	}

	public void addListener(final Class<?> kc, final String key, final PropertyListener listener) {
		checkKey(key);
		internalAddListener(kc.getName() + "." + key, listener);
	}

	synchronized void internalRemoveListener(final String key, final PropertyListener listener) {
		PropertyListener[] list = listeners.get(key);
		if (list != null) {
			list = XArrays.remove(list, listener);
			if (list.length == 0)
				listeners.remove(key);
			else
				listeners.put(key, list);
		}
	}

	public void removeListener(final String key, final PropertyListener listener) {
		checkKey(key);
		internalRemoveListener(key, listener);
	}

	public void removeListener(final Class<?> kc, final String key, final PropertyListener listener) {
		checkKey(key);
		internalRemoveListener(kc.getName() + "." + key, listener);
	}

	private void fireNotification(String key, final Object oldValue, final Object newValue) {
		modifications++;
		for (final PropertyListener a : rootListeners)
			a.propertyChange(key, oldValue, newValue);

		while (true) {
			final PropertyListener[] list = listeners.get(key);
			if (list != null) for (final PropertyListener a : list)
				a.propertyChange(key, oldValue, newValue);

			final int d = key.lastIndexOf('.');
			if (d == -1) break;
			key = key.substring(0, d);
		}
	}

	// Property Object Creators
	public final <T> Property<T> property(final String key, final Class<T> vc) {
		checkKey(key);
		return new Property<T>(this, key);
	}

	public final <T, E> Property<T> property(final Class<E> kc, final String name, final T def) {
		final String key = kc.getName() + "." + name;
		init(key, def);
		return new Property<T>(this, key);
	}

	// Single key methods
	public synchronized void init(final String key, final Object def) {
		checkKey(key);
		if (!map.containsKey(key)) {
			if (def == null) throw new NullPointerException();
			fireNotification(key, map.put(key, def), def);
		}
	}

	synchronized void internalSet(final String key, final Object value) {
		if (value == null) throw new NullPointerException();
		// if (!map.containsKey(key)) throw new RuntimeException("key '" + key + "' not found!");
		final Object o = map.put(key, value);
		if (o != value) fireNotification(key, o, value);
	}

	public final void set(final String key, final Object value) {
		checkKey(key);
		internalSet(key, value);
	}

	public synchronized String getString(final String key) {
		return (String) get(key);
	}

	public synchronized int getInt(final String key) {
		return (Integer) get(key);
	}

	public synchronized boolean getBool(final String key) {
		return (Boolean) get(key);
	}

	synchronized Object internalGet(final String key) {
		final Object o = map.get(key);
		if (o == null) throw new RuntimeException("key '" + key + "' not found!");
		return o;
	}

	public final Object get(final String key) {
		checkKey(key);
		return internalGet(key);
	}

	@SuppressWarnings("unchecked") public final <T> T get(final String key, final Class<T> vc) {
		checkKey(key);
		return (T) internalGet(key);
	}

	@SuppressWarnings("unchecked") public final <T> T get(final Class<?> kc, final String key, final Class<T> vc) {
		checkKey(key);
		return (T) internalGet(kc.toString() + "." + key);
	}

	@SuppressWarnings("unchecked") public synchronized <T> T internalGet(final String key, final T def) {
		if (def == null) throw new NullPointerException();
		if (!map.containsKey(key)) {
			fireNotification(key, map.put(key, def), def);
			return def;
		}
		return (T) map.get(key);
	}

	@SuppressWarnings("unchecked") public synchronized <T> T get(final String key, final T def) {
		checkKey(key);
		return internalGet(key, def);
	}

	@SuppressWarnings("unchecked") public synchronized <T> T get(final Class<?> kc, final String key, final T def) {
		checkKey(key);
		return internalGet(kc.getName() + "." + key, def);
	}

	synchronized void internalRemove(final String key) {
		final Object value = map.remove(key);
		if (map != null) fireNotification(key, value, null);
	}

	public synchronized void remove(final String key) {
		checkKey(key);
		internalRemove(key);
	}

	public synchronized boolean contains(final String key) {
		return map.containsKey(key);
	}

	// Multi key methods
	public synchronized void clear() {
		for (final Map.Entry<String, Object> e : map.entrySet())
			fireNotification(e.getKey(), e.getValue(), null);
		map.clear();
	}

	public synchronized boolean read(final String file) {
		try {
			read(new FileReader(file));
			return true;
		} catch (final FileNotFoundException e) {
			return false;
		}
	}

	private static final Pattern emptyPattern = Pattern.compile("(;.*)?");
	private static final Pattern keyPattern = Pattern.compile("(\\w+(\\.\\w+)*)");
	private static final Pattern pairPattern = Pattern.compile(keyPattern.pattern() + "=(.+)");

	public final void read(final Reader r) {
		final BufferedReader in = new BufferedReader(r);
		final Map<String, Object> xmap = new HashMap<String, Object>();
		try {
			while (true) {
				final String line = in.readLine();
				if (line == null) break;
				if (emptyPattern.matcher(line).matches()) continue;

				final Matcher m = pairPattern.matcher(line);
				if (!m.matches()) throw new RuntimeException("invalid line: '" + line + "'");
				xmap.put(m.group(1).intern(), m.group(3).intern());
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		for (final Map.Entry<String, Object> e : xmap.entrySet()) {
			final Object o = map.put(e.getKey(), e.getValue());
			if (!e.getValue().equals(o)) fireNotification(e.getKey(), o, e.getValue());
		}
	}

	public void write(final String file) {
		try {
			FileWriter w = null;
			try {
				w = new FileWriter(file);
				write(w);
			} finally {
				if (w != null) w.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void write(final Writer w) {
		final String[] keys = map.keySet().toArray(new String[map.size()]);
		Arrays.sort(keys);
		try {
			for (final String key : keys) {
				w.write(key);
				w.write('=');
				w.write(encode(map.get(key)));
				w.write('\n');
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public String toString() {
		final StringWriter w = new StringWriter();
		write(w);
		return w.toString();
	}

	// Utillity methods
	protected <T> Object decode(final String a) {
		try {
			return Integer.valueOf(a);
		} catch (final NumberFormatException e) {}
		if (a.equalsIgnoreCase("true") || a.equalsIgnoreCase("false")) return Boolean.valueOf(a);
		return a;
	}

	protected <T> String encode(final Object o) {
		if (o instanceof List) {
			final StringBuilder b = new StringBuilder();
			b.append('{');
			for (final Object e : (List) o) {
				if (b.length() > 1) b.append(",");
				b.append(encode(e));
			}
			b.append('}');
			return b.toString();
		}
		return o.toString();
	}

	private static void checkKey(final String key) {
		if (key == null || !keyPattern.matcher(key).matches()) throw new RuntimeException();
	}

	// Test
	public static void main(final String[] args) {
		final ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(5);
		a.add(4);
		a.add(3);
		properties.init("list", a);
		properties.init("name", Arrays.asList(1, 2, 3, 4, 5));
		properties.init("point", new Point(0, 0));
		properties.init("vector", new Vector2(0, 0));
		System.out.println(properties);
		//		final Properties p = new Properties();
		//		p.init("ime", "Marko Tintor");
		//		p.property("ime", String.class).get();
		//
		//		new WebServer() {
		//			@Override protected void handle(final String command, final Map<String, String> header,
		//					final Writer out) throws IOException {
		//				if (command.startsWith("/init?"))
		//					for (final String a : command.substring(5).split("&"))
		//						if (a.indexOf('=') != -1)
		//							p.init(a.substring(0, a.indexOf('=')), a.substring(a.indexOf('=') + 1));
		//				if (command.startsWith("/remove?")) for (final String a : command.substring(8).split("&"))
		//					p.remove(a);
		//
		//				out.write("HTTP/1.1 200\n\n");
		//				out.write("<table>");
		//				p.write(out, "<tr><td><b>%s</b></td><td>%s</td></tr>");
		//				out.write("</table>");
		//			}
		//		}.start(80, false);
	}
}