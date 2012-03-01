import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class Main {
	public static void main(String[] args) throws Exception {
		Element root = read(new FileInputStream("example.xml"));

		root.setAttribute("gravity", "mnogo");

		System.out.println(root.getBaseURI());
		print(root);

		System.out.println();
		print((Element) root.getElementsByTagName("i").item(0));

		write(root, new FileOutputStream("output.xml"));
	}

	static void print(Element element) {
		System.out.print("<" + element.getTagName());
		if (element.getAttributes() != null) for (int i = 0; i < element.getAttributes().getLength(); i++) {
			Attr attr = (Attr) element.getAttributes().item(i);
			System.out.print(" " + attr.getName() + "=\"" + attr.getValue() + "\"");
		}

		if (element.getFirstChild() == null) {
			System.out.print("/>");
		} else {
			System.out.print(">");

			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child instanceof Text) System.out.print(child.getNodeValue());
				if (child instanceof Element) print((Element) child);
			}

			System.out.print("</" + element.getTagName() + ">");
		}
	}

	static Element read(InputStream in) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(in).getDocumentElement();
	}

	static void write(Node node, OutputStream os) throws Exception {
		DOMSource source = new DOMSource(node);
		StreamResult result = new StreamResult(os);
		TransformerFactory.newInstance().newTransformer().transform(source, result);
	}
}