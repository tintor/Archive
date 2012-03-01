package assembler;

import assembler.instruction.Number;

public class OrgDirective {
	private final static String dirName = "ORG";

	private boolean valid;
	private boolean isDirectiveName;
	private int val;

	public OrgDirective(String s) {
		s = Util.removeWhitespaces(Util.removeComment(s));
		int pos = Util.findWhitespace(s);
		if(pos != -1) // nema druge reci u redu, sigurno ce da fali
			if(dirName.equalsIgnoreCase(s.substring(0, pos))) {
				// nadji drugu rec i tako to
				isDirectiveName = true;
				Number n = new Number(Util.skipWhitespaces(s.substring(pos + 1)));
				if(n.isNumber()) {
					valid = true;
					val = n.val();
				}
			}
	}

	public boolean isDirective() {
		return isDirectiveName;
	}

	public boolean isValid() {
		return valid;
	}

	public int getVal() {
		return val % 0x10000; // za svaki slucaj, ako ti padne na pamet da das veci broj u asembleru
	}
}
