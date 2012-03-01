package tintor.util;

import org.junit.Assert;
import org.junit.Test;

public class UnionFindTest {
	@Test public void main() {
		UnionFind a = new UnionFind(), b = new UnionFind();
		Assert.assertEquals(a, a.group());
		Assert.assertTrue(a.group() != b.group());
		a.union(b);
		Assert.assertTrue(a.group() == b.group());
	}
}