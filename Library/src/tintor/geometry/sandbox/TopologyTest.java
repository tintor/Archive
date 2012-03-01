package tintor.geometry.sandbox;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tintor.geometry.sandbox.Topology.Face;
import tintor.geometry.sandbox.Topology.Vertex;

public class TopologyTest extends Assert {
	@Test
	public void testVertexConstructor() {
		assertEquals(0, w.vertices.size());

		Vertex a = w.new Vertex();
		assertEquals(1, w.vertices.size());
		assertTrue(w.vertices.contains(a));

		Vertex b = w.new Vertex();
		assertEquals(2, w.vertices.size());
		assertTrue(w.vertices.contains(a));
		assertTrue(w.vertices.contains(b));

		a.delete();
		assertEquals(1, w.vertices.size());
		assertFalse(w.vertices.contains(a));
		assertTrue(w.vertices.contains(b));

		b.delete();
		assertEquals(0, w.vertices.size());
		assertFalse(w.vertices.contains(a));
		assertFalse(w.vertices.contains(b));
	}

	@Test
	public void testFaceConstructorAndDelete() {
		Vertex[] v = { w.new Vertex(), w.new Vertex(), w.new Vertex() };

		assertEquals(0, w.faces.size());

		Face p = w.new Face(v);
		face(p, v[2], v[0], v[1]);
		assertEquals(1, w.faces.size());
		for (int i = 0; i < v.length; i++) {
			assertEquals(1, v[i].next.size());
			assertEquals(1, v[i].prev.size());
		}

		Face q = w.new Face();
		face(q);
		assertEquals(2, w.faces.size());

		p.delete();
		face(p);
		assertEquals(1, w.faces.size());
		for (int i = 0; i < v.length; i++) {
			assertEquals(null, v[i].next(p));
			assertEquals(null, v[i].prev(p));
			assertEquals(0, v[i].next.size());
			assertEquals(0, v[i].prev.size());
		}
		
		q.delete();
		face(q);
		assertEquals(0, w.faces.size());
	}

	private void face(Face f, Vertex... w) {
		assertEquals(w.length, f.vertices.size());
		if (w.length > 0) {
			Vertex a = w[w.length - 1];
			for (Vertex b : w) {
				assertEquals(b, f.next(a));
				assertEquals(a, f.prev(b));
				assertTrue(f.contains(b));
				a = b;
			}
		}
	}

	private static Topology w;

	@Before
	public void setUp() throws Exception {
		w = new Topology();
	}

	@After
	public void tearDown() throws Exception {
		w = null;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			assert false;
			throw new RuntimeException("assertions must be turned on!");
		} catch (AssertionError e) {}
	}
}