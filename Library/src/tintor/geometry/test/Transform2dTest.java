package tintor.geometry.test;

import tintor.geometry.Transform2;
import tintor.geometry.Vector2;
import junit.framework.TestCase;

public class Transform2dTest extends TestCase {
	public void testInvertAndComb() {
		Transform2 a = new Transform2(new Vector2(1, 2), Math.PI / 3);
		// Transform2d b = new Transform2d(new Vector2d(-2, 3), -Math.PI /
		// 4);
		// Vector2d v = new Vector2d(6, 5);

		assertEquals(a, a.invert().invert());
		assertEquals(a, Transform2.comb(new Transform2(), a));
		assertEquals(a, Transform2.comb(a, new Transform2()));

		// there is error on last decimal digit
		// assertEquals(b.apply(a.apply(v)), Transform2d.comb(a,
		// b).apply(v));
		// assertEquals(a.apply(v), a.invert().iapply(v));
	}
}