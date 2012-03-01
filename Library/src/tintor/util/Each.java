package tintor.util;

import java.util.Iterator;

public class Each<T> implements Iterable<T> {
	public Iterator<T> iterator;

	public Each() {}

	public Each(Iterator<T> i) {
		this.iterator = i;
	}

	public Iterator<T> iterator() {
		return iterator;
	}
}