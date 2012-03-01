package tintor.search;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class Search<T> implements Iterable<T> {
	// TODO convert to CashingHashSet
	//	protected final NonCashingHashSet<T> reached = new NonCashingHashSet<T>();
	public final Set<T> reached = new HashSet<T>();
	public final Deque<T> queue = new ArrayDeque<T>();

	public Search() {

	}

	public Search(final T a) {
		reached.add(a);
		queue.add(a);
	}

	public final boolean add(final T a) {
		return addFirst(a); // DFS by default to save space in queue
	}

	public final boolean addFirst(final T a) {
		if (reached.add(a)) {
			queue.addFirst(a);
			return true;
		}
		return false;
	}

	public final boolean addLast(final T a) {
		if (reached.add(a)) {
			queue.addLast(a);
			return true;
		}
		return false;
	}

	public final void addFirstForced(final T a) {
		reached.add(a);
		queue.addFirst(a);
	}

	public final void addLastForced(final T a) {
		reached.add(a);
		queue.addLast(a);
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			@Override public boolean hasNext() {
				return queue.size() > 0;
			}

			@Override public T next() {
				return queue.pollFirst();
			}

			@Override public void remove() {
				throw new RuntimeException();
			}
		};
	}

	public final void clear() {
		reached.clear();
		queue.clear();
	}
}