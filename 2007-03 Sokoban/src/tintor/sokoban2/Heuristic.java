package tintor.sokoban2;

import tintor.sokoban2.cell.Cell;

public abstract class Heuristic {
	public abstract int estimate(Key key);

	public static final Heuristic ClosestGoals = new Heuristic() {
		@Override public int estimate(Key key) {
			int e = 0;
			for (int i = 0; i < key.boxes(); i++) {
				if (key.box(i).goalPushes() == Integer.MAX_VALUE) return Integer.MAX_VALUE;
				e += key.box(i).goalPushes();
			}
			return e;
		}
	};

	public static class MatchingGoals extends Heuristic {
		public final Cell[] matches;
		public final Cell[] goals;

		public MatchingGoals(final Key level) {
			goals = Util.goals(level.agent).toArray(new Cell[0]);
			matches = new Cell[goals.length];
		}

		@Override public int estimate(final Key key) {
			assert key.boxes() == matches.length;

			// initial mapping
			System.arraycopy(goals, 0, matches, 0, matches.length);

			// swaps
			boolean more = true;
			while (more) {
				more = false;
				for (int a = 0; a < matches.length; a++)
					for (int b = a + 1; b < matches.length; b++) {
						int delta = 0, deltaInf = 0;

						final Integer ab = key.box(a).pushes(matches[b]);
						if (ab == null)
							deltaInf = 1;
						else
							delta = ab;

						final Integer ba = key.box(b).pushes(matches[a]);
						if (ba == null)
							deltaInf += 1;
						else
							delta += ba;

						if (deltaInf == 2) continue;

						final Integer aa = key.box(a).pushes(matches[a]);
						if (aa == null)
							deltaInf -= 1;
						else
							delta -= aa;

						final Integer bb = key.box(b).pushes(matches[b]);
						if (bb == null)
							deltaInf -= 1;
						else
							delta -= bb;

						if (deltaInf < 0 || deltaInf == 0 && delta < 0) {
							final Cell c = matches[a];
							matches[a] = matches[b];
							matches[b] = c;
							more = true;
						}
					}
			}

			int e = 0;
			for (int i = 0; i < matches.length; i++) {
				final Integer d = key.box(i).pushes(matches[i]);
				if (d == null) return Integer.MAX_VALUE;
				e += d;
			}
			return e;
		}
	}
}