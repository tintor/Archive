package tintor.sokoban;

import org.junit.Assert;
import org.junit.Test;

public class HeuristicTest {
	Key microban1 = Util.load("levels/microban.soko#Level 1");

	@Test public void pushDistance() {
		Assert.assertEquals(3, Heuristic.ClosestGoals.estimate(microban1));
	}
}