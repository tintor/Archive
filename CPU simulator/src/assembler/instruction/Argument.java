package assembler.instruction;

import assembler.Util;

public class Argument {
	private int argType;

	public static final int NEPOSREDNO = 1;
	public static final int REG_DIRECT = 0;
	public static final int REG_INDIRECT = 2;
	public static final int PC_REL = 3;
	public static final int NONE = -1;

	private int argVal;
	private int regCode;
	private String argWord;
	private boolean hasLabel;

	public Argument(String a) throws BadArgFormatError {
		argType = -1;
		switch(a.charAt(0)) {
		case Util.neposrChar: {
			Word w = new Word(a.substring(1));
			Number n = new Number(a.substring(1));
			if(n.isNumber()) {
				argType = NEPOSREDNO;
				argVal = n.val();
			} else if(w.isWord()) {
				argType = NEPOSREDNO;
				hasLabel = true;
				argWord = w.getWord();
			} else
				throw new BadArgFormatError();
		}
			break;
		case Util.pcRelChar: {
			Number n = new Number(a.substring(1));
			if(n.isNumber()) {
				argType = PC_REL;
				argVal = n.val();
			} else
				throw new BadArgFormatError();
		}
			break;
		default:
			Register r = new Register(a);
			RegIndir i = new RegIndir(a);
			if(r.isRegister()) {
				argType = REG_DIRECT;
				regCode = r.val();
			} else if(i.isValid()) {
				argType = REG_INDIRECT;
				hasLabel = i.hasLabel();
				if(hasLabel)
					argWord = i.getLabel().getWord();
				else
					argVal = i.getNumber().val();
				regCode = i.getReg().val();
			} else
				throw new BadArgFormatError();
		}
	}

	public boolean hasLabel() {
		return hasLabel;
	}

	public int getArgType() {
		return argType;
	}

	public int getArgVal() {
		return argVal;
	}

	public int getRegCode() {
		return regCode;
	}

	public String getLabel() {
		return argWord;
	}

	public String toString() {
		String res = "Argument(";
		switch(argType) {
		case NEPOSREDNO:
			res += "neposredno[";
			if(hasLabel) {
				res += argWord;
			} else {
				res += argVal;
			}
			res += "])";
			break;
		case REG_DIRECT:
			res += "registarsko direktno[ r" + regCode + "])";
			break;
		case REG_INDIRECT:
			res += "reg[ r" + regCode + " ] idnir sa pomerajem[";
			if(hasLabel) {
				res += argWord;
			} else {
				res += argVal;
			}
			res += "])";
			break;
		case PC_REL:
			res += "pc relativno[" + argVal + ")]";
			break;
		default:
			res = "Ne valja ti argument!!";
		}
		return res;
	}
}
