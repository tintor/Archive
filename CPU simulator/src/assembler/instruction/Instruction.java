package assembler.instruction;

public abstract class Instruction {
	protected String name;
	protected byte code;
	protected boolean accimm, accreg, accregind, accpcrel;

	protected Argument arg = null;

	public Instruction(String InstructionName, int bcode, boolean imm, boolean reg, boolean regind,
			boolean pcrel) {
		name = InstructionName;
		code = (new Integer(bcode)).byteValue();
		accimm = imm;
		accreg = reg;
		accregind = regind;
		accpcrel = pcrel;
	}

	public boolean equals(Object a) {
		if(!a.getClass().getName().equals("java.lang.String")) return false;
		return name.equalsIgnoreCase((String)a);
	}

	public boolean acceptsImmediate() {
		return accimm;
	}

	public boolean acceptsRegisterDirect() {
		return accreg;
	}

	public boolean acceptsRegIndir() {
		return accregind;
	}

	public boolean acceptsPCRel() {
		return accpcrel;
	}

	public abstract byte[] encode();

	public void setArgType(int at) throws BadArgTypeError {
		if(((at == Argument.NEPOSREDNO) && accimm) || ((at == Argument.REG_DIRECT) && accreg)
				|| ((at == Argument.REG_INDIRECT) && accregind)
				|| ((at == Argument.PC_REL) && accpcrel)) {} else
			throw new BadArgTypeError();
	}

	public void SetArg(Argument a) throws BadArgTypeError {
		try {
			setArgType(a.getArgType());
			arg = a;
		} catch(BadArgTypeError e) {
			throw e;
		}
	}

	public byte getCode() {
		return code;
	}

	public int getReg() {
		return arg.getRegCode();
	}

	public int getImmediate() {
		return arg.getArgVal();
	}

	public int getShift() {
		return arg.getArgVal();
	}

	public boolean hasLabel() {
		// mali hack, treba klasa Argument da podrzava bezargument kao opciju
		if(arg == null) return false;
		return arg.hasLabel();
	}

	public String getLabel() {
		return arg.getLabel();
	}

	public Argument getArg() {
		return arg;
	}

	public boolean hasArgument() {
		return accimm || accpcrel || accreg || accregind;
	}

	public String toString() {
		int i = 0;
		String ret = name + "[";
		if(accimm) {
			i++;
			ret += "imm";
		}
		if(accreg) {
			if(i > 0) ret += ",";
			i++;
			ret += "reg";
		}
		if(accregind) {
			if(i > 0) ret += ",";
			i++;
			ret += "regindShift";
		}
		if(accpcrel) {
			if(i > 0) ret += ",";
			i++;
			ret += "pcRel";
		}
		return ret + "]";
	}
}