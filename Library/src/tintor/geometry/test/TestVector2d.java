package tintor.geometry.test;

import tintor.geometry.Vector2;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestVector2d extends TestCase {
	// Vector2d(float, float) toString() getX() getY()
	public final void testSimple() {
		Assert.assertEquals("0.0,0.0", Vector2.Zero.toString());
		Assert.assertEquals("-2.0,9.0", new Vector2(-2, 9).toString());
		Assert.assertEquals(6.0, new Vector2(6, 5).x);
		Assert.assertEquals(-3.0, new Vector2(5, -3).y);
	}

	// add(Vector2d) sub(Vector2d) neg() add(Vector2d, float) sub(Vector2d, float) mul(float) div(float)
	public final void testBasicArithmetic() {
		Vector2 a = new Vector2(2, -1), b = new Vector2(-3, 7);
		Assert.assertEquals(new Vector2(-1, 6), a.add(b));
		Assert.assertEquals(new Vector2(5, -8), a.sub(b));
		Assert.assertEquals(new Vector2(-2, 1), a.neg());
		Assert.assertEquals(Vector2.Zero, a.mul(0));
	}
}