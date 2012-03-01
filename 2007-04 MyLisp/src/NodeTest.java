import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

public class NodeTest {
	private static final Object[][] read = { { "   ", null }, { "ab ", new Node("ab") },
			{ "\t(\t)\t", new Node() }, { "\n(:)", new Node(":") }, { "a b", Node.list("a", "b") },
			{ "(+ 1 2)", Node.list("+", 1, 2) }, { "((( )))", new Node(new Node(new Node())) } };

	private static final Object[][] write = { { null, "" }, { Node.list("a", 2), "a 2" } };

	private static final Object[][] eval = { { "+", 0 }, { "+ 1", 1 }, { "+ 1 2", 3 }, { "+ 1 2 -4", -1 } };

	private static final Object[][] equals = { { null, null, true }, { null, new Node(), false },
			{ new Node(), new Node(), true }, { "a", "a", true }, { "a", "b", false },
			{ "a", new Node(), false } };

	@Test public void list() {

	}

	@Test public void equals() {
		for (final Object[] test : equals) {
			Assert.assertEquals(test[2], Node.equals(test[0], test[1]));
			Assert.assertEquals(test[2], Node.equals(test[1], test[0]));
		}
	}

	@Test public void read() throws IOException {
		for (final Object[] test : read)
			Assert.assertEquals(test[1], Node.read((String) test[0]));
	}

	@Test public void write() throws IOException {
		for (final Object[] test : write) {
			final StringWriter w = new StringWriter();
			Node.write(test[0], w);
			Assert.assertEquals(test[1], w.toString());
		}
	}

	@Test public void eval() throws IOException {
		for (final Object[] test : eval)
			Assert.assertEquals(test[1], Node.eval(Node.read((String) test[0])));
	}
}