package tintor;

import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XMLStream {
	public static void main(final String[] args) throws Exception {
		final Reader a = new StringReader("<a></a>");
		final Document x = new SAXReader().read(a);
		new XMLWriter(System.out).write(x);
	}
}