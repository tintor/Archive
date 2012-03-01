package tintor.frpr.util;

import tintor.frpr.servlets.Main;

/** @author Marko Tintor (tintor@gmail.com) */
public class Textbox extends Node {
	public Textbox(final String name, final String text) {
		super("input name='" + name + "' type='text' value='" + Main.escape(text) + "' size='80' maxlength='250'");
	}
}