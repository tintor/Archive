package assembler.instruction;

import assembler.Util;

public class IntInstruction extends Instruction {

	public IntInstruction(String InstructionName, int bcode) {
		super(InstructionName, bcode, true, false, false, false);
	}

	@Override
	public byte[] encode() {
		byte[] res = new byte[2];
		res[0] = code;
		res[1] = Util.SMALLBYTE(arg.getArgVal());
		return res;
	}

}
