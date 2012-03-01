package tintor.graph.sandbox;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

public interface Queue<T> {
	void add(T a);

	boolean isEmpty();

	T remove() throws NoSuchElementException;

	void decrease(T a);

	void clear();
}

abstract class AbstractQueue<T> implements Queue<T> {
	@Override public void clear() {
		deque.clear();
	}

	@Override public boolean isEmpty() {
		return deque.isEmpty();
	}

	@Override public T remove() throws NoSuchElementException {
		return deque.remove();
	}

	@Override public void decrease(T a) {
		assert false;
	}

	protected final Deque<T> deque = new ArrayDeque<T>();
}

class FIFO<T> extends AbstractQueue<T> {
	@Override public void add(T a) {
		deque.addLast(a);
	}
}

class LIFO<T> extends AbstractQueue<T> {
	@Override public void add(T a) {
		deque.addFirst(a);
	}
}