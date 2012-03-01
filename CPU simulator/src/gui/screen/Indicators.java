package gui.screen;

import gui.Screen;

public class Indicators extends Screen {
	public Indicators() {
		super("Indicators");

		wire(cpu.SUB, "40@67 R20"); //SUB
		wire(cpu.CMP, "40@82 R24"); //CMP
		wire(cpu.DEC, "40@96 R20"); //DEC
		wire(cpu.aluC00, "95@82 R19 U10 R16"); //IZLAZ IZ OR ZA PRETHODNA 3

		wire(cpu.alu.C, "109@49 R12"); //C16
		wire(cpu.aluC0, "175@63 R67 D72 R24"); //IZLAZ IZ PRETHODNOG I PRETHODNE GRUPE

		wire(cpu.ADD, "39@136 R16");//ADD
		wire(cpu.INC, "39@158 R12");//INC
		wire(cpu.aluC10, "89@149 R15 U7 R26");

		wire(cpu.alu.C, "110@122 R21");
		wire(cpu.aluC1, "175@135 R25 D20 R80");

		wire(cpu.X15, "115@186 R14");
		wire(cpu.ASL, "115@209 R14");
		wire(cpu.aluC2, "174@198 R26 U24 R80");

		wire(cpu.ASR, "37@261 R18");
		wire(cpu.LSR, "37@284 R18");
		wire(cpu.aluC30, "88@274 R16 U6 R26");

		wire(cpu.X.bit(0), "110@247 R21");
		wire(cpu.aluC3, "174@259 R67 U70 R19");

		wire(cpu.aluC, "318@163 R22 D197 R191");

		wire(cpu.X15, "32@320 R16");
		wire(cpu.Y15, "32@337 R16");
		wire(cpu.F15, "32@353 R9");
		wire(cpu.aluV000, "92@339 R6 D13 R25");

		wire(cpu.X15, "32@374 R9");
		wire(cpu.Y15, "32@389 R9");
		wire(cpu.F15, "32@404 R16");
		wire(cpu.aluV001, "92@395 R6 U13 R25");

		wire(cpu.X15, "32@557 R16");
		wire(cpu.Y15, "32@574 R9");
		wire(cpu.F15, "32@590 R9");
		wire(cpu.aluV300, "92@576 R6 D13 R25");

		wire(cpu.X15, "32@610 R9");
		wire(cpu.Y15, "32@625 R16");
		wire(cpu.F15, "32@641 R16");
		wire(cpu.aluV301, "92@632 R6 U13 R25");

		wire(cpu.aluV30, "158@605 R37");
		wire(cpu.SUB, "175@576 R20");
		wire(cpu.aluV3, "241@590 R66 U72 R24");

		wire(cpu.F15, "175@541 R14");
		wire(cpu.X15, "175@526 R20");
		wire(cpu.DEC, "175@512 R20");
		wire(cpu.aluV2, "241@530 R24 U26 R80");

		wire(cpu.F15, "175@480 R20");
		wire(cpu.X15, "175@464 R14");
		wire(cpu.INC, "175@447 R20");
		wire(cpu.aluV1, "241@465 R24 D20 R80");

		wire(cpu.ADD, "175@402 R20");
		wire(cpu.aluV00, "158@367 R16 D12 R22");
		wire(cpu.aluV0, "241@393 R66 D72 R24");

		wire(cpu.aluV, "383@493 R159");

		wire(cpu.aluZ, "480@181 R50");

		wire(cpu.aluN, "414@18 R116");
	}
}