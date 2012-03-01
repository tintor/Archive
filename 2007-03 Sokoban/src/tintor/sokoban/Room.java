package tintor.sokoban;

import java.util.Deque;

class Room {
	Cell in; // last cell in room; separates room from outside
	Cell out; // last cell in room; separates room from outside
	Dir dir; // in.get(dir) == out
	int cells; // size of room
	int goals; // number of goals in room

	Key level, solution;
	Deque<Key> expandedSolution;

	@Override public String toString() {
		return String.format("[Room in=%s dir=%s goals=%s]", in, dir, goals);
	}
}