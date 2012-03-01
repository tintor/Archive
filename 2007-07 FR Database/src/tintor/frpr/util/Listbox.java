package tintor.frpr.util;

import tintor.frpr.servlets.Main;

/** @author Marko Tintor (tintor@gmail.com) */
public class Listbox extends Node {
	public Listbox(final String name, final String selected) {
		super("select name='" + name + "'");
		this.selected = selected;
	}

	private final String selected;

	public void add(final String value, final String text) {
		super.add(new Node("option value='" + Main.escape(value) + "'" + (value.equals(selected) ? " selected" : ""),
				Main.escape(text)));
	}
}