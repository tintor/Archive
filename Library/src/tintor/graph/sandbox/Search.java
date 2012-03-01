package tintor.graph.sandbox;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Search<T> {
	protected abstract T queueRemove() throws NoSuchElementException;

	protected abstract boolean isGoal(T a);

	protected abstract Iterator<T> getSuccessors(T a);

	protected abstract void queueUpdate(T a, T prev);

	public T run() {
		try {
			while (true) {
				T a = queueRemove();
				if (isGoal(a)) return a;

				Iterator<T> i = getSuccessors(a);
				while (i.hasNext())
					queueUpdate(i.next(), a);
			}
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}