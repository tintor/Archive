package tintor.util;

import java.util.Iterator;

public abstract class Block<T> implements Iterable<T> {
	public static void main(final String[] args) {
		for (final String a : new Block<String>() {
			@Override public void run() {
				yield("ananas");
				for (int i = 0; i < 3; i++)
					yield("banana");
				yield("visnja");
			}
		})
			System.out.println("[" + a + "]");
	}

	abstract protected void run();

	protected final void yield(final T a) {
		next = a;

		main = true;
		Block.this.notifyAll();
		while (main)
			block();
	}

	transient boolean main = true;
	T next;
	boolean more;

	public synchronized Iterator<T> iterator() {
		new Thread() {
			@Override public void run() {
				synchronized (Block.this) {
					Block.this.run();

					more = false;
					next = null;

					main = true;
					Block.this.notifyAll();
				}
			}
		}.start();

		more = true;
		next = null;

		main = false;
		while (!main)
			block();

		return iterator;
	}

	private final Iterator<T> iterator = new Iterator<T>() {
		@Override public boolean hasNext() {
			return more;
		}

		@Override public T next() {
			final T a = next;
			synchronized (Block.this) {
				main = false;
				Block.this.notifyAll();
				while (!main)
					block();
			}
			return a;
		}

		@Override public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	void block() {
		try {
			Block.this.wait();
		} catch (final InterruptedException e) {}
	}
}