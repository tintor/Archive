package gui.screen;

import gui.Screen;
import logic.Gate;

public class InterfaceArbitration extends Screen {
	public InterfaceArbitration() {
		super("Interface - arbitration");

		wire(cpu.busHOLD, "196@170 R53 U41"); //busHOLD
		wire(cpu.brqSTART, "272@229 R55 27@0 U68 R95 U90 R70"); // brqSTART
		wire(cpu.in1_RS_2, "410@229 R18 U138 R38"); // Q izlaz brqSTART flipflopa
		wire(cpu.in1_RS_2.or(cpu.brqSTART), "499@83 R28"); // brqStart OR onaj iz flipflopa
		wire(cpu.BRQ3, "569@96 R35 D80"); // BRQ3
		wire(cpu.BRQ2, "639@147 D31"); // BRQ2
		wire(cpu.BRQ1, "677@147 D31"); // BRQ1
		wire(cpu.BRQ0, "712@147 D31"); // BRQ0
		wire(cpu.BRQCDW, "740@225 R35 D182 L35"); // W codera
		wire(cpu.BRQCD1, "616@280 D75"); // coder out 1
		wire(cpu.BRQCD0, "699@280 D75"); // coder out 0
		wire(cpu.BG3, "605@458 D31 L574 U307 R18"); // BG3
		wire(cpu.BG2, "639@458 D30"); //BG2
		wire(cpu.BG1, "676@458 D30"); //BG1
		wire(cpu.BG0, "711@458 D30"); //BG0

		wire(cpu.brqSTOP, "258@400 U105 0@-44 R237 U250 R22 0@-105 L174 U59 R31 0@-105 R70"); //brqSTOP
		wire(Gate.one, "211@120 R21"); // konstanta jedan kod BUSY komplementa... vezati za jedinicu
		wire(cpu.BUSY.not(), "216@28 R220 119@0 D93 L49 119@36 L142"); // busy complement
		wire(cpu.BUSY, "134@64 L27"); // busy complement complement
		wire(cpu.BUSY.not(), "49@64 L19 D95 R19"); // busy complement complement complement
		wire(cpu.BUSY.not().and(cpu.BG3), "90@170 R25"); // BG3 and not(BUSY)
	}
}