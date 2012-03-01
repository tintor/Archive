package tintor.frpr.util;

import java.util.ArrayList;
import java.util.List;

/** Klasa <code>Node</code> predstavlja jedan cvor u HTML dokumentu.
 *  
 * @author Marko Tintor (tintor@gmail.com) */
public class Node {
	private final String name, attributes;
	private final List<Object> contents = new ArrayList<Object>();

	public Node(final String name) {
		final int c = name.indexOf(' ');
		this.name = c == -1 ? name : name.substring(0, c).intern();
		attributes = c == -1 ? "" : name.substring(c);
	}

	public Node(final String name, final List<?> list) {
		this(name);
		add(list);
	}

	public Node(final String name, final Object... list) {
		this(name);
		add(list);
	}

	public void add(final List<?> list) {
		for (final Object a : list)
			contents.add(a);
	}

	public void add(final Object... list) {
		for (final Object a : list)
			contents.add(a);
	}

	@Override public String toString() {
		final StringBuilder b = new StringBuilder();
		print(b);
		return b.toString();
	}

	private static void printElement(final StringBuilder b, final Object o) {
		if (o instanceof Node)
			((Node) o).print(b);
		else if (o instanceof List<?>)
			for (final Object a : (List<?>) o)
				printElement(b, a);
		else if (o != null) b.append(o);
	}

	private void print(final StringBuilder b) {
		b.append('<').append(name).append(attributes).append('>');
		for (final Object o : contents)
			printElement(b, o);
		if (name != "td" && name != "option") b.append("</").append(name).append('>');
	}
}