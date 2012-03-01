package tintor.sokoban;

import java.util.Locale;

import tintor.Timer;
import tintor.heap.Heap;

class Monitor {
	static Monitor monitor;

	String state = "#";
	int keys, branches, cells;

	int hash_deadlocks, fastBlock_deadlocks, frozenBoxes_deadlocks, macro_deadlocks;

	Heap<Key> queue;
	KeySet set;
	Key lastKey;

	private final Thread thread = new Thread() {
		@Override public void run() {
			try {
				while (true) {
					sleep(5000);
					output();

					final Key k = lastKey;
					if (k != null) System.out.println(k);
				}
			} catch (final InterruptedException e) {
				timer.stop();
			}
			output();
		}
	};

	private int lastKeys, lastBranches, lastCells;
	private final Timer timer = new Timer();
	private final StringBuilder b = new StringBuilder();

	Monitor() {
		assert monitor == null;
		monitor = this;
		Locale.setDefault(Locale.US);
		thread.setDaemon(true);

		timer.restart();
		thread.start();
	}

	void reset(final String state) {
		this.state = state;
		queue = null;
		set = null;
		lastKey = null;
	}

	void stop() {
		thread.interrupt();
		try {
			thread.join();
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
		monitor = null;
	}

	private void output() {
		int deltaKeys = keys - lastKeys;
		final int deltaBranches = branches - lastBranches;
		final int deltaCells = cells - lastCells;
		lastKeys = keys;
		lastBranches = branches;
		lastCells = cells;
		if (deltaKeys == 0) deltaKeys = 1;

		b.setLength(0);
		final double deltaSeconds = Timer.seconds(timer.restart());

		b.append(state).append(':');

		// Time
		b.append(" time=").append(timer);

		// Keys
		b.append(" keys=");
		format(keys);

		b.append(" k/s=");
		format(Math.round(deltaKeys / deltaSeconds));

		b.append(" c/k=");
		format(Math.round(deltaCells / (double) deltaKeys));

		// Branches
		b.append(" branch=");
		format("%.1f(%.1f)", deltaBranches / (double) deltaKeys, branches / (double) keys);

		// Deadlocks
		b.append(" deadlocks[");
		if (fastBlock_deadlocks > 0) {
			b.append("fast2x2=");
			format(fastBlock_deadlocks);
		}
		if (frozenBoxes_deadlocks > 0) {
			b.append(" frozenBoxes=");
			format(frozenBoxes_deadlocks);
		}
		if (hash_deadlocks > 0) {
			b.append(" hash=");
			format(hash_deadlocks);
		}
		if (macro_deadlocks > 0) {
			b.append(" macro=");
			format(macro_deadlocks);
		}
		b.append(']');

		// Queue
		final Heap<Key> q = queue;
		if (q != null) {
			b.append(" queue=");
			format(q.size());
			b.append('(');
			format(q.capacity());
			b.append(')');
		}

		// Set
		final KeySet s = set;
		if (s != null) {
			b.append(" set=");
			format(s.size());
			b.append('(');
			format(s.arraySize());
			b.append(')');
		}

		// Queue / Set ratio
		if (q != null && s != null) {
			b.append(" queue/set=");
			format("%.0f%%", q.size() * 1e2 / s.size());
		}

		// Deadlock / Set ratio
		if (s != null) {
			b.append(" deadlock/set=");
			final int deadlocks = hash_deadlocks + fastBlock_deadlocks + frozenBoxes_deadlocks
					+ macro_deadlocks;
			format("%.2f", (double) deadlocks / s.size());
		}

		b.append(" memory[free=");
		format(Runtime.getRuntime().freeMemory());
		b.append(" max=");
		format(Runtime.getRuntime().maxMemory());
		b.append(']');

		System.out.println(b);
	}

	private void format(final String format, final Object... args) {
		b.append(String.format(format, args));
	}

	private void format(final long a) {
		if (a < 10000)
			b.append(a);
		else if (a < 10000000)
			b.append((a + 500) / 1000).append('k');
		else
			b.append((a + 500000) / 1000000).append('M');
	}
};