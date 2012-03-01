package tintor.sokoban;

public abstract class Heuristic {
	abstract int estimate(Key key);

	static final Heuristic ClosestGoals = new Heuristic() {
		@Override int estimate(Key key) {
			int e = 0;
			for (Cell b : key.boxes) {
				if (b.goalPushes == Integer.MAX_VALUE) return Integer.MAX_VALUE;
				e += b.goalPushes;
			}
			return e;
		}
	};

	public static class MatchingGoals extends Heuristic {
		public final Cell[] goals;
		private final Cell[] backup;

		public MatchingGoals(final Key level) {
			backup = Util.goals(level.agent).toArray(new Cell[0]);
			goals = new Cell[backup.length];
		}

		@Override public int estimate(final Key key) {
			final Cell[] boxes = key.boxes;
			assert boxes.length == goals.length;

			// initial mapping
			System.arraycopy(backup, 0, goals, 0, goals.length);

			// swaps
			boolean more = true;
			while (more) {
				more = false;
				for (int a = 0; a < goals.length; a++)
					for (int b = a + 1; b < goals.length; b++) {
						int delta = 0, deltaInf = 0;

						final Integer ab = boxes[a].pushes.get(goals[b]);
						if (ab == null)
							deltaInf = 1;
						else
							delta = ab;

						final Integer ba = boxes[b].pushes.get(goals[a]);
						if (ba == null)
							deltaInf += 1;
						else
							delta += ba;

						if (deltaInf == 2) continue;

						final Integer aa = boxes[a].pushes.get(goals[a]);
						if (aa == null)
							deltaInf -= 1;
						else
							delta -= aa;

						final Integer bb = boxes[b].pushes.get(goals[b]);
						if (bb == null)
							deltaInf -= 1;
						else
							delta -= bb;

						if (deltaInf < 0 || deltaInf == 0 && delta < 0) {
							final Cell c = goals[a];
							goals[a] = goals[b];
							goals[b] = c;
							more = true;
						}
					}
			}

			int e = 0;
			for (int i = 0; i < goals.length; i++) {
				final Integer d = boxes[i].pushes.get(goals[i]);
				if (d == null) return Integer.MAX_VALUE;
				e += d;
			}
			return e;
		}
	};
};