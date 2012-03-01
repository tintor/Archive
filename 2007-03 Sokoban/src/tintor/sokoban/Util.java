package tintor.sokoban;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.properties.Property;
import tintor.search.BreadthFirstSearch;
import tintor.search.DepthFirstSearch;
import tintor.search.Search;
import tintor.util.DynamicMatrix;

public class Util {
	public final static Property<Boolean> compressTunnels = Property.instance(Util.class, "compressTunnels", true);
	public final static Property<Boolean> removeDeadends = Property.instance(Util.class, "removeDeadends", true);

	public static Key load(final String url) {
		try {
			String file, name = null;
			int id = 1;

			final Matcher m = Pattern.compile("(.+)([#:])(.+)").matcher(url);
			if (m.matches()) {
				name = m.group(2).equals("#") ? m.group(3) : null;
				id = m.group(2).equals(":") ? Integer.parseInt(m.group(3)) : 0;
				file = new File(m.group(1)).exists() ? m.group(1) : m.group(1) + ".soko";
			} else
				file = url;
			return load(new FileReader(file), name, id);
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Level {
		int id;
		String name;
		final List<String> rows = new ArrayList<String>();

		final BufferedReader in;

		Level(final BufferedReader in) {
			this.in = in;
		}

		boolean read() {
			++id;
			name = "";
			rows.clear();
			try {
				while (true) {
					String line = in.readLine();
					if (line == null) return false;
					if (levelRow(line)) {
						rows.add(line);
						break;
					}
					line = line.trim();
					if (!line.equals("") && name == "") name = line;
				}

				while (true) {
					final String line = in.readLine();
					if (line == null || line.trim().equals("")) break;
					rows.add(line);
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}

		private static boolean levelRow(final String line) {
			int wall = 0;
			for (int i = 0; i < line.length(); i++) {
				final char c = line.charAt(i);
				if (c == Code.Wall) wall++;
				if (c != Code.Space && c != Code.Wall) return false;
			}
			return wall > 0 && line.length() >= 3;
		}

		Key open() {
			return load(new StringGrid(rows.toArray(new String[rows.size()])));
		}
	}

	public static Key load(final Reader reader, final String levelname, final int levelID) {
		try {
			final Level level = new Level(new BufferedReader(reader));
			while (level.read())
				if (level.name.equalsIgnoreCase(levelname) || level.name.equalsIgnoreCase("Level 1" + levelname)
						|| level.id == levelID) return level.open();
			return null;
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Key load(final Grid grid) {
		final DynamicMatrix<Cell> cellMatrix = new DynamicMatrix<Cell>(null);
		final List<Cell> boxes = new ArrayList<Cell>();
		Cell agent = null;

		for (int y = 0; y < grid.height(); y++)
			for (int x = 0; x < grid.width(); x++) {
				if (grid.wall(x, y)) continue;
				final Cell cell = new Cell(x, y);

				// attach cell
				cell.attach(Dir.West, cellMatrix.get(x - 1, y));
				cell.attach(Dir.North, cellMatrix.get(x, y - 1));

				// check if special
				if (grid.goal(x, y)) cell.goals = 1;
				if (grid.box(x, y)) boxes.add(cell);
				if (grid.agent(x, y)) agent = cell;

				cellMatrix.put(x, y, cell);
			}

		// ASSERT check invariants!
		for (int y = 0; y < cellMatrix.height(); y++)
			for (int x = 0; x < cellMatrix.width(); x++) {
				final Cell c = cellMatrix.get(x, y);
				if (c != null) c.invariant();
			}

		if (agent == null) throw new RuntimeException("Missing agent!");
		Key level = new Key(agent, boxes.toArray(new Cell[boxes.size()]));

		// optimize level
		calculateMinimalPushes(level); // must be before TunnelCompression
		if (removeDeadends.get()) level = removeDeadends(level); // must be before TunnelCompression
		markArticulations(level.agent); // must be before TunnelCompression
		if (compressTunnels.get()) level = compressTunnels(level);

		return level;
	}

	// TODO agent could be moved outside of deadend
	private static Key removeDeadends(final Key level) {
		final Search<Cell> search = new DepthFirstSearch<Cell>();
		search.add(level.agent);
		for (final Cell a : search) {
			final boolean deadend = a.edges.length == 1 && !a.isGoal() && a != level.agent && !level.hasBox(a);
			for (final Cell.Edge e : a.edges)
				if (deadend)
					search.forcedAdd(e.cell);
				else
					search.add(e.cell);
			if (deadend) a.detachAll();
		}
		return level;
	}

	// TODO optimize!
	private static Key compressTunnels(final Key level) {
		loop: while (true) {
			for (final Cell c : cells(level.agent))
				if (c != level.agent && !c.isGoal() && !c.dispenser && c.isTunnel() && !level.hasBox(c)) {
					final Cell a = c.edges[0].cell, b = c.edges[1].cell;
					if (a.dead() && b.dead() || (a.isTunnel() || a.edges.length == 1)
							&& (b.isTunnel() || b.edges.length == 1) || a.articulation && b.articulation
							&& c.articulation) {
						c.detachTunnelSegment();
						continue loop;
					}
				}
			break;
		}
		return level;
	}

	//	static void calculateMinimalMoves(Key level) {
	//		final Search<Cell> search = new BreadthFirstSearch<Cell>();
	//		for (Cell start : cells(level.agent)) {
	//			search.add(start);
	//			start.moves.put(start, 0);
	//			for (Cell a : search) {
	//				int distance = start.moves.get(a);
	//				for (Cell.Edge e : a.edges)
	//					if (search.add(e.cell)) start.moves.put(e.cell, distance + 1);
	//			}
	//			search.clear();
	//		}
	//	}

	// FIXME bugy!
	// TODO optimize very slow! O(cells^2) => O(goals*cells)
	static void calculateMinimalPushes(final Key level) {
		final Search<Key> keySearch = new BreadthFirstSearch<Key>();
		final Search<Cell> cellSearch = new DepthFirstSearch<Cell>();
		final Iterable<Cell> cells = cells(level.agent);
		final Iterable<Cell> goals = Util.goals(level.agent);

		for (final Cell a : cells)
			a.goalPushes = -1;

		for (final Cell start : cells) {
			start.pushes.clear();
			keySearch.add(new Key(start, start));

			for (final Key keyA : keySearch) {
				if (!start.pushes.containsKey(keyA.boxes[0])) start.pushes.put(keyA.boxes[0], keyA.distance);

				cellSearch.add(keyA.agent);
				for (final Cell cell : cellSearch)
					for (final Cell.Edge e : cell.edges)
						if (keyA.hasBox(e.cell)) {
							final Key keyB = keyA.pushBox(e.cell, e.dir, false);
							if (keyB != null) keySearch.add(keyB);
						} else
							cellSearch.add(e.cell);
				cellSearch.clear();
			}
			keySearch.clear();
		}

		for (final Cell a : cells) {
			a.goalPushes = Integer.MAX_VALUE;
			for (final Cell goal : goals) {
				final Integer p = a.pushes.get(goal);
				if (p != null) a.goalPushes = Math.min(a.goalPushes, p);
			}
		}
	}

	private static void markArticulations(final Cell root) {
		new Articulations(root, true);
	}

	public static Set<Cell> getArticulations(final Cell root) {
		return new Articulations(root, false).result;
	}

	public static class Articulations {
		final Map<Cell, Integer> num = new HashMap<Cell, Integer>();
		final Map<Cell, Integer> low = new HashMap<Cell, Integer>();
		int counter;

		final Set<Cell> result;
		final Image imageNum, imageLow;

		Articulations(final Cell root, final boolean mark) {
			result = mark ? null : new HashSet<Cell>();

			imageNum = new Image(root);
			imageLow = new Image(root);

			num.put(root, counter);
			low.put(root, counter);
			counter++;

			int s = 0;
			for (final Cell.Edge e : root.edges)
				if (!num.containsKey(e.cell)) {
					dfs(e.cell, root);
					s++;
				}
			if (s >= 2) if (result != null)
				result.add(root);
			else
				root.articulation = true;

			for (final Cell a : num.keySet()) {
				imageNum.set(a, Util.hex(num.get(a)));
				imageLow.set(a, Util.hex(low.get(a)));
			}
		}

		void dfs(final Cell a, final Cell from) {
			num.put(a, counter);
			low.put(a, counter);
			counter++;

			for (final Cell.Edge e : a.edges)
				if (e.cell != from) {
					int z;
					if (!num.containsKey(e.cell)) {
						dfs(e.cell, a);
						z = low.get(e.cell);
						if (z >= num.get(a)) if (result != null)
							result.add(a);
						else
							a.articulation = true;
					} else
						z = num.get(e.cell);
					if (z < low.get(a)) low.put(a, z);
				}
		}
	}

	public static Set<Cell> cells(final Cell start) {
		final Search<Cell> search = new DepthFirstSearch<Cell>();
		search.add(start);
		for (final Cell a : search)
			for (final Cell.Edge e : a.edges)
				search.add(e.cell);
		return search.reached;
	}

	public static Cell get(final Cell root, final int x, final int y) {
		final Search<Cell> search = new DepthFirstSearch<Cell>();
		search.add(root);
		for (final Cell a : search) {
			if (a.x == x && a.y == y) return a;
			for (final Cell.Edge e : a.edges)
				search.add(e.cell);
		}
		return null;
	}

	public static char hex(final int a) {
		return (char) (a < 10 ? a + '0' : a - 10 + 'a');
	}

	public static List<Cell> goals(final Cell root) {
		final List<Cell> list = new ArrayList<Cell>();
		final Search<Cell> search = new DepthFirstSearch<Cell>();
		search.add(root);
		for (final Cell a : search) {
			if (a.goals > 0) list.add(a);
			for (final Cell.Edge e : a.edges)
				search.add(e.cell);
		}
		return list;
	}

	// FIXME bug, what if box is in goal room entrance? test will fail instead of returning room
	public static List<Room> findEmptyGoalRooms(final Key level) {
		final List<Room> result = new ArrayList<Room>();
		final Search<Cell> outside = new DepthFirstSearch<Cell>();
		final Search<Cell> inside = new DepthFirstSearch<Cell>();

		outside.add(level.boxes[0]); // start search from outside of all empty goal rooms 
		for (final Cell a : outside)
			if (a.articulation) for (final Cell.Edge e : a.edges)
				if (e.cell.articulation) {
					outside.reached.add(e.cell);
					inside.reached.add(a);

					final Room room = new Room();
					room.in = e.cell;
					room.out = a;
					room.dir = e.dir.opposite();

					inside.add(e.cell);
					int boxes = 0;
					for (final Cell b : inside) {
						room.cells++;
						room.goals += b.goals;
						if (level.hasBox(b)) {
							boxes++;
							break;
						}
						for (final Cell.Edge be : b.edges)
							inside.add(be.cell);
					}
					inside.clear();

					if (boxes == 0 && room.goals > 0) result.add(room);
				} else
					outside.add(e.cell);
		return result;
	}

	public static Deque<Key> expand(Key key) {
		final Deque<Key> result = new ArrayDeque<Key>();
		while (key != null) {
			result.offerFirst(key);
			key = key.prev;
		}
		return result;
	}

	//	// find whitch
	//	public static Cell[] boxDifference(Cell[] before, Cell[] after) {
	//		if(before.length != after.length) throw new IllegalArgumentException();
	//		int s = 0, e = after.length - 1;
	//		while(s <= e && before[s] == after[s]) s++;
	//		while(s <= e && before[e] == after[e]) e++;
	//		
	//		if(before[s] == after[s+1]) return new Cell[] {before[e], after[s]};
	//		if(before[e] == after[e-1]) return new Cell[] {before[s], after[e]};
	//		throw new IllegalArgumentException();
	//	}

	static BigInteger combinations(final int n, final int k) {
		BigInteger c = BigInteger.ONE;
		for (int i = 0; i < k; i++)
			c = c.multiply(BigInteger.valueOf(n - i));
		for (int i = 2; i <= k; i++)
			c = c.divide(BigInteger.valueOf(i));
		return c;
	}
}