package assembler.instruction;

import assembler.Util;

public class DefineInstruction extends Instruction {

	public DefineInstruction(String InstructionName, int bcode) {
		super(InstructionName, bcode, true, false, false, false);
	}

	@Override
	public byte[] encode() {
		byte[] res = null;
		switch(arg.getArgType()) {
		case 1:
			res = new byte[1];
			res[0] = Util.SMALLBYTE(arg.getArgVal());
			break;
		case 2:
			res = new byte[2];
			res[0] = Util.SMALLBYTE(arg.getArgVal());
			res[1] = Util.BIGBYTE(arg.getArgVal());
			break;
		}
		return res;
	}
}
