package tintor.sokoban2;

import java.util.Locale;

import tintor.Timer;
import tintor.heap.Heap;
import tintor.sokoban2.keyset.MemoryKeySet;

public class Monitor {
	static String _state = "#";
	static int _keys, _branches, _cells;

	static int _hash_deadlocks, _fastBlock_deadlocks, _frozenBoxes_deadlocks, _macro_deadlocks;

	static MemoryKeySet _set;
	static Heap<Key> _queue;
	static Key _lastKey;

	private static int _lastKeys, _lastBranches, _lastCells;
	private static final Timer _timer = new Timer();
	private static final StringBuilder _b = new StringBuilder();

	private static Thread _thread;

	public static void start() {
		Locale.setDefault(Locale.US);

		_queue = null;
		_lastKey = null;
		_set = null;

		_keys = _branches = _cells = 0;
		_hash_deadlocks = _fastBlock_deadlocks = _frozenBoxes_deadlocks = _macro_deadlocks = 0;

		_timer.restart();
		_thread = new Thread() {
			@Override public void run() {
				try {
					while (true) {
						Thread.sleep(5000);
						output();

						final Key k = _lastKey;
						if (k != null) System.out.println(k);
					}
				} catch (final InterruptedException e) {}
				output();
			}
		};
		_thread.setDaemon(true);
		_thread.start();
	}

	public static void stop() {
		_thread.interrupt();
		try {
			_thread.join();
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
		_thread = null;
	}

	static String time() {
		return _timer.toString();
	}

	static long _lastSetTime, _lastQueueTime, _lastDeadlockTime;

	static void output() {
		int deltaKeys = _keys - _lastKeys;
		final int deltaBranches = _branches - _lastBranches;
		final int deltaCells = _cells - _lastCells;
		_lastKeys = _keys;
		_lastBranches = _branches;
		_lastCells = _cells;
		if (deltaKeys == 0) deltaKeys = 1;

		_b.setLength(0);
		final double deltaSeconds = Timer.seconds(_timer.restart());

		_b.append(_state).append(':');

		// Time
		_b.append(" time=").append(_timer);

		// Keys
		_b.append(" keys=");
		format(_keys);

		_b.append(" k/s=");
		format(Math.round(deltaKeys / deltaSeconds));

		_b.append(" c/k=");
		format(Math.round(deltaCells / (double) deltaKeys));

		// Branches
		_b.append(" branch=");
		format("%.1f(%.1f)", deltaBranches / (double) deltaKeys, _branches / (double) _keys);

		// Deadlocks
		_b.append(" deadlocks[");
		if (_fastBlock_deadlocks > 0) {
			_b.append("fast2x2=");
			format(_fastBlock_deadlocks);
		}
		if (_frozenBoxes_deadlocks > 0) {
			_b.append(" frozenBoxes=");
			format(_frozenBoxes_deadlocks);
		}
		if (_hash_deadlocks > 0) {
			_b.append(" hash=");
			format(_hash_deadlocks);
		}
		if (_macro_deadlocks > 0) {
			_b.append(" macro=");
			format(_macro_deadlocks);
		}
		_b.append(']');
		final double deltaDeadlockSeconds = (Deadlock.timer.time - _lastDeadlockTime) * 1e-9;
		_lastDeadlockTime = Deadlock.timer.time;
		format("%.0f%%", 1e2 * deltaDeadlockSeconds / deltaSeconds);

		// Queue
		final Heap<Key> q = _queue; // Atomic
		if (q != null) {
			final double deltaQueueSeconds = (q.timer.time - _lastQueueTime) * 1e-9;
			_lastQueueTime = q.timer.time;

			_b.append(" queue=");
			format(q.size());
			_b.append('(');
			format(q.capacity());
			_b.append(')');
			format("%.0f%%", 1e2 * deltaQueueSeconds / deltaSeconds);
		}

		// Set
		final MemoryKeySet s = _set; // Atomic
		if (s != null) {
			final double deltaSetSeconds = (s.timer.time - _lastSetTime) * 1e-9;
			_lastSetTime = s.timer.time;

			_b.append(" set=");
			format(s.size());
			_b.append('(');
			format(s.arraysize());
			_b.append(')');
			format("%.0f%%", 1e2 * deltaSetSeconds / deltaSeconds);
		}

		// Queue / Set ratio
		if (q != null && s != null) {
			_b.append(" queue/set=");
			format("%.0f%%", q.size() * 1e2 / s.size());
		}

		// Deadlock / Set ratio
		if (s != null) {
			_b.append(" deadlock/set=");
			final int deadlocks = _hash_deadlocks + _fastBlock_deadlocks + _frozenBoxes_deadlocks + _macro_deadlocks;
			format("%.2f", (double) deadlocks / s.size());
		}

		_b.append(" memFree=");
		format(Runtime.getRuntime().freeMemory());

		System.out.println(_b);
	}

	private static void format(final String format, final Object... args) {
		_b.append(String.format(format, args));
	}

	private static void format(final long a) {
		if (a < 10000)
			_b.append(a);
		else if (a < 10000000)
			_b.append((a + 500) / 1000).append('k');
		else
			_b.append((a + 500000) / 1000000).append('M');
	}
}