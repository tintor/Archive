package tintor.sokoban.sandbox;

import org.junit.Assert;
import org.junit.Test;

public class MatchingTest {
	@Test public void matching() {
		final int inf = Integer.MAX_VALUE;
		test(7, new int[][] { { inf, 2, inf }, { inf, inf, 1 }, { 4, inf, inf } });
		test(6, new int[][] { { 1, 1, 1 }, { 2, 2, 2 }, { 3, 3, 3 } });
		test(7, new int[][] { { 10000, 2, 10000 }, { 10000, 10000, 1 }, { 4, 10000, 10000 } });
		test(5, new int[][] { { 2, 5 }, { 6, 3 } });
		test(2, new int[][] { { 5, 1 }, { 1, 9 } });
	}

	private void test(int result, int[]... cost) {
//		int[] link = Matching.minimalWeightedMatching(cost);
//		result(link);
//		Assert.assertEquals(result, sum(link, cost));

		int[] link = Matching.simpleMatching(Matching.initial(cost.length), cost);
		result(link);
		Assert.assertEquals(result, sum(link, cost));
	}

	private static void result(int[] link) {
		int s = 0;
		for (int a : link) {
			Assert.assertTrue(a >= 0);
			Assert.assertTrue(a < link.length);
			s += a;
		}
		Assert.assertEquals(link.length * (link.length - 1) / 2, s);
	}

	private static int sum(int[] link, int[][] cost) {
		int c = 0;
		for (int i = 0; i < cost.length; i++)
			c += cost[i][link[i]];
		return c;
	}
}