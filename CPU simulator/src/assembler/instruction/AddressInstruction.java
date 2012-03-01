package assembler.instruction;

import assembler.Util;

public class AddressInstruction extends Instruction {

	public AddressInstruction(String InstructionName, int bcode) {
		super(InstructionName, bcode, true, true, true, true);
	}

	@Override
	public byte[] encode() {
		byte[] res = null;
		int argType = arg.getArgType();
		switch(argType) {
		case Argument.REG_DIRECT:
			res = new byte[1];
			res[0] = (new Integer(code | argType << 2 | arg.getRegCode())).byteValue();
			break;
		case Argument.REG_INDIRECT:
			res = new byte[3];
			res[0] = (new Integer(code | argType << 2 | arg.getRegCode())).byteValue();
			res[1] = Util.SMALLBYTE(arg.getArgVal());
			res[2] = Util.BIGBYTE(arg.getArgVal());
			break;
		case Argument.PC_REL:
		case Argument.NEPOSREDNO:
			res = new byte[3];
			res[0] = (new Integer(code | argType << 2)).byteValue();
			res[1] = Util.SMALLBYTE(arg.getArgVal());
			res[2] = Util.BIGBYTE(arg.getArgVal());
			break;
		}
		return res;
	}

}
