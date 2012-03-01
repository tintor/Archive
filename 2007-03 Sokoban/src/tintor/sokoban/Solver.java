package tintor.sokoban;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import tintor.heap.Heap;
import tintor.properties.Property;
import tintor.stream.DoubleOutputStream;

// TODO eliminate stupid moves
// TODO expert rules for pushing into goal rooms
// TODO greedy and goal driven search 

public class Solver {
	final static String name = "levels/microban:143";

	public static void main(final String[] args) throws Exception {
		final String consoleFile = String.format("logs/%1$tF-%1$tH-%1$tM-%1$tS", new Date());
		final OutputStream consoleStream = new BufferedOutputStream(new DoubleOutputStream(System.out,
				new FileOutputStream(consoleFile)));
		System.setOut(new PrintStream(consoleStream, true));

		System.out.println(name);
		System.out.println();

		final Key level = Util.load(name);

		final Key result = solve(level);

		if (result == null)
			System.out.println("no solution!");
		else {
			System.out.println("solution:\n");
			for (final Key k : Util.expand(result))
				System.out.println(k);
		}
	}

	public final static Property<Boolean> separateGoalRooms = Property.instance(Solver.class, "separateGoalRooms", true);

	public static Key solve(final Key level) {
		final Monitor monitor = new Monitor();
		try {
			if (separateGoalRooms.get()) {
				System.out.println("searching for goal rooms!");
				final List<Room> goalRooms = Util.findEmptyGoalRooms(level);

				int coverage = 0;
				for (final Room room : goalRooms)
					coverage += room.cells;
				System.out.printf("%d goal rooms found, covering %.0f%% of cells\n", goalRooms.size(),
						(double) coverage / Util.cells(level.agent).size());

				if (goalRooms.size() > 0) {
					Key outside = level;

					int id = 0;
					for (final Room room : goalRooms) {
						monitor.reset("room" + id++);

						// separate goal room from outside
						final Cell dispenser = new Cell(room.out.x, room.out.y);
						dispenser.dispenser = true;
						room.in.attach(room.dir, dispenser);
						final Cell[] boxes = new Cell[room.goals];
						for (int i = 0; i < boxes.length; i++)
							boxes[i] = dispenser;
						room.level = new Key(room.in, boxes);
						Util.calculateMinimalPushes(room.level);

						// separate outside from goal room
						final Cell goalRoom = new Cell(room.in.x, room.in.y);
						goalRoom.goals = room.goals;
						room.out.attach(room.dir.opposite(), goalRoom);
						if (Util.cells(room.in).contains(outside.agent)) if (outside.hasBox(room.out)) // TODO method fails, or try to move box outside if simple
								throw new RuntimeException();
							else
								// move agent
								outside = new Key(room.in, outside.boxes);

						System.out.println("outside");
						System.out.println(outside);

						System.out.println("room");
						System.out.println(room.level);
						System.out.println(room.level.renderMinimalPushes(room.in));

						room.solution = astar(room.level, monitor);
						if (room.solution == null) {
							System.out.println("no solution for " + room);
							return null;
						}
						room.expandedSolution = Util.expand(room.solution);
					}

					monitor.reset("outside");
					Util.calculateMinimalPushes(outside);

					final Key outSolution = astar(outside, monitor);
					if (outSolution != null) {
						final Deque<Key> expandedOutSolution = Util.expand(outSolution);

						Key solution = null;
						while (expandedOutSolution.size() > 0) {
							final Key key = expandedOutSolution.pollFirst();

							assert false : "combine solutions";
							// TODO if box is pushed into goal room replace with sequence of pushes from goal room

							key.prev = solution;
							solution = key;
						}
						return solution;
					}

					// restore level
					for (final Room room : goalRooms)
						room.in.attach(room.dir, room.out);
				}
			}
			System.out.println("full search!");
			monitor.reset("full");
			return astar(level, monitor);
		} finally {
			monitor.stop();
		}
	}

	private static Key astar(final Key level, final Monitor monitor) {
		// trivial tests
		if (Util.goals(level.agent).size() < level.boxes.length) {
			System.out.println("not enough goals!");
			System.out.println(level);
			return null;
		}
		if (Deadlock.fullTest(level)) {
			System.out.println("start position is deadlock!");
			System.out.println(level);
			return null;
		}

		// stats
		int nondead = 0;
		for (final Cell a : Util.cells(level.agent))
			if (!a.dead()) nondead++;
		System.out.printf("there are %d nondead cells and %d boxes, maximum of %d keys!\n", nondead, level.boxes.length,
				Util.combinations(nondead, level.boxes.length));

		// key search
		final KeySet reached = new KeySet();
		//final KeyQueue queue = new KeyQueue(); // priority queue
		final Heap<Key> queue = new Heap<Key>(Key.comparator, 1000); // priority queue
		monitor.set = reached;
		monitor.queue = queue;

		final Heuristic heuristic = new Heuristic.MatchingGoals(level);

		// cell search
		final CellSet reachedCells = new CellSet(level.agent);
		final ArrayDeque<Cell> queuedCells = new ArrayDeque<Cell>();

		// go!
		reached.add(level);
		queue.offer(level);
		while (queue.size() > 0) {
			final Key key = queue.poll();

			//System.out.println(key);
			if (key.isGoal()) return key;

			reachedCells.clear();
			if (!key.agent.dispenser) reachedCells.add(key.agent);
			queuedCells.addLast(key.agent);
			while (queuedCells.size() > 0) {
				final Cell a = queuedCells.pollFirst();
				for (final Cell.Edge e : a.edges)
					if (key.hasBox(e.cell)) {
						final Key newKey = key.pushBox(e.cell, e.dir, true);
						if (newKey != null) {
							monitor.branches++;
							if (reached.add(newKey)) {
								final int h = heuristic.estimate(newKey);
								if (h != Integer.MAX_VALUE) {
									newKey.total = newKey.distance + h;
									queue.offer(newKey);
								} else
									monitor.hash_deadlocks++;
							}
						}
					} else if (reachedCells.add(e.cell)) queuedCells.addLast(e.cell);
				monitor.cells++;
			}

			monitor.lastKey = key;
			monitor.keys++;
		}
		return null;
	}
}