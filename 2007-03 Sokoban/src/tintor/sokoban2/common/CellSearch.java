package tintor.sokoban2.common;

import java.util.ArrayDeque;
import java.util.Iterator;

import tintor.sokoban2.cell.Cell;

public class CellSearch implements Iterable<Cell> {
	private final int[] reached = new int[Cell.count()];
	private int timestamp = 1;
	public final ArrayDeque<Cell> queue = new ArrayDeque<Cell>();

	public CellSearch() {

	}

	public CellSearch(final Cell a) {
		force(a);
	}

	public void init() {
		timestamp += 1;
	}

	public void init(final Cell a) {
		init();
		force(a);
	}

	public void reached(final Cell a) {
		reached[a.id()] = timestamp;
	}

	public final boolean add(final Cell a) {
		return addFirst(a);
	}

	public final void force(final Cell a) {
		forceFirst(a);
	}

	public final boolean addFirst(final Cell a) {
		if (reached[a.id()] == timestamp) return false;
		forceFirst(a);
		return true;
	}

	public final void forceFirst(final Cell a) {
		reached(a);
		queue.addFirst(a);
	}

	public final boolean addLast(final Cell a) {
		if (reached[a.id()] == timestamp) return false;
		forceLast(a);
		return true;
	}

	public final void forceLast(final Cell a) {
		reached(a);
		queue.addLast(a);
	}

	public Iterator<Cell> iterator() {
		return iterator;
	}

	private final Iterator<Cell> iterator = new Iterator<Cell>() {
		@Override public boolean hasNext() {
			return queue.size() > 0;
		}

		@Override public Cell next() {
			return queue.pollFirst();
		}

		@Override public void remove() {
			throw new RuntimeException();
		}
	};
}