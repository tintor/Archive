import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import tintor.Stream;

public class Main2 {
	public static void main(final String[] args) throws Exception {
		final XML root = XML.read("world.xml").validate("world.xsd");
		//ERROR root.addLast(XML.parse("<marko/>"));
		//root.println();

		//for (XML node : root.each("//body"))
		//	node.println();
		for (final XML node : XML.read("books.xml").each("//book[author='Neal Stephenson']/title/text()"))
			node.println();

		// --------
		root.save("output.xml");
	}
}

class XML implements Iterable<XML> {
	static Schema defaultSchema() {
		final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema();
		} catch (final SAXException e) {
			throw new RuntimeException(e);
		}
	}

	static Schema loadSchema(final String file) {
		final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(new File(file));
		} catch (final SAXException e) {
			throw new RuntimeException(e);
		}
	}

	XML validate() {
		return validate(defaultSchema());
	}

	XML validate(final String schemaFile) {
		return validate(loadSchema(schemaFile));
	}

	XML validate(final Schema schema) {
		try {
			final Validator validator = schema.newValidator();
			final DOMResult result = new DOMResult();
			validator.validate(new DOMSource(node), result);
			return new XML(((Document) result.getNode()).getDocumentElement());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	static XML read(final Object stream) {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return new XML(builder.parse(Stream.input(stream)).getDocumentElement());
		} catch (final Exception e) {
			throw new RuntimeException("XML parsing", e);
		}
	}

	private XML(final Node node) {
		this.node = node;
	}

	boolean isElement() {
		return node instanceof Element;
	}

	boolean isText() {
		return node instanceof Text;
	}

	String name() {
		return node.getNodeName();
	}

	String text() {
		return node.getNodeValue();
	}

	void setText(final String text) {
		node.setNodeValue(text);
	}

	// Attributes
	Iterable<Attr> attributes() {
		return new Iterate<Attr>(new Iterator<Attr>() {
			int i = 0;
			NamedNodeMap attr = node.getAttributes();

			@Override public boolean hasNext() {
				return i < attr.getLength();
			}

			@Override public Attr next() {
				return (Attr) attr.item(i++);
			}

			@Override public void remove() {}
		});
	}

	String get(final String name) {
		return asElement().getAttribute(name);
	}

	void set(final String name, final String value) {
		asElement().setAttribute(name, value);
	}

	void remove(final String name) {
		asElement().removeAttribute(name);
	}

	// -------

	XML createElement(final String name) {
		return new XML(node.getOwnerDocument().createElement(name));
	}

	XML createText(final String text) {
		return new XML(node.getOwnerDocument().createTextNode(text));
	}

	void addFirst(final XML a) {
		final Node n = node.getFirstChild();
		if (n != null)
			node.insertBefore(a.node, n);
		else
			node.appendChild(a.node);
	}

	void addLast(final XML a) {
		node.getParentNode().appendChild(a.node);
	}

	void addBefore(final XML a) {
		node.getParentNode().insertBefore(a.node, node);
	}

	void addAfter(final XML a) {
		final Node n = node.getNextSibling();
		if (n != null)
			node.getParentNode().insertBefore(a.node, n);
		else
			node.getParentNode().appendChild(a.node);
	}

	void delete() {
		node.getParentNode().removeChild(node);
	}

	XML parent() {
		final Node a = node.getParentNode();
		return a != null ? new XML(a) : null;
	}

	XML first() {
		final Node a = node.getFirstChild();
		return a != null ? new XML(a) : null;
	}

	XML last() {
		final Node a = node.getLastChild();
		return a != null ? new XML(a) : null;
	}

	XML prev() {
		final Node a = node.getPreviousSibling();
		return a != null ? new XML(a) : null;
	}

	XML next() {
		final Node a = node.getNextSibling();
		return a != null ? new XML(a) : null;
	}

	public Iterator<XML> iterator() {
		return new Iterator<XML>() {
			Node n = node.getFirstChild();

			@Override public boolean hasNext() {
				return n != null;
			}

			@Override public XML next() {
				final Node a = n;
				n = n.getNextSibling();
				return new XML(a);
			}

			@Override public void remove() {}
		};
	}

	void save(final String name) {
		try {
			write(new FileOutputStream(name));
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	void write(final OutputStream os) {
		try {
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(node), new StreamResult(os));
		} catch (final TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	private Element asElement() {
		return (Element) node;
	}

	@Override public String toString() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			write(out);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		final String a = out.toString();
		return a.substring(a.indexOf('>') + 1);
	}

	void println() {
		System.out.println(this);
	}

	private NodeList select(final String path) {
		try {
			final XPathFactory xfactory = XPathFactory.newInstance();
			final XPath xpath = xfactory.newXPath();
			final XPathExpression expr = xpath.compile(path);
			return (NodeList) expr.evaluate(node, XPathConstants.NODESET);
		} catch (final XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	XML node(final String path) {
		final NodeList a = select(path);
		if (a.getLength() == 0) throw new RuntimeException();
		return new XML(a.item(0));
	}

	Iterable<XML> each(final String path) {
		return new Iterate<XML>(new Iterator<XML>() {
			int i = 0;
			NodeList nodes = select(path);

			@Override public boolean hasNext() {
				return i < nodes.getLength();
			}

			@Override public XML next() {
				return new XML(nodes.item(i++));
			}

			@Override public void remove() {}
		});
	}

	private final Node node;
}

class Iterate<T> implements Iterable<T> {
	public Iterate(final Iterator<T> it) {
		this.it = it;
	}

	public Iterator<T> iterator() {
		return it;
	}

	private final Iterator<T> it;
}