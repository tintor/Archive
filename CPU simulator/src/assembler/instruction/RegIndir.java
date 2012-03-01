package assembler.instruction;

import assembler.Util;

public class RegIndir {
	private Register r;
	private Word w;
	private Number n;
	private boolean valid;
	private boolean hasLabel;

	public RegIndir(String a) {
		int open = a.indexOf(Util.indirChar1);
		int close = a.indexOf(Util.indirChar2);
		if(!((open == -1) || (close == -1) || (close < open))) {
			r = new Register(Util.removeWhitespaces(a.substring(0, open)));
			if(r.isRegister()) {
				String temp = Util.removeWhitespaces(a.substring(open + 1, close));
				w = new Word(temp);
				n = new Number(temp);
				if((w.isWord() || n.isNumber())) valid = true;
				if(w.isWord()) hasLabel = true;
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public boolean hasLabel() {
		return hasLabel;
	}

	public Register getReg() {
		return r;
	}

	public Number getNumber() {
		return n;
	}

	public Word getLabel() {
		return w;
	}
}
