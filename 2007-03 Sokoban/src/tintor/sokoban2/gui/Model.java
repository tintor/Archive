package tintor.sokoban2.gui;

import java.util.Set;
import java.util.Stack;

import tintor.Timer;
import tintor.properties.Properties;
import tintor.sokoban2.Heuristic;
import tintor.sokoban2.Key;
import tintor.sokoban2.Util;
import tintor.sokoban2.cell.Cell;
import tintor.sokoban2.cell.Loader;
import tintor.sokoban2.cell.Optimizer;
import tintor.sokoban2.common.Dir;

class Model {
	private final static Properties properties = Properties.properties;

	Key key;
	Cell agent;

	Heuristic.MatchingGoals matchingGoals;
	Iterable<Cell> cachedCells;
	Set<Cell> cachedReachable;

	private final Stack<Cell> agentHistory = new Stack<Cell>();
	private final Stack<Cell> agentFuture = new Stack<Cell>();
	private final Stack<Key> redo = new Stack<Key>();

	boolean load(final String file, final int levelId) {
		final Timer timer = new Timer();
		try {
			timer.restart();
			final Key newKey = Optimizer.optimize(Loader.load(file + ":" + levelId));
			if (newKey == null) return false;

			properties.set("level.file", file);
			properties.set("level.id", levelId);

			key = newKey;
			agent = key.agent;
			agentHistory.clear();
			redo.clear();
			agentFuture.clear();
			matchingGoals = new Heuristic.MatchingGoals(key);
			cachedCells = Util.cellList(key.agent);
			refresh();
			return true;
		} finally {
			timer.stop();
			if (Timer.seconds(timer.time) >= 0.35) System.out.println("loading time: " + timer);
		}
	}

	boolean move(final Dir dir) {
		final Cell a = agent.get(dir);
		if (a != null) if (key.hasBox(a)) {
			final Key newKey = key.pushBox(a, dir);
			if (newKey != null) {
				key = newKey;
				agentHistory.push(agent);
				redo.clear();
				agentFuture.clear();
				agent = a;
				refresh();
				return true;
			}
		} else {
			agent = a;
			return true;
		}
		return false;
	}

	boolean undo() {
		if (key.prev != null) {
			redo.push(key);
			agentFuture.push(agent);

			key = key.prev;
			agent = agentHistory.pop();
			refresh();
			return true;
		}
		return false;
	}

	void solution(Key a) {
		while (a != key) {
			agentFuture.add(a.boxRemoved);
			redo.add(a);
			a = a.prev;
		}
	}

	boolean redo() {
		if (!redo.isEmpty()) {
			agentHistory.add(agent);
			key = redo.pop();
			agent = agentFuture.pop();
			refresh();
			return true;
		}
		return false;
	}

	private void refresh() {
		cachedReachable = key.reachable();
		matchingGoals.estimate(key);
	}
}