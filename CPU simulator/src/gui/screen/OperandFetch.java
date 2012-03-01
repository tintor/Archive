package gui.screen;

import gui.Screen;

public class OperandFetch extends Screen {
	public OperandFetch() {
		super("Operand Fetch");

		wire(cpu.pcrel, "382@20 D22"); // pcrel 
		wire(cpu.imm, "190@189 D21"); // imm
		wire(cpu.incDISP, "741@182 L16"); // incDISP
		wire(cpu.immreg, "245@420 R90 30@0 D89 R317 U89 R16"); //immreg
		wire(cpu.ldBL, "568@569 D27 R28"); //ldBL
		wire(cpu.ldBH, "296@569 D29 R28"); //ldBH

		label(cpu.diBL, 654, 546); // dibl
		label(cpu.diBH, 385, 546); // dibh
		label(cpu.diBL, 654, 645); // bl
		label(cpu.diBH, 385, 645); // bh
		
		label(cpu.OPRH, 339, 321); //
		label(cpu.MDR, 416, 321); // 
		label(cpu.OPRL, 611, 321); //
		label(cpu.MDR, 684, 321); //
		
		label(cpu.REG, 69, 223); //
		label(cpu.IR.bits(0,15), 69, 292); // 
		
		label(cpu.REG, 264, 59); //
		label(cpu.PC, 264, 125); //
		label(cpu.OFMP1out, 517, 88); //
		label(cpu.IR.bits(0,15), 643,141); //
		label(cpu.DISP, 600,235); //
	}
}