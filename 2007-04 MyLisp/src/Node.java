import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Node {
	Object item;
	Node next;

	public Node() {}

	public Node(final Object item) {
		this.item = item;
	}

	public Node(final Object item, final Node next) {
		this.item = item;
		this.next = next;
	}

	public static int length(Node a) {
		int s = 0;
		while (a != null) {
			s += 1;
			a = a.next;
		}
		return s;
	}

	public Object get(int i) {
		Node a = this;
		while (i > 0) {
			i -= 1;
			a = a.next;
		}
		return a.item;
	}

	public static Node list(final Object... args) {
		if (args.length == 0) return null;

		final Node head = new Node();
		head.item = args[0];

		Node last = head;
		for (int i = 1; i < args.length; i++) {
			last = last.next = new Node();
			last.item = args[i];
		}
		return head;
	}

	public static void write(final Object a, final Writer w) throws IOException {
		if (a instanceof Node) {
			w.write('(');
			write(((Node) a).item, w);
			for (Node i = ((Node) a).next; i != null; i = i.next) {
				w.append(' ');
				write(i.item, w);
			}
			w.write(')');
		} else if (a != null) w.write(a.toString());
	}

	public static Object read(final String text) {
		try {
			return read(new StringReader(text));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object read(final Reader reader) throws IOException {
		Node.c = reader.read();
		Node.reader = reader;

		while (0 <= c && c <= ' ')
			c = reader.read();
		if (c == -1) return null;
		return read();
	}

	private static int c;
	private static Reader reader;

	private static Object read() throws IOException {
		if (c == -1) throw new RuntimeException("unexpected end");
		if (c == '(') {
			c = reader.read();

			// eat space
			while (0 <= c && c <= ' ')
				c = reader.read();

			if (c == -1) throw new RuntimeException("unexpected end");
			if (c == '(') throw new RuntimeException("unexpected '('");

			final Node head = new Node();
			head.item = read();
			Node last = head;

			while (true) {
				// eat space
				while (0 <= c && c <= ' ')
					c = reader.read();

				if (c == -1) throw new RuntimeException("unexpected end");
				if (c == ')') break;

				last = last.next = new Node();
				last.item = read();
			}
			return head;
		}
		if (c == ')') throw new RuntimeException("unexpected ')'");

		assert buffer.length() == 0;
		while (c > ' ' && c != ')') {
			if (c == '(') throw new RuntimeException("unexpected '('");
			buffer.append((char) c);
			c = reader.read();
		}
		final String a = buffer.toString().intern();

		try {
			if (a == "true") return true;
			if (a == "false") return false;

			return Integer.parseInt(a);
		} catch (final NumberFormatException e) {
			return a;
		} finally {
			buffer.setLength(0);
		}
	}

	private final static StringBuilder buffer = new StringBuilder();

	@Override public boolean equals(final Object o) {
		try {
			Node a = this, b = (Node) o;
			while (true) {
				if (a == b) return true;
				if (a == null || b == null) return false;
				if (!a.item.equals(b.item)) return false;
				a = a.next;
				b = b.next;
			}
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public int hashCode() {
		int h = 0;
		for (Node i = this; i != null; i = i.next) {
			h = (h + 1) * 53;
			if (i.item != null) h += 37 * i.item.hashCode();
		}
		return h;
	}

	@Override public Node clone() {
		return new Node(item instanceof Node ? ((Node) item).clone() : item, next != null ? next.clone()
				: next);
	}

	// name -> ((arg1 arg2 ...) expr)
	public static Map<String, Object> defs = new HashMap<String, Object>();

	static {
		try {
			final Reader reader = new FileReader("system.lisp");
			while (true) {
				final Object a = Node.read(reader);
				if (a == null) break;
				eval(a);
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	// evaluate command with arguments
	static Object evalCommand(final String c, final Node a) {
		if (c == "list") return a;
		if (c == "q") {
			assert a.next == null;
			return a.item;
		}

		if (c == "+") {
			int s = 0;
			for (Node i = a; i != null; i = i.next)
				s += cast(eval(i.item), Integer.class);
			return s;
		}
		if (c == "*") {
			int s = 1;
			for (Node i = a; i != null; i = i.next)
				s *= cast(eval(i.item), Integer.class);
			return s;
		}
		if (c == "-") {
			assert a != null;
			if (a.next == null) return -(Integer) eval(a.item);
			assert a.next != null && a.next.next == null;
			return cast(eval(a.item), Integer.class) - cast(eval(a.next.item), Integer.class);
		}
		if (c == "/") {
			assert a != null && a.next != null && a.next.next == null;
			return cast(eval(a.item), Integer.class) / cast(eval(a.next.item), Integer.class);
		}

		if (c == "=") {
			assert a != null && a.next != null && a.next.next == null;
			final Object m = eval(a.item), n = eval(a.item);
			return m == null ? n == null : m.equals(n);
		}
		if (c == ">") {
			assert a != null && a.next != null && a.next.next == null;
			return cast(eval(a.item), Integer.class) > cast(eval(a.item), Integer.class);
		}
		if (c == "<") {
			assert a != null && a.next != null && a.next.next == null;
			return cast(eval(a.item), Integer.class) < cast(eval(a.item), Integer.class);
		}

		if (c == ":") {
			assert a != null && a.next != null && a.next.next == null;
			return new Node(eval(a.item), (Node) eval(a.next.item));
		}
		if (c == "first") {
			assert a != null && a.next == null;
			return cast(eval(a.item), Node.class).item;
		}
		if (c == "rest") {
			assert a != null && a.next == null;
			return cast(eval(a.item), Node.class).next;
		}

		if (c == "and") {
			for (Node i = a; i != null; i = i.next)
				if (!cast(eval(i.item), Boolean.class)) return false;
			return true;
		}
		if (c == "or") {
			for (Node i = a; i != null; i = i.next)
				if (cast(eval(i.item), Boolean.class)) return true;
			return false;
		}

		// (def a 1)
		// (def (a x) (+ x 1)) => (def a (func (x) (+ x 1)))
		if (c == "def") {
			assert a != null && a.next != null && a.next.next == null;
			if (a.item instanceof String)
				vars.put((String) a.item, a.next.item);
			else if (a.item instanceof Node) {
				final Node b = (Node) a.item;
				functions.put(cast(b.item, String.class), Node.list(b.next, a.next));
			} else
				throw new RuntimeException("eval error at (def " + a + ")");
			return null;
		}
		// (if (> x 0) (q positive) (q negative))
		if (c == "if") {
			assert length(a) == 3;
			return cast(eval(a.item), Boolean.class) ? a.next.item : a.next.next.item;
		}
		//		// (cond (> x 0) (q positive) (= x 0) (q zero) (< x 0) (q negative))
		//		if (c == "cond") {
		//			for (Node i = a; i != null; i = i.next.next)
		//				if (i.item == "else" || cast(eval(i.item), Boolean.class)) return eval(i.next.item);
		//			throw new RuntimeException("error!");
		//		}
		//		// (match x 1 (q small) 2 (q medium) 3 (q large))
		//		if (c == "match") {
		//			final Object x = eval(a.item);
		//			for (Node i = a; i != null; i = i.next.next)
		//				if (i.item == "else" || cast(eval(i.item), Boolean.class)) return eval(i.next.item);
		//			throw new RuntimeException("error!");
		//		}

		if (c == "eval") {
			assert a != null && a.next == null;
			return eval(a.item);
		}

		throw new RuntimeException("invalid command '" + c + "'");
	}

	private static <T> T cast(final Object o, final Class<T> clazz) {
		if (o == null) throw new RuntimeException("expected object of class " + clazz + ", not null");
		try {
			return clazz.cast(o);
		} catch (final ClassCastException e) {
			throw new RuntimeException("expected object of class " + clazz + ", not " + o + " ("
					+ o.getClass() + ")");
		}
	}

	// evaluate object
	public static Object eval(final Object a) {
		if (a instanceof Node) {
			final Node n = (Node) a;
			if (n.item instanceof String) return evalCommand((String) n.item, n.next);
			throw new RuntimeException("eval error at " + n);
		}
		if (a instanceof Integer) return a;
		if (a instanceof String) return vars.get(a);
		throw new RuntimeException("eval error at " + a);
	}

	@Override public String toString() {
		final StringWriter w = new StringWriter();
		try {
			write(this, w);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return w.toString();
	}

	public static void main(final String[] args) throws IOException {
		final Reader reader = new StringReader("(>= 2 2) (>= 2 1) (>= 2 3)");
		//final Reader reader = new InputStreamReader(System.in);
		final Writer w = new OutputStreamWriter(System.out);
		while (true) {
			final Object a = Node.read(reader);
			if (a == null) break;

			w.write("< ");
			Node.write(a, w);
			w.write('\n');
			w.flush();

			w.write("> ");
			Node.write(eval(a), w);
			w.flush();
		}
	}
}