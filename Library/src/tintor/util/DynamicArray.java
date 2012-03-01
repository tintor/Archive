package tintor.util;

import java.util.ArrayList;
import java.util.List;

public class DynamicArray<T> {
	private final T def;
	private final List<T> array = new ArrayList<T>();

	public DynamicArray(final T def) {
		this.def = def;
	}

	public int size() {
		return array.size();
	}

	public void put(final int x, final T a) {
		while (x >= array.size())
			array.add(def);
		array.set(x, a);
	}

	public T get(final int x) {
		if (x >= array.size()) return def;
		return array.get(x);
	}
}