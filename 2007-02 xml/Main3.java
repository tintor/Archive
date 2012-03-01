import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class Main3 {
	@SuppressWarnings("unchecked") public static void main(final String[] args) throws Exception {
		//		Document d = DocumentHelper.parseText("<poruka><bold> hrabri </bold> x <bold> mocni </bold></poruka>");
		//		for (Node node : (List<Node>) d.selectNodes("//bold")) {
		//			new XMLWriter(System.out).write(node);
		//			System.out.println();
		//		}

		Document d = new SAXReader().read("persons.xml");
		d = styleDocument(d, "xml.xsl");
		new XMLWriter(System.out, OutputFormat.createPrettyPrint()).write(d.getRootElement());
	}

	public static Document styleDocument(final Document document, final String stylesheet) throws Exception {
		final Transformer transformer = TransformerFactory.newInstance().newTransformer(
				new StreamSource(stylesheet));
		final DocumentResult result = new DocumentResult();
		transformer.transform(new DocumentSource(document), result);
		return result.getDocument();
	}
}