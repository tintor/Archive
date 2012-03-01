package tintor.sokoban2.deadlockgen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.concurrent.SynchronousQueue;

import tintor.Timer;
import tintor.sokoban2.Key;
import tintor.sokoban2.cell.Cell;
import tintor.sokoban2.cell.Loader;
import tintor.sokoban2.common.CellSet;
import tintor.sokoban2.common.Code;
import tintor.sokoban2.common.Grid;
import tintor.sokoban2.keyset.MemoryKeySet;

// Single CPU
// 2x2 = 3^4 61ms
// 3x2 = 3^6 311ms 
// 4x2 = 3^8 5.3s
// 3x3 = 3^9 61s
// 5x2 = 3^10 8.7m
// 4x3 = 3^12
// 5x3 = 3^15
// 4x4 = 3^16
// 5x4 = 3^20
public class DeadlockGenerator {
	static final int WORKERS = 3; // CPUs - 1
	
	static final int width = 3, height = 3; // width <= height
	static Writer out;

	static Map map = new Map();
	
	static final SynchronousQueue<char[]> syncQueue = new SynchronousQueue<char[]>();
	
	public static void main(final String[] args) throws Exception {
		out = new BufferedWriter(new FileWriter("+" + width + "x" + height + ".txt"));

		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while(true) {
					char[] m = syncQueue.poll();
				}
			}
		};

		Thread[] workers = new Thread[WORKERS];
		for(int i = 0; i < workers.length; i++) workers[i] = new Thread(runnable);
		
		for(Thread w : workers) {
			w.setPriority(Thread.NORM_PRIORITY + 1);
			w.start();
		}
		
		search(0);
		
		for(Thread w : workers)
			w.join();		
		out.write("done\n");
		out.flush();
		

		out.close();
	}

	static void search(final int s) {
		if (!map.isDeadlock())
			for (int i = s; i < map.map.length; i++) {
				map.map[i] = Code.Box;
				map.boxes += 1;
				search(i + 1);
				map.boxes -= 1;

				map.map[i] = Code.Wall;
				search(i + 1);

				map.map[i] = Code.Space;
			}
		else if (!map.freeEdge() && map.isMinimal()) map.print();
	}

	static final Thread t = new Thread() {
		@Override public void run() {
			while (true)
				try {
					sleep(10000);
					timer.restart();
					double r = new BigDecimal(map.progress()).divide(total, 10, RoundingMode.HALF_DOWN).doubleValue();
					System.out.println(100 * r + " " + Timer.format((long) (timer.time * (1 - r) / r)));
					System.out.flush();
				} catch (InterruptedException e) {
					break;
				}
		}
	};

	static final Timer timer = new Timer();
	static final BigInteger three = BigInteger.valueOf(3), two = BigInteger.valueOf(2), one = BigInteger.ONE;
	static final BigDecimal total = new BigDecimal(three.pow(width * height));

	static void timerStart() {
		t.setDaemon(true);
		t.setPriority(Thread.MAX_PRIORITY);
		timer.restart();
		t.start();
	}
	
	static void timerStop() throws InterruptedException {
		t.interrupt();
		t.join();
		timer.stop();
		System.out.println("time " + timer);
	}
	
	public static Grid createGrid(final int width, final char[] map) {
		final int height = map.length / width;
		return new Grid() {
			@Override public boolean agent(final int x, final int y) {
				return x == 0 && y == 0;
			}

			@Override public boolean box(final int x, final int y) {
				if (x == 0 || y == 0 || x > width || y > height) return false;
				return map[(y - 1) * width + x - 1] == Code.Box;
			}

			@Override public boolean goal(final int x, final int y) {
				return false;
			}

			@Override public boolean wall(final int x, final int y) {
				if (x == 0 || y == 0 || x > width || y > height) return false;
				return map[(y - 1) * width + x - 1] == Code.Wall;
			}

			@Override public boolean hole(final int x, final int y) {
				return x == 0 || y == 0 || x > width || y > height;
			}

			@Override public int width() {
				return width + 2;
			}

			@Override public int height() {
				return height + 2;
			}
		};
	}
	
	static class Map {
		int boxes = 0;
		char[] map = new char[width * height]; 
		Grid grid = createGrid(width, map);
		
		Map() {
			for (int i = 0; i < map.length; i++)
				map[i] = Code.Space;
		}

		boolean isMinimal() {
			boolean d;
			for (int i = 0; i < map.length; i++)
				switch (map[i]) {
				case Code.Space:
					break;
				case Code.Box:
					map[i] = Code.Space;
					boxes -= 1;
					d = isDeadlock();
					boxes += 1;
					map[i] = Code.Box;
					if (d) return false;
					break;
				case Code.Wall:
					map[i] = Code.Space;
					d = isDeadlock();
					map[i] = Code.Wall;
					if (d) return false;

					map[i] = Code.Box;
					boxes += 1;
					d = isDeadlock();
					boxes -= 1;
					map[i] = Code.Wall;
					if (d) return false;
					break;
				}
			return true;
		}

		boolean isDeadlock() {
			if (boxes == 0) return false;
			final Key start = Loader.load(grid);

			//Optimizer.calculateMinimalPushes(start);
			//if (Deadlock.fullTest(start)) return true;

			// DFS, exhaustive search
			final MemoryKeySet set = new MemoryKeySet();
			final ArrayDeque<Key> queue = new ArrayDeque<Key>();

			final CellSet cellSet = new CellSet();
			final ArrayDeque<Cell> cellQueue = new ArrayDeque<Cell>();

			set.add(start);
			queue.addLast(start);
			while (!queue.isEmpty()) {
				final Key a = queue.pollLast();
				if (a.isGoal()) return false;

				cellSet.add(a.agent);
				cellQueue.addLast(a.agent);
				while (!cellQueue.isEmpty()) {
					final Cell c = cellQueue.pollLast();
					for (Cell.Edge e = c.edges(); e != null; e = e.next)
						if (a.hasBox(e.cell)) {
							final Key b = a.pushBox(e.cell, e.dir);
							if (b != null && set.add(b)) queue.addLast(b);
						} else if (cellSet.add(e.cell)) cellQueue.addLast(e.cell);
				}
				cellSet.clear();
			}
			return true;
		}

		void print() {
			synchronized (out) {
				try {
					for (int y = 0; y < height; y++) {
						out.write(map, y * width, width);
						out.write('\n');
					}
					out.write("===\n");
					out.flush();
				} catch (final IOException e) {
					throw new RuntimeException();
				}
			}
		}

		boolean freeEdge() {
			boolean b;

			// TOP
			b = true;
			for (int i = 0; i < width; i++)
				if (map[i] != Code.Space) {
					b = false;
					break;
				}
			if (b) return true;

			// LEFT
			b = true;
			for (int i = 0; i < height; i++)
				if (map[i * width] != Code.Space) {
					b = false;
					break;
				}
			if (b) return true;

			// RIGHT
			b = true;
			for (int i = 0; i < height; i++)
				if (map[i * width + width - 1] != Code.Space) {
					b = false;
					break;
				}
			if (b) return true;

			// BOTTOM
			b = true;
			for (int i = 0; i < width; i++)
				if (map[(height - 1) * width + i] != Code.Space) {
					b = false;
					break;
				}
			if (b) return true;

			return false;
		}
		
		BigInteger progress() {
			BigInteger p = BigInteger.ZERO;
			int i = map.length;
			while (i > 0 && map[i - 1] == Code.Space)
				i--;
			while (--i >= 0)
				switch (map[i]) {
				case Code.Box:
					p = one.add(p);
					break;
				case Code.Wall:
					p = one.add(three.pow(map.length - i - 1)).add(p);
					break;
				case Code.Space:
					p = three.pow(map.length - i - 1).multiply(two).add(p);
					break;
				}
			return p;
		}
	}
}