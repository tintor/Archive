package assembler.instruction;

public class Register {
	// ovde radim ovako zbog mogucnosti da se registri imenuju najrazlicitije
	private final static String regNames[] = { "R0", "R1", "R2", "R3" };

	private int regNo(String s) {
		for(int i = 0; i < regNames.length; i++)
			if(regNames[i].equalsIgnoreCase(s)) return i;
		return -1;
	}

	private int regCode;

	public Register(String s) {
		regCode = regNo(s);
	}

	public boolean isRegister() {
		return regCode != -1;
	}

	public int val() {
		return regCode;
	}
}
