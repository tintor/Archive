package tintor.frpr.util;

import java.util.ArrayList;
import java.util.List;

/** @author Marko Tintor (tintor@gmail.com) */
public class Table extends Node {
	public Table(final boolean border) {
		super(border ? "table border='1' cellspacing='0'" : "table");
	}

	private int _columns = 0;

	private final List<Object> _row = new ArrayList<Object>();

	public int columns() {
		return _columns;
	}

	public void hcell(final Object a) {
		_columns++;
		_row.add(new Node("th", a));
	}

	public void hcell(final Object a, final String extra) {
		_columns++;
		_row.add(new Node("th " + extra, a));
	}

	public void cell(final Object a, final String extra) {
		_row.add(new Node("td " + extra, a));
	}

	public void cell(final Object a) {
		_row.add(new Node("td", a));
	}

	/** Dodaj unete celije kao novi red u tabeli. */
	public void row() {
		super.add(new Node("tr", _row));
		_row.clear();
	}

	/** Dodaj unete celije kao novi red u tabeli. */
	public void row(final String attributes) {
		super.add(new Node("tr " + attributes, _row));
		_row.clear();
	}
}