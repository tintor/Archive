package assembler.instruction;

public class NonAddressInstruction extends Instruction {

	public NonAddressInstruction(String InstructionName, int bcode) {
		super(InstructionName, bcode, false, false, false, false);
	}

	@Override
	public byte[] encode() {
		byte[] res = new byte[1];
		res[0] = code;
		return res;
	}

}
