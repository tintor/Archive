package tintor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class XArraysTest {
	@Test public void copy() {
		fail("Not yet implemented");
	}

	@Test public void insertionSortTArrayInt() {
		fail("Not yet implemented");
	}

	@Test public void insertionSortTArray() {
		fail("Not yet implemented");
	}

	@Test public void remove() {
		fail("Not yet implemented");
	}

	@Test public void removeAt() {
		fail("Not yet implemented");
	}

	@Test public void replace() {
		fail("Not yet implemented");
	}

	@Test public void sortedReplace() {
		fail("Not yet implemented");
	}

	@Test public void equals() {
		fail("Not yet implemented");
	}

	@Test public void find() {
		fail("Not yet implemented");
	}

	@Test public void sortedFind() {
		fail("Not yet implemented");
	}

	@Test public void fromIterator() {
		fail("Not yet implemented");
	}

	@Test public void reverse() {
		final Integer[] a = { 1, 2, 3, 4, 5 };
		XArrays.reverse(a, 0, a.length);
		assertArrayEquals(a, new Integer[] { 5, 4, 3, 2, 1 });

		XArrays.reverse(a, 0, 0);
		assertArrayEquals(a, new Integer[] { 5, 4, 3, 2, 1 });

		XArrays.reverse(a, 0, 1);
		assertArrayEquals(a, new Integer[] { 5, 4, 3, 2, 1 });

		XArrays.reverse(a, 0, 2);
		assertArrayEquals(a, new Integer[] { 4, 5, 3, 2, 1 });

		XArrays.reverse(a, 5, 5);
		assertArrayEquals(a, new Integer[] { 4, 5, 3, 2, 1 });

		XArrays.reverse(a, 4, 5);
		assertArrayEquals(a, new Integer[] { 4, 5, 3, 2, 1 });

		XArrays.reverse(a, 3, 5);
		assertArrayEquals(a, new Integer[] { 4, 5, 3, 1, 2 });
	}

	@Test public void contains() {
		assertTrue(XArrays.contains(new Integer[] { 1, 2, 3, 4, 5 }, 5));
		assertTrue(XArrays.contains(new Integer[] { 1, 3, 3, 4, 5 }, 1));
		assertFalse(XArrays.contains(new Integer[] { 1, 3, 4, 5 }, 2));
	}

	@Test public void sortedContains() {
		assertTrue(XArrays.sortedContains(new Integer[] { 1, 2, 3, 4, 5 }, 5));
		assertTrue(XArrays.sortedContains(new Integer[] { 1, 3, 3, 4, 5 }, 1));
		assertFalse(XArrays.sortedContains(new Integer[] { 1, 3, 4, 5 }, 2));
	}

	@Test public void sortedCount() {
		assertEquals(5, XArrays.sortedCount(new Integer[] { 1, 1, 1, 1, 1 }, 1));
		assertEquals(0, XArrays.sortedCount(new Integer[] { 1, 1 }, 2));
		assertEquals(0, XArrays.sortedCount(new Integer[] {}, 2));
	}

	@Test public void sortedStart() {
		assertEquals(-1, XArrays.sortedStart(new Integer[] { 1, 2, 3 }, 4));
		assertEquals(-1, XArrays.sortedStart(new Integer[] {}, 4));
		assertEquals(0, XArrays.sortedStart(new Integer[] { 1, 2, 2, 2, 3 }, 1));
		assertEquals(1, XArrays.sortedStart(new Integer[] { 1, 2, 2, 2, 2, 2, 3 }, 2));
		assertEquals(4, XArrays.sortedStart(new Integer[] { 1, 2, 2, 2, 3 }, 3));
	}

	@Test public void sortedEnd() {
		assertEquals(4, XArrays.sortedEnd(new Integer[] { 1, 2, 2, 2, 3 }, 2));
		assertEquals(1, XArrays.sortedEnd(new Integer[] { 1, 2, 2, 2, 3 }, 1));
		assertEquals(5, XArrays.sortedEnd(new Integer[] { 1, 2, 2, 2, 3 }, 3));
		assertEquals(-1, XArrays.sortedEnd(new Integer[] { 1, 2, 3 }, 4));
		assertEquals(-1, XArrays.sortedEnd(new Integer[] {}, 4));
	}

	@Test public void sub() {
		fail("Not yet implemented");
	}

	@Test public void joinTArrayTArray() {
		fail("Not yet implemented");
	}

	@Test public void joinTArrayArray() {
		fail("Not yet implemented");
	}

	@Test public void add() {
		assertArrayEquals(XArrays.add(new Integer[] {}, 1), new Integer[] { 1 });
		assertArrayEquals(XArrays.add(new Integer[] { 1 }, 3), new Integer[] { 1, 3 });
		assertArrayEquals(XArrays.add(new Integer[] { 1, 3, 5 }, 7), new Integer[] { 1, 3, 5, 7 });
	}

	@Test public void sortedAdd() {
		fail("Not yet implemented");
	}

	@Test public void filter() {
		fail("Not yet implemented");
	}

	@Test public void resizeTArrayInt() {
		fail("Not yet implemented");
	}

	@Test public void resizeTArrayIntT() {
		fail("Not yet implemented");
	}

	@Test public void swap() {
		fail("Not yet implemented");
	}
}