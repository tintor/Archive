package assembler.instruction;

public class Identifier {
	int identType;

	public static final int REG = 0;
	public static final int NUM = 1;
	public static final int WORD = 2;
	public static final int NONE = -1;

	private int ival;
	private String sval;

	public Identifier(String s) {
		identType = NONE;

		Register r = new Register(s);
		if(r.isRegister()) {
			ival = r.val();
			identType = REG;
		} else {
			Word w = new Word(s);
			if(w.isWord()) {
				sval = s;
				identType = WORD;
			} else {
				Number n = new Number(s);
				if(n.isNumber()) {
					ival = n.val();
					identType = NUM;
				}
			}
		}
	}

	public int getType() {
		return identType;
	}

	public int getValue() {
		return ival;
	}

	public int getRegCode() {
		return ival;
	}

	public String getWord() {
		return sval;
	}
}
