package tintor.sokoban;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {
	Key microban1 = Util.load("levels/microban.soko#1");
	Key classic1 = Util.load("levels/classic/1.soko");

	@Test public void loadGrid() {
		Assert.assertEquals(14, Util.cells(microban1.agent).size());
	}

	@Test public void calculateMinimalPushes() {
		Cell start = microban1.agent;
		Cell c34 = Util.get(start, 3, 4);
		Cell c13 = Util.get(start, 1, 3);

		assertEquals(4, c34.pushes.get(Util.get(start, 2, 1)));
		assertEquals(0, c13.pushes.get(c13));

		assertEquals(3, c34.goalPushes);
		assertEquals(0, c13.goalPushes);

	}

	@Test public void articulation() {
		final Set<Cell> aps = Util.getArticulations(classic1.agent);
		assertTrue(!Util.get(classic1.agent, 11, 7).isTunnel());
		assertEquals(6, aps.size());
		assertTrue(aps.contains(Util.get(classic1.agent, 9, 7)));
		assertTrue(aps.contains(Util.get(classic1.agent, 10, 7)));
		assertTrue(aps.contains(Util.get(classic1.agent, 11, 7)));
		assertTrue(aps.contains(Util.get(classic1.agent, 12, 7)));
		assertTrue(aps.contains(Util.get(classic1.agent, 13, 7)));
		assertTrue(aps.contains(Util.get(classic1.agent, 14, 7)));
	}
	
	@Test public void combinations() {
		assertEquals("6", Util.combinations(4, 2).toString());
		System.out.println(Util.combinations(32, 6));
	}
}