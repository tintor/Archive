package assembler;

import assembler.instruction.Number;
import assembler.instruction.Word;

public class DefDirective {
	private final static String dirName = "DEF";

	private boolean valid;
	private boolean isDirectiveName;
	private String name;
	private int val;

	public DefDirective(String s) {
		// podrzacu komentare u ovakvim redovima
		s = Util.removeWhitespaces(Util.removeComment(s));
		int pos = Util.findWhitespace(s);
		if(pos == -1) // nema druge reci u redu, sigurno ce da fali
			valid = false;
		else if(dirName.equalsIgnoreCase(s.substring(0, pos))) {
			// nadji drugu rec i tako to
			isDirectiveName = true;

			s = Util.skipWhitespaces(s.substring(pos + 1));
			pos = Util.findWhitespace(s);
			if(pos == -1) // nema trece reci, koja treba da je broj, tako da iskaci
				valid = false;
			else {
				Word w = new Word(s.substring(0, pos));
				if(!w.isWord()) {
					valid = false;
				} else {
					// trazi trecu rec
					name = w.getWord();

					s = Util.skipWhitespaces(s.substring(pos + 1));
					Number n = new Number(s);
					if(!n.isNumber()) {
						valid = false;
					} else {
						val = n.val();
						valid = true;
					}
				}
			}
		}
	}

	public boolean isDirective() {
		return isDirectiveName;
	}

	public boolean isValid() {
		return valid;
	}

	public String getName() {
		return name;
	}

	public int getVal() {
		return val;
	}
}
