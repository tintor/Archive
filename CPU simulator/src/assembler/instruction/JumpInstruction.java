package assembler.instruction;

import assembler.Util;

public class JumpInstruction extends Instruction {

	public JumpInstruction(String InstructionName, int bcode) {
		super(InstructionName, bcode, true, false, false, false);
	}

	@Override
	public byte[] encode() {
		byte[] res = new byte[3];
		res[0] = code;
		// LITTLE ENDIAN
		res[1] = Util.SMALLBYTE(arg.getArgVal());
		res[2] = Util.BIGBYTE(arg.getArgVal());
		return res;
	}

}
