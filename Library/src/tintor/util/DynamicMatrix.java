package tintor.util;

import java.util.ArrayList;
import java.util.List;

public class DynamicMatrix<T> {
	private final T def;
	private final List<List<T>> rows = new ArrayList<List<T>>();

	public DynamicMatrix(T def) {
		this.def = def;
	}

	public int width() {
		int width = 0;
		for (List<T> row : rows)
			width = Math.max(width, row.size());
		return width;
	}

	public int height() {
		return rows.size();
	}

	public void put(int x, int y, T a) {
		while (y >= rows.size())
			rows.add(new ArrayList<T>());
		final List<T> row = rows.get(y);
		while (x >= row.size())
			row.add(def);
		row.set(x, a);
	}

	public T get(int x, int y) {
		if (y < 0 || x < 0 || y >= rows.size() || x >= rows.get(y).size()) return def;
		return rows.get(y).get(x);
	}
}