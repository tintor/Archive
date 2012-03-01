package sokoban.solver;
import sokoban.Cell;

public abstract class Goal {
	public abstract boolean test(Key key);
	
	public static final Goal Standard = new Goal() {
		public boolean test(Key a) {
			for (Cell b : a.boxes)
				if (!b.goal) return false;
			return true;
		}
	};
}